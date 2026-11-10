package com.example.casalista.data

data class CartItem(val house: House, val quantity: Int)

object Cart {
    private const val MAX_QUANTITY = 9
    private val quantities = linkedMapOf<Int, Int>()

    val items: List<CartItem>
        get() {
            val list = mutableListOf<CartItem>()
            for ((id, quantity) in quantities) {
                val house = Datos.houses.first { it.id == id }
                list.add(CartItem(house, quantity))
            }
            return list
        }

    val total: Long get() = items.sumOf { it.house.price * it.quantity }

    fun add(house: House) = setQuantity(house.id, (quantities[house.id] ?: 0) + 1)

    fun setQuantity(houseId: Int, quantity: Int) {
        quantities[houseId] = quantity.coerceIn(1, MAX_QUANTITY)
    }

    fun remove(houseId: Int) {
        quantities.remove(houseId)
    }

    fun clear() = quantities.clear()
}
