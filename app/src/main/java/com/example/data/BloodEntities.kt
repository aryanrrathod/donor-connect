package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donors")
data class DonorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bloodGroup: String,
    val distanceKm: Double,
    val area: String,
    val city: String,
    val phone: String,
    val isAvailable: Boolean = true,
    val totalDonations: Int = 1,
    val lastDonationDate: String = "15 Jan 2026",
    val latOffset: Float = 0f,
    val lngOffset: Float = 0f
)

@Entity(tableName = "emergency_requests")
data class EmergencyRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientName: String,
    val bloodGroup: String,
    val unitsRequired: Int,
    val hospitalName: String,
    val location: String,
    val urgency: String,
    val contactNumber: String,
    val additionalNotes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Urgent" // "Urgent", "Fulfilled", "Pending"
)

@Entity(tableName = "facilities")
data class FacilityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "Hospital" or "Blood Bank"
    val distanceKm: Double,
    val city: String,
    val address: String,
    val phone: String,
    val isOpen24x7: Boolean = true,
    val availableStockSummary: String = "All groups available",
    val latOffset: Float = 0f,
    val lngOffset: Float = 0f
)

@Entity(tableName = "blood_stock")
data class BloodStockEntity(
    @PrimaryKey
    val bloodGroup: String,
    val unitsAvailable: Int,
    val status: String, // "Available", "Low", "Not Available"
    val lastUpdated: String = "Just now"
)

@Entity(tableName = "donation_history")
data class DonationRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hospitalName: String,
    val donationDate: String,
    val unitsDonated: Int = 1,
    val bloodGroup: String = "O+",
    val certificateId: String
)

val DonorEntity.latitude: Double get() = 18.5204 + latOffset.toDouble()
val DonorEntity.longitude: Double get() = 73.8567 + lngOffset.toDouble()

val FacilityEntity.latitude: Double get() = 18.5204 + latOffset.toDouble()
val FacilityEntity.longitude: Double get() = 73.8567 + lngOffset.toDouble()
