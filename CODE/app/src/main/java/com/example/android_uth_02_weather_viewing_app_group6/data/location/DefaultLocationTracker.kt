package com.example.android_uth_02_weather_viewing_app_group6.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class DefaultLocationTracker(
    private val locationClient: FusedLocationProviderClient,
    private val context: Context
) : LocationTracker {

    override suspend fun getCurrentLocation(): LocationResult {
        val hasAccessFineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasAccessCoarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAccessFineLocationPermission && !hasAccessCoarseLocationPermission) {
            return LocationResult.PermissionDenied
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled) {
            return LocationResult.GpsDisabled
        }

        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            locationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    if (continuation.isActive) continuation.resume(LocationResult.Success(location))
                } else {
                    // Fallback to lastLocation if current location is null
                    locationClient.lastLocation
                        .addOnSuccessListener { lastLoc: Location? ->
                            if (continuation.isActive) {
                                if (lastLoc != null) {
                                    continuation.resume(LocationResult.Success(lastLoc))
                                } else {
                                    continuation.resume(LocationResult.Error("Unable to retrieve current location"))
                                }
                            }
                        }
                        .addOnFailureListener { exc ->
                            if (continuation.isActive) {
                                continuation.resume(LocationResult.Error(exc.localizedMessage ?: "Failed to get location"))
                            }
                        }
                }
            }.addOnFailureListener { exc ->
                if (continuation.isActive) {
                    continuation.resume(LocationResult.Error(exc.localizedMessage ?: "Location fetch failed"))
                }
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }
}
