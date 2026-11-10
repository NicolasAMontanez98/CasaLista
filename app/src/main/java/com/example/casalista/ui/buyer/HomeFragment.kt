package com.example.casalista.ui.buyer

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.casalista.R
import com.example.casalista.ui.common.goTo
import com.example.casalista.data.Datos
import com.example.casalista.databinding.FragmentHomeBinding
import com.example.casalista.ui.common.HouseAdapter
import com.google.android.material.chip.Chip

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentHomeBinding.bind(view)

        binding.searchInput.setOnEditorActionListener { input, action, _ ->
            val search = action == EditorInfo.IME_ACTION_SEARCH
            if (search) openCatalog("query" to input.text.toString())
            search
        }

        Datos.systems.forEach { system ->
            val chip = Chip(requireContext())
            chip.text = system
            chip.setOnClickListener { openCatalog("system" to system) }
            binding.categoryChips.addView(chip)
        }

        val adapter = HouseAdapter(::openHouseDetail)
        binding.featuredList.adapter = adapter
        adapter.submitList(Datos.published.take(3))
    }

    private fun openCatalog(filter: Pair<String, String>) =
        goTo(R.id.catalogFragment, bundleOf(filter))
}
