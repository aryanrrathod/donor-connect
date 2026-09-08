package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object MapUtils {
    /**
     * Opens Google Maps centered on the given coordinates with an optional label marker.
     */
    fun openGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String = "") {
        try {
            val encodedLabel = Uri.encode(label)
            val uri = if (label.isNotBlank()) {
                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
            } else {
                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
            }
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback to web browser or generic map handler
            try {
                val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Cannot open Google Maps: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Opens Google Maps turn-by-turn navigation or directions to a destination coordinate.
     */
    fun openGoogleMapsDirections(context: Context, destLat: Double, destLng: Double, destinationName: String = "") {
        try {
            val gmmIntentUri = Uri.parse("google.navigation:q=$destLat,$destLng")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            try {
                val fallbackUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Cannot open Google Maps navigation: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Opens Google Maps search for nearby blood banks / hospitals near the current user location.
     */
    fun openNearbyBloodBanksInMaps(context: Context, latitude: Double, longitude: Double) {
        try {
            val uri = Uri.parse("geo:$latitude,$longitude?q=blood+banks+hospitals")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            try {
                val fallbackUri = Uri.parse("https://www.google.com/maps/search/blood+banks+hospitals/@$latitude,$longitude,14z")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Unable to open Google Maps", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
