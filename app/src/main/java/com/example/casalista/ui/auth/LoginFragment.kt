package com.example.casalista.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.casalista.R
import com.example.casalista.ui.common.goTo
import com.example.casalista.data.Datos
import com.example.casalista.data.Role
import com.example.casalista.data.User
import com.example.casalista.databinding.FragmentLoginBinding
import com.example.casalista.ui.admin.AdminActivity
import com.example.casalista.ui.buyer.BuyerActivity
import com.example.casalista.ui.seller.SellerActivity
import com.example.casalista.util.SessionManager
import com.example.casalista.util.Validators
import com.example.casalista.util.biometricAvailable
import com.example.casalista.util.showBiometricPrompt

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentLoginBinding.bind(view)
        binding.loginButton.setOnClickListener { login(binding) }
        binding.forgotButton.setOnClickListener {
            goTo(R.id.recoverPasswordFragment)
        }
        binding.registerButton.setOnClickListener {
            goTo(R.id.registerFragment)
        }
        setupFingerprint(binding)
    }

    private fun login(binding: FragmentLoginBinding) {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()
        val emailError = Validators.emailError(email)
        val passwordError = Validators.passwordError(password)
        binding.emailLayout.error = emailError?.let(::getString)
        binding.passwordLayout.error = passwordError?.let(::getString)
        if (emailError != null || passwordError != null) return

        val user = Datos.login(email, password)
        if (user == null) {
            binding.passwordLayout.error = getString(R.string.error_credentials)
        } else if (!user.active) {
            binding.passwordLayout.error = getString(R.string.error_suspended)
        } else {
            SessionManager(requireContext()).lastEmail = user.email
            enter(user)
        }
    }

    private fun enter(user: User) {
        val screen = when (user.role) {
            Role.BUYER -> BuyerActivity::class.java
            Role.SELLER -> SellerActivity::class.java
            Role.ADMIN -> AdminActivity::class.java
        }
        startActivity(Intent(requireContext(), screen))
        requireActivity().finish()
    }

    private fun setupFingerprint(binding: FragmentLoginBinding) {
        val session = SessionManager(requireContext())
        binding.fingerprintButton.isVisible = biometricAvailable() && session.fingerprintEnabled

        binding.fingerprintButton.setOnClickListener {
            showBiometricPrompt(
                getString(R.string.biometric_title),
                getString(R.string.biometric_use_password),
            ) {
                val user = session.user
                if (user == null) {
                    Toast.makeText(requireContext(), R.string.biometric_first, Toast.LENGTH_SHORT).show()
                } else if (!user.active) {
                    Toast.makeText(requireContext(), R.string.error_suspended, Toast.LENGTH_SHORT).show()
                } else {
                    enter(user)
                }
            }
        }
    }
}
