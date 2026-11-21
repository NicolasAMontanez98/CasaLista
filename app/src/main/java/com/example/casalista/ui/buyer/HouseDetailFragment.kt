package com.example.casalista.ui.buyer

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.casalista.R
import com.example.casalista.ui.common.goTo
import com.example.casalista.data.Cart
import com.example.casalista.data.Datos
import com.example.casalista.data.House
import com.example.casalista.databinding.FragmentHouseDetailBinding
import com.example.casalista.util.toPesos
import com.google.android.material.snackbar.Snackbar

/** Abre el detalle de una casa, enviando su id como argumento. */
fun Fragment.openHouseDetail(house: House) =
    goTo(R.id.houseDetailFragment, bundleOf("houseId" to house.id))

/** Detalle de una casa: galería de fotos, precio, características y botón para agregarla al carrito. */
class HouseDetailFragment : Fragment(R.layout.fragment_house_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentHouseDetailBinding.bind(view)
        val house = Datos.houses.first { it.id == requireArguments().getString("houseId") }

        binding.toolbar.title = house.name
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.price.text = house.price.toPesos()
        binding.status.text = house.status
        binding.details.text = getString(
            R.string.house_details, house.area, house.bedrooms, house.system,
        )
        binding.galleryScroll.isVisible = house.images.isNotEmpty()
        house.images.forEachIndexed { index, url ->
            val image = layoutInflater.inflate(R.layout.item_gallery_image, binding.gallery, false) as ImageView
            image.contentDescription = getString(R.string.detail_photo, index + 1)
            Glide.with(image).load(url).into(image)
            binding.gallery.addView(image)
        }
        binding.finishes.text = Datos.finishes.joinToString("\n") { "• $it" }

        binding.addButton.isEnabled = house.inStock
        binding.addButton.setOnClickListener {
            Cart.add(house)
            Snackbar.make(view, R.string.cart_added, Snackbar.LENGTH_SHORT).show()
        }
    }
}
