package com.example.casalista.data

import com.example.casalista.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs

enum class PaymentState { APPROVED, PENDING, REJECTED, FAILED }

data class PaymentResult(val state: PaymentState, val ref: String = "")

object Epayco {
    const val RESPONSE_URL = "https://casalista.app/respuesta"
    private const val VALIDATION_URL = "https://secure.epayco.co/validation/v1/reference/"

    fun checkoutHtml(key: String, invoice: String, amount: Long, description: String) = """
        <html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <script src="https://checkout.epayco.co/checkout.js"></script>
        </head><body>
        <script>
        var handler = ePayco.checkout.configure({ key: '$key', test: true });
        handler.open({
            name: 'CasaLista',
            description: '$description',
            invoice: '$invoice',
            currency: 'cop',
            amount: '$amount',
            tax_base: '0',
            tax: '0',
            country: 'co',
            lang: 'es',
            external: 'true',
            response: '$RESPONSE_URL'
        });
        </script>
        </body></html>
    """.trimIndent()

    fun chargeAmount(total: Long) = minOf(total, BuildConfig.EPAYCO_TEST_AMOUNT)

    fun refOf(url: String): String? =
        Regex("[?&]ref_payco=([^&#]+)").find(url)?.groupValues?.get(1)

    suspend fun validate(ref: String, invoice: String, amount: Long): PaymentResult =
        withContext(Dispatchers.IO) {
            val connection = URL(VALIDATION_URL + ref).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                parse(connection.inputStream.bufferedReader().readText(), invoice, amount)
            } finally {
                connection.disconnect()
            }
        }

    fun parse(json: String, invoice: String, amount: Long): PaymentResult {
        val data = JSONObject(json).optJSONObject("data") ?: return PaymentResult(PaymentState.FAILED)
        val ref = data.optString("x_ref_payco")
        val state = when (data.optString("x_cod_response")) {
            "1" -> PaymentState.APPROVED
            "2" -> PaymentState.REJECTED
            "3" -> PaymentState.PENDING
            else -> PaymentState.FAILED
        }
        val sameInvoice = data.optString("x_id_invoice").let { it.isEmpty() || it == invoice }
        val sameAmount = data.optString("x_amount").toDoubleOrNull()?.let { abs(it - amount) < 1 } ?: true
        if (state == PaymentState.APPROVED && !(sameInvoice && sameAmount)) {
            return PaymentResult(PaymentState.FAILED, ref)
        }
        return PaymentResult(state, ref)
    }
}
