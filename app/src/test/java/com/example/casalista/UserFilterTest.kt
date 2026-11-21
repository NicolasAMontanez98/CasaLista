package com.example.casalista

import com.example.casalista.data.Role
import com.example.casalista.data.User
import com.example.casalista.data.filterUsers
import org.junit.Assert.assertEquals
import org.junit.Test

class UserFilterTest {

    private val users = listOf(
        User("Vicente Vendedor", "vendedor@casalista.co", Role.SELLER),
        User("Ana Compradora", "comprador@casalista.co", Role.BUYER),
        User("Adriana Admin", "admin@casalista.co", Role.ADMIN),
    )

    @Test
    fun filtersByTextAndRole() {
        assertEquals(listOf("Adriana Admin"), filterUsers(users, "adri", null).map { it.name })
        assertEquals(listOf("Vicente Vendedor"), filterUsers(users, "casalista", Role.SELLER).map { it.name })
    }

    @Test
    fun emptyQueryReturnsEveryoneSortedByName() {
        assertEquals(
            listOf("Adriana Admin", "Ana Compradora", "Vicente Vendedor"),
            filterUsers(users, "", null).map { it.name },
        )
    }
}
