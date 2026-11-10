package com.example.casalista.data

enum class Role(val label: String) {
    BUYER("Comprador"),
    SELLER("Vendedor"),
    ADMIN("Administrador"),
}

data class User(var name: String, val email: String, var password: String, var role: Role) {
    var phone = ""
    var active = true
}

data class House(
    val id: Int,
    val name: String,
    val price: Long,
    val area: Int,
    val bedrooms: Int,
    val system: String,
    val status: String,
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
    val houses = mutableListOf(
        House(1, "Casa Aurora", 68_000_000, 45, 2, "Steel framing", "Disponible"),
        House(2, "Casa Brisa", 89_000_000, 60, 2, "Steel framing", "Disponible"),
        House(3, "Casa Cumbre", 120_000_000, 80, 3, "Panel SIP", "Disponible"),
        House(4, "Casa Dalia", 142_000_000, 95, 3, "Panel SIP", "Sobre pedido"),
        House(5, "Casa Eucalipto", 178_000_000, 110, 4, "Madera", "Disponible"),
        House(6, "Casa Fénix", 52_000_000, 36, 1, "Contenedor", "Disponible"),
        House(7, "Casa Guadua", 95_000_000, 70, 3, "Guadua", "Disponible"),
        House(8, "Casa Horizonte", 210_000_000, 130, 4, "Steel framing", "Sobre pedido"),
        House(9, "Casa Isla", 74_000_000, 55, 2, "Contenedor", "Agotada"),
        House(10, "Casa Jacaranda", 250_000_000, 150, 5, "Madera", "Disponible"),
        House(11, "Casa Kiwi", 98_000_000, 65, 2, "Guadua", Status.REVIEW),
        House(12, "Casa Laurel", 135_000_000, 85, 3, "Madera", Status.REVIEW),
    )

    val monthlySales = listOf(
        "Jun" to 210_000_000L,
        "Jul" to 340_000_000L,
        "Ago" to 280_000_000L,
        "Sep" to 450_000_000L,
        "Oct" to 390_000_000L,
        "Nov" to 520_000_000L,
    )

    val users = mutableListOf(
        User("Ana Compradora", "comprador@casalista.co", "123456", Role.BUYER),
        User("Vicente Vendedor", "vendedor@casalista.co", "123456", Role.SELLER),
        User("Adriana Admin", "admin@casalista.co", "123456", Role.ADMIN),
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

    fun nextHouseId() = (houses.maxOfOrNull { it.id } ?: 0) + 1

    fun saveHouse(house: House) {
        val index = houses.indexOfFirst { it.id == house.id }
        if (index >= 0) houses[index] = house else houses.add(house)
    }

    fun reviewHouse(id: Int, approved: Boolean) {
        val house = houses.first { it.id == id }
        saveHouse(house.copy(status = if (approved) Status.AVAILABLE else Status.REJECTED))
    }

    fun deleteHouse(id: Int) {
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

    fun searchUsers(query: String, role: Role?): List<User> {
        val text = query.trim()
        return users
            .filter { role == null || it.role == role }
            .filter { it.name.contains(text, ignoreCase = true) || it.email.contains(text, ignoreCase = true) }
            .sortedBy { it.name }
    }

    fun findByEmail(email: String): User? = users.find { it.email.equals(email, ignoreCase = true) }

    fun login(email: String, password: String): User? =
        findByEmail(email)?.takeIf { it.password == password }
}
