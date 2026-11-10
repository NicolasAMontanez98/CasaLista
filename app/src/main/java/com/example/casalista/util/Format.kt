package com.example.casalista.util

import java.text.NumberFormat
import java.util.Locale

private val pesos = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
    maximumFractionDigits = 0
}

fun Long.toPesos(): String = pesos.format(this)
