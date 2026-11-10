package com.example.casalista.ui.seller

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.casalista.R
import com.example.casalista.ui.common.goTo
import com.example.casalista.data.Datos
import com.example.casalista.databinding.FragmentOrdersHistoryBinding
import com.example.casalista.ui.common.OrderAdapter

class OrdersHistoryFragment : Fragment(R.layout.fragment_orders_history) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentOrdersHistoryBinding.bind(view)
        val adapter = OrderAdapter {
            goTo(R.id.orderDetailFragment, bundleOf("orderId" to it.id))
        }
        binding.orderList.adapter = adapter
        adapter.submitList(Datos.orders.sortedByDescending { it.id })
    }
}
