package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
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
import com.example.data.ChatMessage
import com.example.data.UserProfile
import com.example.ui.HeartSyncViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(viewModel: HeartSyncViewModel) {
    val partner by viewModel.activeChatPartner.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val aiIcebreakers by viewModel.aiIcebreakers.collectAsState()
    val aiIcebreakersLoading by viewModel.aiIcebreakersLoading.collectAsState()

    val aiCompatibility by viewModel.aiCompatibility.collectAsState()
    val aiCompatibilityLoading by viewModel.aiCompatibilityLoading.collectAsState()

    var showAiAssistant by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var replyToMsgText by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val currentPartner = partner

    if (currentPartner == null) {
        // No conversation active state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "No active conversation",
                    tint = TextGray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Conversations Locked",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "A mutual Attraction Sync must occur inside radar range before a chat room unlocks.",
                    fontSize = 13.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.setTab("radar") },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkHot)
                ) {
                    Text("Return to Radar Scan")
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Custom Chat Header ---
        Card(
            colors = CardDefaults.cardColors(containerColor = AmethystCard),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setChatPartner(null); viewModel.setTab("matches") }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .background(GlassWhite, shape = CircleShape)
                ) {
                    Text(text = currentPartner.avatarUrl, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentPartner.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (currentPartner.isOnline) BlueNeon else Color.Gray, shape = CircleShape)
                        )
                    }
                    Text(
                        text = if (currentPartner.isOnline) "Active in radar sweep" else "Offline",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }

                // AI Matchmaker Trigger Button
                Button(
                    onClick = { showAiAssistant = !showAiAssistant },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAiAssistant) PinkHot else BlueNeon.copy(alpha = 0.15f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Matchmaker Drawer",
                        tint = if (showAiAssistant) Color.White else BlueNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AI Match",
                        fontSize = 12.sp,
                        color = if (showAiAssistant) Color.White else BlueNeon,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- AI Matchmaker Drawer Content (Expandable Panel) ---
        AnimatedVisibility(
            visible = showAiAssistant,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmethystDeep),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = BlueNeon, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "HeartSync AI Assistant", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        IconButton(onClick = { showAiAssistant = false }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Drawer", tint = TextGray, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        // Compatibility Analyzer Button
                        Button(
                            onClick = { viewModel.fetchAiCompatibility(currentPartner) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmethystCard),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "AI Match Score", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Icebreaker Generator Button
                        Button(
                            onClick = { viewModel.fetchAiIcebreakers(currentPartner) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmethystCard),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "AI Conversation Starters", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Loading Indicators
                    if (aiCompatibilityLoading || aiIcebreakersLoading) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            CircularProgressIndicator(color = PinkHot, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Consulting HeartSync AI Model...", fontSize = 12.sp, color = TextGray)
                        }
                    }

                    // Compatibility Response View
                    aiCompatibility?.let { (score, analysis) ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PinkHot.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(PinkHot, shape = CircleShape)
                            ) {
                                Text(text = "$score%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(text = "AI Compatibility Insights", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PinkNeon)
                                Text(text = analysis, fontSize = 11.sp, color = TextWhite, lineHeight = 14.sp)
                            }
                        }
                    }

                    // Icebreakers Response View
                    if (aiIcebreakers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "AI Conversation Starters (Tap to use)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BlueNeon)
                            for (icebreaker in aiIcebreakers) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = AmethystMidnight),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            textInput = icebreaker
                                            showAiAssistant = false
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = "Use starter", tint = BlueNeon, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = icebreaker, fontSize = 11.sp, color = TextWhite)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Chat Message Stream Logs ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == "current_user"
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = sdf.format(Date(msg.timestamp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    // Replied-To Message Header View
                    if (msg.replyToText != null) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 2.dp, start = if (isMe) 24.dp else 0.dp, end = if (isMe) 0.dp else 24.dp)
                                .background(
                                    color = if (isMe) AmethystCard.copy(alpha = 0.5f) else GlassWhite,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Reply, contentDescription = "Reply to", tint = TextGray, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = msg.replyToText,
                                    fontSize = 10.sp,
                                    color = TextGray,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Chat Bubble Row
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!isMe) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(GlassWhite, shape = CircleShape)
                            ) {
                                Text(text = currentPartner.avatarUrl, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Message bubble body
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) PinkHot else AmethystCard
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            modifier = Modifier
                                .widthIn(max = 260.dp)
                                .clickable {
                                    // Tap message to reply instantly
                                    replyToMsgText = msg.text
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp
                                )

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = formattedTime,
                                        fontSize = 10.sp,
                                        color = if (isMe) TextWhite.copy(alpha = 0.7f) else TextGray
                                    )
                                    if (isMe) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = "Read status",
                                            tint = BlueNeon,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Emoji Reaction overlay
                        if (msg.reactions.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .offset(y = 10.dp, x = if (isMe) (-12).dp else 12.dp)
                                    .background(AmethystDeep, shape = CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = msg.reactions, fontSize = 10.sp)
                            }
                        }

                        // Options for Reactions
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clickable { viewModel.addEmojiReaction(msg, "❤️") }
                        ) {
                            Text(text = "❤️", fontSize = 12.sp, modifier = Modifier.padding(2.dp))
                        }
                    }
                }
            }
        }

        // --- Replied message review footer ---
        replyToMsgText?.let { reply ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AmethystDeep)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Reply, contentDescription = "Replying", tint = PinkHot, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Replying to: '$reply'",
                    color = TextGray,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                IconButton(onClick = { replyToMsgText = null }, modifier = Modifier.size(20.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel reply", tint = TextGray, modifier = Modifier.size(14.dp))
                }
            }
        }

        // --- Message Input Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AmethystCard)
                .navigationBarsPadding() // Keep clear of Android systems nav indicators!
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Typing Indicator
            IconButton(onClick = { textInput = "I love this match! let's meet up 💓" }) {
                Icon(imageVector = Icons.Default.SentimentSatisfiedAlt, contentDescription = "Emoji select", tint = TextGray)
            }

            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text(text = "Message ${currentPartner.name}...", color = TextGray) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = AmethystMidnight,
                    unfocusedContainerColor = AmethystMidnight,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .testTag("chat_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendChatMessage(textInput, replyToMsgText)
                        textInput = ""
                        replyToMsgText = null
                    }
                },
                modifier = Modifier
                    .testTag("send_message_button")
                    .size(44.dp)
                    .background(PinkHot, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Send,
                    contentDescription = "Send message",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
