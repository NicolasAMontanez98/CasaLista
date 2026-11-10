package com.example.casalista.util

fun salesCsv(byMonth: List<Pair<String, Long>>, bySystem: List<Pair<String, Long>>): String =
    buildString {
        appendLine("Mes,Ventas")
        byMonth.forEach { (month, total) -> appendLine("$month,$total") }
        appendLine()
        appendLine("Sistema,Ventas")
        bySystem.forEach { (system, total) -> appendLine("$system,$total") }
    }
