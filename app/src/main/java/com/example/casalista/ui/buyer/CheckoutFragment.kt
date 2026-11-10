package com.example.casalista.ui.buyer

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.casalista.R
import com.example.casalista.data.Cart
import com.example.casalista.data.Datos
import com.example.casalista.databinding.FragmentCheckoutBinding
import com.example.casalista.util.SessionManager
import com.example.casalista.util.Validators
import com.example.casalista.util.biometricAvailable
import com.example.casalista.util.showBiometricPrompt
import com.example.casalista.util.toPesos

class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    private lateinit var binding: FragmentCheckoutBinding
    private var step = 0

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        it?.let { photo ->
            binding.lotPhoto.setImageBitmap(photo)
            binding.lotPhoto.isVisible = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentCheckoutBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.photoButton.setOnClickListener { takePhoto.launch(null) }
        binding.backButton.setOnClickListener { showStep(step - 1) }
        binding.homeButton.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }

        binding.nextButton.setOnClickListener {
            when (step) {
                0 -> goToMethodIfAddressValid()
                1 -> showStep(2)
                else -> if (biometricAvailable()) {
                    showBiometricPrompt(
                        getString(R.string.checkout_pay),
                        getString(R.string.checkout_back),
                        ::approve,
                    )
                } else {
                    approve()
                }
            }
        }
        showStep(0)
    }

    private fun goToMethodIfAddressValid() {
        val error = Validators.addressError(binding.addressInput.text.toString().trim())
        binding.addressLayout.error = error?.let(::getString)
        if (error == null) showStep(1)
    }

    private fun showStep(index: Int) {
        step = index
        listOf(binding.stepLot, binding.stepMethod, binding.stepConfirm, binding.stepResult)
            .forEachIndexed { i, group -> group.isVisible = i == index }
        val inProgress = index < 3
        binding.stepLabel.isVisible = inProgress
        binding.buttons.isVisible = inProgress
        binding.stepLabel.text = getString(R.string.checkout_step, index + 1)
        binding.backButton.isVisible = index > 0
        binding.nextButton.setText(if (index == 2) R.string.checkout_pay else R.string.checkout_next)
        if (index == 2) showSummary()
    }

    private fun showSummary() {
        val method = binding.root.findViewById<RadioButton>(binding.methodGroup.checkedRadioButtonId)
        binding.summaryText.text = getString(
            R.string.checkout_summary,
            Cart.total.toPesos(),
            binding.addressInput.text.toString().trim(),
            method.text,
        )
    }

    private fun approve() {
        val order = "CL-%05d".format(System.currentTimeMillis() % 100_000)
        binding.resultMessage.text = getString(R.string.checkout_done_message, order)
        Datos.placeOrders(
            order,
            SessionManager(requireContext()).user?.name.orEmpty(),
            binding.addressInput.text.toString().trim(),
        )
        Cart.clear()
        showStep(3)
    }
}
