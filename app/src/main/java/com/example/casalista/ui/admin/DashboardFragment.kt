package com.example.casalista.ui.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.casalista.R
import com.example.casalista.data.Datos
import com.example.casalista.data.HouseRepository
import com.example.casalista.data.OrderRepository
import com.example.casalista.data.OrderStatus
import com.example.casalista.data.Status
import com.example.casalista.data.UserRepository
import com.example.casalista.databinding.FragmentDashboardBinding
import com.example.casalista.util.toPesos
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Panel del administrador: ventas totales, pedidos, usuarios, productos y la gráfica de ventas.
 * Si no hay casas en Firestore, carga las de ejemplo la primera vez.
 */
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentDashboardBinding.bind(view)
        binding.salesValue.text = "…"
        binding.ordersValue.text = "…"
        binding.pendingOrders.text = ""
        binding.usersValue.text = "…"
        binding.productsValue.text = "…"
        binding.pendingProducts.text = ""

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                HouseRepository.seedIfEmpty()
                HouseRepository.load()
                OrderRepository.loadAll()
                binding.salesValue.text = Datos.totalSales.toPesos()
                binding.ordersValue.text = Datos.orders.size.toString()
                binding.salesChart.bars = Datos.monthlySales(6)
                binding.pendingOrders.text = getString(
                    R.string.dash_pending_orders, Datos.orders.count { it.status == OrderStatus.PENDING },
                )
                binding.productsValue.text = Datos.published.size.toString()
                binding.pendingProducts.text = getString(
                    R.string.dash_pending_products, Datos.houses.count { it.status == Status.REVIEW },
                )
                binding.usersValue.text = UserRepository.getAll().size.toString()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.usersValue.text = "—"
                binding.productsValue.text = "—"
                binding.salesValue.text = "—"
                binding.ordersValue.text = "—"
            }
        }
    }
}
