package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DonationRecordEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.dialPhoneNumber
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonPrimaryDark
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSubtle
import com.example.viewmodel.UserProfileState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorProfileScreen(
    userProfile: UserProfileState,
    donationHistory: List<DonationRecordEntity>,
    onToggleAvailability: (Boolean) -> Unit,
    onUpdateProfile: (name: String, group: String, phone: String, location: String) -> Unit,
    onEmergencyRequestClick: () -> Unit,
    onNavigateHospitals: () -> Unit,
    onNavigateDonors: () -> Unit,
    onViewDonorCard: () -> Unit = {},
    onRegisterDonor: () -> Unit = {},
    onResetRegistration: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEmergencyContactsDialog by remember { mutableStateOf(false) }

    // History Dialog
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Text(
                    text = "My Donation History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (donationHistory.isEmpty()) {
                        Text("No past donation records yet.", color = TextSubtle)
                    } else {
                        donationHistory.forEach { record ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        tint = CrimsonPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(record.hospitalName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(record.donationDate, fontSize = 12.sp, color = TextSubtle)
                                        Text("Cert: ${record.certificateId}", fontSize = 10.sp, color = CrimsonPrimary)
                                    }
                                    Text("${record.unitsDonated} Unit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Close", color = CrimsonPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Emergency Contacts Dialog
    if (showEmergencyContactsDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyContactsDialog = false },
            title = {
                Text("Emergency Helplines", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val contacts = listOf(
                        "National Blood Helpline" to "104",
                        "National Emergency SOS" to "112",
                        "Ambulance Services" to "108",
                        "Red Cross Central Blood Bank" to "020-26131444"
                    )
                    contacts.forEach { (name, phone) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8F9FA), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(phone, color = CrimsonPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            IconButton(onClick = { dialPhoneNumber(context, phone) }) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = Color(0xFF1976D2)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEmergencyContactsDialog = false }) {
                    Text("Done", color = CrimsonPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("donor_profile_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Donor Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextCharcoal
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card (Matching screenshot)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(CrimsonPrimary, CrimsonPrimaryDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.name.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = userProfile.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${userProfile.bloodGroup} • ${userProfile.role}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CrimsonPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (userProfile.isRegisteredDonor) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verified Donor • ID: ${userProfile.donorId} (Registered Once)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Not Registered as Donor Yet",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        StatusBadge(status = if (userProfile.isAvailable) "Available" else "On Break")

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Horizontal Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8F9FA), RoundedCornerShape(14.dp))
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = userProfile.totalDonations.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPrimary
                                )
                                Text(
                                    text = "Donations",
                                    fontSize = 11.sp,
                                    color = TextSubtle
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .width(1.dp)
                                    .background(BorderLight)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = userProfile.livesSaved.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "Lives Saved",
                                    fontSize = 11.sp,
                                    color = TextSubtle
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .width(1.dp)
                                    .background(BorderLight)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = userProfile.lastDonated,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextCharcoal
                                )
                                Text(
                                    text = "Last Donated",
                                    fontSize = 11.sp,
                                    color = TextSubtle
                                )
                            }
                        }
                    }
                }
            }

            // Giant One-Tap Emergency Blood Request Banner (Matching Screenshot)
            item {
                Card(
                    onClick = onEmergencyRequestClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CrimsonPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_emergency_request_cta")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Emergency Blood Request",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tap here to request urgent blood",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            // Emergency Support Quick Tiles (Nearby Hospitals, Blood Banks, Donors, Emergency Contacts)
            item {
                Text(
                    text = "Emergency Support",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextCharcoal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EmergencyTile(
                        title = "Nearby\nHospitals",
                        icon = Icons.Default.LocalHospital,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateHospitals
                    )
                    EmergencyTile(
                        title = "Nearby\nBlood Banks",
                        icon = Icons.Default.WaterDrop,
                        color = CrimsonPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateHospitals
                    )
                    EmergencyTile(
                        title = "Nearby\nDonors",
                        icon = Icons.Default.NearMe,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateDonors
                    )
                    EmergencyTile(
                        title = "Emergency\nContacts",
                        icon = Icons.Default.Call,
                        color = Color(0xFFE65100),
                        modifier = Modifier.weight(1f),
                        onClick = { showEmergencyContactsDialog = true }
                    )
                }
            }

            // Profile Options Menu (Matching Screenshot)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Profile Information
                        ProfileMenuItem(
                            icon = Icons.Default.Person,
                            title = "Profile Information",
                            onClick = {
                                Toast.makeText(context, "${userProfile.name} (${userProfile.bloodGroup}) • ${userProfile.location}", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF1F3F5))

                        // Digital Donor Card
                        ProfileMenuItem(
                            icon = Icons.Default.CheckCircle,
                            title = "Digital Donor Card",
                            badgeText = if (userProfile.isRegisteredDonor) "Active ✓" else "Register",
                            onClick = {
                                if (userProfile.isRegisteredDonor) {
                                    onViewDonorCard()
                                } else {
                                    onRegisterDonor()
                                }
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF1F3F5))

                        // My Donation History
                        ProfileMenuItem(
                            icon = Icons.Default.History,
                            title = "My Donation History",
                            onClick = { showHistoryDialog = true }
                        )
                        HorizontalDivider(color = Color(0xFFF1F3F5))

                        // Requests Received with badge
                        ProfileMenuItem(
                            icon = Icons.Default.NotificationsActive,
                            title = "Requests Received",
                            badgeText = "${userProfile.pendingRequestsCount}",
                            onClick = {
                                Toast.makeText(context, "${userProfile.pendingRequestsCount} matching urgent requests in your area", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF1F3F5))

                        // Update Availability Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ToggleOn,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Update Availability",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = TextCharcoal
                                    )
                                    Text(
                                        text = if (userProfile.isAvailable) "Ready to donate" else "Currently on break",
                                        fontSize = 12.sp,
                                        color = TextSubtle
                                    )
                                }
                            }
                            Switch(
                                checked = userProfile.isAvailable,
                                onCheckedChange = onToggleAvailability,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CrimsonPrimary
                                ),
                                modifier = Modifier.testTag("availability_toggle_switch")
                            )
                        }
                        HorizontalDivider(color = Color(0xFFF1F3F5))

                        // Logout
                        ProfileMenuItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            title = "Logout",
                            textColor = CrimsonPrimary,
                            onClick = {
                                Toast.makeText(context, "Session active as ${userProfile.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF1F3F5))

                        // Reset Registration (Demo Mode)
                        ProfileMenuItem(
                            icon = Icons.Default.Refresh,
                            title = "Reset Registration (Demo Mode)",
                            textColor = Color(0xFF757575),
                            onClick = onResetRegistration
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyTile(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextCharcoal,
                lineHeight = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    badgeText: String? = null,
    textColor: Color = TextCharcoal,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (textColor == CrimsonPrimary) CrimsonPrimary else TextSubtle,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badgeText != null) {
                Surface(
                    color = CrimsonPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFFB0B7C3),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
