package com.example.casalista.data

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
    val id: Int,
    val code: String,
    val buyer: String,
    val houseName: String,
    val quantity: Int,
    val total: Long,
    val address: String,
    val status: OrderStatus,
)

object Datos {
    val houses = mutableListOf<House>()

    val monthlySales = listOf(
        "Jun" to 210_000_000L,
        "Jul" to 340_000_000L,
        "Ago" to 280_000_000L,
        "Sep" to 450_000_000L,
        "Oct" to 390_000_000L,
        "Nov" to 520_000_000L,
    )

    val orders = mutableListOf(
        SaleOrder(1, "CL-10231", "Ana Compradora", "Casa Aurora", 1, 68_000_000, "Lote 12, vereda La Mesa", OrderStatus.PENDING),
        SaleOrder(2, "CL-10232", "Carlos Rojas", "Casa Brisa", 1, 89_000_000, "Calle 8 #4-20, Chía", OrderStatus.PENDING),
        SaleOrder(3, "CL-10233", "Laura Gómez", "Casa Cumbre", 1, 120_000_000, "Finca El Roble, Villeta", OrderStatus.IN_PROGRESS),
        SaleOrder(4, "CL-10234", "Mateo Pardo", "Casa Fénix", 2, 104_000_000, "Lote 3, Tabio", OrderStatus.IN_PROGRESS),
        SaleOrder(5, "CL-10235", "Sofía Díaz", "Casa Eucalipto", 1, 178_000_000, "Km 5 vía Subachoque", OrderStatus.SHIPPED),
        SaleOrder(6, "CL-10236", "Julián Mora", "Casa Guadua", 1, 95_000_000, "Vereda San José, Silvania", OrderStatus.DELIVERED),
        SaleOrder(7, "CL-10237", "Valentina Ruiz", "Casa Isla", 1, 74_000_000, "Lote 7, Sopó", OrderStatus.DELIVERED),
        SaleOrder(8, "CL-10238", "Andrés Vega", "Casa Dalia", 1, 142_000_000, "Calle 15 #9-33, Zipaquirá", OrderStatus.CANCELLED),
    )

    fun updateOrder(order: SaleOrder) {
        val index = orders.indexOfFirst { it.id == order.id }
        if (index >= 0) orders[index] = order
    }

    fun placeOrders(code: String, buyer: String, address: String) {
        Cart.items.forEach { item ->
            val id = (orders.maxOfOrNull { it.id } ?: 0) + 1
            orders.add(
                SaleOrder(
                    id, code, buyer, item.house.name, item.quantity,
                    item.house.price * item.quantity, address, OrderStatus.PENDING,
                ),
            )
        }
    }

    fun salesBySystem(): List<Pair<String, Long>> = orders
        .filter { it.status != OrderStatus.CANCELLED }
        .groupBy { order -> houses.find { it.name == order.houseName }?.system ?: "Otro" }
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
