package com.example.casalista

import com.example.casalista.data.Cart
import com.example.casalista.data.Datos
import com.example.casalista.data.SampleHouses
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CartTest {
    private val aurora = SampleHouses.all[0]
    private val brisa = SampleHouses.all[1]

    @Before
    fun resetState() {
        Datos.houses.clear()
        Datos.houses.addAll(SampleHouses.all)
        Cart.clear()
    }

    @Test
    fun addingTwiceRaisesQuantityAndTotal() {
        Cart.add(aurora)
        Cart.add(aurora)
        Cart.add(brisa)
        assertEquals(2, Cart.items.first { it.house == aurora }.quantity)
        assertEquals(aurora.price * 2 + brisa.price, Cart.total)
    }

    @Test
    fun quantityStaysBetweenOneAndNine() {
        Cart.add(aurora)
        Cart.setQuantity(aurora.id, 0)
        assertEquals(1, Cart.items.single().quantity)
        Cart.setQuantity(aurora.id, 50)
        assertEquals(9, Cart.items.single().quantity)
    }

    @Test
    fun removeTakesTheHouseOut() {
        Cart.add(aurora)
        Cart.remove(aurora.id)
        assertEquals(0L, Cart.total)
    }
}
