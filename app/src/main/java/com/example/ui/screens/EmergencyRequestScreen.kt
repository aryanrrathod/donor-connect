package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import com.example.util.MapUtils
import com.example.location.LiveGpsLocation
import com.example.ui.components.EmergencyCalloutCard
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonPrimaryDark
import com.example.ui.theme.HighDensityGreen
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSubtle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyRequestScreen(
    currentLocation: LiveGpsLocation? = null,
    onRequestGpsPermission: () -> Unit = {},
    onRefreshGps: () -> Unit = {},
    onBackClick: () -> Unit,
    onTrackRequestClick: (Long?) -> Unit = {},
    onSubmitRequest: (
        patientName: String,
        bloodGroup: String,
        units: Int,
        hospital: String,
        location: String,
        urgency: String,
        contact: String,
        onSuccess: (Long) -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var patientName by remember { mutableStateOf("Raj Kumar") }
    var selectedGroup by remember { mutableStateOf("O+") }
    var groupDropdownExpanded by remember { mutableStateOf(false) }
    var unitsRequired by remember { mutableIntStateOf(2) }
    var hospitalName by remember { mutableStateOf("Ruby Hall Clinic") }
    var location by remember { mutableStateOf("Pune, Maharashtra") }
    var urgencyLevel by remember { mutableStateOf("High") }
    var urgencyDropdownExpanded by remember { mutableStateOf(false) }
    var contactNumber by remember { mutableStateOf("9876543210") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var createdRequestId by remember { mutableStateOf<Long?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val bloodGroups = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")
    val urgencyLevels = listOf("High", "Medium", "Low")

    val patientPresets = remember {
        listOf(
            PatientPreset("1", "Raj Kumar", "O+", "Ruby Hall Clinic", "Pune, Maharashtra", "High", "9823011223"),
            PatientPreset("2", "Priya Deshmukh", "B+", "KEM Hospital", "Pune, Maharashtra", "High", "9823044556"),
            PatientPreset("3", "Sunil Kulkarni", "A-", "Jehangir Hospital", "Pune, Maharashtra", "Medium", "9823077889"),
            PatientPreset("4", "Meera Nair", "AB+", "Sahyadri Hospital", "Pune, Maharashtra", "High", "9823099001")
        )
    }
    var selectedPatientId by remember { mutableStateOf<String?>("1") }

    if (showSuccessDialog) {
        // Automatically route user to Live Request Tracker after placing request
        LaunchedEffect(showSuccessDialog, createdRequestId) {
            delay(1400)
            if (showSuccessDialog) {
                showSuccessDialog = false
                onTrackRequestClick(createdRequestId)
            }
        }

        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onTrackRequestClick(createdRequestId)
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Request Placed & Live!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextCharcoal,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Emergency request for $unitsRequired Units of $selectedGroup blood at $hospitalName is now broadcasted to nearby matching donors.",
                        fontSize = 14.sp,
                        color = TextSubtle,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF2563EB)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Redirecting to Live Tracker...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onTrackRequestClick(createdRequestId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_track_request_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Track Live Request", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onTrackRequestClick(createdRequestId)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Tracker Now", color = TextSubtle, fontSize = 13.sp)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("emergency_request_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Emergency Request",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextCharcoal
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick, modifier = Modifier.testTag("emergency_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextCharcoal
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = { onTrackRequestClick(null) },
                    modifier = Modifier.testTag("top_bar_track_requests_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tracker",
                        color = CrimsonPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
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
            // Urgent Alert Card
            item {
                EmergencyCalloutCard()
            }

            // Form Fields Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Select Patient from List Section
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Select Patient from List",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle
                                )
                                Text(
                                    text = "Select or Enter New",
                                    fontSize = 11.sp,
                                    color = CrimsonPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("patient_selection_list")
                            ) {
                                items(patientPresets) { preset ->
                                    val isSelected = selectedPatientId == preset.id
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) CrimsonContainer else Color(0xFFF8F9FA),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) CrimsonPrimary else Color(0xFFE9ECEF)
                                        ),
                                        modifier = Modifier
                                            .clickable {
                                                selectedPatientId = preset.id
                                                patientName = preset.name
                                                selectedGroup = preset.bloodGroup
                                                hospitalName = preset.hospital
                                                location = preset.location
                                                urgencyLevel = preset.urgency
                                                contactNumber = preset.contact
                                            }
                                            .testTag("patient_chip_${preset.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) CrimsonPrimary else Color(0xFFE2E8F0)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = preset.bloodGroup,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else TextCharcoal
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = preset.name,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) CrimsonPrimaryDark else TextCharcoal
                                                )
                                                Text(
                                                    text = preset.hospital,
                                                    fontSize = 10.sp,
                                                    color = TextSubtle
                                                )
                                            }
                                        }
                                    }
                                }

                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedPatientId == null) CrimsonContainer else Color(0xFFF8F9FA),
                                        border = BorderStroke(
                                            1.dp,
                                            if (selectedPatientId == null) CrimsonPrimary else Color(0xFFE9ECEF)
                                        ),
                                        modifier = Modifier
                                            .clickable {
                                                selectedPatientId = null
                                                patientName = ""
                                            }
                                            .testTag("patient_chip_custom")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "New Patient",
                                                tint = CrimsonPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Custom Patient",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = CrimsonPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Patient Name
                        Column {
                            Text(
                                text = "Patient Name",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = patientName,
                                onValueChange = {
                                    patientName = it
                                    selectedPatientId = null
                                },
                                placeholder = { Text("e.g. Raj Kumar") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = CrimsonPrimary
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimary,
                                    unfocusedBorderColor = BorderLight
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_patient_name")
                            )
                        }

                        // Blood Group Selection
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Blood Group",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle
                                )
                                Text(
                                    text = "Selected: $selectedGroup",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive Blood Group Selection Chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("blood_group_chips_row")
                            ) {
                                items(bloodGroups) { group ->
                                    val isSelected = group == selectedGroup
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedGroup = group },
                                        label = {
                                            Text(
                                                text = group,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CrimsonPrimary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFFF1F3F5),
                                            labelColor = TextCharcoal
                                        ),
                                        modifier = Modifier.testTag("chip_group_$group")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Dropdown Picker with reliable clickable overlay
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = "Blood Group: $selectedGroup",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { groupDropdownExpanded = true }) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Select Blood Group",
                                                tint = CrimsonPrimary
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CrimsonPrimary,
                                        unfocusedBorderColor = BorderLight
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                // Transparent overlay ensures click on the field opens the menu
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { groupDropdownExpanded = true }
                                        .testTag("select_blood_group_dropdown")
                                )
                                DropdownMenu(
                                    expanded = groupDropdownExpanded,
                                    onDismissRequest = { groupDropdownExpanded = false }
                                ) {
                                    bloodGroups.forEach { group ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = group,
                                                    fontWeight = if (group == selectedGroup) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (group == selectedGroup) CrimsonPrimary else TextCharcoal
                                                )
                                            },
                                            onClick = {
                                                selectedGroup = group
                                                groupDropdownExpanded = false
                                            },
                                            modifier = Modifier.testTag("dropdown_item_group_$group")
                                        )
                                    }
                                }
                            }
                        }

                        // Required Units Stepper
                        Column {
                            Text(
                                text = "Required Units",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$unitsRequired ${if (unitsRequired > 1) "Units" else "Unit"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextCharcoal
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (unitsRequired > 1) unitsRequired-- },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceLight)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            tint = TextCharcoal
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    IconButton(
                                        onClick = { if (unitsRequired < 10) unitsRequired++ },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(CrimsonPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Hospital Name with Google Maps Sync
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hospital Name",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle
                                )
                                TextButton(
                                    onClick = {
                                        MapUtils.openGoogleMaps(context, 18.5204, 73.8567, hospitalName)
                                    },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                    modifier = Modifier.testTag("btn_hospital_google_maps")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        tint = CrimsonPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Google Maps",
                                        fontSize = 11.sp,
                                        color = CrimsonPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = hospitalName,
                                onValueChange = { hospitalName = it },
                                placeholder = { Text("e.g. Ruby Hall Clinic") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = CrimsonPrimary
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimary,
                                    unfocusedBorderColor = BorderLight
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_hospital_name")
                            )
                        }

                        // Location
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Location",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle
                                )
                                if (currentLocation != null) {
                                    Text(
                                        text = "🟢 GPS Available",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HighDensityGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                placeholder = { Text("e.g. Pune, Maharashtra") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = CrimsonPrimary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        if (currentLocation != null) {
                                            location = currentLocation.formattedAddress()
                                        } else {
                                            onRequestGpsPermission()
                                            onRefreshGps()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = "Use Live GPS",
                                            tint = if (currentLocation != null) Color(0xFF1976D2) else CrimsonPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
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
                                    .testTag("input_request_location")
                            )

                            if (currentLocation != null) {
                                TextButton(
                                    onClick = {
                                        location = currentLocation.formattedAddress()
                                    },
                                    modifier = Modifier.testTag("btn_autofill_gps_location")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = null,
                                        tint = Color(0xFF1976D2),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Auto-fill Current GPS (${currentLocation.formattedCoordinates()})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1976D2)
                                    )
                                }
                            }
                        }

                        // Urgency Level
                        Column {
                            Text(
                                text = "Urgency Level",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                urgencyLevels.forEach { level ->
                                    val isSelected = level == urgencyLevel
                                    val chipColor = when (level) {
                                        "High" -> Color(0xFFD32F2F)
                                        "Medium" -> Color(0xFFEF6C00)
                                        else -> Color(0xFF1976D2)
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { urgencyLevel = level },
                                        label = {
                                            Text(
                                                text = level,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = chipColor,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFFF1F3F5),
                                            labelColor = TextCharcoal
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Contact Number
                        Column {
                            Text(
                                text = "Contact Number",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = contactNumber,
                                onValueChange = { contactNumber = it },
                                placeholder = { Text("10-digit mobile number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = CrimsonPrimary
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimary,
                                    unfocusedBorderColor = BorderLight
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_contact_number")
                            )
                        }
                    }
                }
            }

            // Prominent "Send Request" CTA
            item {
                Button(
                    onClick = {
                        if (patientName.isBlank() || contactNumber.isBlank()) {
                            Toast.makeText(context, "Please fill in patient name and contact number", Toast.LENGTH_SHORT).show()
                        } else {
                            isSubmitting = true
                            onSubmitRequest(
                                patientName,
                                selectedGroup,
                                unitsRequired,
                                hospitalName,
                                location,
                                urgencyLevel,
                                contactNumber
                            ) { newId ->
                                isSubmitting = false
                                createdRequestId = newId
                                showSuccessDialog = true
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("send_emergency_request_cta")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Broadcasting Request...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send Request & Track Live",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

data class PatientPreset(
    val id: String,
    val name: String,
    val bloodGroup: String,
    val hospital: String,
    val location: String,
    val urgency: String,
    val contact: String
)
