package com.example.casalista

import com.example.casalista.data.Cart
import com.example.casalista.data.Datos
import com.example.casalista.data.House
import com.example.casalista.data.Order
import com.example.casalista.data.OrderStatus
import com.example.casalista.data.Role
import com.example.casalista.data.SaleOrder
import com.example.casalista.data.SampleHouses
import com.example.casalista.data.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class DatosTest {

    @Before
    fun loadSampleHouses() {
        Datos.houses.clear()
        Datos.houses.addAll(SampleHouses.all)
    }

    @Test
    fun searchFiltersByNameAndSystem() {
        assertEquals(listOf("Casa Aurora"), Datos.search(" aurora ", null, Order.NAME).map { it.name })
        assertEquals(setOf("Madera"), Datos.search("", "Madera", Order.NAME).map { it.system }.toSet())
    }

    @Test
    fun searchFiltersByStatus() {
        val soldOut = Datos.search("", null, Order.NAME, "Agotada")
        assertEquals(listOf("Casa Isla"), soldOut.map { it.name })
    }

    @Test
    fun savedHouseIsListedAndDeletedHouseIsGone() {
        val house = House("prueba", "Casa Prueba", 1, 1, 1, "Madera", Status.AVAILABLE)
        Datos.saveHouse(house)
        assertEquals(listOf(house), Datos.search("prueba", null, Order.NAME))
        Datos.deleteHouse(house.id)
        assertEquals(emptyList<House>(), Datos.search("prueba", null, Order.NAME))
    }

    @Test
    fun pausedHousesAreHiddenFromBuyers() {
        val aurora = Datos.houses.first { it.name == "Casa Aurora" }
        Datos.saveHouse(aurora.copy(status = Status.PAUSED))
        try {
            assertEquals(emptyList<House>(), Datos.search("aurora", null, Order.NAME))
            assertEquals(1, Datos.search("aurora", null, Order.NAME, includePaused = true).size)
        } finally {
            Datos.saveHouse(aurora)
        }
    }

    @Test
    fun orderStatusAdvancesUntilDeliveredAndCancelsOnlyEarly() {
        assertEquals(OrderStatus.IN_PROGRESS, OrderStatus.PENDING.next)
        assertEquals(OrderStatus.DELIVERED, OrderStatus.SHIPPED.next)
        assertNull(OrderStatus.DELIVERED.next)
        assertNull(OrderStatus.CANCELLED.next)
        assertEquals(listOf(true, true, false, false, false), OrderStatus.entries.map { it.canCancel })
    }

    @Test
    fun housesInReviewAreHiddenFromBuyers() {
        assertEquals(emptyList<House>(), Datos.search("kiwi", null, Order.NAME))
        assertEquals(1, Datos.search("kiwi", null, Order.NAME, includePaused = true).size)
    }

    @Test
    fun salesIgnoreCancelledOrdersAndGroupByMonth() {
        val now = java.util.Calendar.getInstance().timeInMillis
        val house = Datos.houses[0]
        fun order(id: String, status: OrderStatus, total: Long) =
            SaleOrder(id, "CL-1", "u", "Ana", house.id, house.name, 1, total, "Lote 1", status, now)
        Datos.orders.addAll(listOf(order("a", OrderStatus.PENDING, 100), order("b", OrderStatus.CANCELLED, 50)))
        try {
            assertEquals(100L, Datos.totalSales)
            assertEquals(100L, Datos.monthlySales(3).last().second)
            assertEquals(0L, Datos.monthlySales(3).first().second)
            assertEquals(listOf(house.system to 100L), Datos.salesBySystem())
        } finally {
            Datos.orders.clear()
        }
    }

    @Test
    fun approvedHouseGoesPublicAndRejectedStaysHidden() {
        val kiwi = Datos.houses.first { it.name == "Casa Kiwi" }
        Datos.reviewHouse(kiwi.id, approved = true)
        try {
            assertEquals(1, Datos.search("kiwi", null, Order.NAME).size)
            Datos.reviewHouse(kiwi.id, approved = false)
            assertEquals(0, Datos.search("kiwi", null, Order.NAME).size)
            assertEquals(Status.REJECTED, Datos.houses.first { it.id == kiwi.id }.status)
        } finally {
            Datos.saveHouse(kiwi)
        }
    }

    @Test
    fun statusAfterSaveSendsNewAndRejectedToReview() {
        assertEquals(Status.REVIEW, Status.afterSave(null, Status.AVAILABLE))
        assertEquals(Status.REVIEW, Status.afterSave(Status.REJECTED, Status.AVAILABLE))
        assertEquals(Status.PAUSED, Status.afterSave(Status.PAUSED, Status.AVAILABLE))
        assertEquals(Status.SOLD_OUT, Status.afterSave(Status.AVAILABLE, Status.SOLD_OUT))
    }

    @Test
    fun salesBySystemAddsUpToTotalAndIsSortedDescending() {
        val bySystem = Datos.salesBySystem()
        assertEquals(Datos.totalSales, bySystem.sumOf { it.second })
        assertEquals(bySystem.sortedByDescending { it.second }, bySystem)
    }

    @Test
    fun searchSortsByOrder() {
        val prices = Datos.search("", null, Order.PRICE_ASC).map { it.price }
        assertEquals(prices.sorted(), prices)
        assertEquals("Casa Jacaranda", Datos.search("", null, Order.AREA_DESC).first().name)
    }
}
