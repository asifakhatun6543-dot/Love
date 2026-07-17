package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HeartSyncViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest

@Composable
fun ProfileScreen(viewModel: HeartSyncViewModel) {
    val currentUser by viewModel.currentUserProfile.collectAsState()
    val invisibleMode by viewModel.privacyInvisibleMode.collectAsState()
    val hideDistance by viewModel.privacyHideDistance.collectAsState()
    val premiumUnlocked by viewModel.premiumUnlocked.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Permissions & diagnostics states
    val bluetoothPermission by viewModel.bluetoothPermission.collectAsState()
    val gpsPermission by viewModel.gpsPermission.collectAsState()
    val geofencingPermission by viewModel.geofencingPermission.collectAsState()
    val backgroundDetection by viewModel.backgroundDetection.collectAsState()
    val batteryOptimization by viewModel.batteryOptimization.collectAsState()
    val mobileDisplayPermission by viewModel.mobileDisplayPermission.collectAsState()
    val offlineCache by viewModel.offlineCache.collectAsState()
    val liveDistanceUpdates by viewModel.liveDistanceUpdates.collectAsState()
    val radarRadius by viewModel.radarRadius.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        permissionsMap.forEach { (perm, isGranted) ->
            when (perm) {
                Manifest.permission.ACCESS_FINE_LOCATION -> viewModel.toggleGpsPermission(isGranted)
                Manifest.permission.BLUETOOTH_SCAN -> viewModel.toggleBluetoothPermission(isGranted)
            }
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }

    // Local input states for editing
    var inputName by remember { mutableStateOf("") }
    var inputAge by remember { mutableStateOf("") }
    var inputBio by remember { mutableStateOf("") }
    var inputInterests by remember { mutableStateOf("") }

    val user = currentUser

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PinkHot)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Core Profile Avatar & Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmethystCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Glowing Profile Avatar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(PinkNeon.copy(alpha = 0.35f), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(80.dp)
                                .background(GlassWhite, shape = CircleShape)
                        ) {
                            Text(text = user.avatarUrl, fontSize = 48.sp)
                        }

                        // Little verification seal
                        if (user.isVerified) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(BlueNeon, shape = CircleShape)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-4).dp, y = (-4).dp)
                            ) {
                                Text(text = "✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmethystMidnight)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${user.name}, ${user.age}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "HeartSync Verified Discoverer",
                        fontSize = 12.sp,
                        color = BlueNeon,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user.bio,
                        fontSize = 13.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic Interest Chips
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val list = user.interests.split(",").map { it.trim() }
                        for (interest in list) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .background(GlassWhite, shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = interest, fontSize = 11.sp, color = TextWhite)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            inputName = user.name
                            inputAge = user.age.toString()
                            inputBio = user.bio
                            inputInterests = user.interests
                            showEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PinkHot),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Profile Details")
                    }
                }
            }
        }

        // --- Premium VIP Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (premiumUnlocked) AmethystCard else AmethystCard.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (premiumUnlocked) PinkNeon.copy(alpha = 0.15f) else GlassWhite,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "VIP Status",
                            tint = if (premiumUnlocked) PinkNeon else TextGray,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (premiumUnlocked) "HeartSync Premium Active" else "Upgrade to HeartSync Premium",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (premiumUnlocked) "Enjoy priority matching & unlimited daily syncs." else "Unlock incognito discovery, priority matching, & mock location overrides.",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }

                    if (!premiumUnlocked) {
                        Button(
                            onClick = { viewModel.purchasePremium() },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkNeon),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("premium_upgrade_button")
                        ) {
                            Text("Join VIP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Privacy & Discoverability Panel ---
        item {
            Text(
                text = "Privacy & Discoverability",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmethystCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Invisible Mode Toggle
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Invisible / Incognito Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Hides your signature from surrounding radar syncs.", fontSize = 11.sp, color = TextGray)
                        }
                        Switch(
                            checked = invisibleMode,
                            onCheckedChange = { viewModel.toggleInvisibleMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PinkHot, checkedTrackColor = PinkNeon.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hide Distance Toggle
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Obfuscate Distance Meters", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Shows 'satisfied' instead of exact distance meters.", fontSize = 11.sp, color = TextGray)
                        }
                        Switch(
                            checked = hideDistance,
                            onCheckedChange = { viewModel.toggleHideDistance(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PinkHot, checkedTrackColor = PinkNeon.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Wipe cache / DB reset button
                    Button(
                        onClick = { viewModel.adminResetDatabase() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Wipe", tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Simulation Database", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Engine, Permissions & Diagnostics ---
        item {
            Text(
                text = "Engine, Permissions & Diagnostics",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmethystCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Nearby Discovery
                    PermissionRow(
                        icon = Icons.Default.Search,
                        title = "Nearby Discovery",
                        description = "Enables proximity signals to detect match signatures in physical proximity.",
                        statusText = if (bluetoothPermission && gpsPermission) "ACTIVE" else "LIMITED",
                        statusColor = if (bluetoothPermission && gpsPermission) BlueNeon else Color.Yellow,
                        checked = bluetoothPermission && gpsPermission,
                        onCheckedChange = { isChecked ->
                            viewModel.toggleBluetoothPermission(isChecked)
                            viewModel.toggleGpsPermission(isChecked)
                            if (isChecked) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.BLUETOOTH_SCAN,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                )
                            }
                        },
                        testTag = "perm_nearby_discovery"
                    )

                    HorizontalDivider(color = BorderGlow.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // Bluetooth Low Energy
                    PermissionRow(
                        icon = Icons.Default.Share,
                        title = "Bluetooth Low Energy",
                        description = "Emits and scans high-frequency BLE beacon advertisements.",
                        statusText = if (bluetoothPermission) "AUTHORIZED" else "DISABLED",
                        statusColor = if (bluetoothPermission) PinkHot else TextGray,
                        checked = bluetoothPermission,
                        onCheckedChange = { isChecked ->
                            viewModel.toggleBluetoothPermission(isChecked)
                            if (isChecked) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.BLUETOOTH_SCAN,
                                        Manifest.permission.BLUETOOTH_ADVERTISE,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    )
                                )
                            }
                        },
                        testTag = "perm_bluetooth_le"
                    )

                    HorizontalDivider(color = BorderGlow.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // GPS Precision Tracking
                    PermissionRow(
                        icon = Icons.Default.LocationOn,
                        title = "GPS Precision Tracking",
                        description = "Queries device location coordinates to measure physical distance meters.",
                        statusText = if (gpsPermission) "AUTHORIZED" else "REVOKED",
                        statusColor = if (gpsPermission) BlueNeon else TextGray,
                        checked = gpsPermission,
                        onCheckedChange = { isChecked ->
                            viewModel.toggleGpsPermission(isChecked)
                            if (isChecked) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        testTag = "perm_gps_location"
                    )

                    HorizontalDivider(color = BorderGlow.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // Geofencing Boundaries
                    PermissionRow(
                        icon = Icons.Default.Place,
                        title = "Geofencing Boundaries",
                        description = "Triggers local entry/exit push events near target device beacons.",
                        statusText = if (geofencingPermission) "AUTHORIZED" else "DISABLED",
                        statusColor = if (geofencingPermission) BlueNeon else TextGray,
                        checked = geofencingPermission,
                        onCheckedChange = { isChecked ->
                            viewModel.toggleGeofencingPermission(isChecked)
                            if (isChecked) {
                                permissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                )
                            }
                        },
                        testTag = "perm_geofencing"
                    )

                    HorizontalDivider(color = BorderGlow.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // Background Detection
                    PermissionRow(
                        icon = Icons.Default.Refresh,
                        title = "Background Detection",
                        description = "Sustains beacon scans when the app is completely backgrounded.",
                        statusText = if (backgroundDetection) "RUNNING" else "STOPPED",
                        statusColor = if (backgroundDetection) PinkHot else TextGray,
                        checked = backgroundDetection,
                        onCheckedChange = { viewModel.toggleBackgroundDetection(it) },
                        testTag = "perm_background_detection"
                    )

                    HorizontalDivider(color = BorderGlow.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // Adjustable Distance Radius (Display + Adjust Slider)
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(BlueNeon.copy(alpha = 0.12f), shape = CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Adjustable Distance Radius",
                                        tint = BlueNeon,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Adjustable Distance Radius",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Active radar searching scope range.",
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                            }
                            Text(
                                text = "${radarRadius.toInt()}m",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BlueNeon
                            )
                        }
                        Slider(
                            value = radarRadius,
                            onValueChange = { viewModel.setRadarRadius(it) },
                            valueRange = 10f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = PinkHot,
                                activeTrackColor = PinkNeon,
                                inactiveTrackColor = BorderGlow
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    HorizontalDivider(color = BorderGlow.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // Live Distance Updates
                    PermissionRow(
                        icon = Icons.Default.PlayArrow,
                        title = "Live Distance Updates",
                        description = "Enables real-time drifting and calculations of nearby targets.",
                        statusText = if (liveDistanceUpdates) "LIVE FEED ACTIVE" else "PAUSED",
                        statusColor = if (liveDistanceUpdates) BlueNeon else TextGray,
                        checked = liveDistanceUpdates,
                        onCheckedChange = { viewModel.toggleLiveDistanceUpdates(it) },
                        testTag = "perm_live_distance"
                    )

                    HorizontalDivider(color = BorderGlow.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // Battery Optimization Override
                    PermissionRow(
                        icon = Icons.Default.Warning,
                        title = "Battery Optimization Override",
                        description = "Exempts app from system standby and sleep limits.",
                        statusText = if (batteryOptimization) "WHITELISTED" else "OPTIMIZED",
                        statusColor = if (batteryOptimization) PinkHot else TextGray,
                        checked = batteryOptimization,
                        onCheckedChange = { viewModel.toggleBatteryOptimization(it) },
                        testTag = "perm_battery_opt"
                    )

                    HorizontalDivider(color = BorderGlow.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // Offline Detection Cache
                    PermissionRow(
                        icon = Icons.Default.Build,
                        title = "Offline Detection Cache (Room)",
                        description = "Saves radar profiles in local encrypted Room SQLite cache.",
                        statusText = if (offlineCache) "OFFLINE CACHE ACTIVE" else "LIVE-ONLY",
                        statusColor = if (offlineCache) BlueNeon else TextGray,
                        checked = offlineCache,
                        onCheckedChange = { viewModel.toggleOfflineCache(it) },
                        testTag = "perm_offline_cache"
                    )

                    HorizontalDivider(color = BorderGlow.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // Mobile Display Overlay Permission
                    PermissionRow(
                        icon = Icons.Default.Notifications,
                        title = "Mobile Display Overlay",
                        description = "Allows love alarms to trigger alert notifications over other apps.",
                        statusText = if (mobileDisplayPermission) "GRANTED" else "DENIED",
                        statusColor = if (mobileDisplayPermission) PinkHot else TextGray,
                        checked = mobileDisplayPermission,
                        onCheckedChange = { viewModel.toggleMobileDisplayPermission(it) },
                        testTag = "perm_mobile_display"
                    )
                }
            }
        }
    }

    // --- Profile Editing Dialog ---
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(text = "Edit Discoverer Profile", color = Color.White) },
            containerColor = AmethystDeep,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Discoverer Name") },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = AmethystCard, unfocusedContainerColor = AmethystCard)
                    )
                    TextField(
                        value = inputAge,
                        onValueChange = { inputAge = it },
                        label = { Text("Age") },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = AmethystCard, unfocusedContainerColor = AmethystCard)
                    )
                    TextField(
                        value = inputBio,
                        onValueChange = { inputBio = it },
                        label = { Text("Short Bio") },
                        maxLines = 3,
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = AmethystCard, unfocusedContainerColor = AmethystCard)
                    )
                    TextField(
                        value = inputInterests,
                        onValueChange = { inputInterests = it },
                        label = { Text("Interests (comma separated)") },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = AmethystCard, unfocusedContainerColor = AmethystCard)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ageInt = inputAge.toIntOrNull() ?: user.age
                        coroutineScope.launch {
                            viewModel.updateProfile(
                                user.copy(
                                    name = inputName,
                                    age = ageInt,
                                    bio = inputBio,
                                    interests = inputInterests
                                )
                            )
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkHot)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    statusText: String,
    statusColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(statusColor.copy(alpha = 0.12f), shape = CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = statusColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = description,
                fontSize = 11.sp,
                color = TextGray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PinkHot,
                checkedTrackColor = PinkNeon.copy(alpha = 0.5f),
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = AmethystMidnight
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
