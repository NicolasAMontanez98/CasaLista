package com.example.casalista.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Guarda y recupera el carrito de cada comprador en el documento `carts/{uid}`. */
object CartRepository {
    private val carts = FirebaseFirestore.getInstance().collection("carts")

    /** Guarda en Firestore las cantidades actuales del carrito. */
    suspend fun save(uid: String) {
        carts.document(uid).set(mapOf("items" to Cart.quantities)).await()
    }

    /** Lee el carrito guardado y lo carga en memoria. */
    suspend fun load(uid: String) {
        val saved = carts.document(uid).get().await().get("items") as? Map<*, *>
        Cart.restore(saved.orEmpty().entries.associate { it.key.toString() to (it.value as? Number ?: 1).toInt() })
    }
}
