package com.example.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DigitalDonorCardDialog
import com.example.ui.screens.AwarenessScreen
import com.example.ui.screens.BloodAvailabilityScreen
import com.example.ui.screens.DonorProfileScreen
import com.example.ui.screens.EmergencyRequestScreen
import com.example.ui.screens.FindBloodScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HospitalsDirectoryScreen
import com.example.ui.screens.NearbyDonorsMapScreen
import com.example.ui.screens.RequestTrackerScreen
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.HighDensityGray100
import com.example.ui.theme.HighDensityGray400
import com.example.ui.theme.HighDensityGray600
import com.example.ui.theme.HighDensityRed50
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSubtle
import com.example.util.MapsIntegration
import com.example.viewmodel.BloodViewModel
import kotlinx.coroutines.launch

enum class ScreenRoute {
    HOME,
    FIND_BLOOD,
    EMERGENCY_REQUEST,
    REQUEST_TRACKER,
    NEARBY_DONORS_MAP,
    BLOOD_AVAILABILITY,
    HOSPITALS_DIRECTORY,
    DONOR_PROFILE,
    AWARENESS
}

data class NavItem(
    val route: ScreenRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun BloodDonationApp(
    viewModel: BloodViewModel = viewModel()
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val liveUpdateTicker by viewModel.liveUpdateTicker.collectAsState()

    var currentScreen by remember { mutableStateOf(ScreenRoute.HOME) }
    var showRegisterDonorDialog by remember { mutableStateOf(false) }
    var showDonorCardDialog by remember { mutableStateOf(false) }
    var showAlreadyRegisteredDialog by remember { mutableStateOf(false) }

    // ViewModel State
    val userProfile by viewModel.userProfile.collectAsState()
    val allDonors by viewModel.allDonors.collectAsState()
    val filteredDonors by viewModel.filteredDonors.collectAsState()
    val allRequests by viewModel.allRequests.collectAsState()
    val selectedTrackedRequestId by viewModel.selectedTrackedRequestId.collectAsState()
    val trackerCustomizations by viewModel.trackerCustomizations.collectAsState()
    val allStock by viewModel.allStock.collectAsState()
    val filteredFacilities by viewModel.filteredFacilities.collectAsState()
    val donationHistory by viewModel.donationHistory.collectAsState()
    val selectedBloodGroupFilter by viewModel.selectedBloodGroupFilter.collectAsState()
    val selectedLocationFilter by viewModel.selectedLocationFilter.collectAsState()
    val selectedFacilityTab by viewModel.selectedFacilityTab.collectAsState()
    val facilitySearchQuery by viewModel.facilitySearchQuery.collectAsState()

    // Mobile GPS State
    val currentGpsLocation by viewModel.currentGpsLocation.collectAsState()
    val gpsStatus by viewModel.gpsTrackingStatus.collectAsState()
    val donorsWithGpsDistance by viewModel.donorsWithGpsDistance.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.startLiveGpsTracking()
            Toast.makeText(context, "Mobile GPS Live Location Active", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location permission required for mobile GPS", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.hasLocationPermission()) {
            viewModel.startLiveGpsTracking()
        }
        if (!viewModel.hasSeenOnboardingRegistration() && !userProfile.isRegisteredDonor) {
            showRegisterDonorDialog = true
        }
    }

    val onRequestGpsPermission: () -> Unit = {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    val onOpenGpsSettings: () -> Unit = {
        viewModel.openLocationSettings()
    }
    val onRefreshGps: () -> Unit = {
        if (viewModel.hasLocationPermission()) {
            viewModel.refreshGpsLocation()
            Toast.makeText(context, "Refreshing GPS Satellite Fix...", Toast.LENGTH_SHORT).show()
        } else {
            onRequestGpsPermission()
        }
    }

    // Register Donor Dialog State
    var regName by remember { mutableStateOf("") }
    var regAge by remember { mutableStateOf("26") }
    var regGender by remember { mutableStateOf("Male") }
    var regBloodGroup by remember { mutableStateOf("O+") }
    var regHasAnemia by remember { mutableStateOf(false) }
    var regPhone by remember { mutableStateOf("") }
    var regArea by remember { mutableStateOf("Aundh") }
    var regCity by remember { mutableStateOf("Pune") }

    if (showRegisterDonorDialog) {
        AlertDialog(
            onDismissRequest = {
                showRegisterDonorDialog = false
                viewModel.markOnboardingRegistrationSeen()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(HighDensityRed50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Register as a Blood Donor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextCharcoal
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Join Pune's verified rapid response donor network. One-time setup.",
                        fontSize = 12.sp,
                        color = TextSubtle
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = regName,
                        onValueChange = { regName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            focusedLabelColor = CrimsonPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = regAge,
                            onValueChange = { regAge = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text("Age") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                focusedLabelColor = CrimsonPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("reg_age_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text("Gender", fontSize = 11.sp, color = TextSubtle)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("Male", "Female").forEach { g ->
                                    FilterChip(
                                        selected = regGender == g,
                                        onClick = { regGender = g },
                                        label = { Text(g, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = HighDensityRed50,
                                            selectedLabelColor = CrimsonPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Blood Group", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextCharcoal)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        items(listOf("O+", "A+", "B+", "AB+", "O-", "A-", "B-", "AB-")) { group ->
                            FilterChip(
                                selected = regBloodGroup == group,
                                onClick = { regBloodGroup = group },
                                label = { Text(group, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Anemia Check
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { regHasAnemia = !regHasAnemia }
                            .padding(vertical = 4.dp)
                    ) {
                        FilterChip(
                            selected = !regHasAnemia,
                            onClick = { regHasAnemia = false },
                            label = { Text("No Anemia (Eligible)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE8F5E9),
                                selectedLabelColor = Color(0xFF2E7D32)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = regHasAnemia,
                            onClick = { regHasAnemia = true },
                            label = { Text("Has Anemia", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HighDensityRed50,
                                selectedLabelColor = CrimsonPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = regPhone,
                        onValueChange = { regPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            focusedLabelColor = CrimsonPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("reg_phone_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = regArea,
                            onValueChange = { regArea = it },
                            label = { Text("Area / Locality") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                focusedLabelColor = CrimsonPrimary
                            ),
                            modifier = Modifier.weight(1.2f).testTag("reg_area_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = regCity,
                            onValueChange = { regCity = it },
                            label = { Text("City") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                focusedLabelColor = CrimsonPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("reg_city_input")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (regName.isBlank() || regPhone.isBlank()) {
                            Toast.makeText(context, "Please fill required name and phone", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.registerAsDonor(
                                name = regName,
                                age = regAge.toIntOrNull() ?: 26,
                                gender = regGender,
                                bloodGroup = regBloodGroup,
                                hasAnemia = regHasAnemia,
                                phone = regPhone,
                                area = regArea,
                                city = regCity
                            ) { success ->
                                showRegisterDonorDialog = false
                                if (success) {
                                    showDonorCardDialog = true
                                    Toast.makeText(context, "Registration successful! You are now an active donor.", Toast.LENGTH_LONG).show()
                                } else {
                                    showAlreadyRegisteredDialog = true
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    modifier = Modifier.testTag("confirm_register_donor_btn")
                ) {
                    Text("Register Now", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRegisterDonorDialog = false
                    viewModel.markOnboardingRegistrationSeen()
                }) {
                    Text("Cancel", color = TextSubtle)
                }
            }
        )
    }

    // Already Registered Alert Dialog
    if (showAlreadyRegisteredDialog) {
        AlertDialog(
            onDismissRequest = { showAlreadyRegisteredDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Already Registered",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextCharcoal
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "You are already registered as a blood donor (${userProfile.name} • ${userProfile.bloodGroup}).",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextCharcoal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Registration occurs only once per donor. You can view your Digital Donor ID Card or edit your contact details in your Profile.",
                        fontSize = 13.sp,
                        color = TextSubtle,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAlreadyRegisteredDialog = false
                        showDonorCardDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("View Donor Card", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlreadyRegisteredDialog = false }) {
                    Text("Close", color = TextSubtle)
                }
            }
        )
    }

    // Digital Donor Card Dialog
    if (showDonorCardDialog) {
        DigitalDonorCardDialog(
            userProfile = userProfile,
            onDismiss = { showDonorCardDialog = false },
            onNavigateToProfile = { currentScreen = ScreenRoute.DONOR_PROFILE }
        )
    }

    val navItems = listOf(
        NavItem(ScreenRoute.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        NavItem(ScreenRoute.EMERGENCY_REQUEST, "Requests", Icons.Filled.Emergency, Icons.Outlined.Emergency, "nav_requests"),
        NavItem(ScreenRoute.NEARBY_DONORS_MAP, "Donors", Icons.Filled.NearMe, Icons.Outlined.NearMe, "nav_donors"),
        NavItem(ScreenRoute.DONOR_PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile"),
        NavItem(ScreenRoute.HOSPITALS_DIRECTORY, "Menu", Icons.Filled.Menu, Icons.Outlined.Menu, "nav_menu")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp),
                drawerContainerColor = SurfaceLight
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
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
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Donor Connect",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextCharcoal
                        )
                        Text(
                            text = if (userProfile.isRegisteredDonor) "Verified Donor Network" else "Save Lives • Pune",
                            fontSize = 12.sp,
                            color = TextSubtle
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = HighDensityGray100)

                val drawerMenuItems = listOf(
                    Triple(ScreenRoute.HOME, "Home Overview", Icons.Filled.Home),
                    Triple(ScreenRoute.EMERGENCY_REQUEST, "Emergency Requests", Icons.Filled.Emergency),
                    Triple(ScreenRoute.NEARBY_DONORS_MAP, "Nearby Donors Map", Icons.Filled.NearMe),
                    Triple(ScreenRoute.FIND_BLOOD, "Find Blood & Donors", Icons.Filled.Search),
                    Triple(ScreenRoute.BLOOD_AVAILABILITY, "Blood Stock Availability", Icons.Filled.TableChart),
                    Triple(ScreenRoute.HOSPITALS_DIRECTORY, "Hospitals & Blood Banks", Icons.Filled.LocalHospital),
                    Triple(ScreenRoute.AWARENESS, "Awareness & Eligibility", Icons.Filled.MenuBook),
                    Triple(ScreenRoute.DONOR_PROFILE, "Donor Profile & ID Card", Icons.Filled.Person)
                )

                drawerMenuItems.forEach { (route, label, icon) ->
                    NavigationDrawerItem(
                        label = { Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                        icon = { Icon(icon, contentDescription = null, tint = if (currentScreen == route) CrimsonPrimary else HighDensityGray600) },
                        selected = currentScreen == route,
                        onClick = {
                            currentScreen = route
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = HighDensityRed50,
                            selectedTextColor = CrimsonPrimary,
                            unselectedTextColor = TextCharcoal
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(1.dp, HighDensityGray100)
                    .testTag("bottom_navigation_bar")
            ) {
                navItems.forEach { item ->
                    val isSelected = when (item.route) {
                        ScreenRoute.HOME -> currentScreen == ScreenRoute.HOME
                        ScreenRoute.EMERGENCY_REQUEST -> currentScreen == ScreenRoute.EMERGENCY_REQUEST || currentScreen == ScreenRoute.REQUEST_TRACKER
                        ScreenRoute.NEARBY_DONORS_MAP -> currentScreen == ScreenRoute.NEARBY_DONORS_MAP || currentScreen == ScreenRoute.FIND_BLOOD
                        ScreenRoute.DONOR_PROFILE -> currentScreen == ScreenRoute.DONOR_PROFILE
                        ScreenRoute.HOSPITALS_DIRECTORY -> currentScreen == ScreenRoute.HOSPITALS_DIRECTORY || currentScreen == ScreenRoute.BLOOD_AVAILABILITY || currentScreen == ScreenRoute.AWARENESS
                        else -> false
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (item.route == ScreenRoute.HOSPITALS_DIRECTORY) {
                                coroutineScope.launch { drawerState.open() }
                            } else {
                                currentScreen = item.route
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            indicatorColor = HighDensityRed50,
                            unselectedIconColor = HighDensityGray400,
                            unselectedTextColor = HighDensityGray600.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                when (screen) {
                    ScreenRoute.HOME -> HomeScreen(
                        userProfile = userProfile,
                        urgentRequests = allRequests,
                        stockList = allStock,
                        currentLocation = currentGpsLocation,
                        gpsStatus = gpsStatus,
                        onRequestGpsPermission = onRequestGpsPermission,
                        onOpenGpsSettings = onOpenGpsSettings,
                        onRefreshGps = onRefreshGps,
                        onNavigateToFindBlood = { currentScreen = ScreenRoute.FIND_BLOOD },
                        onNavigateToEmergencyRequest = { currentScreen = ScreenRoute.EMERGENCY_REQUEST },
                        onNavigateToMap = { currentScreen = ScreenRoute.NEARBY_DONORS_MAP },
                        onNavigateToAvailability = { currentScreen = ScreenRoute.BLOOD_AVAILABILITY },
                        onNavigateToHospitals = { currentScreen = ScreenRoute.HOSPITALS_DIRECTORY },
                        onNavigateToAwareness = { currentScreen = ScreenRoute.AWARENESS },
                        onNavigateToTracker = { reqId ->
                            if (reqId != null) {
                                viewModel.setSelectedTrackedRequestId(reqId)
                            }
                            currentScreen = ScreenRoute.REQUEST_TRACKER
                        },
                        onRegisterAsDonor = {
                            if (userProfile.isRegisteredDonor) {
                                showAlreadyRegisteredDialog = true
                            } else {
                                showRegisterDonorDialog = true
                            }
                        },
                        onViewDonorCard = { showDonorCardDialog = true },
                        onOpenNavigationMenu = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        liveUpdateTicker = liveUpdateTicker
                    )

                    ScreenRoute.FIND_BLOOD -> FindBloodScreen(
                        donors = filteredDonors,
                        selectedBloodGroup = selectedBloodGroupFilter,
                        selectedLocation = selectedLocationFilter,
                        currentLocation = currentGpsLocation,
                        gpsStatus = gpsStatus,
                        onRequestGpsPermission = onRequestGpsPermission,
                        onOpenGpsSettings = onOpenGpsSettings,
                        onRefreshGps = onRefreshGps,
                        onBloodGroupChange = { viewModel.setBloodGroupFilter(it) },
                        onLocationChange = { viewModel.setLocationFilter(it) },
                        onBackClick = { currentScreen = ScreenRoute.HOME },
                        onRegisterDonorClick = {
                            if (userProfile.isRegisteredDonor) {
                                showAlreadyRegisteredDialog = true
                            } else {
                                showRegisterDonorDialog = true
                            }
                        }
                    )

                    ScreenRoute.EMERGENCY_REQUEST -> EmergencyRequestScreen(
                        currentLocation = currentGpsLocation,
                        onRequestGpsPermission = onRequestGpsPermission,
                        onRefreshGps = onRefreshGps,
                        onBackClick = { currentScreen = ScreenRoute.HOME },
                        onTrackRequestClick = { reqId ->
                            if (reqId != null) {
                                viewModel.setSelectedTrackedRequestId(reqId)
                            }
                            currentScreen = ScreenRoute.REQUEST_TRACKER
                        },
                        onSubmitRequest = { name, group, units, hospital, loc, urgency, contact, onSuccess ->
                            viewModel.submitEmergencyRequest(
                                patientName = name,
                                bloodGroup = group,
                                unitsRequired = units,
                                hospitalName = hospital,
                                location = loc,
                                urgency = urgency,
                                contactNumber = contact
                            ) { newId ->
                                viewModel.setSelectedTrackedRequestId(newId)
                                onSuccess(newId)
                            }
                        }
                    )

                    ScreenRoute.REQUEST_TRACKER -> RequestTrackerScreen(
                        requests = allRequests,
                        selectedRequestId = selectedTrackedRequestId,
                        currentLocation = currentGpsLocation,
                        trackerCustomizations = trackerCustomizations,
                        onSelectRequest = { id -> viewModel.setSelectedTrackedRequestId(id) },
                        onAdvanceStage = { id, currentStatus -> viewModel.advanceTrackingStage(id, currentStatus) },
                        onMarkFulfilled = { id -> viewModel.updateRequestStatus(id, "Fulfilled") },
                        onCancelRequest = { id -> viewModel.cancelEmergencyRequest(id) },
                        onSaveCustomization = { id, custom -> viewModel.saveTrackerCustomization(id, custom) },
                        onUpdateRequest = { req -> viewModel.updateEmergencyRequest(req) },
                        onNavigateToEmergencyRequest = { currentScreen = ScreenRoute.EMERGENCY_REQUEST },
                        onBackClick = { currentScreen = ScreenRoute.HOME }
                    )

                    ScreenRoute.NEARBY_DONORS_MAP -> NearbyDonorsMapScreen(
                        donors = donorsWithGpsDistance,
                        currentLocation = currentGpsLocation,
                        gpsStatus = gpsStatus,
                        onRequestGpsPermission = onRequestGpsPermission,
                        onOpenGpsSettings = onOpenGpsSettings,
                        onRefreshGps = onRefreshGps,
                        onBackClick = { currentScreen = ScreenRoute.HOME }
                    )

                    ScreenRoute.BLOOD_AVAILABILITY -> BloodAvailabilityScreen(
                        stockList = allStock,
                        onBackClick = { currentScreen = ScreenRoute.HOME },
                        onRequestBloodClick = {
                            viewModel.setBloodGroupFilter(it)
                            currentScreen = ScreenRoute.EMERGENCY_REQUEST
                        }
                    )

                    ScreenRoute.HOSPITALS_DIRECTORY -> HospitalsDirectoryScreen(
                        facilities = filteredFacilities,
                        selectedTab = selectedFacilityTab,
                        searchQuery = facilitySearchQuery,
                        currentLocation = currentGpsLocation,
                        onTabChange = { viewModel.setFacilityTab(it) },
                        onSearchChange = { viewModel.setFacilitySearchQuery(it) },
                        onBackClick = { currentScreen = ScreenRoute.HOME }
                    )

                    ScreenRoute.DONOR_PROFILE -> DonorProfileScreen(
                        userProfile = userProfile,
                        donationHistory = donationHistory,
                        onToggleAvailability = { viewModel.toggleUserAvailability(it) },
                        onUpdateProfile = { n, g, p, l -> viewModel.updateUserProfile(n, g, p, l) },
                        onEmergencyRequestClick = { currentScreen = ScreenRoute.EMERGENCY_REQUEST },
                        onNavigateHospitals = { currentScreen = ScreenRoute.HOSPITALS_DIRECTORY },
                        onNavigateDonors = { currentScreen = ScreenRoute.NEARBY_DONORS_MAP },
                        onViewDonorCard = { showDonorCardDialog = true },
                        onRegisterDonor = {
                            if (userProfile.isRegisteredDonor) {
                                showAlreadyRegisteredDialog = true
                            } else {
                                showRegisterDonorDialog = true
                            }
                        },
                        onResetRegistration = {
                            viewModel.resetRegistrationForTesting()
                            Toast.makeText(context, "Registration reset for demo testing.", Toast.LENGTH_SHORT).show()
                        }
                    )

                    ScreenRoute.AWARENESS -> AwarenessScreen(
                        onBackClick = { currentScreen = ScreenRoute.HOME }
                    )
                }
            }
        }
    }
}
}
