package com.example.casalista.ui.seller

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.casalista.R
import com.example.casalista.ui.common.goTo
import com.example.casalista.data.AuthRepository
import com.example.casalista.data.Datos
import com.example.casalista.data.HouseRepository
import com.example.casalista.data.Order
import com.example.casalista.databinding.FragmentProductPanelBinding
import com.example.casalista.ui.common.HouseAdapter
import com.example.casalista.ui.common.addFilterChip
import com.example.casalista.ui.common.selectedValue
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ProductPanelFragment : Fragment(R.layout.fragment_product_panel) {

    private var status: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentProductPanelBinding.bind(view)
        val adapter = HouseAdapter { openForm(it.id) }
        binding.productList.adapter = adapter
        binding.addButton.setOnClickListener { openForm("") }

        fun refresh() {
            val found = Datos.search(
                binding.searchInput.text.toString(), null, Order.NAME, status, includePaused = true,
            )
            adapter.submitList(found)
            binding.emptyText.isVisible = found.isEmpty()
        }

        binding.searchInput.doAfterTextChanged { refresh() }
        binding.statusChips.setOnCheckedStateChangeListener { group, ids ->
            status = (group as ChipGroup).selectedValue(ids)
            refresh()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                HouseRepository.load()
                binding.statusChips.addFilterChip(getString(R.string.catalog_all), null, status == null)
                Datos.statuses.forEach { binding.statusChips.addFilterChip(it, it, it == status) }
                refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(requireContext(), AuthRepository.errorMessage(e), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openForm(houseId: String) =
        goTo(R.id.productFormFragment, bundleOf("houseId" to houseId))
}
