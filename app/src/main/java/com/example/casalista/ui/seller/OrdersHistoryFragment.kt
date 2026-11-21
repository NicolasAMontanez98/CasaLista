package com.example.casalista.ui.seller

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.casalista.R
import com.example.casalista.data.AuthRepository
import com.example.casalista.data.Datos
import com.example.casalista.data.OrderRepository
import com.example.casalista.databinding.FragmentOrdersHistoryBinding
import com.example.casalista.ui.common.OrderAdapter
import com.example.casalista.ui.common.goTo
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class OrdersHistoryFragment : Fragment(R.layout.fragment_orders_history) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentOrdersHistoryBinding.bind(view)
        val adapter = OrderAdapter {
            goTo(R.id.orderDetailFragment, bundleOf("orderId" to it.id))
        }
        binding.orderList.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                OrderRepository.loadAll()
                adapter.submitList(Datos.orders)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Snackbar.make(view, AuthRepository.errorMessage(e), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
