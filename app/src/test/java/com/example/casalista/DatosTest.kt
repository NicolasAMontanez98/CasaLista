package com.example.casalista

import com.example.casalista.data.Cart
import com.example.casalista.data.Datos
import com.example.casalista.data.House
import com.example.casalista.data.Order
import com.example.casalista.data.OrderStatus
import com.example.casalista.data.Role
import com.example.casalista.data.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DatosTest {

    @Test
    fun loginReturnsUserWithItsRole() {
        assertEquals(Role.SELLER, Datos.login("vendedor@casalista.co", "123456")?.role)
        assertEquals(Role.ADMIN, Datos.login("ADMIN@casalista.co", "123456")?.role)
    }

    @Test
    fun loginFailsWithWrongPasswordOrUnknownEmail() {
        assertNull(Datos.login("vendedor@casalista.co", "otra"))
        assertNull(Datos.login("nadie@casalista.co", "123456"))
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
        val house = House(Datos.nextHouseId(), "Casa Prueba", 1, 1, 1, "Madera", Status.AVAILABLE)
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
    fun cartBecomesPendingOrders() {
        Cart.clear()
        Cart.add(Datos.houses[0])
        Cart.add(Datos.houses[0])
        val before = Datos.orders.size
        Datos.placeOrders("CL-00001", "Ana", "Lote 1")
        val order = Datos.orders.last()
        assertEquals(before + 1, Datos.orders.size)
        assertEquals(Datos.houses[0].price * 2, order.total)
        assertEquals(OrderStatus.PENDING, order.status)
        Datos.orders.remove(order)
        Cart.clear()
    }

    @Test
    fun housesInReviewAreHiddenFromBuyers() {
        assertEquals(emptyList<House>(), Datos.search("kiwi", null, Order.NAME))
        assertEquals(1, Datos.search("kiwi", null, Order.NAME, includePaused = true).size)
    }

    @Test
    fun totalSalesLeavesOutCancelledOrders() {
        val cancelled = Datos.orders.filter { it.status == OrderStatus.CANCELLED }.sumOf { it.total }
        assertEquals(Datos.orders.sumOf { it.total } - cancelled, Datos.totalSales)
    }

    @Test
    fun searchUsersByTextAndRole() {
        assertEquals(listOf("Adriana Admin"), Datos.searchUsers("adri", null).map { it.name })
        assertEquals(listOf("Vicente Vendedor"), Datos.searchUsers("casalista", Role.SELLER).map { it.name })
        assertEquals(3, Datos.searchUsers("", null).size)
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
