package com.example.casalista.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.casalista.R
import com.example.casalista.data.AuthRepository
import com.example.casalista.databinding.FragmentRegisterBinding
import com.example.casalista.util.Validators
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentRegisterBinding.bind(view)
        binding.registerButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString()
            val nameError = Validators.nameError(name)
            val emailError = Validators.emailError(email)
            val passwordError = Validators.passwordError(password)
            val ageMissing = !binding.ageCheck.isChecked
            binding.nameLayout.error = nameError?.let(::getString)
            binding.emailLayout.error = emailError?.let(::getString)
            binding.passwordLayout.error = passwordError?.let(::getString)
            binding.ageError.isVisible = ageMissing
            if (listOf(nameError, emailError, passwordError).all { it == null } && !ageMissing) {
                register(binding, name, email, password)
            }
        }
    }

    private fun register(binding: FragmentRegisterBinding, name: String, email: String, password: String) {
        binding.registerButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                AuthRepository.register(name, email, password)
                Toast.makeText(requireContext(), R.string.register_done, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = getString(AuthRepository.errorMessage(e))
                if (e is FirebaseAuthUserCollisionException) {
                    binding.emailLayout.error = message
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
                binding.registerButton.isEnabled = true
            }
        }
    }
}
