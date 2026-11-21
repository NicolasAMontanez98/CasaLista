package com.example.casalista.data

data class CartItem(val house: House, val quantity: Int)

object Cart {
    private const val MAX_QUANTITY = 9
    private val saved = linkedMapOf<String, Int>()

    var onChange: (() -> Unit)? = null

    val quantities: Map<String, Int> get() = saved

    val items: List<CartItem>
        get() = saved.mapNotNull { (id, quantity) ->
            Datos.houses.find { it.id == id }?.let { CartItem(it, quantity) }
        }

    val total: Long get() = items.sumOf { it.house.price * it.quantity }

    fun add(house: House) = setQuantity(house.id, (saved[house.id] ?: 0) + 1)

    fun setQuantity(houseId: String, quantity: Int) {
        saved[houseId] = quantity.coerceIn(1, MAX_QUANTITY)
        onChange?.invoke()
    }

    fun remove(houseId: String) {
        saved.remove(houseId)
        onChange?.invoke()
    }

    fun clear() {
        saved.clear()
        onChange?.invoke()
    }

    fun restore(quantities: Map<String, Int>) {
        saved.clear()
        quantities.forEach { (id, quantity) -> saved[id] = quantity.coerceIn(1, MAX_QUANTITY) }
    }
}
