package com.example.casalista.ui.buyer

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Message
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.casalista.BuildConfig
import com.example.casalista.R
import com.example.casalista.data.Epayco
import com.example.casalista.data.PaymentResult
import com.example.casalista.data.PaymentState
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
fun Fragment.showPayment(invoice: String, amount: Long, onResult: (PaymentResult?) -> Unit) {
    val context = requireContext()
    val dialog = Dialog(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar)
    val frame = FrameLayout(context)
    var handled = false

    val client = object : WebViewClient() {
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            val url = request.url.toString()
            val ref = Epayco.refOf(url)
            if (ref == null || handled) return null
            handled = true
            view.post {
                viewLifecycleOwner.lifecycleScope.launch {
                    val result = try {
                        Epayco.validate(ref, invoice, amount)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        PaymentResult(PaymentState.FAILED)
                    }
                    dialog.dismiss()
                    onResult(result)
                }
            }
            return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
        }
    }

    lateinit var newWebView: () -> WebView

    // ePayco opens the 3D Secure step with window.open and talks to that popup, so it needs its own WebView
    val chrome = object : WebChromeClient() {
        override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
            val popup = newWebView()
            frame.addView(popup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            (resultMsg.obj as WebView.WebViewTransport).webView = popup
            resultMsg.sendToTarget()
            return true
        }

        override fun onCloseWindow(window: WebView) {
            frame.removeView(window)
            window.destroy()
        }
    }

    newWebView = {
        WebView(context).also {
            it.settings.javaScriptEnabled = true
            it.settings.domStorageEnabled = true
            it.settings.javaScriptCanOpenWindowsAutomatically = true
            it.settings.setSupportMultipleWindows(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(it, true)
            it.webViewClient = client
            it.webChromeClient = chrome
        }
    }

    val web = newWebView()
    frame.addView(web, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    val close = MaterialButton(context).apply {
        setText(R.string.pay_cancel)
        setOnClickListener { dialog.cancel() }
    }
    val content = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        fitsSystemWindows = true
        addView(close, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        addView(frame, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    }
    dialog.setContentView(content)
    dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    dialog.setOnCancelListener { onResult(null) }
    dialog.setOnDismissListener {
        for (i in 0 until frame.childCount) (frame.getChildAt(i) as? WebView)?.destroy()
        frame.removeAllViews()
    }
    dialog.show()

    web.loadDataWithBaseURL(
        "https://casalista.app/",
        Epayco.checkoutHtml(BuildConfig.EPAYCO_KEY, invoice, amount, getString(R.string.pay_description)),
        "text/html", "UTF-8", null,
    )
}
