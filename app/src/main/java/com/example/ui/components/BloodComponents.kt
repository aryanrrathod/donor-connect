package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import com.example.util.MapUtils
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BloodStockEntity
import com.example.data.DonorEntity
import com.example.data.EmergencyRequestEntity
import com.example.data.FacilityEntity
import com.example.viewmodel.UserProfileState
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonOnContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonPrimaryDark
import com.example.ui.theme.CrimsonSecondary
import com.example.ui.theme.HighDensityGray100
import com.example.ui.theme.HighDensityGray400
import com.example.ui.theme.HighDensityGray50
import com.example.ui.theme.HighDensityGray600
import com.example.ui.theme.HighDensityGreen
import com.example.ui.theme.HighDensityOrange
import com.example.ui.theme.HighDensityRed100
import com.example.ui.theme.HighDensityRed200
import com.example.ui.theme.HighDensityRed50
import com.example.ui.theme.HighDensityRed600
import com.example.ui.theme.HighDensityRed700
import com.example.ui.theme.StatusAvailableBg
import com.example.ui.theme.StatusAvailableText
import com.example.ui.theme.StatusLowBg
import com.example.ui.theme.StatusLowText
import com.example.ui.theme.StatusNotAvailableBg
import com.example.ui.theme.StatusNotAvailableText
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSubtle

fun dialPhoneNumber(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Dialer unavailable: $phoneNumber", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "available" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        "low" -> Color(0xFFFFEDD5) to Color(0xFFC2410C)
        else -> HighDensityRed100 to HighDensityRed700
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun HighDensityActionItem(
    title: String,
    icon: ImageVector,
    isEmergency: Boolean = false,
    badgeCount: Int? = null,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isEmergency) HighDensityRed50 else Color.White)
                    .border(
                        width = 1.dp,
                        color = if (isEmergency) HighDensityRed100 else HighDensityGray100,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isEmergency) HighDensityRed700 else CrimsonPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (badgeCount != null && badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(CrimsonPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isEmergency) HighDensityRed700 else HighDensityGray600,
            textAlign = TextAlign.Center,
            letterSpacing = 0.4.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun QuickCategoryCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    badgeCount: Int? = null,
    testTag: String,
    onClick: () -> Unit
) {
    HighDensityActionItem(
        title = title,
        icon = icon,
        isEmergency = title.contains("Emergency", ignoreCase = true),
        badgeCount = badgeCount,
        testTag = testTag,
        onClick = onClick
    )
}

@Composable
fun HeroBanner(
    isRegistered: Boolean = false,
    userName: String = "",
    bloodGroup: String = "O+",
    location: String = "",
    donorId: String = "",
    onRegisterClick: () -> Unit = {},
    onViewDonorCardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isRegistered) {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFC62828), Color(0xFFB71C1C))
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE53935), Color(0xFFD32F2F))
                        )
                    }
                )
                .padding(20.dp)
        ) {
            // Background subtle watermark vector
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.BottomEnd)
                    .padding(top = 10.dp)
            ) {
                Icon(
                    imageVector = if (isRegistered) Icons.Default.CheckCircle else Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.16f),
                    modifier = Modifier.size(100.dp)
                )
            }

            Column(modifier = Modifier.fillMaxWidth(0.82f)) {
                if (isRegistered) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "REGISTERED DONOR",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "You're a Life Saver!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Registered $bloodGroup Donor • ${location.ifBlank { "Pune" }}\nRegistration verified (Registered Once). Ready for emergency alerts.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onViewDonorCardClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = CrimsonPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("hero_view_donor_card_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VIEW DONOR CARD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary,
                            letterSpacing = 0.6.sp
                        )
                    }
                } else {
                    Text(
                        text = "Be a Donor, Be a Hero",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your single donation can save up to three lives in your community.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onRegisterClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = CrimsonPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("hero_register_button")
                    ) {
                        Text(
                            text = "REGISTER NOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DigitalDonorCardDialog(
    userProfile: UserProfileState,
    onDismiss: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("digital_donor_card_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Card Header with Crimson Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFD32F2F), Color(0xFFB71C1C))
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SMART BLOOD DONOR",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Official Verified Donor Card",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Card Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar & Blood Group Badge
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF0F0))
                            .border(2.dp, CrimsonPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.bloodGroup,
                            color = CrimsonPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = userProfile.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212529)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

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
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Registration Verified • Registered Once",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detail Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F9FA), RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Donor ID", fontSize = 12.sp, color = Color(0xFF6C757D))
                            Text(userProfile.donorId.ifBlank { "BD-74892" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212529))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Age & Gender", fontSize = 12.sp, color = Color(0xFF6C757D))
                            Text("${userProfile.age} yrs • ${userProfile.gender}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212529))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Anemia Status", fontSize = 12.sp, color = Color(0xFF6C757D))
                            Text(
                                if (userProfile.hasAnemia) "Yes (Anemic)" else "No Anemia (Healthy)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userProfile.hasAnemia) Color(0xFFC62828) else Color(0xFF2E7D32)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Location", fontSize = 12.sp, color = Color(0xFF6C757D))
                            Text(userProfile.location, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212529))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Contact", fontSize = 12.sp, color = Color(0xFF6C757D))
                            Text(userProfile.phone.ifBlank { "Verified" }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212529))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Availability", fontSize = 12.sp, color = Color(0xFF6C757D))
                            Text(
                                if (userProfile.isAvailable) "Active & Available" else "On Break",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userProfile.isAvailable) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Decorative barcode lines
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(Color(0xFFFAFAFA), RoundedCornerShape(6.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(24) { index ->
                            Box(
                                modifier = Modifier
                                    .width(if (index % 3 == 0) 3.dp else if (index % 2 == 0) 2.dp else 1.dp)
                                    .height(18.dp)
                                    .background(Color.DarkGray.copy(alpha = 0.6f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Close", color = Color(0xFF495057))
                        }
                        Button(
                            onClick = {
                                onDismiss()
                                onNavigateToProfile()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                        ) {
                            Text("My Profile", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PulseHeartbeatBanner(
    title: String = "Every Drop Can Save a Life",
    subtitle: String = "Connect blood donors with people in urgent need",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CrimsonPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(CrimsonPrimary, CrimsonPrimaryDark)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Heartbeat Pulse Line Graphic
                    HeartbeatLine(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 3D-styled drop icon with pulse scale
                Box(
                    modifier = Modifier
                        .size((70 * pulseScale).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeartbeatLine(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            val h = size.height
            val w = size.width
            moveTo(0f, h * 0.5f)
            lineTo(w * 0.25f, h * 0.5f)
            lineTo(w * 0.32f, h * 0.15f)
            lineTo(w * 0.40f, h * 0.85f)
            lineTo(w * 0.48f, h * 0.05f)
            lineTo(w * 0.56f, h * 0.70f)
            lineTo(w * 0.62f, h * 0.5f)
            lineTo(w, h * 0.5f)
        }
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.75f),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EmergencyCalloutCard(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CrimsonContainer),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CrimsonPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
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
                    .background(CrimsonPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "This is an Urgent Blood Request",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonOnContainer
                )
                Text(
                    text = "Nearby verified donors will be notified instantly",
                    fontSize = 12.sp,
                    color = CrimsonPrimaryDark
                )
            }
        }
    }
}

@Composable
fun HighDensityAvailabilityCard(
    stockList: List<BloodStockEntity>,
    activeRequest: EmergencyRequestEntity?,
    onAvailabilityClick: () -> Unit,
    onRequestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, HighDensityGray100, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAvailabilityClick),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(CrimsonPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real-time Availability",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                }
                Text(
                    text = "Updated: 2m ago",
                    fontSize = 11.sp,
                    color = HighDensityGray400,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4-column Grid of Blood Groups
            val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
            val stockMap = stockList.associateBy { it.bloodGroup }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Row 1 (A+, A-, B+, B-)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bloodGroups.take(4).forEach { group ->
                        val item = stockMap[group]
                        val status = item?.status?.lowercase() ?: "available"
                        val indicatorColor = when (status) {
                            "available" -> HighDensityGreen
                            "low" -> HighDensityOrange
                            else -> HighDensityRed600
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(HighDensityGray50)
                                .border(1.dp, HighDensityGray100, RoundedCornerShape(12.dp))
                                .clickable(onClick = onAvailabilityClick)
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = group,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextCharcoal
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(indicatorColor)
                                )
                            }
                        }
                    }
                }

                // Row 2 (AB+, AB-, O+, O-)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bloodGroups.drop(4).forEach { group ->
                        val item = stockMap[group]
                        val status = item?.status?.lowercase() ?: "available"
                        val indicatorColor = when (status) {
                            "available" -> HighDensityGreen
                            "low" -> HighDensityOrange
                            else -> HighDensityRed600
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(HighDensityGray50)
                                .border(1.dp, HighDensityGray100, RoundedCornerShape(12.dp))
                                .clickable(onClick = onAvailabilityClick)
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = group,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextCharcoal
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(indicatorColor)
                                )
                            }
                        }
                    }
                }
            }

            // Active Request Callout inside the Card
            if (activeRequest != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(HighDensityRed50)
                        .border(1.dp, HighDensityRed100, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(HighDensityRed200),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityRed600)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ACTIVE REQUEST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "${activeRequest.bloodGroup} Required at ${activeRequest.hospitalName.take(16)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextCharcoal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onRequestClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensityRed600,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "HELP NOW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonorCard(
    donor: DonorEntity,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, HighDensityGray100, RoundedCornerShape(16.dp))
            .testTag("donor_card_${donor.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donor avatar circle with initials & blood icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(HighDensityRed50)
                    .border(1.dp, HighDensityRed100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = donor.name.take(1).uppercase(),
                    color = CrimsonPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = donor.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextCharcoal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${donor.bloodGroup} • ${donor.distanceKm} km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${donor.area}, ${donor.city}",
                        fontSize = 12.sp,
                        color = TextSubtle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(status = if (donor.isAvailable) "Available" else "On Break")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val lat = 18.5204 + donor.latOffset
                        val lng = 73.8567 + donor.lngOffset
                        MapUtils.openGoogleMapsDirections(context, lat, lng, donor.name)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HighDensityRed50)
                        .testTag("maps_donor_${donor.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Directions to ${donor.name}",
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Quick Call Button
                IconButton(
                    onClick = onCallClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE))
                        .testTag("call_donor_${donor.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call ${donor.name}",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FacilityCard(
    facility: FacilityEntity,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, HighDensityGray100, RoundedCornerShape(16.dp))
            .testTag("facility_card_${facility.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (facility.type == "Hospital") Color(0xFFEFF6FF) else HighDensityRed50)
                    .border(
                        1.dp,
                        if (facility.type == "Hospital") Color(0xFFDBEAFE) else HighDensityRed100,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (facility.type == "Hospital") Icons.Default.LocalHospital else Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = if (facility.type == "Hospital") Color(0xFF2563EB) else CrimsonPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = facility.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextCharcoal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${facility.distanceKm} km • ${facility.city}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = CrimsonPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = facility.address,
                    fontSize = 11.sp,
                    color = TextSubtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val lat = 18.5204 + facility.latOffset
                        val lng = 73.8567 + facility.lngOffset
                        MapUtils.openGoogleMapsDirections(context, lat, lng, facility.name)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HighDensityRed50)
                        .testTag("maps_facility_${facility.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Directions to ${facility.name}",
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onCallClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE))
                        .testTag("call_facility_${facility.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call ${facility.name}",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
