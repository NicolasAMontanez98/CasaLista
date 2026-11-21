package com.example.casalista.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Acceso a las casas: los datos están en la colección `houses` de Firestore y las
 * fotos en Firebase Storage (`houses/{id}/archivo.jpg`). Cada cambio se guarda en la
 * nube y luego se refleja en la copia local de [Datos].
 */
object HouseRepository {
    private val houses = FirebaseFirestore.getInstance().collection("houses")
    private val storage = FirebaseStorage.getInstance()

    /** Descarga todas las casas y reemplaza la copia local. */
    suspend fun load() {
        val loaded = houses.get().await().documents.map { it.toHouse() }
        Datos.houses.clear()
        Datos.houses.addAll(loaded)
    }

    /** Genera un id nuevo para una casa que aún no se ha guardado. */
    fun newId(): String = houses.document().id

    /** Crea o actualiza una casa. */
    suspend fun save(house: House) {
        houses.document(house.id).set(house.toMap()).await()
        Datos.saveHouse(house)
    }

    /** El administrador aprueba o rechaza una casa en revisión. */
    suspend fun review(house: House, approved: Boolean) {
        val status = if (approved) Status.AVAILABLE else Status.REJECTED
        houses.document(house.id).update("status", status).await()
        Datos.reviewHouse(house.id, approved)
    }

    /** Borra la casa y sus fotos. */
    suspend fun delete(house: House) {
        houses.document(house.id).delete().await()
        deleteImages(house.images)
        Datos.deleteHouse(house.id)
    }

    /** Sube una foto (ya comprimida) a Storage y devuelve su URL de descarga. */
    suspend fun uploadImage(houseId: String, bytes: ByteArray): String {
        val ref = storage.reference.child("houses/$houseId/${UUID.randomUUID()}.jpg")
        val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
        ref.putBytes(bytes, metadata).await()
        return ref.downloadUrl.await().toString()
    }

    /** Borra fotos por su URL; si alguna ya no existe, se ignora el error. */
    suspend fun deleteImages(urls: List<String>) {
        for (url in urls) {
            runCatching { storage.getReferenceFromUrl(url).delete().await() }
        }
    }

    /** Si la colección está vacía, carga las casas de ejemplo en una sola operación. */
    suspend fun seedIfEmpty() {
        if (!houses.limit(1).get().await().isEmpty) return
        val batch = FirebaseFirestore.getInstance().batch()
        SampleHouses.all.forEach { batch.set(houses.document(it.id), it.toMap()) }
        batch.commit().await()
    }

    /** Convierte la casa en el mapa de campos que se guarda en Firestore. */
    private fun House.toMap() = mapOf(
        "name" to name,
        "price" to price,
        "area" to area,
        "bedrooms" to bedrooms,
        "system" to system,
        "status" to status,
        "images" to images,
    )

    /** Reconstruye una casa desde un documento de Firestore; si el estado falta, queda en revisión. */
    private fun DocumentSnapshot.toHouse() = House(
        id = id,
        name = getString("name").orEmpty(),
        price = getLong("price") ?: 0,
        area = (getLong("area") ?: 0).toInt(),
        bedrooms = (getLong("bedrooms") ?: 0).toInt(),
        system = getString("system").orEmpty(),
        status = getString("status") ?: Status.REVIEW,
        images = (get("images") as? List<*>)?.filterIsInstance<String>().orEmpty(),
    )
}
