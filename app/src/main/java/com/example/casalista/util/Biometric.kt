package com.example.casalista.util

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/** Indica si el dispositivo tiene un lector de huella disponible con una huella registrada. */
fun Fragment.biometricAvailable() = BiometricManager.from(requireContext())
    .canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

/**
 * Muestra el cuadro del sistema para pedir la huella. Ejecuta [onSuccess] solo si la
 * huella es válida; si el usuario cancela o falla, no pasa nada.
 */
fun Fragment.showBiometricPrompt(title: String, cancelText: String, onSuccess: () -> Unit) {
    val prompt = BiometricPrompt(
        this,
        ContextCompat.getMainExecutor(requireContext()),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) =
                onSuccess()
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setNegativeButtonText(cancelText)
        .setAllowedAuthenticators(BIOMETRIC_STRONG)
        .build()
    prompt.authenticate(info)
}
