package com.example.casalista.ui.buyer

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.casalista.R
import com.example.casalista.data.Datos
import com.example.casalista.data.Order
import com.example.casalista.databinding.FragmentCatalogBinding
import com.example.casalista.ui.common.HouseAdapter
import com.example.casalista.ui.common.addFilterChip
import com.example.casalista.ui.common.selectedValue
import com.google.android.material.chip.ChipGroup

class CatalogFragment : Fragment(R.layout.fragment_catalog) {

    private var system: String? = null
    private var order = Order.PRICE_ASC

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentCatalogBinding.bind(view)
        val adapter = HouseAdapter(::openHouseDetail)
        binding.houseList.adapter = adapter

        fun refresh() {
            val found = Datos.search(binding.searchInput.text.toString(), system, order)
            adapter.submitList(found)
            binding.emptyText.isVisible = found.isEmpty()
        }

        system = arguments?.getString("system")
        binding.searchInput.setText(arguments?.getString("query"))
        binding.searchInput.doAfterTextChanged { refresh() }

        binding.filterChips.addFilterChip(getString(R.string.catalog_all), null, system == null)
        Datos.systems.forEach { binding.filterChips.addFilterChip(it, it, it == system) }
        binding.filterChips.setOnCheckedStateChangeListener { group, ids ->
            system = (group as ChipGroup).selectedValue(ids)
            refresh()
        }

        val labels = resources.getStringArray(R.array.order_options)
        binding.orderInput.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, labels),
        )
        binding.orderInput.setText(labels[order.ordinal], false)
        binding.orderInput.setOnItemClickListener { _, _, position, _ ->
            order = Order.entries[position]
            refresh()
        }

        refresh()
    }
}
