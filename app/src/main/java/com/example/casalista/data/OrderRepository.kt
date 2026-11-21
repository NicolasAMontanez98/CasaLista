package com.example.casalista.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Pedidos guardados en la colección `orders` de Firestore. */
object OrderRepository {
    private val orders = FirebaseFirestore.getInstance().collection("orders")

    /** Carga todos los pedidos (vendedor y administrador). */
    suspend fun loadAll() = fill(orders.get().await().documents)

    /** Carga solo los pedidos del comprador con ese uid. */
    suspend fun loadMine(uid: String) = fill(orders.whereEqualTo("buyerUid", uid).get().await().documents)

    /** Reemplaza la copia local, con los más recientes primero. */
    private fun fill(docs: List<DocumentSnapshot>) {
        Datos.orders.clear()
        Datos.orders.addAll(docs.map { it.toOrder() }.sortedByDescending { it.createdAt })
    }

    /**
     * Guarda una compra: un pedido por cada producto del carrito, todos con el mismo
     * [code] y en estado pendiente. Se escriben en un lote para que se guarden todos o ninguno.
     */
    suspend fun place(
        code: String, uid: String, buyer: String, address: String,
        items: List<CartItem>, lat: Double?, lng: Double?, paymentRef: String,
    ) {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        val now = System.currentTimeMillis()
        items.forEach { item ->
            val order = SaleOrder(
                orders.document().id, code, uid, buyer, item.house.id, item.house.name, item.quantity,
                item.house.price * item.quantity, address, OrderStatus.PENDING, now, lat, lng, paymentRef,
            )
            batch.set(orders.document(order.id), order.toMap())
        }
        batch.commit().await()
    }

    /** Cambia el estado de un pedido (aceptar, enviar, entregar o cancelar). */
    suspend fun updateStatus(order: SaleOrder, status: OrderStatus) {
        orders.document(order.id).update("status", status.name).await()
        Datos.updateOrder(order.copy(status = status))
    }

    /** Convierte el pedido en el mapa de campos que se guarda en Firestore. */
    private fun SaleOrder.toMap() = mapOf(
        "lat" to lat,
        "paymentRef" to paymentRef,
        "lng" to lng,
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

    /** Reconstruye un pedido desde un documento de Firestore, con valores por defecto si falta algún campo. */
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
        lat = getDouble("lat"),
        lng = getDouble("lng"),
        paymentRef = getString("paymentRef").orEmpty(),
    )
}
