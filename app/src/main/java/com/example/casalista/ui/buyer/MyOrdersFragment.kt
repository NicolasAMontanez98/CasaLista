package com.example.casalista.ui.buyer

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.casalista.R
import com.example.casalista.data.AuthRepository
import com.example.casalista.data.Datos
import com.example.casalista.data.OrderRepository
import com.example.casalista.databinding.FragmentMyOrdersBinding
import com.example.casalista.ui.common.OrderAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Historial de compras: muestra los pedidos del comprador con su estado actual. */
class MyOrdersFragment : Fragment(R.layout.fragment_my_orders) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentMyOrdersBinding.bind(view)
        val adapter = OrderAdapter {}
        binding.orderList.adapter = adapter
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                OrderRepository.loadMine(uid)
                adapter.submitList(Datos.orders)
                binding.emptyText.isVisible = Datos.orders.isEmpty()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Snackbar.make(view, AuthRepository.errorMessage(e), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
