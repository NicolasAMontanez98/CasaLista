package com.example.casalista.ui.buyer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.casalista.R
import com.example.casalista.data.Cart
import com.example.casalista.data.CartRepository
import com.example.casalista.data.HouseRepository
import com.example.casalista.databinding.ActivityBuyerBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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
        syncCart()
    }

    private fun syncCart() {
        Cart.onChange = null
        Cart.restore(emptyMap())
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        lifecycleScope.launch {
            runCatching {
                HouseRepository.load()
                CartRepository.load(uid)
            }
            Cart.onChange = { lifecycleScope.launch { runCatching { CartRepository.save(uid) } } }
        }
    }

    override fun onDestroy() {
        Cart.onChange = null
        super.onDestroy()
    }
}
