package com.example.casalista.ui.seller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.casalista.R
import com.example.casalista.databinding.ActivitySellerBinding

class SellerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySellerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        val topLevel = setOf(R.id.productPanelFragment, R.id.ordersHistoryFragment, R.id.sellerProfileFragment)
        binding.toolbar.setupWithNavController(
            navHost.navController,
            AppBarConfiguration(topLevel, binding.drawerLayout),
        )
        binding.navView.setupWithNavController(navHost.navController)
    }
}
