package com.example.casalista.ui.buyer

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.casalista.BuildConfig
import com.example.casalista.R
import com.example.casalista.data.Cart
import com.example.casalista.data.Epayco
import com.example.casalista.data.OrderRepository
import com.example.casalista.data.PaymentState
import com.example.casalista.databinding.FragmentCheckoutBinding
import com.example.casalista.util.SessionManager
import com.example.casalista.util.Validators
import com.example.casalista.util.addressOf
import com.example.casalista.util.biometricAvailable
import com.example.casalista.util.currentLocation
import com.example.casalista.util.showBiometricPrompt
import com.example.casalista.util.toPesos
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Proceso de pago en cuatro pasos: 1) dirección del lote (a mano o con el GPS) y foto opcional,
 * 2) método de pago, 3) confirmación con huella y cobro en ePayco, 4) resultado. El pedido solo se
 * guarda cuando ePayco confirma el pago.
 */
class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    private lateinit var binding: FragmentCheckoutBinding
    private var step = 0

    private var lat: Double? = null
    private var lng: Double? = null
    private var typingByUser = true
    private var code = ""
    private var paidRef: String? = null

    private val askLocation = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.any { it }) useMyLocation() else showMessage(R.string.checkout_location_denied)
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        it?.let { photo ->
            binding.lotPhoto.setImageBitmap(photo)
            binding.lotPhoto.isVisible = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentCheckoutBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.locationButton.setOnClickListener {
            askLocation.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
        binding.addressInput.doAfterTextChanged {
            if (typingByUser) {
                lat = null
                lng = null
            }
        }
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

    /** Muestra un aviso corto en la parte inferior. */
    private fun showMessage(message: Int) = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()

    /**
     * Obtiene la ubicación del GPS, la convierte en dirección y la escribe en el campo;
     * guarda las coordenadas para el pedido.
     */
    private fun useMyLocation() {
        binding.locationButton.isEnabled = false
        binding.locationButton.setText(R.string.checkout_locating)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val location = currentLocation(requireContext())
                if (location == null) {
                    showMessage(R.string.checkout_location_failed)
                } else {
                    val text = addressOf(requireContext(), location)
                        ?: "%.5f, %.5f".format(location.latitude, location.longitude)
                    typingByUser = false
                    binding.addressInput.setText(text)
                    typingByUser = true
                    lat = location.latitude
                    lng = location.longitude
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showMessage(R.string.checkout_location_failed)
            }
            binding.locationButton.isEnabled = true
            binding.locationButton.setText(R.string.checkout_use_location)
        }
    }

    /** Avanza al paso del método de pago solo si la dirección no está vacía. */
    private fun goToMethodIfAddressValid() {
        val error = Validators.addressError(binding.addressInput.text.toString().trim())
        binding.addressLayout.error = error?.let(::getString)
        if (error == null) showStep(1)
    }

    /** Muestra el paso indicado y ajusta la etiqueta y los botones. */
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

    /** Escribe el resumen de la compra (total, dirección y método) antes de pagar. */
    private fun showSummary() {
        val method = binding.root.findViewById<RadioButton>(binding.methodGroup.checkedRadioButtonId)
        binding.summaryText.text = getString(
            R.string.checkout_summary,
            Cart.total.toPesos(),
            binding.addressInput.text.toString().trim(),
            method.text,
        )
    }

    /**
     * Abre el cobro en ePayco. Si ya hay un pago aprobado pendiente de guardar, solo reintenta guardar el pedido
     * sin cobrar de nuevo.
     */
    private fun approve() {
        paidRef?.let { return placeOrder(it) }
        if (BuildConfig.EPAYCO_KEY.isBlank()) return showMessage(R.string.pay_no_key)
        code = "CL-%05d".format(System.currentTimeMillis() % 100_000)
        showPayment(code, Epayco.chargeAmount(Cart.total)) { result ->
            when (result?.state) {
                PaymentState.APPROVED -> {
                    paidRef = result.ref
                    placeOrder(result.ref)
                }
                PaymentState.REJECTED -> showMessage(R.string.pay_rejected)
                PaymentState.PENDING -> showMessage(R.string.pay_pending)
                PaymentState.FAILED -> showMessage(R.string.pay_failed)
                null -> showMessage(R.string.pay_cancelled)
            }
        }
    }

    /**
     * Guarda los pedidos en Firestore con la referencia del pago, vacía el carrito y muestra el resultado.
     * Si falla, avisa y deja reintentar sin volver a pagar.
     */
    private fun placeOrder(ref: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.nextButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                OrderRepository.place(
                    code, uid, SessionManager.current?.name.orEmpty(),
                    binding.addressInput.text.toString().trim(), Cart.items, lat, lng, ref,
                )
                binding.resultMessage.text = getString(R.string.checkout_done_message, code)
                paidRef = null
                Cart.clear()
                showStep(3)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.nextButton.isEnabled = true
                Snackbar.make(binding.root, getString(R.string.pay_saved_failed, ref), Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
