package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HeartSyncViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.data.FirebaseManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseManager.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            HeartSyncTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer() {
    val viewModel: HeartSyncViewModel = viewModel()
    val currentRoute by viewModel.currentRoute.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val celebrationProfile by viewModel.showMatchCelebration.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentRoute) {
            "splash" -> {
                SplashScreen(onGetStarted = { viewModel.setRoute("dashboard") })
            }
            "dashboard" -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = AmethystCard,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentTab == "radar",
                                onClick = { viewModel.setTab("radar") },
                                label = { Text("Radar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                icon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Radar") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PinkHot,
                                    selectedTextColor = PinkHot,
                                    indicatorColor = GlassWhite,
                                    unselectedIconColor = TextGray,
                                    unselectedTextColor = TextGray
                                ),
                                modifier = Modifier.testTag("nav_tab_radar")
                            )

                            NavigationBarItem(
                                selected = currentTab == "matches" || currentTab == "chat",
                                onClick = { viewModel.setTab("matches") },
                                label = { Text("Inbox", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                icon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Inbox") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PinkHot,
                                    selectedTextColor = PinkHot,
                                    indicatorColor = GlassWhite,
                                    unselectedIconColor = TextGray,
                                    unselectedTextColor = TextGray
                                ),
                                modifier = Modifier.testTag("nav_tab_inbox")
                            )

                            NavigationBarItem(
                                selected = currentTab == "profile",
                                onClick = { viewModel.setTab("profile") },
                                label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PinkHot,
                                    selectedTextColor = PinkHot,
                                    indicatorColor = GlassWhite,
                                    unselectedIconColor = TextGray,
                                    unselectedTextColor = TextGray
                                ),
                                modifier = Modifier.testTag("nav_tab_profile")
                            )

                            NavigationBarItem(
                                selected = currentTab == "admin",
                                onClick = { viewModel.setTab("admin") },
                                label = { Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                icon = { Icon(imageVector = Icons.Default.BugReport, contentDescription = "Admin") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PinkHot,
                                    selectedTextColor = PinkHot,
                                    indicatorColor = GlassWhite,
                                    unselectedIconColor = TextGray,
                                    unselectedTextColor = TextGray
                                ),
                                modifier = Modifier.testTag("nav_tab_admin")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AmethystMidnight)
                            .padding(innerPadding)
                            .statusBarsPadding()
                    ) {
                        when (currentTab) {
                            "radar" -> RadarScreen(viewModel)
                            "matches" -> MatchesScreen(viewModel)
                            "chat" -> ChatScreen(viewModel)
                            "profile" -> ProfileScreen(viewModel)
                            "admin" -> AdminScreen(viewModel)
                        }
                    }
                }
            }
        }

        // --- Core Celebratory Love Alarm Match Overlay Dialog ---
        AnimatedVisibility(
            visible = celebrationProfile != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut()
        ) {
            val matchedUser = celebrationProfile
            if (matchedUser != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.92f))
                        .clickable { viewModel.dismissMatchCelebration() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Flashing Celebration Star Icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(PinkNeon.copy(alpha = 0.45f), Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Sparkles",
                                tint = PinkHot,
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "It's a HeartSync! 💓",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Your proximity radar detected a mutual attraction synchronization!",
                            fontSize = 14.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Interlocking Avatars View
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Current User avatar
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(GlassWhite, shape = CircleShape)
                            ) {
                                Text(text = "✨", fontSize = 40.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Sync Connection",
                                tint = PinkHot,
                                modifier = Modifier.size(40.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Matched User avatar
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(GlassWhite, shape = CircleShape)
                            ) {
                                Text(text = matchedUser.avatarUrl, fontSize = 40.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "You and ${matchedUser.name} synchronized at close range!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        // Active CTAs
                        Button(
                            onClick = {
                                viewModel.setChatPartner(matchedUser)
                                viewModel.setTab("chat")
                                viewModel.dismissMatchCelebration()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkHot),
                            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 14.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("celebration_open_chat")
                        ) {
                            Text("Open Secure Chat Room", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = { viewModel.dismissMatchCelebration() }) {
                            Text("Continue Discovering", color = TextGray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
