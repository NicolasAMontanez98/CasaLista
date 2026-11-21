package com.example.casalista.ui.common

import android.view.LayoutInflater
import android.view.View
import com.example.casalista.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/** Agrega un chip de filtro al grupo; [value] se guarda en el chip para leerlo después. */
fun ChipGroup.addFilterChip(label: String, value: String?, checked: Boolean) {
    val chip = LayoutInflater.from(context).inflate(R.layout.chip_filter, this, false) as Chip
    chip.id = View.generateViewId()
    chip.text = label
    chip.tag = value
    chip.isChecked = checked
    addView(chip)
}

/** Devuelve el valor del chip marcado (el primero de [checkedIds]). */
fun ChipGroup.selectedValue(checkedIds: List<Int>): String? =
    findViewById<Chip>(checkedIds.first()).tag as String?
