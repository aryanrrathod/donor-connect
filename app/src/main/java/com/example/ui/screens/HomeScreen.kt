package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.dialPhoneNumber
import com.example.util.MapUtils
import com.example.util.MapsIntegration
import com.example.data.BloodStockEntity
import com.example.data.EmergencyRequestEntity
import com.example.location.GpsTrackingStatus
import com.example.location.LiveGpsLocation
import com.example.ui.components.HeroBanner
import com.example.ui.components.HighDensityActionItem
import com.example.ui.components.HighDensityAvailabilityCard
import com.example.ui.components.LiveGpsLocationBar
import com.example.ui.components.PulseHeartbeatBanner
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.HighDensityGray100
import com.example.ui.theme.HighDensityGray400
import com.example.ui.theme.HighDensityGray600
import com.example.ui.theme.HighDensityGreen
import com.example.ui.theme.HighDensityRed100
import com.example.ui.theme.HighDensityRed50
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSubtle
import com.example.viewmodel.UserProfileState

@Composable
fun HomeScreen(
    userProfile: UserProfileState,
    urgentRequests: List<EmergencyRequestEntity>,
    stockList: List<BloodStockEntity> = emptyList(),
    currentLocation: LiveGpsLocation? = null,
    gpsStatus: GpsTrackingStatus = GpsTrackingStatus.Idle,
    onRequestGpsPermission: () -> Unit = {},
    onOpenGpsSettings: () -> Unit = {},
    onRefreshGps: () -> Unit = {},
    onNavigateToFindBlood: () -> Unit,
    onNavigateToEmergencyRequest: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAvailability: () -> Unit,
    onNavigateToHospitals: () -> Unit,
    onNavigateToAwareness: () -> Unit,
    onNavigateToTracker: (Long?) -> Unit = {},
    onRegisterAsDonor: () -> Unit,
    onViewDonorCard: () -> Unit = {},
    onOpenNavigationMenu: () -> Unit = {},
    liveUpdateTicker: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // High Density Header: Clean white surface with upper-left menu button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, HighDensityGray100, RoundedCornerShape(0.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Navigation Menu at upper-left corner
                    IconButton(
                        onClick = onOpenNavigationMenu,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("home_upper_left_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Navigation Menu",
                            tint = CrimsonPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (userProfile.isRegisteredDonor) "REGISTERED DONOR" else "WELCOME TO DONOR CONNECT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (userProfile.isRegisteredDonor) CrimsonPrimary else HighDensityGray400,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userProfile.name.ifBlank { "Alex Donovan" },
                            color = TextCharcoal,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onNavigateToTracker(null) },
                            modifier = Modifier.testTag("header_tracker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Request Tracker",
                                tint = CrimsonPrimary
                            )
                        }
                        IconButton(
                            onClick = onNavigateToFindBlood,
                            modifier = Modifier.testTag("header_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextCharcoal
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(HighDensityRed50)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = userProfile.name.split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .joinToString("") { it.take(1).uppercase() }
                                .ifBlank { "AD" }
                            Text(
                                text = initials,
                                color = CrimsonPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            // Active status green indicator dot
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityGreen)
                                    .border(1.5.dp, Color.White, CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }
        }

        // Live Mobile GPS Telemetry Card
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                LiveGpsLocationBar(
                    status = gpsStatus,
                    currentLocation = currentLocation,
                    onEnableGpsClick = onRequestGpsPermission,
                    onOpenSettingsClick = onOpenGpsSettings,
                    onRefreshClick = onRefreshGps
                )
            }
        }

        // Hero Card: "Be a Donor, Be a Hero" / "Registered Donor"
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                HeroBanner(
                    isRegistered = userProfile.isRegisteredDonor,
                    userName = userProfile.name,
                    bloodGroup = userProfile.bloodGroup,
                    location = userProfile.location,
                    donorId = userProfile.donorId,
                    onRegisterClick = onRegisterAsDonor,
                    onViewDonorCardClick = onViewDonorCard
                )
            }
        }

        // High Density 4-Column Action Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        HighDensityActionItem(
                            title = "Find Blood",
                            icon = Icons.Default.Search,
                            testTag = "category_find_blood",
                            onClick = onNavigateToFindBlood
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        HighDensityActionItem(
                            title = "Emergency",
                            icon = Icons.Default.Emergency,
                            isEmergency = true,
                            badgeCount = if (urgentRequests.isNotEmpty()) urgentRequests.size else null,
                            testTag = "category_emergency_request",
                            onClick = onNavigateToEmergencyRequest
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        HighDensityActionItem(
                            title = "Donors",
                            icon = Icons.Default.NearMe,
                            testTag = "category_nearby_donors",
                            onClick = onNavigateToMap
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        HighDensityActionItem(
                            title = "Hospitals",
                            icon = Icons.Default.LocalHospital,
                            testTag = "category_hospitals",
                            onClick = onNavigateToHospitals
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Row: Blood Availability & Awareness
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, HighDensityGray100),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onNavigateToAvailability)
                                .testTag("category_blood_availability")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AVAILABILITY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityGray600,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, HighDensityGray100),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onNavigateToAwareness)
                                .testTag("category_awareness")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AWARENESS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityGray600,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // High Density Real-time Availability & Active Request Widget
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                HighDensityAvailabilityCard(
                    stockList = stockList,
                    activeRequest = urgentRequests.firstOrNull(),
                    onAvailabilityClick = onNavigateToAvailability,
                    onRequestClick = onNavigateToEmergencyRequest
                )
            }
        }

        // Heartbeat Pulse Banner
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                PulseHeartbeatBanner(
                    title = "Every Drop Can Save a Life",
                    subtitle = liveUpdateTicker.ifBlank { "Connect blood donors with people in urgent need" }
                )
            }
        }

        // Urgent Blood Requests Preview
        if (urgentRequests.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Urgent Blood Requests",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${urgentRequests.size} Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = HighDensityRed50,
                                modifier = Modifier
                                    .clickable { onNavigateToTracker(null) }
                                    .testTag("urgent_section_tracker_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = CrimsonPrimary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "TRACK ALL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonPrimary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(urgentRequests) { request ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .border(1.dp, HighDensityGray100, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToTracker(request.id) }
                        .testTag("urgent_request_card_${request.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(HighDensityRed50)
                                .border(1.dp, HighDensityRed100, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = request.bloodGroup,
                                color = CrimsonPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = request.patientName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextCharcoal
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = HighDensityRed50,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${request.unitsRequired} Units",
                                        color = CrimsonPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${request.hospitalName} • ${request.location}",
                                fontSize = 11.sp,
                                color = TextSubtle
                            )
                        }

                        Surface(
                            color = Color(0xFFFFCDD2),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = request.urgency.uppercase(),
                                color = Color(0xFFB71C1C),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Live Request Tracking Bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFAFAFA))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = HighDensityGreen,
                                    modifier = Modifier.size(7.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Live Tracking: Active Broadcast • Matching Donors Alerted",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HighDensityGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Live Request Tracker Action
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CrimsonPrimary,
                                modifier = Modifier
                                    .weight(1.1f)
                                    .clickable { onNavigateToTracker(request.id) }
                                    .testTag("track_request_btn_${request.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Track Live",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Google Maps Navigation Sync
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        MapUtils.openGoogleMapsDirections(
                                            context = context,
                                            destLat = 18.5204,
                                            destLng = 73.8567,
                                            destinationName = request.hospitalName
                                        )
                                    }
                                    .testTag("track_maps_btn_${request.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        tint = CrimsonPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Maps",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonPrimary
                                    )
                                }
                            }

                            // Call Contact / Hospital
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE0F2FE),
                                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .clickable {
                                        dialPhoneNumber(context, request.contactNumber)
                                    }
                                    .testTag("call_hospital_btn_${request.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Call",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quote & Trust Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(1.dp, HighDensityGray100, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "❝",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "One donation can save up to 3 lives. Be someone's lifeline today.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextCharcoal,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
