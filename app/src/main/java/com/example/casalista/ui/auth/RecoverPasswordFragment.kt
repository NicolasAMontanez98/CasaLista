package com.example.casalista.ui.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.casalista.R
import com.example.casalista.data.AuthRepository
import com.example.casalista.databinding.FragmentRecoverPasswordBinding
import com.example.casalista.util.Validators
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Pantalla para recuperar la contraseña: pide el correo y envía el enlace de restablecimiento. */
class RecoverPasswordFragment : Fragment(R.layout.fragment_recover_password) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentRecoverPasswordBinding.bind(view)
        binding.sendButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val error = Validators.emailError(email)
            binding.emailLayout.error = error?.let(::getString)
            if (error == null) send(binding, email)
        }
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
    }

    /** Envía el correo y cambia el formulario por el mensaje de confirmación. */
    private fun send(binding: FragmentRecoverPasswordBinding, email: String) {
        binding.sendButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                AuthRepository.sendPasswordReset(email)
                binding.formGroup.isVisible = false
                binding.confirmationGroup.isVisible = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.emailLayout.error = getString(AuthRepository.errorMessage(e))
                binding.sendButton.isEnabled = true
            }
        }
    }
}
