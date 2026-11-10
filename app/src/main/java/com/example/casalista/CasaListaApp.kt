package com.example.casalista

import android.app.Application
import com.example.casalista.util.ThemeManager

class CasaListaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.apply(this)
    }
}
