package com.telotaxi.planner.weather

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class WeatherRepository(private val context: Context) {

    private val api = OpenMeteoApi.create()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /** Récupère la position GPS actuelle du chauffeur (nécessite la permission de localisation). */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<Double, Double>? = suspendCancellableCoroutine { cont ->
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(Pair(location.latitude, location.longitude))
                } else {
                    cont.resume(null)
                }
            }
            .addOnFailureListener { cont.resume(null) }
    }

    suspend fun getForecast(lat: Double, lon: Double): WeatherResponse {
        return api.getForecast(latitude = lat, longitude = lon)
    }
}
