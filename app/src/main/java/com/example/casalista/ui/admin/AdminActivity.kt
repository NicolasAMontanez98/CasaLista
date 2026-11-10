package com.example.casalista.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.casalista.R
import com.example.casalista.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        val topLevel = setOf(
            R.id.dashboardFragment,
            R.id.usersFragment,
            R.id.adminProductsFragment,
            R.id.salesReportFragment,
            R.id.adminProfileFragment,
        )
        binding.toolbar.setupWithNavController(
            navHost.navController,
            AppBarConfiguration(topLevel, binding.drawerLayout),
        )
        binding.navView.setupWithNavController(navHost.navController)
    }
}
