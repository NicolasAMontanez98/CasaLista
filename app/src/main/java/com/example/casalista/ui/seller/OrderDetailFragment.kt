package com.example.casalista.ui.seller

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.casalista.R
import com.example.casalista.data.Datos
import com.example.casalista.data.OrderStatus
import com.example.casalista.databinding.FragmentOrderDetailBinding
import com.example.casalista.util.toPesos
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class OrderDetailFragment : Fragment(R.layout.fragment_order_detail) {

    private lateinit var binding: FragmentOrderDetailBinding
    private var orderId = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentOrderDetailBinding.bind(view)
        orderId = requireArguments().getInt("orderId")
        binding.advanceButton.setOnClickListener {
            val order = Datos.orders.first { it.id == orderId }
            order.status.next?.let { Datos.updateOrder(order.copy(status = it)) }
            render()
        }
        binding.cancelButton.setOnClickListener { confirmCancel() }
        render()
    }

    private fun confirmCancel() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.order_cancel_title)
            .setMessage(R.string.order_cancel_message)
            .setNegativeButton(R.string.detail_back, null)
            .setPositiveButton(R.string.order_cancel_confirm) { _, _ ->
                val order = Datos.orders.first { it.id == orderId }
                Datos.updateOrder(order.copy(status = OrderStatus.CANCELLED))
                render()
            }
            .show()
    }

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
    }
}
