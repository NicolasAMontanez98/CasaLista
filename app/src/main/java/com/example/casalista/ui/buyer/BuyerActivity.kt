package com.example.casalista.ui.buyer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.casalista.R
import com.example.casalista.databinding.ActivityBuyerBinding

class BuyerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBuyerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        binding.bottomNav.setupWithNavController(navHost.navController)
        binding.bottomNav.setOnItemReselectedListener {
            navHost.navController.popBackStack(it.itemId, false)
        }
    }
}
