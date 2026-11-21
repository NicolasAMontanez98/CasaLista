package com.example.casalista

import com.example.casalista.data.Epayco
import com.example.casalista.data.PaymentState
import org.junit.Assert.assertEquals
import org.junit.Test

class EpaycoTest {

    private fun response(code: Int, invoice: String = "CL-00001", amount: String = "68000000") =
        """{"success":true,"data":{"x_cod_response":$code,"x_ref_payco":"777","x_id_invoice":"$invoice","x_amount":"$amount"}}"""

    @Test
    fun responseCodesMapToPaymentStates() {
        assertEquals(PaymentState.APPROVED, Epayco.parse(response(1), "CL-00001", 68_000_000).state)
        assertEquals(PaymentState.REJECTED, Epayco.parse(response(2), "CL-00001", 68_000_000).state)
        assertEquals(PaymentState.PENDING, Epayco.parse(response(3), "CL-00001", 68_000_000).state)
        assertEquals(PaymentState.FAILED, Epayco.parse(response(4), "CL-00001", 68_000_000).state)
    }

    @Test
    fun approvedPaymentMustMatchInvoiceAndAmount() {
        assertEquals(PaymentState.FAILED, Epayco.parse(response(1, invoice = "OTRA"), "CL-00001", 68_000_000).state)
        assertEquals(PaymentState.FAILED, Epayco.parse(response(1, amount = "1000"), "CL-00001", 68_000_000).state)
        assertEquals("777", Epayco.parse(response(1), "CL-00001", 68_000_000).ref)
    }

    @Test
    fun missingDataIsFailedAndRefIsReadFromUrl() {
        assertEquals(PaymentState.FAILED, Epayco.parse("""{"status":false}""", "CL-00001", 1).state)
        assertEquals("abc123", Epayco.refOf("https://casalista.app/respuesta?ref_payco=abc123&x=1"))
    }
}
