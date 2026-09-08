package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Utility for synchronizing real-time location and opening Google Maps
 * for directions, blood banks, donors, and hospital navigation.
 */
object MapsIntegration {

    /**
     * Opens Google Maps centered on the specified coordinates with an optional pin label.
     */
    fun openGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String = "") {
        try {
            val uriStr = if (label.isNotBlank()) {
                "geo:$latitude,$longitude?q=${Uri.encode("$latitude,$longitude ($label)")}"
            } else {
                "geo:$latitude,$longitude?q=$latitude,$longitude"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback to web browser Google Maps
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        } catch (e: Exception) {
            try {
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            } catch (e2: Exception) {
                Toast.makeText(context, "Unable to open Google Maps", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Opens Google Maps in Turn-by-Turn Navigation mode to the destination.
     */
    fun openDirectionsInGoogleMaps(context: Context, destLat: Double, destLng: Double, label: String = "") {
        try {
            val navUri = Uri.parse("google.navigation:q=$destLat,$destLng")
            val intent = Intent(Intent.ACTION_VIEW, navUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng")
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        } catch (e: Exception) {
            try {
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng")
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            } catch (e2: Exception) {
                Toast.makeText(context, "Unable to launch Google Maps directions", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openDirectionsInGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String = "", dummy: Unit = Unit) {
        openDirectionsInGoogleMaps(context, destLat = latitude, destLng = longitude, label = label)
    }

    /**
     * Searches for nearby blood banks or emergency blood centers in Google Maps.
     */
    fun searchNearbyInGoogleMaps(
        context: Context,
        query: String = "Blood Bank",
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        try {
            val uriStr = if (latitude != null && longitude != null) {
                "geo:$latitude,$longitude?q=${Uri.encode(query)}"
            } else {
                "geo:0,0?q=${Uri.encode(query)}"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val webUri = Uri.parse("https://www.google.com/maps/search/${Uri.encode(query)}")
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to search Google Maps", Toast.LENGTH_SHORT).show()
        }
    }
}
