package com.example.casalista

import com.example.casalista.util.salesCsv
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvTest {

    @Test
    fun salesCsvListsMonthsThenSystems() {
        val csv = salesCsv(listOf("Oct" to 390L, "Nov" to 520L), listOf("Madera" to 900L))
        assertEquals("Mes,Ventas\nOct,390\nNov,520\n\nSistema,Ventas\nMadera,900\n", csv)
    }
}
