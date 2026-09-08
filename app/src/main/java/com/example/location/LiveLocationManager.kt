package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class LiveGpsLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val altitudeMeters: Double = 0.0,
    val speedMps: Float = 0f,
    val provider: String = "GPS",
    val addressLine: String = "",
    val area: String = "",
    val city: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun formattedCoordinates(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lngDir = if (longitude >= 0) "E" else "W"
        return String.format(Locale.US, "%.4f° %s, %.4f° %s", kotlin.math.abs(latitude), latDir, kotlin.math.abs(longitude), lngDir)
    }

    fun formattedAccuracy(): String {
        return if (accuracyMeters > 0) "±${accuracyMeters.roundToInt()}m" else "High"
    }

    fun formattedAddress(): String {
        return when {
            area.isNotBlank() && city.isNotBlank() -> "$area, $city"
            addressLine.isNotBlank() -> addressLine
            city.isNotBlank() -> city
            else -> formattedCoordinates()
        }
    }
}

sealed interface GpsTrackingStatus {
    object Idle : GpsTrackingStatus
    object Locating : GpsTrackingStatus
    data class Active(val location: LiveGpsLocation) : GpsTrackingStatus
    object PermissionRequired : GpsTrackingStatus
    object GpsDisabled : GpsTrackingStatus
    data class Error(val message: String) : GpsTrackingStatus
}

class LiveLocationManager(private val context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _currentLocation = MutableStateFlow<LiveGpsLocation?>(null)
    val currentLocation: StateFlow<LiveGpsLocation?> = _currentLocation.asStateFlow()

    private val _trackingStatus = MutableStateFlow<GpsTrackingStatus>(GpsTrackingStatus.Idle)
    val trackingStatus: StateFlow<GpsTrackingStatus> = _trackingStatus.asStateFlow()

    private var isTracking = false

    private val fusedLocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                handleNewLocation(location, "Fused GPS")
            }
        }
    }

    private val fallbackLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            handleNewLocation(location, location.provider ?: "GPS")
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {
            if (provider == LocationManager.GPS_PROVIDER) {
                _trackingStatus.value = GpsTrackingStatus.GpsDisabled
            }
        }
    }

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    fun isGpsProviderEnabled(): Boolean {
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    @SuppressLint("MissingPermission")
    fun startLiveTracking() {
        if (!hasLocationPermission()) {
            _trackingStatus.value = GpsTrackingStatus.PermissionRequired
            return
        }

        if (!isGpsProviderEnabled()) {
            _trackingStatus.value = GpsTrackingStatus.GpsDisabled
            return
        }

        if (isTracking) return
        isTracking = true
        _trackingStatus.value = GpsTrackingStatus.Locating

        try {
            // First get last known location immediately
            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                if (lastLoc != null) {
                    handleNewLocation(lastLoc, "Last Known")
                }
            }

            // Request continuous live updates with high accuracy
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000L // update every 3 seconds
            ).apply {
                setMinUpdateIntervalMillis(1500L)
                setMinUpdateDistanceMeters(0.5f) // updates on small movements
                setWaitForAccurateLocation(false)
            }.build()

            fusedClient.requestLocationUpdates(
                locationRequest,
                fusedLocationCallback,
                Looper.getMainLooper()
            ).addOnFailureListener {
                startFrameworkFallbackTracking()
            }
        } catch (e: Exception) {
            startFrameworkFallbackTracking()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startFrameworkFallbackTracking() {
        if (locationManager == null || !hasLocationPermission()) return
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    1f,
                    fallbackLocationListener,
                    Looper.getMainLooper()
                )
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,
                    1f,
                    fallbackLocationListener,
                    Looper.getMainLooper()
                )
            }
        } catch (e: Exception) {
            _trackingStatus.value = GpsTrackingStatus.Error(e.message ?: "Failed to start GPS")
        }
    }

    fun stopLiveTracking() {
        if (!isTracking) return
        isTracking = false
        try {
            fusedClient.removeLocationUpdates(fusedLocationCallback)
            locationManager?.removeUpdates(fallbackLocationListener)
        } catch (_: Exception) {}
        _trackingStatus.value = GpsTrackingStatus.Idle
    }

    private fun handleNewLocation(location: Location, providerName: String) {
        coroutineScope.launch {
            val addressInfo = reverseGeocode(location.latitude, location.longitude)
            val gpsLocation = LiveGpsLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = location.accuracy,
                altitudeMeters = location.altitude,
                speedMps = location.speed,
                provider = providerName,
                addressLine = addressInfo.addressLine,
                area = addressInfo.area,
                city = addressInfo.city,
                timestamp = System.currentTimeMillis()
            )
            _currentLocation.value = gpsLocation
            _trackingStatus.value = GpsTrackingStatus.Active(gpsLocation)
        }
    }

    private data class GeocodeResult(
        val addressLine: String,
        val area: String,
        val city: String
    )

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): GeocodeResult =
        withContext(Dispatchers.IO) {
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val area = addr.subLocality ?: addr.locality ?: addr.subAdminArea ?: ""
                        val city = addr.locality ?: addr.adminArea ?: "Pune"
                        val fullLine = addr.getAddressLine(0) ?: "$area, $city"
                        return@withContext GeocodeResult(fullLine, area, city)
                    }
                }
            } catch (_: Exception) {}
            // Fallback default city info if geocoder is offline
            GeocodeResult("Current GPS Location", "Local Area", "Pune")
        }

    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    companion object {
        /**
         * Calculates the Haversine distance in kilometers between two GPS points
         */
        fun calculateDistanceKm(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double
        ): Double {
            val r = 6371.0 // Radius of the Earth in km
            val latDistance = Math.toRadians(lat2 - lat1)
            val lonDistance = Math.toRadians(lon2 - lon1)
            val a = sin(latDistance / 2) * sin(latDistance / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(lonDistance / 2) * sin(lonDistance / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            val distance = r * c
            return (distance * 10.0).roundToInt() / 10.0
        }
    }
}
