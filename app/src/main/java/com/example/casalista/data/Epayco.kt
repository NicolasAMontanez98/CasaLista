package com.example.casalista.data

import com.example.casalista.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs

/** Resultado de un pago según ePayco. */
enum class PaymentState { APPROVED, PENDING, REJECTED, FAILED }

/** Estado del pago y su referencia en ePayco ([ref]). */
data class PaymentResult(val state: PaymentState, val ref: String = "")

/**
 * Integración con ePayco en modo de pruebas: arma la página del checkout, detecta la
 * referencia del pago y la valida con la API pública de ePayco.
 */
object Epayco {
    /** Dirección a la que ePayco redirige al terminar; la app la intercepta, nunca se carga. */
    const val RESPONSE_URL = "https://casalista.app/respuesta"
    private const val VALIDATION_URL = "https://secure.epayco.co/validation/v1/reference/"

    /** HTML que abre el checkout de ePayco con la llave pública, el monto y la factura. */
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

    /** Monto a cobrar: el total, con el tope que permite la cuenta de pruebas. */
    fun chargeAmount(total: Long) = minOf(total, BuildConfig.EPAYCO_TEST_AMOUNT)

    /** Extrae el parámetro `ref_payco` de una dirección, o null si no lo trae. */
    fun refOf(url: String): String? =
        Regex("[?&]ref_payco=([^&#]+)").find(url)?.groupValues?.get(1)

    /** Consulta a ePayco el resultado de la transacción y lo compara con la factura y el monto. */
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

    /**
     * Interpreta la respuesta JSON de ePayco. El código 1 es aprobada, 2 rechazada,
     * 3 pendiente y cualquier otro, fallida. Una aprobada solo se acepta si la factura
     * y el monto coinciden con los de esta compra.
     */
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
