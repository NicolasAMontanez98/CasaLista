package com.example.casalista.ui.buyer

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.casalista.R
import com.example.casalista.ui.common.goTo
import com.example.casalista.data.Cart
import com.example.casalista.databinding.FragmentCartBinding
import com.example.casalista.ui.common.CartAdapter
import com.example.casalista.util.toPesos

class CartFragment : Fragment(R.layout.fragment_cart) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentCartBinding.bind(view)
        lateinit var adapter: CartAdapter

        fun refresh() {
            val items = Cart.items
            adapter.submitList(items)
            binding.emptyText.isVisible = items.isEmpty()
            binding.summary.isVisible = items.isNotEmpty()
            binding.totalText.text = Cart.total.toPesos()
        }

        adapter = CartAdapter(
            onMinus = { Cart.setQuantity(it.house.id, it.quantity - 1); refresh() },
            onPlus = { Cart.setQuantity(it.house.id, it.quantity + 1); refresh() },
            onRemove = { Cart.remove(it.house.id); refresh() },
        )
        binding.cartList.adapter = adapter
        binding.checkoutButton.setOnClickListener {
            goTo(R.id.checkoutFragment)
        }
        refresh()
    }
}
