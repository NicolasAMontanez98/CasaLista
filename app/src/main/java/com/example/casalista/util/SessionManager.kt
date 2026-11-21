package com.example.casalista.util

import android.content.Context
import com.example.casalista.data.User

/**
 * Datos de la sesión actual. El usuario con sesión abierta ([current]) vive en memoria;
 * la preferencia de huella se guarda en el dispositivo con SharedPreferences.
 */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    /** Si el usuario permite entrar con huella (activada por defecto). */
    var fingerprintEnabled: Boolean
        get() = prefs.getBoolean("fingerprint", true)
        set(value) = prefs.edit().putBoolean("fingerprint", value).apply()

    val user: User? get() = current

    companion object {
        /** Usuario con sesión abierta, o null si nadie ha entrado. */
        var current: User? = null
    }
}
