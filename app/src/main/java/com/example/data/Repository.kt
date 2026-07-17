package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class HeartSyncRepository(private val db: AppDatabase) {

    private val userProfileDao = db.userProfileDao()
    private val chatMessageDao = db.chatMessageDao()
    private val notificationLogDao = db.notificationLogDao()

    // --- Profiles ---
    val allProfilesFlow: Flow<List<UserProfile>> = userProfileDao.getAllProfilesFlow()
    val matchesFlow: Flow<List<UserProfile>> = userProfileDao.getMatchesFlow()

    fun getProfileFlow(id: String): Flow<UserProfile?> = userProfileDao.getProfileFlow(id)

    suspend fun getProfile(id: String): UserProfile? = userProfileDao.getProfile(id)

    suspend fun insertProfile(profile: UserProfile) {
        userProfileDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: UserProfile) {
        userProfileDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: UserProfile) {
        userProfileDao.deleteProfile(profile)
    }

    suspend fun deleteProfileById(id: String) {
        userProfileDao.deleteProfileById(id)
    }

    // --- Messages ---
    fun getConversationFlow(user1: String, user2: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getConversationMessagesFlow(user1, user2)
    }

    suspend fun sendMessage(senderId: String, receiverId: String, text: String, replyToText: String? = null): Long {
        val msg = ChatMessage(
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            replyToText = replyToText
        )
        return chatMessageDao.insertMessage(msg)
    }

    suspend fun addReaction(msg: ChatMessage, emoji: String) {
        val currentReactions = if (msg.reactions.isEmpty()) {
            emoji
        } else {
            val list = msg.reactions.split(",").toMutableList()
            if (!list.contains(emoji)) {
                list.add(emoji)
            }
            list.joinToString(",")
        }
        chatMessageDao.updateMessage(msg.copy(reactions = currentReactions))
    }

    suspend fun deleteMessage(id: Long) {
        chatMessageDao.deleteMessageById(id)
    }

    suspend fun editMessage(msg: ChatMessage, newText: String) {
        chatMessageDao.updateMessage(msg.copy(text = newText))
    }

    // --- Notifications ---
    val allNotificationsFlow: Flow<List<NotificationLog>> = notificationLogDao.getAllNotificationsFlow()

    suspend fun addNotification(title: String, body: String, type: String) {
        notificationLogDao.insertNotification(
            NotificationLog(title = title, body = body, type = type)
        )
    }

    suspend fun clearNotifications() {
        notificationLogDao.clearAllNotifications()
    }

    // --- Database Seeding ---
    suspend fun seedDatabaseIfEmpty() {
        val existingCurrentUser = userProfileDao.getProfile("current_user")
        if (existingCurrentUser == null) {
            // Seed Current User
            val currentUser = UserProfile(
                id = "current_user",
                name = "Aria",
                age = 23,
                bio = "Design enthusiast, amateur photographer, and tech optimist. Let's explore serendipitous connections!",
                gender = "Female",
                avatarUrl = "✨",
                interests = "Design, Tech, Photography, Coffee",
                relationshipPreference = "Long-term",
                isVerified = true,
                distance = 0.0,
                liked = false,
                likedBack = false,
                isIncognito = false,
                isOnline = true
            )
            userProfileDao.insertProfile(currentUser)

            // Seed Simulated Nearby Profiles
            val mockProfiles = listOf(
                UserProfile(
                    id = "nearby_stella",
                    name = "Stella",
                    age = 24,
                    bio = "AI research assistant, sci-fi book nerd, and stargazer. Let's talk about space travel or quantum physics!",
                    gender = "Female",
                    avatarUrl = "🌌",
                    interests = "AI, Reading, Stargazing, Board Games",
                    relationshipPreference = "Friends",
                    isVerified = true,
                    distance = 18.0, // meters away initially
                    liked = false,
                    likedBack = true, // Stella liked back! Meaning when current user likes her, match activates!
                    isIncognito = false,
                    isOnline = true
                ),
                UserProfile(
                    id = "nearby_leo",
                    name = "Leo",
                    age = 26,
                    bio = "Street photographer, vinyl collector, and matcha lover. Always searching for the perfect cinematic shot.",
                    gender = "Male",
                    avatarUrl = "📷",
                    interests = "Photography, Music, Art, Coffee",
                    relationshipPreference = "Casual",
                    isVerified = false,
                    distance = 32.0,
                    liked = false,
                    likedBack = true, // Leo liked back!
                    isIncognito = false,
                    isOnline = true
                ),
                UserProfile(
                    id = "nearby_chloe",
                    name = "Chloe",
                    age = 23,
                    bio = "Weekend hiker, plant parent, and baking explorer. Let's find the best scenic trails in the area.",
                    gender = "Female",
                    avatarUrl = "🌿",
                    interests = "Hiking, Nature, Cooking, Baking",
                    relationshipPreference = "Long-term",
                    isVerified = true,
                    distance = 120.0, // Too far initially (>100m) but will simulate moving closer!
                    liked = false,
                    likedBack = false, // Will require AI conversion starter to convince her to like back!
                    isIncognito = false,
                    isOnline = false
                ),
                UserProfile(
                    id = "nearby_marcus",
                    name = "Marcus",
                    age = 27,
                    bio = "Tech builder, espresso enthusiast, and jazz bass player. High key addicted to coding and vinyl records.",
                    gender = "Male",
                    avatarUrl = "🎸",
                    interests = "Tech, Coding, Music, Coffee",
                    relationshipPreference = "Friends",
                    isVerified = true,
                    distance = 45.0,
                    liked = false,
                    likedBack = true, // Likes back!
                    isIncognito = false,
                    isOnline = true
                )
            )
            userProfileDao.insertProfiles(mockProfiles)

            // Seed a welcome notification
            addNotification(
                title = "Welcome to HeartSync!",
                body = "Your proximity radar is armed. Activate BLE sync to discover anonymous hearts nearby.",
                type = "SYSTEM"
            )
        }
    }
}
