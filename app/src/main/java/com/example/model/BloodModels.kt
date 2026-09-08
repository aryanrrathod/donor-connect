package com.example.model

enum class BloodGroup(val label: String) {
    A_POS("A+"),
    A_NEG("A-"),
    B_POS("B+"),
    B_NEG("B-"),
    AB_POS("AB+"),
    AB_NEG("AB-"),
    O_POS("O+"),
    O_NEG("O-");

    companion object {
        fun fromLabel(label: String): BloodGroup =
            entries.find { it.label.equals(label, ignoreCase = true) } ?: O_POS
    }
}

enum class AvailabilityStatus(val label: String) {
    AVAILABLE("Available"),
    LOW("Low"),
    NOT_AVAILABLE("Not Available")
}

enum class UrgencyLevel(val label: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

enum class FacilityType(val label: String) {
    HOSPITAL("Hospital"),
    BLOOD_BANK("Blood Bank")
}
