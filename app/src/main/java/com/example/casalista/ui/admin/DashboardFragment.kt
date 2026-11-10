package com.example.casalista.ui.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.casalista.R
import com.example.casalista.data.Datos
import com.example.casalista.data.OrderStatus
import com.example.casalista.data.Status
import com.example.casalista.databinding.FragmentDashboardBinding
import com.example.casalista.util.toPesos

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentDashboardBinding.bind(view)
        binding.salesValue.text = Datos.totalSales.toPesos()
        binding.ordersValue.text = Datos.orders.size.toString()
        binding.usersValue.text = Datos.users.size.toString()
        binding.productsValue.text = Datos.published.size.toString()
        binding.salesChart.bars = Datos.monthlySales
        binding.pendingOrders.text = getString(
            R.string.dash_pending_orders, Datos.orders.count { it.status == OrderStatus.PENDING },
        )
        binding.pendingProducts.text = getString(
            R.string.dash_pending_products, Datos.houses.count { it.status == Status.REVIEW },
        )
    }
}
