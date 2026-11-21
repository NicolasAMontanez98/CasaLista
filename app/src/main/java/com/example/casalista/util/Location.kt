package com.example.casalista.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Pide al GPS la ubicación actual con la mayor precisión posible. Devuelve null si no
 * se pudo obtener. Quien llama debe haber conseguido antes el permiso de ubicación.
 */
@SuppressLint("MissingPermission")
suspend fun currentLocation(context: Context): Location? =
    LocationServices.getFusedLocationProviderClient(context)
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .await()

/** Convierte unas coordenadas en una dirección escrita (geocodificación inversa); null si no hay. */
suspend fun addressOf(context: Context, location: Location): String? = withContext(Dispatchers.IO) {
    runCatching {
        @Suppress("DEPRECATION")
        Geocoder(context, Locale.getDefault())
            .getFromLocation(location.latitude, location.longitude, 1)
            ?.firstOrNull()?.getAddressLine(0)
    }.getOrNull()
}
