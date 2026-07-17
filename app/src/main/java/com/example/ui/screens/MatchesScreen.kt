package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HeartSyncViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MatchesScreen(viewModel: HeartSyncViewModel) {
    val matches by viewModel.matches.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var activeSubTab by remember { mutableStateOf("matches") } // "matches" or "notifications"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Tab Switcher ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AmethystCard, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { activeSubTab = "matches" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubTab == "matches") PinkHot else Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Matches (${matches.size})",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { activeSubTab = "notifications" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubTab == "notifications") PinkHot else Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Radar Logs",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- SUBTAB: Matches ---
        if (activeSubTab == "matches") {
            if (matches.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmethystCard.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "No matches",
                                tint = TextGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Attraction Syncs Yet",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "A HeartSync triggers only when you and another nearby user like each other inside BLE sync range.",
                                color = TextGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(matches) { match ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmethystCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setChatPartner(match)
                                viewModel.setTab("chat")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar with glowing border
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(PinkNeon.copy(alpha = 0.4f), Color.Transparent)
                                        ),
                                        shape = CircleShape
                                    )
                            ) {
                                Text(
                                    text = match.avatarUrl,
                                    fontSize = 32.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = match.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Click to open secure chat",
                                    fontSize = 12.sp,
                                    color = BlueNeon
                                )
                                Text(
                                    text = match.bio,
                                    fontSize = 11.sp,
                                    color = TextGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Chat Icon Indicator
                            IconButton(
                                onClick = {
                                    viewModel.setChatPartner(match)
                                    viewModel.setTab("chat")
                                },
                                modifier = Modifier
                                    .testTag("chat_button_${match.id}")
                                    .size(40.dp)
                                    .background(PinkHot.copy(alpha = 0.12f), shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Open Chat",
                                    tint = PinkHot,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SUBTAB: Radar Logs (Notifications) ---
        if (activeSubTab == "notifications") {
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "History & Sync Alerts",
                        fontSize = 14.sp,
                        color = TextGray,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Clear All",
                        fontSize = 12.sp,
                        color = PinkHot,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.clearNotifications() }
                            .padding(4.dp)
                    )
                }
            }

            if (notifications.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmethystCard.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "No notifications",
                                tint = TextGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Radar Logs Empty",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Any proximity warnings or system alerts will appear logged here in chronological sequence.",
                                color = TextGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(notifications) { log ->
                    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val formattedTime = sdf.format(Date(log.timestamp))

                    val (color, icon) = when (log.type) {
                        "MATCH" -> Pair(PinkHot, Icons.Default.Favorite)
                        "NEARBY" -> Pair(BlueNeon, Icons.Default.Info)
                        else -> Pair(TextGray, Icons.Default.Notifications)
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmethystCard.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(color.copy(alpha = 0.1f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = log.type,
                                    tint = color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = log.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = formattedTime,
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                                Text(
                                    text = log.body,
                                    fontSize = 12.sp,
                                    color = TextGray,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
