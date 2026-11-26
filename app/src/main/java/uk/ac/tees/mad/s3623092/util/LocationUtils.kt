package uk.ac.tees.mad.s3623092.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationUtils {

    fun hasLocationPermission(
        context: Context
    ): Boolean {
        val finePermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarsePermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return finePermission || coarsePermission
    }

    fun hasPreciseLocationPermission(
        context: Context
    ): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCurrentLocation(
        context: Context,
        locationClient: FusedLocationProviderClient,
        onSuccess: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onError(
                "Location permission is required."
            )
            return
        }

        try {
            val cancellationTokenSource =
                CancellationTokenSource()

            locationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location)
                } else {
                    onError(
                        "Current location is unavailable."
                    )
                }
            }.addOnFailureListener {
                onError(
                    "Unable to get current location."
                )
            }
        } catch (_: SecurityException) {
            onError(
                "Location permission was not granted."
            )
        }
    }

    suspend fun getAddressFromLocation(
        context: Context,
        location: Location
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (
                    !Geocoder.isPresent()
                ) {
                    return@withContext null
                }

                val geocoder = Geocoder(
                    context,
                    Locale.getDefault()
                )

                @Suppress(
                    "DEPRECATION"
                )
                val addresses: List<Address>? =
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    )

                val address = addresses
                    ?.firstOrNull()
                    ?: return@withContext null

                formatAddress(address)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun formatCoordinates(
        location: Location
    ): String {
        return String.format(
            Locale.UK,
            "%.5f, %.5f",
            location.latitude,
            location.longitude
        )
    }

    private fun formatAddress(
        address: Address
    ): String {
        val addressParts = listOfNotNull(
            address.locality,
            address.adminArea,
            address.countryName
        ).filter {
            it.isNotBlank()
        }

        return addressParts.joinToString(
            separator = ", "
        ).ifBlank {
            address.getAddressLine(0)
        }
    }
}