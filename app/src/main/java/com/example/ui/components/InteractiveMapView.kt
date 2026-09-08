package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DonorEntity
import com.example.location.LiveGpsLocation
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.HighDensityGreen
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSubtle

@Composable
fun InteractiveDonorsMap(
    donors: List<DonorEntity>,
    selectedDonor: DonorEntity?,
    onDonorSelect: (DonorEntity) -> Unit,
    currentLocation: LiveGpsLocation? = null,
    onRecenterClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val radarRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_radius"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_alpha"
    )

    // Predefined coordinates mapped to canvas
    val donorPinOffsets = remember(donors) {
        listOf(
            Offset(0.25f, 0.32f),
            Offset(0.72f, 0.28f),
            Offset(0.35f, 0.70f),
            Offset(0.80f, 0.65f),
            Offset(0.18f, 0.55f),
            Offset(0.60f, 0.82f),
            Offset(0.45f, 0.22f),
            Offset(0.85f, 0.42f)
        )
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(donors) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height
                        donors.forEachIndexed { index, donor ->
                            val norm = donorPinOffsets.getOrElse(index) { Offset(0.5f, 0.5f) }
                            val pinX = norm.x * w
                            val pinY = norm.y * h
                            val dist = kotlin.math.hypot(tapOffset.x - pinX, tapOffset.y - pinY)
                            if (dist < 80f) {
                                onDonorSelect(donor)
                            }
                        }
                    }
                }
                .testTag("interactive_canvas_map")
        ) {
            val w = size.width
            val h = size.height

            // Background map canvas (city land color)
            drawRect(color = Color(0xFFE8ECEF))

            // Parks / Green zones
            drawRoundRect(
                color = Color(0xFFDCE7DC),
                topLeft = Offset(w * 0.05f, h * 0.1f),
                size = Size(w * 0.25f, h * 0.2f),
                cornerRadius = CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = Color(0xFFDCE7DC),
                topLeft = Offset(w * 0.65f, h * 0.55f),
                size = Size(w * 0.3f, h * 0.28f),
                cornerRadius = CornerRadius(20f, 20f)
            )

            // Rivers/Water canal
            val riverPath = Path().apply {
                moveTo(0f, h * 0.45f)
                cubicTo(w * 0.3f, h * 0.42f, w * 0.5f, h * 0.58f, w, h * 0.48f)
                lineTo(w, h * 0.53f)
                cubicTo(w * 0.5f, h * 0.63f, w * 0.3f, h * 0.47f, 0f, h * 0.50f)
                close()
            }
            drawPath(path = riverPath, color = Color(0xFFCFE2FE))

            // Major arterial roads (light gray/white road grid)
            val roadColor = Color.White
            val roadOutline = Color(0xFFD0D7DE)

            // Horizontal roads
            val hRoads = listOf(0.25f, 0.50f, 0.75f)
            hRoads.forEach { yRatio ->
                drawLine(
                    color = roadOutline,
                    start = Offset(0f, h * yRatio),
                    end = Offset(w, h * yRatio),
                    strokeWidth = 14f
                )
                drawLine(
                    color = roadColor,
                    start = Offset(0f, h * yRatio),
                    end = Offset(w, h * yRatio),
                    strokeWidth = 10f
                )
            }

            // Vertical roads
            val vRoads = listOf(0.30f, 0.55f, 0.80f)
            vRoads.forEach { xRatio ->
                drawLine(
                    color = roadOutline,
                    start = Offset(w * xRatio, 0f),
                    end = Offset(w * xRatio, h),
                    strokeWidth = 14f
                )
                drawLine(
                    color = roadColor,
                    start = Offset(w * xRatio, 0f),
                    end = Offset(w * xRatio, h),
                    strokeWidth = 10f
                )
            }

            // User location: Center (0.5, 0.5)
            val userCenter = Offset(w * 0.52f, h * 0.48f)

            // Radar pulse circle
            drawCircle(
                color = Color(0xFF1976D2).copy(alpha = radarAlpha),
                radius = radarRadius * 1.8f,
                center = userCenter,
                style = Stroke(width = 3f)
            )

            // User Location Blue Marker
            drawCircle(
                color = Color(0xFF1976D2).copy(alpha = 0.25f),
                radius = 20f,
                center = userCenter
            )
            drawCircle(
                color = Color.White,
                radius = 11f,
                center = userCenter
            )
            drawCircle(
                color = Color(0xFF1976D2),
                radius = 7.5f,
                center = userCenter
            )

            // Donor Pins (Crimson Red with white border)
            donors.forEachIndexed { index, donor ->
                val norm = donorPinOffsets.getOrElse(index) { Offset(0.5f, 0.5f) }
                val pinX = norm.x * w
                val pinY = norm.y * h
                val isSelected = donor.id == selectedDonor?.id

                // Pin shadow
                drawOval(
                    color = Color.Black.copy(alpha = 0.2f),
                    topLeft = Offset(pinX - 12f, pinY + 16f),
                    size = Size(24f, 10f)
                )

                // Pin teardrop/circle shape
                val pinColor = if (isSelected) Color(0xFFB71C1C) else CrimsonPrimary
                val pinRadius = if (isSelected) 22f else 18f

                // Outer ring
                drawCircle(
                    color = Color.White,
                    radius = pinRadius + 4f,
                    center = Offset(pinX, pinY)
                )
                // Main Pin
                drawCircle(
                    color = pinColor,
                    radius = pinRadius,
                    center = Offset(pinX, pinY)
                )
                // Center white core
                drawCircle(
                    color = Color.White,
                    radius = pinRadius * 0.45f,
                    center = Offset(pinX, pinY)
                )
            }
        }

        // Floating GPS Telemetry Overlay (Top Left)
        if (currentLocation != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.92f),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(HighDensityGreen)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "GPS: ${currentLocation.formattedCoordinates()}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                }
            }
        }

        // Floating Map Controls (Top Right)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SurfaceLight,
                shadowElevation = 3.dp,
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(onClick = onRecenterClick) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Live selected donor card overlay at the bottom of map
        if (selectedDonor != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CrimsonContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedDonor.bloodGroup,
                            color = CrimsonPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedDonor.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextCharcoal
                        )
                        Text(
                            text = "${selectedDonor.distanceKm} km • ${selectedDonor.area}",
                            fontSize = 12.sp,
                            color = TextSubtle
                        )
                    }

                    IconButton(
                        onClick = { dialPhoneNumber(context, selectedDonor.phone) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
