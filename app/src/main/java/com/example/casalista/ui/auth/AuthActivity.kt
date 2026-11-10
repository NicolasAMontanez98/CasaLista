package com.example.casalista.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.casalista.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityAuthBinding.inflate(layoutInflater).root)
    }
}
