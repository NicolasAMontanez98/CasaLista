package com.example.casalista.data

import java.util.Calendar

/** Rol de un usuario. Define qué pantallas ve (comprador, vendedor o administrador). */
enum class Role(val label: String) {
    BUYER("Comprador"),
    SELLER("Vendedor"),
    ADMIN("Administrador"),
}

/**
 * Usuario de la app. El [uid] es el identificador de Firebase Authentication y
 * también el id del documento en la colección `users` de Firestore.
 */
data class User(var name: String, val email: String, var role: Role) {
    var phone = ""
    var active = true
    var uid = ""
}

/**
 * Casa prefabricada del catálogo. Se guarda en la colección `houses` de Firestore.
 * [images] contiene las URL de las fotos subidas a Firebase Storage.
 */
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
    /** Indica si todavía se puede comprar (cualquier estado menos "Agotada"). */
    val inStock get() = status != Status.SOLD_OUT
}

/** Estados posibles de una casa y las reglas para pasar de uno a otro. */
object Status {
    const val AVAILABLE = "Disponible"
    const val ON_REQUEST = "Sobre pedido"
    const val SOLD_OUT = "Agotada"
    const val PAUSED = "Pausada"
    const val REVIEW = "En revisión"

    const val REJECTED = "Rechazada"

    /** Estados en los que la casa NO se muestra a los compradores. */
    val hidden = listOf(PAUSED, REVIEW, REJECTED)

    /**
     * Calcula el estado que queda al guardar un producto.
     * Una casa nueva o rechazada vuelve a revisión; una oculta conserva su estado;
     * en cualquier otro caso se respeta el estado elegido por el vendedor.
     */
    fun afterSave(current: String?, chosen: String): String {
        if (current == null || current == REJECTED) return REVIEW
        if (current in hidden) return current
        return chosen
    }

    /** Estados que el vendedor puede elegir en el formulario. */
    val editable = listOf(AVAILABLE, ON_REQUEST, SOLD_OUT)
}

/** Criterios de orden del catálogo. */
enum class Order { PRICE_ASC, PRICE_DESC, AREA_DESC, NAME }

/** Estados de un pedido, desde que se paga hasta que se entrega o se cancela. */
enum class OrderStatus(val label: String) {
    PENDING("Pendiente"),
    IN_PROGRESS("En preparación"),
    SHIPPED("Enviado"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado");

    /** Siguiente estado del flujo normal, o null si ya terminó o fue cancelado. */
    val next: OrderStatus?
        get() = when (this) {
            PENDING -> IN_PROGRESS
            IN_PROGRESS -> SHIPPED
            SHIPPED -> DELIVERED
            else -> null
        }

    /** Un pedido solo se puede cancelar antes de que salga hacia el comprador. */
    val canCancel get() = this == PENDING || this == IN_PROGRESS
}

/**
 * Pedido de una casa. Se crea uno por cada producto del carrito y todos los de una
 * misma compra comparten el [code]. [lat] y [lng] son las coordenadas de entrega
 * (pueden faltar) y [paymentRef] es la referencia del pago en ePayco.
 */
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
    val paymentRef: String = "",
)

/**
 * Copia en memoria de las casas y los pedidos cargados desde Firestore, junto con
 * las consultas que usan las pantallas (búsqueda, ventas por mes, etc.).
 * Los repositorios llenan estas listas; las pantallas solo las leen.
 */
object Datos {
    val houses = mutableListOf<House>()

    private val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

    val orders = mutableListOf<SaleOrder>()

    /**
     * Ventas de los últimos [months] meses (incluido el actual), una pareja
     * (nombre del mes, total) por mes. Los pedidos cancelados no suman.
     */
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

    /** Reemplaza en la copia local el pedido con el mismo id. */
    fun updateOrder(order: SaleOrder) {
        val index = orders.indexOfFirst { it.id == order.id }
        if (index >= 0) orders[index] = order
    }

    /** Total vendido por sistema constructivo, de mayor a menor, sin contar cancelados. */
    fun salesBySystem(): List<Pair<String, Long>> = orders
        .filter { it.status != OrderStatus.CANCELLED }
        .groupBy { order -> houses.find { it.id == order.houseId }?.system ?: "Otro" }
        .map { (system, list) -> system to list.sumOf { it.total } }
        .sortedByDescending { it.second }

    /** Suma de todos los pedidos que no están cancelados. */
    val totalSales get() = orders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.total }

    /** Casas visibles para los compradores. */
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

    /** Agrega la casa a la copia local o reemplaza la que tenga el mismo id. */
    fun saveHouse(house: House) {
        val index = houses.indexOfFirst { it.id == house.id }
        if (index >= 0) houses[index] = house else houses.add(house)
    }

    /** Aprueba (queda "Disponible") o rechaza una casa que estaba en revisión. */
    fun reviewHouse(id: String, approved: Boolean) {
        val house = houses.first { it.id == id }
        saveHouse(house.copy(status = if (approved) Status.AVAILABLE else Status.REJECTED))
    }

    /** Quita la casa de la copia local y también del carrito. */
    fun deleteHouse(id: String) {
        houses.removeAll { it.id == id }
        Cart.remove(id)
    }

    /**
     * Busca casas por nombre y filtra por sistema y estado, con el [order] pedido.
     * Con [includePaused] también devuelve las ocultas (para vendedor y administrador).
     */
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
