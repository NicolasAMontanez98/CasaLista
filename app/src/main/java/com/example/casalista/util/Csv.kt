package com.example.casalista.util

/**
 * Arma el texto CSV del reporte de ventas: primero una tabla de ventas por mes y,
 * separada por una línea en blanco, otra de ventas por sistema constructivo.
 */
fun salesCsv(byMonth: List<Pair<String, Long>>, bySystem: List<Pair<String, Long>>): String =
    buildString {
        appendLine("Mes,Ventas")
        byMonth.forEach { (month, total) -> appendLine("$month,$total") }
        appendLine()
        appendLine("Sistema,Ventas")
        bySystem.forEach { (system, total) -> appendLine("$system,$total") }
    }
