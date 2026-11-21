package com.example.casalista.data

import java.util.Calendar

enum class Role(val label: String) {
    BUYER("Comprador"),
    SELLER("Vendedor"),
    ADMIN("Administrador"),
}

data class User(var name: String, val email: String, var role: Role) {
    var phone = ""
    var active = true
    var uid = ""
}

data class House(
    val id: String,
    val name: String,
    val price: Long,
    val area: Int,
    val bedrooms: Int,
    val system: String,
    val status: String,
    val images: List<String> = emptyList(),
) {
    val inStock get() = status != Status.SOLD_OUT
}

object Status {
    const val AVAILABLE = "Disponible"
    const val ON_REQUEST = "Sobre pedido"
    const val SOLD_OUT = "Agotada"
    const val PAUSED = "Pausada"
    const val REVIEW = "En revisión"

    const val REJECTED = "Rechazada"

    val hidden = listOf(PAUSED, REVIEW, REJECTED)

    fun afterSave(current: String?, chosen: String): String {
        if (current == null || current == REJECTED) return REVIEW
        if (current in hidden) return current
        return chosen
    }

    val editable = listOf(AVAILABLE, ON_REQUEST, SOLD_OUT)
}

enum class Order { PRICE_ASC, PRICE_DESC, AREA_DESC, NAME }

enum class OrderStatus(val label: String) {
    PENDING("Pendiente"),
    IN_PROGRESS("En preparación"),
    SHIPPED("Enviado"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado");

    val next: OrderStatus?
        get() = when (this) {
            PENDING -> IN_PROGRESS
            IN_PROGRESS -> SHIPPED
            SHIPPED -> DELIVERED
            else -> null
        }

    val canCancel get() = this == PENDING || this == IN_PROGRESS
}

data class SaleOrder(
    val id: String,
    val code: String,
    val buyerUid: String,
    val buyer: String,
    val houseId: String,
    val houseName: String,
    val quantity: Int,
    val total: Long,
    val address: String,
    val status: OrderStatus,
    val createdAt: Long,
    val lat: Double? = null,
    val lng: Double? = null,
)

object Datos {
    val houses = mutableListOf<House>()

    private val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

    val orders = mutableListOf<SaleOrder>()

    fun monthlySales(months: Int, now: Long = System.currentTimeMillis()): List<Pair<String, Long>> {
        val cal = Calendar.getInstance().apply { timeInMillis = now; set(Calendar.DAY_OF_MONTH, 1) }
        cal.add(Calendar.MONTH, -(months - 1))
        return List(months) {
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val total = orders.filter { order ->
                val date = Calendar.getInstance().apply { timeInMillis = order.createdAt }
                order.status != OrderStatus.CANCELLED &&
                    date.get(Calendar.MONTH) == month && date.get(Calendar.YEAR) == year
            }.sumOf { it.total }
            cal.add(Calendar.MONTH, 1)
            monthNames[month] to total
        }
    }

    fun updateOrder(order: SaleOrder) {
        val index = orders.indexOfFirst { it.id == order.id }
        if (index >= 0) orders[index] = order
    }

    fun salesBySystem(): List<Pair<String, Long>> = orders
        .filter { it.status != OrderStatus.CANCELLED }
        .groupBy { order -> houses.find { it.id == order.houseId }?.system ?: "Otro" }
        .map { (system, list) -> system to list.sumOf { it.total } }
        .sortedByDescending { it.second }

    val totalSales get() = orders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.total }

    val published get() = houses.filter { it.status !in Status.hidden }

    val systems get() = published.map { it.system }.distinct()
    val statuses get() = houses.map { it.status }.distinct()

    val systemOptions = listOf("Steel framing", "Panel SIP", "Madera", "Contenedor", "Guadua")

    val finishes = listOf(
        "Piso en porcelanato",
        "Cocina integral",
        "Ventanería en aluminio",
        "Techo en teja termoacústica",
    )

    fun saveHouse(house: House) {
        val index = houses.indexOfFirst { it.id == house.id }
        if (index >= 0) houses[index] = house else houses.add(house)
    }

    fun reviewHouse(id: String, approved: Boolean) {
        val house = houses.first { it.id == id }
        saveHouse(house.copy(status = if (approved) Status.AVAILABLE else Status.REJECTED))
    }

    fun deleteHouse(id: String) {
        houses.removeAll { it.id == id }
        Cart.remove(id)
    }

    fun search(
        query: String,
        system: String?,
        order: Order,
        status: String? = null,
        includePaused: Boolean = false,
    ): List<House> {
        val found = (if (includePaused) houses else published).filter {
            (system == null || it.system == system) &&
                (status == null || it.status == status) &&
                it.name.contains(query.trim(), ignoreCase = true)
        }
        return when (order) {
            Order.PRICE_ASC -> found.sortedBy { it.price }
            Order.PRICE_DESC -> found.sortedByDescending { it.price }
            Order.AREA_DESC -> found.sortedByDescending { it.area }
            Order.NAME -> found.sortedBy { it.name }
        }
    }
}
