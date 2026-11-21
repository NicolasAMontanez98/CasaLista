package com.example.casalista.ui.seller

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.casalista.R
import com.example.casalista.data.AuthRepository
import com.example.casalista.data.Datos
import com.example.casalista.data.House
import com.example.casalista.data.HouseRepository
import com.example.casalista.data.Status
import com.example.casalista.databinding.FragmentProductFormBinding
import com.example.casalista.util.Validators
import com.example.casalista.util.compressImage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProductFormFragment : Fragment(R.layout.fragment_product_form) {

    private lateinit var binding: FragmentProductFormBinding
    private val savedPhotos = mutableListOf<String>()
    private val newPhotos = mutableListOf<Uri>()
    private var cameraUri: Uri? = null

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        val uri = cameraUri
        if (taken && uri != null) addPhoto(uri)
    }

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) addPhoto(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentProductFormBinding.bind(view)
        val house = Datos.houses.find { it.id == arguments?.getString("houseId") }

        binding.systemInput.setAdapter(dropdown(Datos.systemOptions))
        binding.statusInput.setAdapter(dropdown(Status.editable))
        binding.systemInput.setText(house?.system ?: Datos.systemOptions.first(), false)
        binding.statusInput.setText(house?.status ?: Status.AVAILABLE, false)
        if (house != null) fill(house)
        binding.statusLayout.isVisible = house != null && house.status !in Status.hidden

        binding.photoButton.setOnClickListener { choosePhotoSource() }
        binding.saveButton.setOnClickListener { save(house) }
        if (house != null) setupEditing(house)
        renderPhotos()
    }

    private fun dropdown(options: List<String>) =
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, options)

    private fun fill(house: House) {
        binding.nameInput.setText(house.name)
        binding.priceInput.setText(house.price.toString())
        binding.areaInput.setText(house.area.toString())
        binding.bedroomsInput.setText(house.bedrooms.toString())
        if (savedPhotos.isEmpty()) savedPhotos.addAll(house.images)
    }

    private fun choosePhotoSource() {
        if (savedPhotos.size + newPhotos.size >= MAX_PHOTOS) {
            Toast.makeText(requireContext(), R.string.form_photo_max, Toast.LENGTH_SHORT).show()
            return
        }
        val options = arrayOf(getString(R.string.form_photo_camera), getString(R.string.form_photo_gallery))
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.form_photo)
            .setItems(options) { _, which ->
                if (which == 0) openCamera() else {
                    pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
            .show()
    }

    private fun openCamera() {
        val folder = File(requireContext().cacheDir, "photos").apply { mkdirs() }
        val file = File.createTempFile("photo_", ".jpg", folder)
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        cameraUri = uri
        takePhoto.launch(uri)
    }

    private fun addPhoto(uri: Uri) {
        newPhotos.add(uri)
        renderPhotos()
    }

    private fun renderPhotos() {
        binding.photoRow.removeAllViews()
        val size = resources.getDimensionPixelSize(R.dimen.thumbnail)
        val photos: List<Any> = savedPhotos + newPhotos
        photos.forEach { photo ->
            val image = ImageView(requireContext())
            image.layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = resources.getDimensionPixelSize(R.dimen.space_sm)
            }
            image.scaleType = ImageView.ScaleType.CENTER_CROP
            image.contentDescription = getString(R.string.form_photo_desc)
            Glide.with(image).load(photo).into(image)
            image.setOnClickListener {
                savedPhotos.remove(photo)
                newPhotos.remove(photo)
                renderPhotos()
            }
            binding.photoRow.addView(image)
        }
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

        val houseId = current?.id ?: HouseRepository.newId()
        val oldPhotos = current?.images.orEmpty()
        val keptPhotos = savedPhotos.toList()
        val toUpload = newPhotos.toList()
        val system = binding.systemInput.text.toString()
        val status = Status.afterSave(current?.status, binding.statusInput.text.toString())
        val context = requireContext()

        setButtonsEnabled(false)
        viewLifecycleOwner.lifecycleScope.launch {
            val uploaded = mutableListOf<String>()
            try {
                for (uri in toUpload) {
                    val bytes = withContext(Dispatchers.IO) { compressImage(context, uri) }
                    uploaded.add(HouseRepository.uploadImage(houseId, bytes))
                }
                val house = House(
                    id = houseId,
                    name = name,
                    price = price.toLong(),
                    area = area.toInt(),
                    bedrooms = bedrooms.toInt(),
                    system = system,
                    status = status,
                    images = keptPhotos + uploaded,
                )
                HouseRepository.save(house)
                HouseRepository.deleteImages(oldPhotos - keptPhotos.toSet())
                findNavController().popBackStack()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                HouseRepository.deleteImages(uploaded)
                showError(e)
                setButtonsEnabled(true)
            }
        }
    }

    private fun setupEditing(house: House) {
        val paused = house.status == Status.PAUSED
        binding.pauseButton.isVisible = house.status != Status.REVIEW && house.status != Status.REJECTED
        binding.pauseButton.setText(if (paused) R.string.form_resume else R.string.form_pause)
        binding.pauseButton.setOnClickListener {
            finishWith {
                HouseRepository.save(house.copy(status = if (paused) Status.AVAILABLE else Status.PAUSED))
            }
        }

        binding.deleteButton.isVisible = true
        binding.deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.form_delete_title)
                .setMessage(R.string.form_delete_message)
                .setNegativeButton(R.string.profile_cancel, null)
                .setPositiveButton(R.string.profile_delete_confirm) { _, _ ->
                    finishWith { HouseRepository.delete(house) }
                }
                .show()
        }
    }

    private fun finishWith(action: suspend () -> Unit) {
        setButtonsEnabled(false)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                action()
                findNavController().popBackStack()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
                setButtonsEnabled(true)
            }
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.saveButton.isEnabled = enabled
        binding.pauseButton.isEnabled = enabled
        binding.deleteButton.isEnabled = enabled
        binding.photoButton.isEnabled = enabled
    }

    private fun showError(e: Exception) {
        Toast.makeText(requireContext(), AuthRepository.errorMessage(e), Toast.LENGTH_SHORT).show()
    }

    private companion object {
        const val MAX_PHOTOS = 5
    }
}
