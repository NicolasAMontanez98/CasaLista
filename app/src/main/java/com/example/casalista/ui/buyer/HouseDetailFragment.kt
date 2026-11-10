package com.example.casalista.ui.buyer

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.casalista.R
import com.example.casalista.ui.common.goTo
import com.example.casalista.data.Cart
import com.example.casalista.data.Datos
import com.example.casalista.data.House
import com.example.casalista.databinding.FragmentHouseDetailBinding
import com.example.casalista.util.toPesos
import com.google.android.material.snackbar.Snackbar

fun Fragment.openHouseDetail(house: House) =
    goTo(R.id.houseDetailFragment, bundleOf("houseId" to house.id))

class HouseDetailFragment : Fragment(R.layout.fragment_house_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentHouseDetailBinding.bind(view)
        val house = Datos.houses.first { it.id == requireArguments().getInt("houseId") }

        binding.toolbar.title = house.name
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.price.text = house.price.toPesos()
        binding.status.text = house.status
        binding.details.text = getString(
            R.string.house_details, house.area, house.bedrooms, house.system,
        )
        binding.finishes.text = Datos.finishes.joinToString("\n") { "• $it" }

        binding.addButton.isEnabled = house.inStock
        binding.addButton.setOnClickListener {
            Cart.add(house)
            Snackbar.make(view, R.string.cart_added, Snackbar.LENGTH_SHORT).show()
        }
    }
}
