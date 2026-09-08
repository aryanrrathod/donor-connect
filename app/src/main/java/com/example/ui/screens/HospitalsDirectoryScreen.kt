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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.FacilityEntity
import com.example.location.LiveGpsLocation
import com.example.ui.components.FacilityCard
import com.example.ui.components.dialPhoneNumber
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSubtle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalsDirectoryScreen(
    facilities: List<FacilityEntity>,
    selectedTab: String,
    searchQuery: String,
    currentLocation: LiveGpsLocation? = null,
    onTabChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAllFacilities by remember { mutableStateOf(false) }

    val displayedFacilities = if (showAllFacilities) facilities else facilities.take(4)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("hospitals_directory_screen")
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Hospitals & Blood Banks",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                    if (currentLocation != null) {
                        Text(
                            text = "🟢 Proximity via GPS (${currentLocation.formattedAddress()})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF16A34A)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick, modifier = Modifier.testTag("facilities_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextCharcoal
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
            // Segment Switcher [Hospitals | Blood Banks]
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F3F5), RoundedCornerShape(14.dp))
                        .padding(4.dp)
                ) {
                    val tabs = listOf("Hospitals", "Blood Banks")
                    tabs.forEach { tab ->
                        val isSelected = tab == selectedTab
                        Button(
                            onClick = { onTabChange(tab) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) CrimsonPrimary else Color.Transparent,
                                contentColor = if (isSelected) Color.White else TextCharcoal
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("tab_${tab.replace(" ", "_").lowercase()}")
                        ) {
                            Text(
                                text = tab,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search $selectedTab by name or area", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
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
                        .testTag("search_facility_input")
                )
            }

            // Header info
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verified Facilities Near Pune",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                    Text(
                        text = "${facilities.size} Open 24/7",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CrimsonPrimary
                    )
                }
            }

            if (facilities.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
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
                            Text(text = "🏥", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No facilities match \"$searchQuery\"",
                                fontSize = 14.sp,
                                color = TextSubtle
                            )
                        }
                    }
                }
            } else {
                items(displayedFacilities) { facility ->
                    FacilityCard(
                        facility = facility,
                        onCallClick = { dialPhoneNumber(context, facility.phone) }
                    )
                }

                if (facilities.size > 4) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                onClick = { showAllFacilities = !showAllFacilities },
                                modifier = Modifier.testTag("view_all_facilities_toggle")
                            ) {
                                Text(
                                    text = if (showAllFacilities) "Show Less" else "View All (${facilities.size})",
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
