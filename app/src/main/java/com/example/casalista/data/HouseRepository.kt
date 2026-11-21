package com.example.casalista.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.util.UUID

object HouseRepository {
    private val houses = FirebaseFirestore.getInstance().collection("houses")
    private val storage = FirebaseStorage.getInstance()

    suspend fun load() {
        val loaded = houses.get().await().documents.map { it.toHouse() }
        Datos.houses.clear()
        Datos.houses.addAll(loaded)
    }

    fun newId(): String = houses.document().id

    suspend fun save(house: House) {
        houses.document(house.id).set(house.toMap()).await()
        Datos.saveHouse(house)
    }

    suspend fun review(house: House, approved: Boolean) {
        val status = if (approved) Status.AVAILABLE else Status.REJECTED
        houses.document(house.id).update("status", status).await()
        Datos.reviewHouse(house.id, approved)
    }

    suspend fun delete(house: House) {
        houses.document(house.id).delete().await()
        deleteImages(house.images)
        Datos.deleteHouse(house.id)
    }

    suspend fun uploadImage(houseId: String, bytes: ByteArray): String {
        val ref = storage.reference.child("houses/$houseId/${UUID.randomUUID()}.jpg")
        val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
        ref.putBytes(bytes, metadata).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deleteImages(urls: List<String>) {
        for (url in urls) {
            runCatching { storage.getReferenceFromUrl(url).delete().await() }
        }
    }

    suspend fun seedIfEmpty() {
        if (!houses.limit(1).get().await().isEmpty) return
        val batch = FirebaseFirestore.getInstance().batch()
        SampleHouses.all.forEach { batch.set(houses.document(it.id), it.toMap()) }
        batch.commit().await()
    }

    private fun House.toMap() = mapOf(
        "name" to name,
        "price" to price,
        "area" to area,
        "bedrooms" to bedrooms,
        "system" to system,
        "status" to status,
        "images" to images,
    )

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
