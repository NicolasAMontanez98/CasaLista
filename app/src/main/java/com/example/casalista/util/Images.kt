package com.example.casalista.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import java.io.ByteArrayOutputStream

/**
 * Lee la imagen de [uri], la reduce para que su lado más largo no pase de [maxSide]
 * píxeles y la devuelve en JPEG al 80 % de calidad. Así las fotos pesan poco al subirlas.
 */
fun compressImage(context: Context, uri: Uri, maxSide: Int = 1280): ByteArray {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val scale = maxSide.toFloat() / maxOf(info.size.width, info.size.height)
        if (scale < 1f) {
            decoder.setTargetSize((info.size.width * scale).toInt(), (info.size.height * scale).toInt())
        }
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    }
    val out = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
    return out.toByteArray()
}
