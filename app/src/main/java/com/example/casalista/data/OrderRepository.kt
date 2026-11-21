package com.example.casalista.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object OrderRepository {
    private val orders = FirebaseFirestore.getInstance().collection("orders")

    suspend fun loadAll() = fill(orders.get().await().documents)

    suspend fun loadMine(uid: String) = fill(orders.whereEqualTo("buyerUid", uid).get().await().documents)

    private fun fill(docs: List<DocumentSnapshot>) {
        Datos.orders.clear()
        Datos.orders.addAll(docs.map { it.toOrder() }.sortedByDescending { it.createdAt })
    }

    suspend fun place(code: String, uid: String, buyer: String, address: String, items: List<CartItem>) {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        val now = System.currentTimeMillis()
        items.forEach { item ->
            val order = SaleOrder(
                orders.document().id, code, uid, buyer, item.house.id, item.house.name, item.quantity,
                item.house.price * item.quantity, address, OrderStatus.PENDING, now,
            )
            batch.set(orders.document(order.id), order.toMap())
        }
        batch.commit().await()
    }

    suspend fun updateStatus(order: SaleOrder, status: OrderStatus) {
        orders.document(order.id).update("status", status.name).await()
        Datos.updateOrder(order.copy(status = status))
    }

    private fun SaleOrder.toMap() = mapOf(
        "code" to code,
        "buyerUid" to buyerUid,
        "buyer" to buyer,
        "houseId" to houseId,
        "houseName" to houseName,
        "quantity" to quantity,
        "total" to total,
        "address" to address,
        "status" to status.name,
        "createdAt" to createdAt,
    )

    private fun DocumentSnapshot.toOrder() = SaleOrder(
        id = id,
        code = getString("code").orEmpty(),
        buyerUid = getString("buyerUid").orEmpty(),
        buyer = getString("buyer").orEmpty(),
        houseId = getString("houseId").orEmpty(),
        houseName = getString("houseName").orEmpty(),
        quantity = (getLong("quantity") ?: 1).toInt(),
        total = getLong("total") ?: 0,
        address = getString("address").orEmpty(),
        status = OrderStatus.entries.find { it.name == getString("status") } ?: OrderStatus.PENDING,
        createdAt = getLong("createdAt") ?: 0,
    )
}
