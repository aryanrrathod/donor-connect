package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.components.DonorCard
import com.example.ui.components.InteractiveDonorsMap
import com.example.ui.components.LiveGpsLocationBar
import com.example.ui.components.dialPhoneNumber
import com.example.util.MapsIntegration
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextCharcoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyDonorsMapScreen(
    donors: List<DonorEntity>,
    currentLocation: LiveGpsLocation? = null,
    gpsStatus: GpsTrackingStatus = GpsTrackingStatus.Idle,
    onRequestGpsPermission: () -> Unit = {},
    onOpenGpsSettings: () -> Unit = {},
    onRefreshGps: () -> Unit = {},
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedDonor by remember { mutableStateOf(donors.firstOrNull()) }
    var showAll by remember { mutableStateOf(false) }

    val displayedList = if (showAll) donors else donors.take(3)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("nearby_donors_map_screen")
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Nearby Donors",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                    Text(
                        text = if (currentLocation != null) "GPS Live: ${currentLocation.formattedCoordinates()}" else "Mobile GPS Locator",
                        fontSize = 11.sp,
                        color = CrimsonPrimary
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick, modifier = Modifier.testTag("map_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextCharcoal
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        MapsIntegration.searchNearbyInGoogleMaps(
                            context = context,
                            query = "Blood Donors",
                            latitude = currentLocation?.latitude,
                            longitude = currentLocation?.longitude
                        )
                    },
                    modifier = Modifier.testTag("map_screen_google_maps_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Open Google Maps",
                        tint = Color(0xFF1976D2)
                    )
                }
                IconButton(onClick = onRefreshGps) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = CrimsonPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
        )

        // Live Mobile GPS Status Banner
        LiveGpsLocationBar(
            status = gpsStatus,
            currentLocation = currentLocation,
            onEnableGpsClick = onRequestGpsPermission,
            onOpenSettingsClick = onOpenGpsSettings,
            onRefreshClick = onRefreshGps,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        // Upper half: Interactive Map Canvas
        InteractiveDonorsMap(
            donors = donors,
            selectedDonor = selectedDonor,
            onDonorSelect = { selectedDonor = it },
            currentLocation = currentLocation,
            onRecenterClick = onRefreshGps,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        // Lower half: Donor List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Donors in your Area",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextCharcoal
                    )
                    Text(
                        text = "${donors.size} Donors Online",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = CrimsonPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(displayedList) { donor ->
                DonorCard(
                    donor = donor,
                    onCallClick = { dialPhoneNumber(context, donor.phone) }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            MapsIntegration.openDirectionsInGoogleMaps(
                                context = context,
                                destLat = donor.latitude,
                                destLng = donor.longitude,
                                label = "${donor.name} (${donor.area})"
                            )
                        },
                        modifier = Modifier.testTag("map_directions_donor_${donor.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Directions in Google Maps (${donor.distanceKm} km)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }

            if (donors.size > 3) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = { showAll = !showAll },
                            modifier = Modifier.testTag("view_all_donors_toggle")
                        ) {
                            Text(
                                text = if (showAll) "Show Less" else "View All Donors (${donors.size})",
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
