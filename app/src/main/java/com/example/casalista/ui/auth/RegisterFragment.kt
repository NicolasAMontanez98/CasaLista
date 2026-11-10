package com.example.casalista.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.casalista.R
import com.example.casalista.data.Datos
import com.example.casalista.data.Role
import com.example.casalista.data.User
import com.example.casalista.databinding.FragmentRegisterBinding
import com.example.casalista.util.Validators

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentRegisterBinding.bind(view)
        binding.registerButton.setOnClickListener {
            val nameError = Validators.nameError(binding.nameInput.text.toString().trim())
            val emailError = Validators.newEmailError(binding.emailInput.text.toString().trim())
            val passwordError = Validators.passwordError(binding.passwordInput.text.toString())
            val ageMissing = !binding.ageCheck.isChecked
            binding.nameLayout.error = nameError?.let(::getString)
            binding.emailLayout.error = emailError?.let(::getString)
            binding.passwordLayout.error = passwordError?.let(::getString)
            binding.ageError.isVisible = ageMissing
            if (listOf(nameError, emailError, passwordError).all { it == null } && !ageMissing) {
                val user = User(
                    binding.nameInput.text.toString().trim(),
                    binding.emailInput.text.toString().trim(),
                    binding.passwordInput.text.toString(),
                    Role.BUYER,
                )
                Datos.users.add(user)
                Toast.makeText(requireContext(), R.string.register_done, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }
}
