package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ControlPoint
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.data.FirebaseManager
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Settings
import com.example.ui.HeartSyncViewModel
import com.example.ui.theme.*

@Composable
fun AdminScreen(viewModel: HeartSyncViewModel) {
    val allProfiles by viewModel.allProfiles.collectAsState()
    val radarRadius by viewModel.radarRadius.collectAsState()
    val reportsLog by viewModel.reportsLog.collectAsState()

    val context = LocalContext.current
    val isInitialized by FirebaseManager.isInitialized.collectAsState()
    val isRealFirebase by FirebaseManager.isRealFirebase.collectAsState()
    val firebaseStatus by FirebaseManager.statusMessage.collectAsState()
    val customApiKey by FirebaseManager.customApiKey.collectAsState()
    val customProjectId by FirebaseManager.customProjectId.collectAsState()
    val customAppId by FirebaseManager.customAppId.collectAsState()

    var localApiKey by remember(customApiKey) { mutableStateOf(customApiKey) }
    var localProjectId by remember(customProjectId) { mutableStateOf(customProjectId) }
    var localAppId by remember(customAppId) { mutableStateOf(customAppId) }
    var showManualConfig by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Admin Header ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmethystCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.BugReport, contentDescription = "Developer", tint = PinkHot)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Simulation Control Panel", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text(
                        text = "Use these debug tools to manually trigger and test attraction alarms, BLE pulses, and distance configurations without physical walking.",
                        fontSize = 11.sp,
                        color = TextGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // --- Firebase Integration Config Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmethystCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Firebase Sync",
                                tint = BlueNeon,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firebase Cloud Engine",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Status badge
                        val badgeBg = if (isRealFirebase) PinkHot.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)
                        val badgeColor = if (isRealFirebase) PinkHot else TextGray
                        val badgeText = if (isRealFirebase) "CLOUD ACTIVE" else "SIMULATOR"

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeBg,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = firebaseStatus,
                        fontSize = 12.sp,
                        color = if (isRealFirebase) BlueNeon else TextGray,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expandable config trigger
                    OutlinedButton(
                        onClick = { showManualConfig = !showManualConfig },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configure",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (showManualConfig) "Hide Configuration" else "Configure Custom Credentials",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (showManualConfig) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "To sync with your live project, enter Firebase parameters below:",
                            fontSize = 11.sp,
                            color = TextGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // API Key
                        OutlinedTextField(
                            value = localApiKey,
                            onValueChange = { localApiKey = it },
                            label = { Text("API Key", fontSize = 11.sp, color = TextGray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PinkNeon,
                                unfocusedBorderColor = BorderGlow
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Project ID
                        OutlinedTextField(
                            value = localProjectId,
                            onValueChange = { localProjectId = it },
                            label = { Text("Project ID", fontSize = 11.sp, color = TextGray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PinkNeon,
                                unfocusedBorderColor = BorderGlow
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // App ID
                        OutlinedTextField(
                            value = localAppId,
                            onValueChange = { localAppId = it },
                            label = { Text("Application ID (App ID)", fontSize = 11.sp, color = TextGray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PinkNeon,
                                unfocusedBorderColor = BorderGlow
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val success = FirebaseManager.initializeWithManualOptions(
                                        context = context,
                                        apiKey = localApiKey,
                                        projectId = localProjectId,
                                        appId = localAppId
                                    )
                                    if (success) {
                                        showManualConfig = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PinkHot),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text("Connect Cloud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    FirebaseManager.resetToSimulation(context)
                                    localApiKey = ""
                                    localProjectId = ""
                                    localAppId = ""
                                    showManualConfig = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AmethystMidnight),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset / Local", fontSize = 12.sp, color = TextWhite)
                            }
                        }
                    }
                }
            }
        }

        // --- Stats row ---
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AmethystCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(text = "BLE Radius", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                        Text(text = "${radarRadius.toInt()}m", fontSize = 18.sp, color = BlueNeon, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = AmethystCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(text = "Local Nodes", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                        Text(text = "${allProfiles.size}", fontSize = 18.sp, color = PinkNeon, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // --- Distance Manipulator Section ---
        item {
            Text(
                text = "Manually Override Node Proximity",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        items(allProfiles) { profile ->
            Card(
                colors = CardDefaults.cardColors(containerColor = AmethystCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .background(GlassWhite, shape = CircleShape)
                    ) {
                        Text(text = profile.avatarUrl, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Current: ${String.format("%.1f", profile.distance)}m",
                            fontSize = 11.sp,
                            color = if (profile.distance <= radarRadius) PinkHot else TextGray,
                            fontWeight = if (profile.distance <= radarRadius) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // Predefined Quick Proximity Triggers
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { viewModel.adminMockNearbyUser(profile.id, 8.0) },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkHot),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("teleport_near_${profile.id}")
                        ) {
                            Text(text = "Near (8m)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.adminMockNearbyUser(profile.id, 65.0) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmethystMidnight),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(text = "Far (65m)", fontSize = 10.sp, color = TextWhite)
                        }
                    }
                }
            }
        }

        // --- System Logs / Guardrail History ---
        item {
            Text(
                text = "Moderation Guardrails & BLE Telemetry",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmethystCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.List, contentDescription = "Logs", tint = BlueNeon, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Real-Time Security Event Logs", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (log in reportsLog) {
                            Text(
                                text = "• $log",
                                fontSize = 11.sp,
                                color = TextGray,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
