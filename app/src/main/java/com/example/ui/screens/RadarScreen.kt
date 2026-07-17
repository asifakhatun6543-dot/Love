package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.HeartSyncViewModel
import com.example.ui.theme.*

@Composable
fun RadarScreen(viewModel: HeartSyncViewModel) {
    val isScanning by viewModel.isScanning.collectAsState()
    val radarRadius by viewModel.radarRadius.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val invisibleMode by viewModel.privacyInvisibleMode.collectAsState()
    val hideDistance by viewModel.privacyHideDistance.collectAsState()

    // Animating sonar rings
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, delayMillis = 1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse2"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Radar Header & Sonar Circle ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmethystCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "HeartSync Radar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = if (invisibleMode) "Invisible Mode Enabled" else "BLE Proximity Sync Broadcast Enabled",
                        fontSize = 12.sp,
                        color = if (invisibleMode) PinkHot else BlueNeon,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Draw Pulsing Sonar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(AmethystMidnight)
                            .clickable { viewModel.startBleRadarScan() }
                    ) {
                        if (isScanning) {
                            // Pulsing glowing rings
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(pulseScale1)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                PinkNeon.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(pulseScale2)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                BlueNeon.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )
                        }

                        // Static sonar grid drawing
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val maxRadius = size.width / 2

                            // Concentric rings
                            drawCircle(color = BorderGlow.copy(alpha = 0.3f), radius = maxRadius * 0.35f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
                            drawCircle(color = BorderGlow.copy(alpha = 0.3f), radius = maxRadius * 0.7f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
                            drawCircle(color = BorderGlow.copy(alpha = 0.3f), radius = maxRadius, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))

                            // Horizontal and vertical axis
                            drawLine(color = BorderGlow.copy(alpha = 0.15f), start = Offset(0f, center.y), end = Offset(size.width, center.y), strokeWidth = 1.dp.toPx())
                            drawLine(color = BorderGlow.copy(alpha = 0.15f), start = Offset(center.x, 0f), end = Offset(center.x, size.height), strokeWidth = 1.dp.toPx())

                            // Simulated sweep line
                            if (isScanning) {
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(PinkNeon.copy(alpha = 0.5f), Color.Transparent),
                                        center = center
                                    ),
                                    startAngle = sweepAngle,
                                    sweepAngle = 60f,
                                    useCenter = true
                                )
                            }
                        }

                        // Core Central Button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(PinkHot, PinkNeon)
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Pulse Sync Button",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = if (isScanning) "SYNCING" else "TAP SYNC",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Location Disclaimer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location status",
                            tint = BlueNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GPS & BLE simulated: Active radius ${radarRadius.toInt()} meters",
                            fontSize = 12.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Radius Adjustment Slider
                    Slider(
                        value = radarRadius,
                        onValueChange = { viewModel.setRadarRadius(it) },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = PinkHot,
                            activeTrackColor = PinkNeon,
                            inactiveTrackColor = BorderGlow
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // --- Nearby Users Header ---
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nearby Sync Matches",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "${allProfiles.size} active inside range",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }

        // --- Simulated User List ---
        if (invisibleMode) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AmethystCard.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Invisible active warning",
                            tint = PinkHot,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Incognito Mode is Enabled",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Disable Incognito in Profile Settings to let neighboring devices discover you.",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else if (allProfiles.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AmethystCard.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No nearby users",
                            tint = BlueNeon,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Searching Nearby Space...",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Tap the central HeartSync button to emit a high-power attraction wave.",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(allProfiles) { profile ->
                val isInsideRadius = profile.distance <= radarRadius
                val distanceText = if (hideDistance) {
                    "Sync range satisfied"
                } else {
                    "${String.format("%.1f", profile.distance)} meters away"
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isInsideRadius) AmethystCard else AmethystCard.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setTab("insights"); viewModel.loadAiInsightsForProfile(profile.id, profile.bio, profile.interests) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Bubble
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .background(GlassWhite, shape = CircleShape)
                        ) {
                            Text(
                                text = profile.avatarUrl,
                                fontSize = 28.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Profile Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${profile.name}, ${profile.age}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (profile.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(BlueNeon, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "✓",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AmethystMidnight
                                        )
                                    }
                                }
                            }

                            Text(
                                text = distanceText,
                                fontSize = 12.sp,
                                color = if (isInsideRadius) PinkNeon else TextGray,
                                fontWeight = if (isInsideRadius) FontWeight.Bold else FontWeight.Medium
                            )

                            Text(
                                text = profile.bio,
                                fontSize = 11.sp,
                                color = TextGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Like Heart Button
                        IconButton(
                            onClick = { viewModel.toggleLike(profile.id) },
                            modifier = Modifier
                                .testTag("like_button_${profile.id}")
                                .size(44.dp)
                                .background(
                                    if (profile.liked) PinkHot.copy(alpha = 0.15f) else Color.Transparent,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (profile.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like Profile",
                                tint = if (profile.liked) PinkHot else TextGray,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
