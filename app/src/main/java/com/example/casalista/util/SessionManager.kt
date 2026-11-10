package com.example.casalista.util

import android.content.Context
import com.example.casalista.data.Datos
import com.example.casalista.data.User

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    var lastEmail: String?
        get() = prefs.getString("email", null)
        set(value) = prefs.edit().putString("email", value).apply()

    var fingerprintEnabled: Boolean
        get() = prefs.getBoolean("fingerprint", true)
        set(value) = prefs.edit().putBoolean("fingerprint", value).apply()

    val user: User? get() = lastEmail?.let(Datos::findByEmail)
}
