package com.example.casalista

import android.app.Application
import com.example.casalista.util.ThemeManager

/** Clase de la aplicación: se ejecuta una vez al abrir la app y aplica el tema guardado. */
class CasaListaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.apply(this)
    }
}
