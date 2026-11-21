package com.example.casalista.ui.seller

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.casalista.R
import com.example.casalista.data.AuthRepository
import com.example.casalista.data.Datos
import com.example.casalista.data.OrderRepository
import com.example.casalista.data.OrderStatus
import com.example.casalista.databinding.FragmentOrderDetailBinding
import com.example.casalista.util.toPesos
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Detalle de un pedido para el vendedor: datos de la compra, botón para avanzar su estado,
 * cancelarlo y ver la dirección de entrega en el mapa.
 */
class OrderDetailFragment : Fragment(R.layout.fragment_order_detail) {

    private lateinit var binding: FragmentOrderDetailBinding
    private lateinit var orderId: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentOrderDetailBinding.bind(view)
        orderId = requireArguments().getString("orderId").orEmpty()
        binding.advanceButton.setOnClickListener {
            Datos.orders.first { it.id == orderId }.status.next?.let(::changeStatus)
        }
        binding.cancelButton.setOnClickListener { confirmCancel() }
        binding.mapButton.setOnClickListener { openMap() }
        render()
    }

    /** Guarda el nuevo estado del pedido y vuelve a dibujar la pantalla. */
    private fun changeStatus(status: OrderStatus) {
        binding.advanceButton.isEnabled = false
        binding.cancelButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                OrderRepository.updateStatus(Datos.orders.first { it.id == orderId }, status)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Snackbar.make(binding.root, AuthRepository.errorMessage(e), Snackbar.LENGTH_SHORT).show()
            }
            binding.advanceButton.isEnabled = true
            binding.cancelButton.isEnabled = true
            render()
        }
    }

    /** Abre la app de mapas en las coordenadas de entrega; avisa si el teléfono no tiene ninguna. */
    private fun openMap() {
        val order = Datos.orders.first { it.id == orderId }
        val label = Uri.encode(order.address)
        val map = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${order.lat},${order.lng}?q=${order.lat},${order.lng}($label)"))
        try {
            startActivity(map)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(binding.root, R.string.order_no_map_app, Snackbar.LENGTH_SHORT).show()
        }
    }

    /** Pide confirmación antes de cancelar el pedido, porque no se puede deshacer. */
    private fun confirmCancel() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.order_cancel_title)
            .setMessage(R.string.order_cancel_message)
            .setNegativeButton(R.string.detail_back, null)
            .setPositiveButton(R.string.order_cancel_confirm) { _, _ -> changeStatus(OrderStatus.CANCELLED) }
            .show()
    }

    /** Muestra los datos del pedido y deja visibles solo los botones que aplican a su estado. */
    private fun render() {
        val order = Datos.orders.first { it.id == orderId }
        binding.orderCode.text = order.code
        binding.orderDetails.text = getString(
            R.string.order_details,
            order.buyer, order.houseName, order.quantity,
            order.total.toPesos(), order.address, order.status.label,
        )
        val next = order.status.next
        binding.advanceButton.isVisible = next != null
        when (next) {
            OrderStatus.IN_PROGRESS -> binding.advanceButton.setText(R.string.order_accept)
            OrderStatus.SHIPPED -> binding.advanceButton.setText(R.string.order_ship)
            OrderStatus.DELIVERED -> binding.advanceButton.setText(R.string.order_deliver)
            else -> Unit
        }
        binding.cancelButton.isVisible = order.status.canCancel
        binding.mapButton.isVisible = order.lat != null && order.lng != null
    }
}
