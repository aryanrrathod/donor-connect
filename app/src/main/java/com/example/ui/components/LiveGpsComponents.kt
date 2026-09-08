package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.location.GpsTrackingStatus
import com.example.location.LiveGpsLocation
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.HighDensityGray100
import com.example.ui.theme.HighDensityGray400
import com.example.ui.theme.HighDensityGray600
import com.example.ui.theme.HighDensityGreen
import com.example.ui.theme.HighDensityOrange
import com.example.ui.theme.HighDensityRed50
import com.example.ui.theme.HighDensityRed600
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSubtle

@Composable
fun LiveGpsLocationBar(
    status: GpsTrackingStatus,
    currentLocation: LiveGpsLocation?,
    onEnableGpsClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gps_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, HighDensityGray100, RoundedCornerShape(16.dp))
            .testTag("live_gps_status_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (status) {
                        is GpsTrackingStatus.Active -> {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityGreen.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE GPS ACTIVE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityGreen,
                                letterSpacing = 0.8.sp
                            )
                        }
                        is GpsTrackingStatus.Locating -> {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityOrange.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACQUIRING GPS SATELLITES...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityOrange,
                                letterSpacing = 0.8.sp
                            )
                        }
                        is GpsTrackingStatus.GpsDisabled -> {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityRed600)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DEVICE GPS DISABLED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityRed600,
                                letterSpacing = 0.8.sp
                            )
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityGray400)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MOBILE GPS STANDBY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityGray600,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }

                // Action button depending on status
                when (status) {
                    is GpsTrackingStatus.PermissionRequired -> {
                        Button(
                            onClick = onEnableGpsClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_grant_gps_permission")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Enable GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    is GpsTrackingStatus.GpsDisabled -> {
                        Button(
                            onClick = onOpenSettingsClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HighDensityRed600,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_open_gps_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsOff,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Turn On", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        IconButton(
                            onClick = onRefreshClick,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("btn_refresh_gps")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh GPS",
                                tint = HighDensityGray600,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Coordinates & Address Telemetry
            if (currentLocation != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentLocation.formattedAddress(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentLocation.formattedCoordinates(),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = TextSubtle
                        )
                    }

                    // Accuracy & Provider Chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, HighDensityGray100)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentLocation.formattedAccuracy(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Tap Enable GPS to detect nearby blood donors, hospitals, and emergency requests via mobile GPS.",
                    fontSize = 12.sp,
                    color = TextSubtle,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun LiveGpsHeaderChip(
    currentLocation: LiveGpsLocation?,
    onGpsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = HighDensityRed50,
        border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
        modifier = modifier
            .clickable(onClick = onGpsClick)
            .testTag("header_gps_chip")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (currentLocation != null) HighDensityGreen else CrimsonPrimary)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = currentLocation?.area?.ifBlank { currentLocation.city } ?: "GPS Ready",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CrimsonPrimary
            )
        }
    }
}
