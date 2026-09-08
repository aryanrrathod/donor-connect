package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BloodRepository
import com.example.data.BloodStockEntity
import com.example.data.DonationRecordEntity
import com.example.data.DonorEntity
import com.example.data.EmergencyRequestEntity
import com.example.data.FacilityEntity
import com.example.location.GpsTrackingStatus
import com.example.location.LiveGpsLocation
import com.example.location.LiveLocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserProfileState(
    val name: String = "",
    val age: Int = 26,
    val gender: String = "Male",
    val bloodGroup: String = "O+",
    val hasAnemia: Boolean = false,
    val role: String = "Community Member",
    val phone: String = "9876543210",
    val location: String = "Aundh, Pune",
    val isAvailable: Boolean = true,
    val totalDonations: Int = 0,
    val livesSaved: Int = 0,
    val lastDonated: String = "Never",
    val nextEligible: String = "Eligible Now",
    val pendingRequestsCount: Int = 2,
    val isRegisteredDonor: Boolean = false,
    val donorId: String = "BD-74892",
    val registrationDate: String = ""
)

data class TrackerCustomization(
    val alertRadiusKm: Int = 10,
    val updateFrequencySec: Int = 15,
    val trackingMode: String = "Live Transit",
    val assignedResponderName: String = "Rohit Verma",
    val responderRole: String = "Motorcycle Blood Courier",
    val responderContact: String = "9823011223",
    val customEtaMinutes: Int = 14,
    val hospitalWardRoom: String = "ICU Wing - Bed #4",
    val transportMode: String = "Bike Courier",
    val sirenSoundAlerts: Boolean = true,
    val smsAttendantAlerts: Boolean = true
)

class BloodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BloodRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = BloodRepository(db.bloodDao(), viewModelScope)
    }

    val allDonors: StateFlow<List<DonorEntity>> = repository.allDonors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRequests: StateFlow<List<EmergencyRequestEntity>> = repository.allRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFacilities: StateFlow<List<FacilityEntity>> = repository.allFacilities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStock: StateFlow<List<BloodStockEntity>> = repository.allStock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val donationHistory: StateFlow<List<DonationRecordEntity>> = repository.donationHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mobile GPS Live Location Manager
    val liveLocationManager = LiveLocationManager(application)
    val currentGpsLocation: StateFlow<LiveGpsLocation?> = liveLocationManager.currentLocation
    val gpsTrackingStatus: StateFlow<GpsTrackingStatus> = liveLocationManager.trackingStatus

    // Dynamic Donors sorted by real distance from live GPS location
    val donorsWithGpsDistance: StateFlow<List<DonorEntity>> = combine(
        allDonors,
        currentGpsLocation
    ) { donors, gpsLoc ->
        if (gpsLoc == null) {
            donors
        } else {
            donors.map { donor ->
                val donorLat = 18.5204 + donor.latOffset
                val donorLng = 73.8567 + donor.lngOffset
                val dist = LiveLocationManager.calculateDistanceKm(
                    gpsLoc.latitude,
                    gpsLoc.longitude,
                    donorLat,
                    donorLng
                )
                donor.copy(distanceKm = dist)
            }.sortedBy { it.distanceKm }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic Facilities sorted by real distance from live GPS location
    val facilitiesWithGpsDistance: StateFlow<List<FacilityEntity>> = combine(
        allFacilities,
        currentGpsLocation
    ) { facilities, gpsLoc ->
        if (gpsLoc == null) {
            facilities
        } else {
            facilities.map { facility ->
                val facLat = 18.5204 + facility.latOffset
                val facLng = 73.8567 + facility.lngOffset
                val dist = LiveLocationManager.calculateDistanceKm(
                    gpsLoc.latitude,
                    gpsLoc.longitude,
                    facLat,
                    facLng
                )
                facility.copy(distanceKm = dist)
            }.sortedBy { it.distanceKm }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filters for "Find Blood"
    private val _selectedBloodGroupFilter = MutableStateFlow("O+")
    val selectedBloodGroupFilter: StateFlow<String> = _selectedBloodGroupFilter.asStateFlow()

    private val _selectedLocationFilter = MutableStateFlow("Pune, Maharashtra")
    val selectedLocationFilter: StateFlow<String> = _selectedLocationFilter.asStateFlow()

    val filteredDonors: StateFlow<List<DonorEntity>> = combine(
        donorsWithGpsDistance,
        _selectedBloodGroupFilter,
        _selectedLocationFilter
    ) { donors, group, location ->
        donors.filter { donor ->
            val matchGroup = if (group == "All" || group.isEmpty()) true else donor.bloodGroup.equals(group, ignoreCase = true)
            val matchLoc = if (location == "All" || location.isEmpty()) true else {
                donor.city.contains(location, ignoreCase = true) ||
                        location.contains(donor.city, ignoreCase = true) ||
                        donor.area.contains(location, ignoreCase = true) ||
                        location.contains(donor.area, ignoreCase = true)
            }
            matchGroup && matchLoc
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Facility switcher: "Hospitals" or "Blood Banks"
    private val _selectedFacilityTab = MutableStateFlow("Hospitals")
    val selectedFacilityTab: StateFlow<String> = _selectedFacilityTab.asStateFlow()

    private val _facilitySearchQuery = MutableStateFlow("")
    val facilitySearchQuery: StateFlow<String> = _facilitySearchQuery.asStateFlow()

    val filteredFacilities: StateFlow<List<FacilityEntity>> = combine(
        facilitiesWithGpsDistance,
        _selectedFacilityTab,
        _facilitySearchQuery
    ) { facilities, tab, query ->
        val targetType = if (tab == "Hospitals") "Hospital" else "Blood Bank"
        facilities.filter { facility ->
            val matchesType = facility.type.equals(targetType, ignoreCase = true)
            val matchesQuery = query.isEmpty() ||
                    facility.name.contains(query, ignoreCase = true) ||
                    facility.city.contains(query, ignoreCase = true) ||
                    facility.address.contains(query, ignoreCase = true)
            matchesType && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time updates ticker for live emergency & blood availability synchronization
    private val _liveUpdateTicker = MutableStateFlow("All blood requests synced • Live GPS Tracking active")
    val liveUpdateTicker: StateFlow<String> = _liveUpdateTicker.asStateFlow()

    // Selected Request ID for Live Request Tracker
    private val _selectedTrackedRequestId = MutableStateFlow<Long?>(null)
    val selectedTrackedRequestId: StateFlow<Long?> = _selectedTrackedRequestId.asStateFlow()

    fun setSelectedTrackedRequestId(id: Long?) {
        _selectedTrackedRequestId.value = id
    }

    fun updateRequestStatus(requestId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateRequestStatus(requestId, newStatus)
            _liveUpdateTicker.value = "Request #$requestId status updated: $newStatus"
        }
    }

    fun advanceTrackingStage(requestId: Long, currentStatus: String) {
        val nextStatus = when (currentStatus) {
            "Urgent", "Broadcasting", "Active", "Pending" -> "Donors Notified"
            "Donors Notified" -> "Donor Accepted"
            "Donor Accepted" -> "In Transit"
            "In Transit" -> "In Screening"
            "In Screening" -> "Fulfilled"
            else -> "Fulfilled"
        }
        updateRequestStatus(requestId, nextStatus)
    }

    fun cancelEmergencyRequest(requestId: Long) {
        viewModelScope.launch {
            repository.deleteEmergencyRequest(requestId)
            if (_selectedTrackedRequestId.value == requestId) {
                _selectedTrackedRequestId.value = null
            }
            _liveUpdateTicker.value = "Request #$requestId cancelled"
        }
    }

    // Per-request Tracker Customizations
    private val _trackerCustomizations = MutableStateFlow<Map<Long, TrackerCustomization>>(emptyMap())
    val trackerCustomizations: StateFlow<Map<Long, TrackerCustomization>> = _trackerCustomizations.asStateFlow()

    fun saveTrackerCustomization(requestId: Long, customization: TrackerCustomization) {
        _trackerCustomizations.value = _trackerCustomizations.value + (requestId to customization)
        _liveUpdateTicker.value = "Tracking preferences customized for #REQ-100$requestId"
    }

    fun updateEmergencyRequest(request: EmergencyRequestEntity) {
        viewModelScope.launch {
            repository.updateEmergencyRequest(request)
            _liveUpdateTicker.value = "Request #REQ-100${request.id} details updated in database"
        }
    }

    fun triggerLiveRefresh() {
        val count = allRequests.value.size
        _liveUpdateTicker.value = "Updated just now • $count emergency requests active • Donors notified"
    }

    // User Profile with SharedPreferences Persistence
    private val prefs: SharedPreferences =
        application.getSharedPreferences("smart_blood_user_prefs", Context.MODE_PRIVATE)

    fun hasSeenOnboardingRegistration(): Boolean {
        return prefs.getBoolean("has_seen_onboarding_reg", false)
    }

    fun markOnboardingRegistrationSeen() {
        prefs.edit().putBoolean("has_seen_onboarding_reg", true).apply()
    }

    private fun loadInitialUserProfile(): UserProfileState {
        val isRegistered = prefs.getBoolean("is_registered_donor", false)
        val name = prefs.getString("user_name", if (isRegistered) "Alex Donovan" else "") ?: ""
        val age = prefs.getInt("user_age", 26)
        val gender = prefs.getString("user_gender", "Male") ?: "Male"
        val bloodGroup = prefs.getString("user_blood_group", "O+") ?: "O+"
        val hasAnemia = prefs.getBoolean("user_has_anemia", false)
        val phone = prefs.getString("user_phone", "9876543210") ?: "9876543210"
        val location = prefs.getString("user_location", "Aundh, Pune") ?: "Aundh, Pune"
        val donorId = prefs.getString("donor_id", if (isRegistered) "BD-74892" else "") ?: ""
        val regDate = prefs.getString("registration_date", if (isRegistered) "Registered" else "") ?: ""
        val isAvailable = prefs.getBoolean("user_available", true)
        val totalDonations = prefs.getInt("total_donations", if (isRegistered) 1 else 0)
        val livesSaved = prefs.getInt("lives_saved", if (isRegistered) 3 else 0)

        return UserProfileState(
            name = name,
            age = age,
            gender = gender,
            bloodGroup = bloodGroup,
            hasAnemia = hasAnemia,
            role = if (isRegistered) "Verified Donor" else "Community Member",
            phone = phone,
            location = location,
            isAvailable = isAvailable,
            totalDonations = totalDonations,
            livesSaved = livesSaved,
            lastDonated = if (isRegistered) "Recent" else "Never",
            nextEligible = "Eligible Now",
            pendingRequestsCount = 2,
            isRegisteredDonor = isRegistered,
            donorId = donorId,
            registrationDate = regDate
        )
    }

    private val _userProfile = MutableStateFlow(loadInitialUserProfile())
    val userProfile: StateFlow<UserProfileState> = _userProfile.asStateFlow()

    fun setBloodGroupFilter(group: String) {
        _selectedBloodGroupFilter.value = group
    }

    fun setLocationFilter(location: String) {
        _selectedLocationFilter.value = location
    }

    fun setFacilityTab(tab: String) {
        _selectedFacilityTab.value = tab
    }

    fun setFacilitySearchQuery(query: String) {
        _facilitySearchQuery.value = query
    }

    fun toggleUserAvailability(isAvailable: Boolean) {
        _userProfile.value = _userProfile.value.copy(isAvailable = isAvailable)
        prefs.edit().putBoolean("user_available", isAvailable).apply()
    }

    fun updateUserProfile(
        name: String,
        group: String,
        phone: String,
        location: String
    ) {
        updateUserProfile(
            name = name,
            age = _userProfile.value.age,
            gender = _userProfile.value.gender,
            bloodGroup = group,
            hasAnemia = _userProfile.value.hasAnemia,
            phone = phone,
            location = location
        )
    }

    fun updateUserProfile(
        name: String,
        age: Int = 26,
        gender: String = "Male",
        bloodGroup: String = "O+",
        hasAnemia: Boolean = false,
        phone: String = "",
        location: String = ""
    ) {
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        val trimmedLoc = location.trim()

        prefs.edit()
            .putString("user_name", trimmedName)
            .putInt("user_age", age)
            .putString("user_gender", gender)
            .putString("user_blood_group", bloodGroup)
            .putBoolean("user_has_anemia", hasAnemia)
            .putString("user_phone", trimmedPhone)
            .putString("user_location", trimmedLoc)
            .commit()

        _userProfile.value = _userProfile.value.copy(
            name = trimmedName,
            age = age,
            gender = gender,
            bloodGroup = bloodGroup,
            hasAnemia = hasAnemia,
            phone = trimmedPhone,
            location = trimmedLoc
        )
    }

    fun submitEmergencyRequest(
        patientName: String,
        bloodGroup: String,
        unitsRequired: Int,
        hospitalName: String,
        location: String,
        urgency: String,
        contactNumber: String,
        notes: String = "",
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val request = EmergencyRequestEntity(
                patientName = patientName,
                bloodGroup = bloodGroup,
                unitsRequired = unitsRequired,
                hospitalName = hospitalName,
                location = location,
                urgency = urgency,
                contactNumber = contactNumber,
                additionalNotes = notes,
                timestamp = System.currentTimeMillis(),
                status = "Urgent"
            )
            val newId = repository.insertEmergencyRequest(request)
            _selectedTrackedRequestId.value = newId
            _liveUpdateTicker.value = "Emergency broadcast sent for $patientName ($bloodGroup) at $hospitalName"
            onSuccess(newId)
        }
    }

    /**
     * Registers user as a blood donor.
     * Enforces one-time registration: if already registered, it returns false and prevents duplicate entry.
     * Persists: Name, Age, Gender, Blood Group, and Anemia Status (Yes/No).
     */
    fun registerAsDonor(
        name: String,
        age: Int = 26,
        gender: String = "Male",
        bloodGroup: String = "O+",
        hasAnemia: Boolean = false,
        phone: String = "",
        area: String = "Aundh",
        city: String = "Pune",
        onResult: (Boolean) -> Unit = {}
    ) {
        // Enforce: registration should occur once after being registered
        if (_userProfile.value.isRegisteredDonor) {
            onResult(false)
            return
        }

        val generatedDonorId = "BD-${(10000..99999).random()}"
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        val locationStr = if (area.isNotBlank() && city.isNotBlank()) "$area, $city".trim() else "Aundh, Pune"

        prefs.edit()
            .putBoolean("is_registered_donor", true)
            .putString("user_name", trimmedName)
            .putInt("user_age", age)
            .putString("user_gender", gender)
            .putString("user_blood_group", bloodGroup)
            .putBoolean("user_has_anemia", hasAnemia)
            .putString("user_phone", trimmedPhone)
            .putString("user_location", locationStr)
            .putString("donor_id", generatedDonorId)
            .putString("registration_date", "Today")
            .putInt("total_donations", 1)
            .putInt("lives_saved", 3)
            .putBoolean("has_seen_onboarding_reg", true)
            .commit()

        _userProfile.value = _userProfile.value.copy(
            name = trimmedName,
            age = age,
            gender = gender,
            bloodGroup = bloodGroup,
            hasAnemia = hasAnemia,
            phone = trimmedPhone,
            location = locationStr,
            role = "Verified Donor",
            isAvailable = true,
            isRegisteredDonor = true,
            donorId = generatedDonorId,
            registrationDate = "Today",
            totalDonations = 1,
            livesSaved = 3
        )

        viewModelScope.launch(Dispatchers.IO) {
            val newDonor = DonorEntity(
                name = trimmedName,
                bloodGroup = bloodGroup,
                distanceKm = 0.5,
                area = area,
                city = city,
                phone = trimmedPhone,
                isAvailable = true,
                totalDonations = 1,
                lastDonationDate = "Registered today",
                latOffset = 0.002f,
                lngOffset = 0.002f
            )
            repository.insertDonor(newDonor)
        }

        onResult(true)
    }

    /**
     * Resets donor registration status for testing / demo verification.
     */
    fun resetRegistrationForTesting() {
        prefs.edit().clear().commit()
        _userProfile.value = UserProfileState(
            name = "Alex Donovan",
            age = 26,
            gender = "Male",
            bloodGroup = "O+",
            hasAnemia = false,
            role = "Community Member",
            phone = "",
            location = "Pune",
            isAvailable = true,
            totalDonations = 0,
            livesSaved = 0,
            lastDonated = "Never",
            nextEligible = "Eligible Now",
            pendingRequestsCount = 2,
            isRegisteredDonor = false,
            donorId = "",
            registrationDate = ""
        )
    }

    fun startLiveGpsTracking() {
        liveLocationManager.startLiveTracking()
    }

    fun stopLiveGpsTracking() {
        liveLocationManager.stopLiveTracking()
    }

    fun openLocationSettings() {
        liveLocationManager.openLocationSettings()
    }

    fun refreshGpsLocation() {
        liveLocationManager.startLiveTracking()
    }

    fun hasLocationPermission(): Boolean {
        return liveLocationManager.hasLocationPermission()
    }

    override fun onCleared() {
        super.onCleared()
        liveLocationManager.stopLiveTracking()
    }
}
