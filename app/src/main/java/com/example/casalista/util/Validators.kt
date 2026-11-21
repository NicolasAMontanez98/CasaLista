package com.example.casalista.util

import com.example.casalista.R

/**
 * Validaciones de los formularios. Cada función devuelve el recurso del mensaje de error,
 * o null si el valor es correcto.
 */
object Validators {
    private val emailFormat = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    /** El nombre no puede estar vacío. */
    fun nameError(name: String): Int? =
        if (name.isEmpty()) R.string.error_name_required else null

    /** El campo es obligatorio: no puede estar vacío. */
    fun requiredError(value: String): Int? =
        if (value.isEmpty()) R.string.error_required else null

    /** El texto debe ser un número entero mayor que cero. */
    fun positiveError(value: String): Int? =
        if ((value.toLongOrNull() ?: 0) > 0) null else R.string.error_number_positive

    /** La dirección no puede estar vacía. */
    fun addressError(address: String): Int? =
        if (address.isEmpty()) R.string.error_address_required else null

    /** El correo no puede estar vacío y debe tener forma de correo (algo@dominio.ext). */
    fun emailError(email: String): Int? = when {
        email.isEmpty() -> R.string.error_email_required
        !emailFormat.matches(email) -> R.string.error_email_invalid
        else -> null
    }

    /** La contraseña no puede estar vacía y debe tener al menos 6 caracteres. */
    fun passwordError(password: String): Int? = when {
        password.isEmpty() -> R.string.error_password_required
        password.length < 6 -> R.string.error_password_short
        else -> null
    }
}
