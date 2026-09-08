package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DonorEntity
import com.example.data.latitude
import com.example.data.longitude
import com.example.location.GpsTrackingStatus
import com.example.location.LiveGpsLocation
import com.example.model.BloodGroup
import com.example.ui.components.DonorCard
import com.example.ui.components.LiveGpsLocationBar
import com.example.ui.components.dialPhoneNumber
import com.example.util.MapsIntegration
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Call
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.HighDensityGreen
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSubtle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindBloodScreen(
    donors: List<DonorEntity>,
    selectedBloodGroup: String,
    selectedLocation: String,
    currentLocation: LiveGpsLocation? = null,
    gpsStatus: GpsTrackingStatus = GpsTrackingStatus.Idle,
    onRequestGpsPermission: () -> Unit = {},
    onOpenGpsSettings: () -> Unit = {},
    onRefreshGps: () -> Unit = {},
    onBloodGroupChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onRegisterDonorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAllDonors by remember { mutableStateOf(false) }
    var locationInput by remember(selectedLocation) { mutableStateOf(selectedLocation) }
    var groupDropdownExpanded by remember { mutableStateOf(false) }

    val bloodGroups = listOf("All", "O+", "A+", "B+", "AB+", "O-", "A-", "B-", "AB-")
    val popularLocations = listOf("Pune, Maharashtra", "Aundh", "Baner", "Wakad", "Kothrud", "Hinjawadi")

    val displayedDonors = if (showAllDonors) donors else donors.take(4)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("find_blood_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Find Blood",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextCharcoal
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick, modifier = Modifier.testTag("find_blood_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextCharcoal
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Reset filters */ onBloodGroupChange("All") }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
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
            // Filter Form Card
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
                            .padding(16.dp)
                    ) {
                        // Blood Group Selection
                        Text(
                            text = "Select Blood Group",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSubtle
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Chips Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(bloodGroups) { group ->
                                val isSelected = group.equals(selectedBloodGroup, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onBloodGroupChange(group) },
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Location Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Location",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle
                            )
                            if (currentLocation != null) {
                                Text(
                                    text = "🟢 GPS: ${currentLocation.formattedAddress()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = locationInput,
                            onValueChange = {
                                locationInput = it
                                onLocationChange(it)
                            },
                            placeholder = { Text("Enter city or area (e.g. Pune, Aundh)", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = CrimsonPrimary
                                )
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (locationInput.isNotBlank() && locationInput != "All") {
                                        IconButton(onClick = {
                                            locationInput = "All"
                                            onLocationChange("All")
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = TextSubtle,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    IconButton(onClick = {
                                        if (currentLocation != null) {
                                            val detected = currentLocation.formattedAddress()
                                            locationInput = detected
                                            onLocationChange(detected)
                                        } else {
                                            onRequestGpsPermission()
                                            onRefreshGps()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = "Current GPS Location",
                                            tint = if (currentLocation != null) Color(0xFF1976D2) else CrimsonPrimary,
                                            modifier = Modifier.size(20.dp)
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
                                .testTag("location_input_field")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Popular Area Chips
                        Text(
                            text = "Popular Pune Areas:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSubtle
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(popularLocations) { loc ->
                                val isSelected = locationInput.contains(loc, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) CrimsonContainer else Color(0xFFF1F3F5),
                                    border = BorderStroke(1.dp, if (isSelected) CrimsonPrimary else Color.Transparent),
                                    modifier = Modifier
                                        .clickable {
                                            locationInput = loc
                                            onLocationChange(loc)
                                        }
                                        .testTag("area_chip_$loc")
                                ) {
                                    Text(
                                        text = loc,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) CrimsonPrimary else TextCharcoal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Quick GPS action button
                        if (currentLocation != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    val detected = currentLocation.formattedAddress()
                                    locationInput = detected
                                    onLocationChange(detected)
                                },
                                modifier = Modifier.testTag("use_detected_gps_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Use Current GPS Location (${currentLocation.formattedCoordinates()})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = {
                                    onRequestGpsPermission()
                                    onRefreshGps()
                                },
                                modifier = Modifier.testTag("detect_gps_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Detect with Mobile GPS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Row of Actions: Search Donors + Google Maps Search
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onLocationChange(locationInput)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("search_donors_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Search",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    MapsIntegration.searchNearbyInGoogleMaps(
                                        context = context,
                                        query = "Blood Bank",
                                        latitude = currentLocation?.latitude,
                                        longitude = currentLocation?.longitude
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("google_maps_blood_banks_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Google Maps",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Results Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Donors",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                    Text(
                        text = "${donors.size} Found",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CrimsonPrimary
                    )
                }
            }

            if (donors.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🔍", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No donors found for $selectedBloodGroup in $selectedLocation",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSubtle,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onBloodGroupChange("All")
                                    onLocationChange("All")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonContainer)
                            ) {
                                Text("Reset Filters", color = CrimsonPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(displayedDonors) { donor ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                            .testTag("donor_result_card_${donor.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CrimsonContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = donor.bloodGroup,
                                        color = CrimsonPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = donor.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextCharcoal
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified Donor",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${donor.area}, ${donor.city} • ${donor.distanceKm} km away",
                                        fontSize = 12.sp,
                                        color = TextSubtle
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (donor.isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                ) {
                                    Text(
                                        text = if (donor.isAvailable) "Available" else "Busy",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (donor.isAvailable) Color(0xFF2E7D32) else Color(0xFFE65100),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { dialPhoneNumber(context, donor.phone) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("call_donor_btn_${donor.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call Donor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        MapsIntegration.openDirectionsInGoogleMaps(
                                            context = context,
                                            destLat = donor.latitude,
                                            destLng = donor.longitude,
                                            label = "${donor.name} (${donor.area})"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("maps_directions_donor_btn_${donor.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Google Maps", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                if (donors.size > 4) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                onClick = { showAllDonors = !showAllDonors },
                                modifier = Modifier.testTag("view_more_donors_toggle")
                            ) {
                                Text(
                                    text = if (showAllDonors) "Show Less" else "View More Donors (${donors.size - 4}+)",
                                    color = CrimsonPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
