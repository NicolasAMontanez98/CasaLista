package com.example.casalista

import com.example.casalista.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test
    fun nameError() {
        assertEquals(R.string.error_name_required, Validators.nameError(""))
        assertNull(Validators.nameError("Ana"))
    }

    @Test
    fun requiredError() {
        assertEquals(R.string.error_required, Validators.requiredError(""))
        assertNull(Validators.requiredError("Casa Nueva"))
    }

    @Test
    fun positiveError() {
        assertEquals(R.string.error_number_positive, Validators.positiveError(""))
        assertEquals(R.string.error_number_positive, Validators.positiveError("0"))
        assertEquals(R.string.error_number_positive, Validators.positiveError("abc"))
        assertNull(Validators.positiveError("85000000"))
    }

    @Test
    fun addressError() {
        assertEquals(R.string.error_address_required, Validators.addressError(""))
        assertNull(Validators.addressError("Lote 4, vereda La Mesa"))
    }

    @Test
    fun emailError() {
        assertEquals(R.string.error_email_required, Validators.emailError(""))
        assertEquals(R.string.error_email_invalid, Validators.emailError("ana@casa"))
        assertEquals(R.string.error_email_invalid, Validators.emailError("ana casa@x.co"))
        assertNull(Validators.emailError("ana@casa.co"))
    }

    @Test
    fun newEmailErrorRejectsTakenEmails() {
        assertEquals(R.string.error_email_taken, Validators.newEmailError("admin@casalista.co"))
        assertEquals(R.string.error_email_invalid, Validators.newEmailError("admin"))
        assertNull(Validators.newEmailError("nuevo@casalista.co"))
    }

    @Test
    fun passwordError() {
        assertEquals(R.string.error_password_required, Validators.passwordError(""))
        assertEquals(R.string.error_password_short, Validators.passwordError("12345"))
        assertNull(Validators.passwordError("123456"))
    }
}
