package com.example.casalista.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.casalista.R
import com.example.casalista.data.AuthRepository
import com.example.casalista.data.Role
import com.example.casalista.data.User
import com.example.casalista.databinding.FragmentLoginBinding
import com.example.casalista.ui.admin.AdminActivity
import com.example.casalista.ui.buyer.BuyerActivity
import com.example.casalista.ui.common.goTo
import com.example.casalista.ui.seller.SellerActivity
import com.example.casalista.util.SessionManager
import com.example.casalista.util.Validators
import com.example.casalista.util.biometricAvailable
import com.example.casalista.util.showBiometricPrompt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio de sesión. Valida el correo y la contraseña, entra con Firebase y
 * abre la pantalla que corresponde al rol. También permite entrar con la huella cuando
 * ya hay una sesión guardada.
 */
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

    /** Valida los campos y, si son correctos, inicia sesión; los errores se muestran en el campo. */
    private fun login(binding: FragmentLoginBinding) {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()
        val emailError = Validators.emailError(email)
        val passwordError = Validators.passwordError(password)
        binding.emailLayout.error = emailError?.let(::getString)
        binding.passwordLayout.error = passwordError?.let(::getString)
        if (emailError != null || passwordError != null) return

        binding.loginButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                enter(AuthRepository.login(email, password))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.passwordLayout.error = getString(AuthRepository.errorMessage(e))
                binding.loginButton.isEnabled = true
            }
        }
    }

    /** Guarda el usuario en la sesión y abre la pantalla de su rol, cerrando la de acceso. */
    private fun enter(user: User) {
        SessionManager.current = user
        val screen = when (user.role) {
            Role.BUYER -> BuyerActivity::class.java
            Role.SELLER -> SellerActivity::class.java
            Role.ADMIN -> AdminActivity::class.java
        }
        startActivity(Intent(requireContext(), screen))
        requireActivity().finish()
    }

    /**
     * Muestra el botón de huella solo si el dispositivo la soporta, el usuario la tiene
     * activada y hay una sesión de Firebase guardada. Al validar la huella, entra sin contraseña.
     */
    private fun setupFingerprint(binding: FragmentLoginBinding) {
        val session = SessionManager(requireContext())
        binding.fingerprintButton.isVisible =
            biometricAvailable() && session.fingerprintEnabled && AuthRepository.isSignedIn

        binding.fingerprintButton.setOnClickListener {
            showBiometricPrompt(
                getString(R.string.biometric_title),
                getString(R.string.biometric_use_password),
            ) {
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        enter(AuthRepository.loadCurrent())
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), AuthRepository.errorMessage(e), Toast.LENGTH_SHORT).show()
                        binding.fingerprintButton.isVisible = AuthRepository.isSignedIn
                    }
                }
            }
        }
    }
}
