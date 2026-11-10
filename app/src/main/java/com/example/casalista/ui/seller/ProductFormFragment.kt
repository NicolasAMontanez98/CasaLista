package com.example.casalista.ui.seller

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.casalista.R
import com.example.casalista.data.Datos
import com.example.casalista.data.House
import com.example.casalista.data.Status
import com.example.casalista.databinding.FragmentProductFormBinding
import com.example.casalista.util.Validators
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProductFormFragment : Fragment(R.layout.fragment_product_form) {

    private lateinit var binding: FragmentProductFormBinding

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        it?.let(::addThumbnail)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentProductFormBinding.bind(view)
        val house = Datos.houses.find { it.id == arguments?.getInt("houseId") }

        binding.systemInput.setAdapter(dropdown(Datos.systemOptions))
        binding.statusInput.setAdapter(dropdown(Status.editable))
        binding.systemInput.setText(house?.system ?: Datos.systemOptions.first(), false)
        binding.statusInput.setText(house?.status ?: Status.AVAILABLE, false)
        if (house != null) fill(house)
        binding.statusLayout.isVisible = house != null && house.status !in Status.hidden

        binding.photoButton.setOnClickListener { takePhoto.launch(null) }
        binding.saveButton.setOnClickListener { save(house) }
        if (house != null) setupEditing(house)
    }

    private fun dropdown(options: List<String>) =
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, options)

    private fun fill(house: House) {
        binding.nameInput.setText(house.name)
        binding.priceInput.setText(house.price.toString())
        binding.areaInput.setText(house.area.toString())
        binding.bedroomsInput.setText(house.bedrooms.toString())
    }

    private fun save(current: House?) {
        val name = binding.nameInput.text.toString().trim()
        val price = binding.priceInput.text.toString()
        val area = binding.areaInput.text.toString()
        val bedrooms = binding.bedroomsInput.text.toString()
        val errors = listOf(
            binding.nameLayout to Validators.requiredError(name),
            binding.priceLayout to Validators.positiveError(price),
            binding.areaLayout to Validators.positiveError(area),
            binding.bedroomsLayout to Validators.positiveError(bedrooms),
        )
        errors.forEach { (layout, error) -> layout.error = error?.let(::getString) }
        if (errors.any { it.second != null }) return

        Datos.saveHouse(
            House(
                id = current?.id ?: Datos.nextHouseId(),
                name = name,
                price = price.toLong(),
                area = area.toInt(),
                bedrooms = bedrooms.toInt(),
                system = binding.systemInput.text.toString(),
                status = Status.afterSave(current?.status, binding.statusInput.text.toString()),
            ),
        )
        findNavController().popBackStack()
    }

    private fun setupEditing(house: House) {
        val paused = house.status == Status.PAUSED
        binding.pauseButton.isVisible = house.status != Status.REVIEW && house.status != Status.REJECTED
        binding.pauseButton.setText(if (paused) R.string.form_resume else R.string.form_pause)
        binding.pauseButton.setOnClickListener {
            Datos.saveHouse(house.copy(status = if (paused) Status.AVAILABLE else Status.PAUSED))
            findNavController().popBackStack()
        }

        binding.deleteButton.isVisible = true
        binding.deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.form_delete_title)
                .setMessage(R.string.form_delete_message)
                .setNegativeButton(R.string.profile_cancel, null)
                .setPositiveButton(R.string.profile_delete_confirm) { _, _ ->
                    Datos.deleteHouse(house.id)
                    findNavController().popBackStack()
                }
                .show()
        }
    }

    private fun addThumbnail(photo: Bitmap) {
        val size = resources.getDimensionPixelSize(R.dimen.thumbnail)
        val image = ImageView(requireContext())
        image.layoutParams = LinearLayout.LayoutParams(size, size).apply {
            marginEnd = resources.getDimensionPixelSize(R.dimen.space_sm)
        }
        image.scaleType = ImageView.ScaleType.CENTER_CROP
        image.contentDescription = getString(R.string.form_photo_desc)
        image.setImageBitmap(photo)
        binding.photoRow.addView(image)
    }
}
