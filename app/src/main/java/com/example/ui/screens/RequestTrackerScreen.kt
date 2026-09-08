package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmergencyRequestEntity
import com.example.location.LiveGpsLocation
import com.example.location.LiveLocationManager
import com.example.ui.components.dialPhoneNumber
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonPrimaryDark
import com.example.ui.theme.HighDensityGray100
import com.example.ui.theme.HighDensityGray400
import com.example.ui.theme.HighDensityGray600
import com.example.ui.theme.HighDensityGreen
import com.example.ui.theme.HighDensityRed100
import com.example.ui.theme.HighDensityRed50
import com.example.ui.theme.HighDensityRed700
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSubtle
import com.example.util.MapsIntegration
import com.example.viewmodel.TrackerCustomization

enum class TrackingStage(
    val step: Int,
    val title: String,
    val description: String,
    val statusKey: String
) {
    BROADCASTED(1, "Request Broadcasted", "Emergency broadcast sent to hospitals & local donor network", "Urgent"),
    DONORS_ALERTED(2, "Donors Alerted", "Matching donors within 5 km notified via push notifications", "Donors Notified"),
    DONOR_ACCEPTED(3, "Donor Accepted & En Route", "Verified donor matched and traveling to hospital", "Donor Accepted"),
    HOSPITAL_SCREENING(4, "Blood Screening & Cross-Matching", "Donor arrived at blood bank; sample testing in progress", "In Screening"),
    FULFILLED(5, "Delivered & Fulfilled", "Blood units safely delivered and received by medical team", "Fulfilled")
}

fun mapStatusToStage(status: String): TrackingStage {
    return when (status.trim().lowercase()) {
        "donors notified", "notified" -> TrackingStage.DONORS_ALERTED
        "donor accepted", "accepted", "en route", "in transit" -> TrackingStage.DONOR_ACCEPTED
        "in screening", "screening", "cross-matching", "sample verification" -> TrackingStage.HOSPITAL_SCREENING
        "fulfilled", "completed", "delivered" -> TrackingStage.FULFILLED
        else -> TrackingStage.BROADCASTED
    }
}

fun shareRequestTracking(context: Context, request: EmergencyRequestEntity) {
    val trackingText = """
        🚨 Donor Connect - Emergency Blood Request Tracking
        Tracking ID: #REQ-100${request.id}
        Patient: ${request.patientName}
        Blood Group: ${request.bloodGroup} (${request.unitsRequired} Units required)
        Hospital: ${request.hospitalName} (${request.location})
        Urgency: ${request.urgency.uppercase()}
        Current Status: ${request.status}
        
        Contact Coordinator: ${request.contactNumber}
        Track real-time status in the Donor Connect app.
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, trackingText)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Request Tracking Info")
    context.startActivity(shareIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestTrackerScreen(
    requests: List<EmergencyRequestEntity>,
    selectedRequestId: Long? = null,
    currentLocation: LiveGpsLocation? = null,
    trackerCustomizations: Map<Long, TrackerCustomization> = emptyMap(),
    onSelectRequest: (Long) -> Unit = {},
    onAdvanceStage: (Long, String) -> Unit = { _, _ -> },
    onMarkFulfilled: (Long) -> Unit = {},
    onCancelRequest: (Long) -> Unit = {},
    onSaveCustomization: (Long, TrackerCustomization) -> Unit = { _, _ -> },
    onUpdateRequest: (EmergencyRequestEntity) -> Unit = {},
    onNavigateToEmergencyRequest: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var showCustomizeDialog by remember { mutableStateOf(false) }

    // Determine the currently active request to track
    val activeRequest = remember(requests, selectedRequestId) {
        if (selectedRequestId != null) {
            requests.firstOrNull { it.id == selectedRequestId } ?: requests.firstOrNull()
        } else {
            requests.firstOrNull()
        }
    }

    val currentCustomization = remember(activeRequest?.id, trackerCustomizations) {
        if (activeRequest != null) {
            trackerCustomizations[activeRequest.id] ?: TrackerCustomization()
        } else {
            TrackerCustomization()
        }
    }

    val currentStage = remember(activeRequest?.status) {
        if (activeRequest != null) mapStatusToStage(activeRequest.status) else TrackingStage.BROADCASTED
    }

    // Glowing animation for active live pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Filter requests for switcher
    val filteredRequests = remember(requests, searchQuery) {
        if (searchQuery.isBlank()) {
            requests
        } else {
            requests.filter {
                it.patientName.contains(searchQuery, ignoreCase = true) ||
                        it.bloodGroup.contains(searchQuery, ignoreCase = true) ||
                        it.hospitalName.contains(searchQuery, ignoreCase = true) ||
                        "#REQ-100${it.id}".contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Cancel Request Confirmation Dialog
    if (showCancelConfirmDialog && activeRequest != null) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = {
                Text(
                    text = "Cancel Blood Request?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextCharcoal
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel the emergency request for ${activeRequest.patientName} (${activeRequest.bloodGroup})? This will notify active donors.",
                    fontSize = 14.sp,
                    color = TextSubtle
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelRequest(activeRequest.id)
                        showCancelConfirmDialog = false
                        Toast.makeText(context, "Request #REQ-100${activeRequest.id} cancelled", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Yes, Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("Keep Active", color = TextSubtle)
                }
            }
        )
    }

    // Customize Tracker Preferences Dialog
    if (showCustomizeDialog && activeRequest != null) {
        CustomizeTrackerDialog(
            initialCustomization = currentCustomization,
            currentUnits = activeRequest.unitsRequired,
            currentUrgency = activeRequest.urgency,
            currentNotes = activeRequest.additionalNotes,
            patientName = activeRequest.patientName,
            bloodGroup = activeRequest.bloodGroup,
            hospitalName = activeRequest.hospitalName,
            onDismiss = { showCustomizeDialog = false },
            onApply = { updatedCustomization, newUnits, newUrgency, newNotes ->
                onSaveCustomization(activeRequest.id, updatedCustomization)
                val updatedReq = activeRequest.copy(
                    unitsRequired = newUnits,
                    urgency = newUrgency,
                    additionalNotes = newNotes
                )
                onUpdateRequest(updatedReq)
                showCustomizeDialog = false
                Toast.makeText(
                    context,
                    "Tracker customized: ${updatedCustomization.transportMode} • ${updatedCustomization.alertRadiusKm} km radius • ETA ${updatedCustomization.customEtaMinutes}m",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("request_tracker_screen")
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Request Tracker",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                    Text(
                        text = "Live Dispatch & Donor Coordination",
                        fontSize = 11.sp,
                        color = TextSubtle
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("tracker_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextCharcoal
                    )
                }
            },
            actions = {
                if (activeRequest != null) {
                    IconButton(
                        onClick = { showCustomizeDialog = true },
                        modifier = Modifier.testTag("tracker_customize_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize Tracker",
                            tint = CrimsonPrimary
                        )
                    }
                    IconButton(
                        onClick = { shareRequestTracking(context, activeRequest) },
                        modifier = Modifier.testTag("tracker_share_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Tracking Link",
                            tint = CrimsonPrimary
                        )
                    }
                }
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Tracker synced with hospital emergency network", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("tracker_refresh_action")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = CrimsonPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search & Multi-Request Switcher
            item {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by patient, blood group, or #ID") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = CrimsonPrimary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = HighDensityGray600
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = BorderLight
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tracker_search_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal List of Tracked Requests
                    if (filteredRequests.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tracked Blood Requests (${filteredRequests.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSubtle
                            )
                            Text(
                                text = "Tap to switch",
                                fontSize = 11.sp,
                                color = CrimsonPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("tracker_request_chips_row")
                        ) {
                            items(filteredRequests) { req ->
                                val isSelected = activeRequest?.id == req.id
                                val isFulfilled = req.status.equals("fulfilled", ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) CrimsonContainer else Color.White,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) CrimsonPrimary else HighDensityGray100
                                    ),
                                    modifier = Modifier
                                        .clickable { onSelectRequest(req.id) }
                                        .testTag("tracker_chip_${req.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (isFulfilled) HighDensityGreen else CrimsonPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = req.bloodGroup,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = req.patientName,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) CrimsonPrimaryDark else TextCharcoal
                                            )
                                            Text(
                                                text = "#REQ-100${req.id} • ${req.status}",
                                                fontSize = 10.sp,
                                                color = if (isFulfilled) Color(0xFF2E7D32) else TextSubtle
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // If No Active Requests Exist
            if (activeRequest == null) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityRed50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Active Requests Found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextCharcoal
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "There are currently no blood requests being tracked. You can broadcast a new emergency request to notify nearby donors.",
                                fontSize = 13.sp,
                                color = TextSubtle,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToEmergencyRequest,
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("tracker_create_request_cta")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Emergency Request", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Customize Tracker Preferences Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clickable { showCustomizeDialog = true }
                            .testTag("tracker_customize_banner_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Customize Tracker",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextCharcoal
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = CrimsonPrimary
                                    ) {
                                        Text(
                                            text = currentCustomization.transportMode.uppercase(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Radius: ${currentCustomization.alertRadiusKm} km • ETA: ${currentCustomization.customEtaMinutes}m • ${currentCustomization.assignedResponderName}",
                                    fontSize = 11.sp,
                                    color = TextSubtle
                                )
                            }
                            OutlinedButton(
                                onClick = { showCustomizeDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CrimsonPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("edit_tracker_settings_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPrimary
                                )
                            }
                        }
                    }
                }

                // Active Request Details Hero Card
                item {
                    val isFulfilled = activeRequest.status.equals("fulfilled", ignoreCase = true)
                    val isNewlyTracked = selectedRequestId != null && activeRequest.id == selectedRequestId
                    if (isNewlyTracked) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Now tracking Request for ${activeRequest.patientName} (${activeRequest.bloodGroup})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                if (isFulfilled) HighDensityGreen.copy(alpha = 0.5f) else CrimsonPrimary.copy(alpha = 0.3f),
                                RoundedCornerShape(20.dp)
                            )
                            .testTag("active_tracked_request_hero")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            // Top Row: ID and Live Status Banner
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isFulfilled) HighDensityGreen else CrimsonPrimary.copy(alpha = pulseAlpha),
                                        modifier = Modifier.size(10.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "LIVE TRACKING: #REQ-100${activeRequest.id}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isFulfilled) Color(0xFF2E7D32) else CrimsonPrimary,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Surface(
                                    color = if (isFulfilled) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = activeRequest.urgency.uppercase() + " PRIORITY",
                                        color = if (isFulfilled) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Patient & Blood Details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(HighDensityRed50)
                                        .border(2.dp, HighDensityRed100, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = activeRequest.bloodGroup,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CrimsonPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeRequest.patientName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextCharcoal
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = HighDensityRed50,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "${activeRequest.unitsRequired} Units Required",
                                                color = CrimsonPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Status: ${activeRequest.status}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isFulfilled) Color(0xFF2E7D32) else HighDensityGray600
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Hospital & Destination Info Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8F9FA),
                                border = BorderStroke(1.dp, Color(0xFFE9ECEF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = CrimsonPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = activeRequest.hospitalName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextCharcoal
                                        )
                                        Text(
                                            text = activeRequest.location,
                                            fontSize = 11.sp,
                                            color = TextSubtle
                                        )
                                    }

                                    IconButton(
                                        onClick = { dialPhoneNumber(context, activeRequest.contactNumber) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE0F2FE))
                                            .testTag("tracker_call_hospital_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Call Contact",
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            if (currentCustomization.hospitalWardRoom.isNotBlank() || activeRequest.additionalNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFFF7ED),
                                    border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Emergency,
                                            contentDescription = null,
                                            tint = Color(0xFFEA580C),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            if (currentCustomization.hospitalWardRoom.isNotBlank()) {
                                                Text(
                                                    text = "Ward / Room: ${currentCustomization.hospitalWardRoom}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF9A3412)
                                                )
                                            }
                                            if (activeRequest.additionalNotes.isNotBlank()) {
                                                Text(
                                                    text = activeRequest.additionalNotes,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFC2410C)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Progress Bar & Stage Indicator
                item {
                    val progressValue = when (currentStage) {
                        TrackingStage.BROADCASTED -> 0.2f
                        TrackingStage.DONORS_ALERTED -> 0.45f
                        TrackingStage.DONOR_ACCEPTED -> 0.7f
                        TrackingStage.HOSPITAL_SCREENING -> 0.9f
                        TrackingStage.FULFILLED -> 1.0f
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Dispatch & Transit Progress",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextCharcoal
                                )
                                Text(
                                    text = "Stage ${currentStage.step} of 5",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CrimsonPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progressValue },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (currentStage == TrackingStage.FULFILLED) HighDensityGreen else CrimsonPrimary,
                                trackColor = Color(0xFFEEEEEE),
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Step by Step Vertical Stepper
                            TrackingStage.entries.forEach { stage ->
                                val isCompleted = stage.step < currentStage.step || currentStage == TrackingStage.FULFILLED
                                val isCurrent = stage == currentStage && currentStage != TrackingStage.FULFILLED
                                val isPending = stage.step > currentStage.step && currentStage != TrackingStage.FULFILLED

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Step Indicator Icon
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCompleted -> Color(0xFFE8F5E9)
                                                    isCurrent -> HighDensityRed50
                                                    else -> Color(0xFFF1F3F5)
                                                }
                                            )
                                            .border(
                                                1.5.dp,
                                                when {
                                                    isCompleted -> HighDensityGreen
                                                    isCurrent -> CrimsonPrimary
                                                    else -> Color(0xFFCED4DA)
                                                },
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Done,
                                                contentDescription = null,
                                                tint = HighDensityGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${stage.step}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) CrimsonPrimary else HighDensityGray400
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stage.title,
                                                fontSize = 13.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isCurrent) CrimsonPrimaryDark else if (isCompleted) TextCharcoal else HighDensityGray600
                                            )

                                            if (isCurrent) {
                                                Surface(
                                                    color = HighDensityRed50,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "IN PROGRESS",
                                                        color = CrimsonPrimary,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stage.description,
                                            fontSize = 11.sp,
                                            color = if (isCurrent) TextCharcoal else TextSubtle,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Responding Donor / Live Coordination Card
                item {
                    val donorName = currentCustomization.assignedResponderName.ifBlank {
                        if (activeRequest.bloodGroup.contains("O")) "Vikram Malhotra" else "Rohan Deshpande"
                    }
                    val responderPhone = currentCustomization.responderContact.ifBlank { "9823011223" }
                    val donorDist = if (currentLocation != null) {
                        "${currentCustomization.alertRadiusKm} km radar • ETA ~${currentCustomization.customEtaMinutes} mins"
                    } else {
                        "${currentCustomization.alertRadiusKm} km radar • ETA ~${currentCustomization.customEtaMinutes} mins"
                    }

                    val transportIcon = when (currentCustomization.transportMode) {
                        "Emergency Ambulance" -> Icons.Default.AirportShuttle
                        "Personal Car" -> Icons.Default.DirectionsCar
                        else -> Icons.Default.DirectionsBike
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = transportIcon,
                                        contentDescription = null,
                                        tint = CrimsonPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Assigned Transit & Donor",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextCharcoal
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        text = currentCustomization.transportMode.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(HighDensityRed50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = CrimsonPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = donorName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextCharcoal
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = CrimsonContainer
                                        ) {
                                            Text(
                                                text = "${currentCustomization.updateFrequencySec}s GPS",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CrimsonPrimary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${currentCustomization.responderRole} • $donorDist",
                                        fontSize = 11.sp,
                                        color = TextSubtle
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Google Maps Navigation & Directions Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // Open real Google Maps directions to hospital
                                        MapsIntegration.openDirectionsInGoogleMaps(
                                            context = context,
                                            destLat = 18.5204,
                                            destLng = 73.8567,
                                            label = activeRequest.hospitalName
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("tracker_open_google_maps_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Navigate GPS",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                OutlinedButton(
                                    onClick = { dialPhoneNumber(context, responderPhone) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CrimsonPrimary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tracker_call_donor_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = null,
                                        tint = CrimsonPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Call Responder",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Coordinator & Lifecycle Management Action Controls
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "Tracker Live Actions",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextCharcoal
                            )
                            Text(
                                text = "Advance stages or mark request fulfilled in real-time database",
                                fontSize = 11.sp,
                                color = TextSubtle
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action 0: Customize Tracker Preferences
                            OutlinedButton(
                                onClick = { showCustomizeDialog = true },
                                border = BorderStroke(1.dp, CrimsonPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("tracker_customize_settings_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Customize Tracker Preferences",
                                    color = CrimsonPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action 1: Advance Stage Button
                            if (currentStage != TrackingStage.FULFILLED) {
                                Button(
                                    onClick = {
                                        onAdvanceStage(activeRequest.id, activeRequest.status)
                                        Toast.makeText(
                                            context,
                                            "Request updated to next stage",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("tracker_advance_stage_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Advance to Next Stage",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Action 2: Mark Fulfilled Button
                            if (currentStage != TrackingStage.FULFILLED) {
                                Button(
                                    onClick = {
                                        onMarkFulfilled(activeRequest.id)
                                        Toast.makeText(
                                            context,
                                            "Request marked as Fulfilled! Thank you for saving lives.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("tracker_mark_fulfilled_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mark as Fulfilled",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Action 3: Cancel Request Button
                            OutlinedButton(
                                onClick = { showCancelConfirmDialog = true },
                                border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("tracker_cancel_request_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cancel Request",
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeTrackerDialog(
    initialCustomization: TrackerCustomization,
    currentUnits: Int,
    currentUrgency: String,
    currentNotes: String,
    patientName: String,
    bloodGroup: String,
    hospitalName: String,
    onDismiss: () -> Unit,
    onApply: (customization: TrackerCustomization, units: Int, urgency: String, notes: String) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Transit & Responder state
    var transportMode by remember { mutableStateOf(initialCustomization.transportMode) }
    var responderName by remember { mutableStateOf(initialCustomization.assignedResponderName) }
    var responderContact by remember { mutableStateOf(initialCustomization.responderContact) }
    var customEta by remember { mutableStateOf(initialCustomization.customEtaMinutes) }

    // Hospital & Emergency State
    var unitsRequired by remember { mutableStateOf(currentUnits) }
    var urgencyLevel by remember { mutableStateOf(currentUrgency) }
    var hospitalWard by remember { mutableStateOf(initialCustomization.hospitalWardRoom) }
    var emergencyNotes by remember { mutableStateOf(currentNotes) }

    // Radar & Tracking Preferences State
    var alertRadiusKm by remember { mutableStateOf(initialCustomization.alertRadiusKm) }
    var updateFrequencySec by remember { mutableStateOf(initialCustomization.updateFrequencySec) }
    var trackingMode by remember { mutableStateOf(initialCustomization.trackingMode) }
    var sirenSoundAlerts by remember { mutableStateOf(initialCustomization.sirenSoundAlerts) }
    var smsAttendantAlerts by remember { mutableStateOf(initialCustomization.smsAttendantAlerts) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CrimsonContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Customize Tracker",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Text(
                            text = "$patientName • $bloodGroup at $hospitalName",
                            fontSize = 11.sp,
                            color = TextSubtle
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSubtle)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Tab Selection: Transit | Emergency | Radar & Alerts
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color(0xFFF8F9FA),
                    contentColor = CrimsonPrimary
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Transit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Emergency", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("Radar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTabIndex) {
                    0 -> {
                        // Courier & Transit Customization
                        Text(
                            text = "Transport Mode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Bike Courier", "Emergency Ambulance", "Personal Car").forEach { mode ->
                                val isSelected = transportMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { transportMode = mode },
                                    label = {
                                        Text(
                                            text = when (mode) {
                                                "Bike Courier" -> "🏍️ Bike"
                                                "Emergency Ambulance" -> "🚑 Ambulance"
                                                else -> "🚗 Car"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CrimsonContainer,
                                        selectedLabelColor = CrimsonPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = responderName,
                            onValueChange = { responderName = it },
                            label = { Text("Assigned Courier / Responder Name", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_responder_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                focusedLabelColor = CrimsonPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = responderContact,
                            onValueChange = { responderContact = it },
                            label = { Text("Responder Mobile Number", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_responder_contact_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                focusedLabelColor = CrimsonPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimated Time of Arrival (ETA)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextCharcoal
                            )
                            Surface(
                                color = CrimsonContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$customEta mins",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CrimsonPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Slider(
                            value = customEta.toFloat(),
                            onValueChange = { customEta = it.toInt() },
                            valueRange = 5f..45f,
                            steps = 7,
                            colors = SliderDefaults.colors(
                                thumbColor = CrimsonPrimary,
                                activeTrackColor = CrimsonPrimary
                            )
                        )
                    }

                    1 -> {
                        // Emergency & Ward Customization
                        Text(
                            text = "Units of Blood Required",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            IconButton(
                                onClick = { if (unitsRequired > 1) unitsRequired-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEEEEE))
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                            }
                            Text(
                                text = "$unitsRequired Units",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CrimsonPrimary
                            )
                            IconButton(
                                onClick = { if (unitsRequired < 10) unitsRequired++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonContainer)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = CrimsonPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Urgency Priority",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Critical", "Urgent", "High", "Medium").forEach { urg ->
                                val isSelected = urgencyLevel.equals(urg, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { urgencyLevel = urg },
                                    label = { Text(urg, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (urg == "Critical") Color(0xFFFFEBEE) else CrimsonContainer,
                                        selectedLabelColor = if (urg == "Critical") Color(0xFFC62828) else CrimsonPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = hospitalWard,
                            onValueChange = { hospitalWard = it },
                            label = { Text("Hospital Ward / Bed / Room", fontSize = 12.sp) },
                            placeholder = { Text("e.g. ICU Wing - Bed #4, 2nd Floor") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_ward_room_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                focusedLabelColor = CrimsonPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = emergencyNotes,
                            onValueChange = { emergencyNotes = it },
                            label = { Text("Special Attendant Instructions", fontSize = 12.sp) },
                            placeholder = { Text("e.g. Report to Blood Bank counter directly") },
                            maxLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_emergency_notes_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                focusedLabelColor = CrimsonPrimary
                            )
                        )
                    }

                    2 -> {
                        // Radar & Alerts Preferences
                        Text(
                            text = "Donor Broadcast Radar Radius",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(2, 5, 10, 20).forEach { radius ->
                                val isSelected = alertRadiusKm == radius
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { alertRadiusKm = radius },
                                    label = {
                                        Text(
                                            text = "$radius km",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CrimsonContainer,
                                        selectedLabelColor = CrimsonPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Live GPS Refresh Frequency",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5 to "Rapid (5s)", 15 to "Standard (15s)", 30 to "Eco (30s)").forEach { (sec, label) ->
                                val isSelected = updateFrequencySec == sec
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { updateFrequencySec = sec },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CrimsonContainer,
                                        selectedLabelColor = CrimsonPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (sirenSoundAlerts) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = null,
                                    tint = if (sirenSoundAlerts) CrimsonPrimary else TextSubtle,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Emergency Siren Audio Alert", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextCharcoal)
                                    Text("Chime when transit stage advances", fontSize = 10.sp, color = TextSubtle)
                                }
                            }
                            Switch(
                                checked = sirenSoundAlerts,
                                onCheckedChange = { sirenSoundAlerts = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (smsAttendantAlerts) CrimsonPrimary else TextSubtle,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("SMS Dispatch Notifications", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextCharcoal)
                                    Text("Send milestone SMS to attendants", fontSize = 10.sp, color = TextSubtle)
                                }
                            }
                            Switch(
                                checked = smsAttendantAlerts,
                                onCheckedChange = { smsAttendantAlerts = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonPrimary)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedCustomization = initialCustomization.copy(
                        transportMode = transportMode,
                        assignedResponderName = responderName.trim().ifBlank { "Vikram Malhotra" },
                        responderContact = responderContact.trim().ifBlank { "9823011223" },
                        customEtaMinutes = customEta,
                        hospitalWardRoom = hospitalWard.trim(),
                        alertRadiusKm = alertRadiusKm,
                        updateFrequencySec = updateFrequencySec,
                        sirenSoundAlerts = sirenSoundAlerts,
                        smsAttendantAlerts = smsAttendantAlerts,
                        responderRole = when (transportMode) {
                            "Emergency Ambulance" -> "Critical Care Ambulance Driver"
                            "Personal Car" -> "Volunteer Blood Courier"
                            else -> "Motorcycle Blood Courier"
                        }
                    )
                    onApply(updatedCustomization, unitsRequired, urgencyLevel, emergencyNotes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("apply_tracker_customization_btn")
            ) {
                Icon(imageVector = Icons.Default.Done, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save & Apply", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSubtle)
            }
        }
    )
}

