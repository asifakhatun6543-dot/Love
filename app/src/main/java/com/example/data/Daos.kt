package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles ORDER BY id ASC")
    fun getAllProfilesFlow(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    fun getProfileFlow(id: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getProfile(id: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE liked = 1 AND likedBack = 1")
    fun getMatchesFlow(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<UserProfile>)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Delete
    suspend fun deleteProfile(profile: UserProfile)

    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: String)
}

@Dao
interface ChatMessageDao {
    @Query("""
        SELECT * FROM chat_messages 
        WHERE (senderId = :user1 AND receiverId = :user2) 
           OR (senderId = :user2 AND receiverId = :user1) 
        ORDER BY timestamp ASC
    """)
    fun getConversationMessagesFlow(user1: String, user2: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Update
    suspend fun updateMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)
}

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationLog)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}
