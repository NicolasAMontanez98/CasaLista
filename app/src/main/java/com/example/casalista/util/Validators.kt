package com.example.casalista.util

import com.example.casalista.R
import com.example.casalista.data.Datos

object Validators {
    private val emailFormat = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun nameError(name: String): Int? =
        if (name.isEmpty()) R.string.error_name_required else null

    fun requiredError(value: String): Int? =
        if (value.isEmpty()) R.string.error_required else null

    fun positiveError(value: String): Int? =
        if ((value.toLongOrNull() ?: 0) > 0) null else R.string.error_number_positive

    fun addressError(address: String): Int? =
        if (address.isEmpty()) R.string.error_address_required else null

    fun emailError(email: String): Int? = when {
        email.isEmpty() -> R.string.error_email_required
        !emailFormat.matches(email) -> R.string.error_email_invalid
        else -> null
    }

    fun newEmailError(email: String): Int? = emailError(email)
        ?: if (Datos.findByEmail(email) != null) R.string.error_email_taken else null

    fun passwordError(password: String): Int? = when {
        password.isEmpty() -> R.string.error_password_required
        password.length < 6 -> R.string.error_password_short
        else -> null
    }
}
