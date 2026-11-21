package com.example.casalista.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.casalista.R
import com.example.casalista.data.AuthRepository
import com.example.casalista.data.Datos
import com.example.casalista.data.HouseRepository
import com.example.casalista.data.Status
import com.example.casalista.databinding.FragmentAdminProductsBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Cola de revisión: casas que los vendedores enviaron y esperan que el administrador las apruebe o rechace. */
class AdminProductsFragment : Fragment(R.layout.fragment_admin_products) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentAdminProductsBinding.bind(view)
        lateinit var adapter: ReviewAdapter

        /** Vuelve a calcular la cola con las casas en revisión. */
        fun refresh() {
            val queue = Datos.houses.filter { it.status == Status.REVIEW }
            adapter.submitList(queue)
            binding.emptyText.isVisible = queue.isEmpty()
        }

        /** Muestra el mensaje de error que corresponde a la excepción. */
        fun showError(e: Exception) =
            Toast.makeText(requireContext(), AuthRepository.errorMessage(e), Toast.LENGTH_SHORT).show()

        adapter = ReviewAdapter { house, approved ->
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    HouseRepository.review(house, approved)
                    refresh()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    showError(e)
                }
            }
        }
        binding.reviewList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                HouseRepository.load()
                refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
            }
        }
    }
}
