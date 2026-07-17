package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String,
    val name: String,
    val age: Int,
    val bio: String,
    val gender: String,
    val avatarUrl: String, // Can be a URL, local image asset name, or emoji ID
    val interests: String, // Comma-separated list
    val relationshipPreference: String,
    val isVerified: Boolean = false,
    val distance: Double = 50.0, // Simulated distance in meters
    val liked: Boolean = false,
    val likedBack: Boolean = false,
    val isIncognito: Boolean = false,
    val isOnline: Boolean = true,
    val isFake: Boolean = false // Admin moderation flag
) : Serializable

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val replyToText: String? = null,
    val reactions: String = "" // Comma-separated list of emojis or empty
) : Serializable

@Entity(tableName = "notifications")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val type: String, // "MATCH", "NEARBY", "SYSTEM"
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
