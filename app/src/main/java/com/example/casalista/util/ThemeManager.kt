package com.example.casalista.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val KEY = "dark"

    private fun prefs(context: Context) = context.getSharedPreferences("theme", Context.MODE_PRIVATE)

    fun apply(context: Context) {
        val prefs = prefs(context)
        if (!prefs.contains(KEY)) return
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.getBoolean(KEY, true)) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO,
        )
    }

    fun setDark(context: Context, dark: Boolean) {
        prefs(context).edit().putBoolean(KEY, dark).apply()
        apply(context)
    }
}
