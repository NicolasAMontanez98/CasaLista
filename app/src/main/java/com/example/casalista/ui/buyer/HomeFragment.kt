package com.example.casalista.ui.buyer

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.casalista.R
import com.example.casalista.ui.common.goTo
import com.example.casalista.data.AuthRepository
import com.example.casalista.data.Datos
import com.example.casalista.data.HouseRepository
import com.example.casalista.databinding.FragmentHomeBinding
import com.example.casalista.ui.common.HouseAdapter
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio del comprador: buscador, atajos por sistema constructivo y las
 * tres primeras casas publicadas como destacadas.
 */
class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentHomeBinding.bind(view)

        binding.searchInput.setOnEditorActionListener { input, action, _ ->
            val search = action == EditorInfo.IME_ACTION_SEARCH
            if (search) openCatalog("query" to input.text.toString())
            search
        }

        val adapter = HouseAdapter(::openHouseDetail)
        binding.featuredList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                HouseRepository.load()
                Datos.systems.forEach { system ->
                    val chip = Chip(requireContext())
                    chip.text = system
                    chip.setOnClickListener { openCatalog("system" to system) }
                    binding.categoryChips.addView(chip)
                }
                adapter.submitList(Datos.published.take(3))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(requireContext(), AuthRepository.errorMessage(e), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Abre el catálogo con un filtro inicial (texto buscado o sistema elegido). */
    private fun openCatalog(filter: Pair<String, String>) =
        goTo(R.id.catalogFragment, bundleOf(filter))
}
