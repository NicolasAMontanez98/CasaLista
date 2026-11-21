package com.example.casalista.data

/** Una línea del carrito: la casa y cuántas unidades. */
data class CartItem(val house: House, val quantity: Int)

/**
 * Carrito de compras en memoria. Solo guarda el id de cada casa y su cantidad;
 * la casa completa se busca en [Datos.houses]. Cuando cambia, avisa con [onChange]
 * para que se guarde en Firestore.
 */
object Cart {
    private const val MAX_QUANTITY = 9
    private val saved = linkedMapOf<String, Int>()

    /** Se ejecuta después de cada cambio (agregar, quitar, vaciar). */
    var onChange: (() -> Unit)? = null

    val quantities: Map<String, Int> get() = saved

    /** Líneas del carrito; se omiten las casas que ya no existen en el catálogo. */
    val items: List<CartItem>
        get() = saved.mapNotNull { (id, quantity) ->
            Datos.houses.find { it.id == id }?.let { CartItem(it, quantity) }
        }

    val total: Long get() = items.sumOf { it.house.price * it.quantity }

    /** Suma una unidad de la casa. */
    fun add(house: House) = setQuantity(house.id, (saved[house.id] ?: 0) + 1)

    /** Fija la cantidad, limitada entre 1 y [MAX_QUANTITY]. */
    fun setQuantity(houseId: String, quantity: Int) {
        saved[houseId] = quantity.coerceIn(1, MAX_QUANTITY)
        onChange?.invoke()
    }

    /** Quita la casa del carrito. */
    fun remove(houseId: String) {
        saved.remove(houseId)
        onChange?.invoke()
    }

    /** Vacía el carrito. */
    fun clear() {
        saved.clear()
        onChange?.invoke()
    }

    /** Reemplaza el contenido con el que vino de Firestore, sin avisar de un cambio. */
    fun restore(quantities: Map<String, Int>) {
        saved.clear()
        quantities.forEach { (id, quantity) -> saved[id] = quantity.coerceIn(1, MAX_QUANTITY) }
    }
}
