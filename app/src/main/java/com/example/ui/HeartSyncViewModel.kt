package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class HeartSyncViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HeartSyncRepository(db)

    // --- Routing & Navigation ---
    private val _currentRoute = MutableStateFlow("splash")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    private val _currentTab = MutableStateFlow("radar")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // --- UI Controls & Dialog States ---
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _showMatchCelebration = MutableStateFlow<UserProfile?>(null)
    val showMatchCelebration: StateFlow<UserProfile?> = _showMatchCelebration.asStateFlow()

    private val _activeChatPartner = MutableStateFlow<UserProfile?>(null)
    val activeChatPartner: StateFlow<UserProfile?> = _activeChatPartner.asStateFlow()

    // --- AI Feature States ---
    private val _aiIcebreakers = MutableStateFlow<List<String>>(emptyList())
    val aiIcebreakers: StateFlow<List<String>> = _aiIcebreakers.asStateFlow()

    private val _aiIcebreakersLoading = MutableStateFlow(false)
    val aiIcebreakersLoading: StateFlow<Boolean> = _aiIcebreakersLoading.asStateFlow()

    private val _aiCompatibility = MutableStateFlow<Pair<Int, String>?>(null)
    val aiCompatibility: StateFlow<Pair<Int, String>?> = _aiCompatibility.asStateFlow()

    private val _aiCompatibilityLoading = MutableStateFlow(false)
    val aiCompatibilityLoading: StateFlow<Boolean> = _aiCompatibilityLoading.asStateFlow()

    private val _aiInsights = MutableStateFlow<Map<String, String>>(emptyMap())
    val aiInsights: StateFlow<Map<String, String>> = _aiInsights.asStateFlow()

    // --- User Profile & Database Streams ---
    val currentUserProfile: StateFlow<UserProfile?> = repository.getProfileFlow("current_user")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allProfiles: StateFlow<List<UserProfile>> = repository.allProfilesFlow
        .map { list -> list.filter { it.id != "current_user" && !it.isFake } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matches: StateFlow<List<UserProfile>> = repository.matchesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationLog>> = repository.allNotificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Message stream depends on active chat partner
    val messages: StateFlow<List<ChatMessage>> = _activeChatPartner
        .flatMapLatest { partner ->
            if (partner == null) {
                flowOf(emptyList())
            } else {
                repository.getConversationFlow("current_user", partner.id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Settings & Simulation Configurations ---
    private val _radarRadius = MutableStateFlow(30f) // meters
    val radarRadius: StateFlow<Float> = _radarRadius.asStateFlow()

    private val _privacyInvisibleMode = MutableStateFlow(false)
    val privacyInvisibleMode: StateFlow<Boolean> = _privacyInvisibleMode.asStateFlow()

    private val _privacyHideDistance = MutableStateFlow(false)
    val privacyHideDistance: StateFlow<Boolean> = _privacyHideDistance.asStateFlow()

    private val _premiumUnlocked = MutableStateFlow(false)
    val premiumUnlocked: StateFlow<Boolean> = _premiumUnlocked.asStateFlow()

    private val _radarPingsCount = MutableStateFlow(0)
    val radarPingsCount: StateFlow<Int> = _radarPingsCount.asStateFlow()

    // Reports / Moderation logs
    private val _reportsLog = MutableStateFlow<List<String>>(listOf(
        "Stella (AI Shield): Clean background telemetry",
        "Leo (AI Shield): Dynamic profile verification verified",
        "System: Multi-device session authorized successfully"
    ))
    val reportsLog: StateFlow<List<String>> = _reportsLog.asStateFlow()

    // --- Permissions & Diagnostics States ---
    private val _bluetoothPermission = MutableStateFlow(true)
    val bluetoothPermission = _bluetoothPermission.asStateFlow()

    private val _gpsPermission = MutableStateFlow(true)
    val gpsPermission = _gpsPermission.asStateFlow()

    private val _geofencingPermission = MutableStateFlow(true)
    val geofencingPermission = _geofencingPermission.asStateFlow()

    private val _backgroundDetection = MutableStateFlow(true)
    val backgroundDetection = _backgroundDetection.asStateFlow()

    private val _batteryOptimization = MutableStateFlow(true)
    val batteryOptimization = _batteryOptimization.asStateFlow()

    private val _mobileDisplayPermission = MutableStateFlow(true)
    val mobileDisplayPermission = _mobileDisplayPermission.asStateFlow()

    private val _offlineCache = MutableStateFlow(true)
    val offlineCache = _offlineCache.asStateFlow()

    private val _liveDistanceUpdates = MutableStateFlow(true)
    val liveDistanceUpdates = _liveDistanceUpdates.asStateFlow()

    private fun addReportLog(message: String) {
        _reportsLog.update { current ->
            listOf(message, *current.toTypedArray())
        }
    }

    fun toggleBluetoothPermission(granted: Boolean) {
        _bluetoothPermission.value = granted
        addReportLog("System: Bluetooth LE state updated -> " + if (granted) "AUTHORIZED" else "REVOKED")
    }

    fun toggleGpsPermission(granted: Boolean) {
        _gpsPermission.value = granted
        addReportLog("System: GPS / Coarse location -> " + if (granted) "AUTHORIZED" else "REVOKED")
    }

    fun toggleGeofencingPermission(granted: Boolean) {
        _geofencingPermission.value = granted
        addReportLog("System: Geofencing Background Location -> " + if (granted) "AUTHORIZED" else "REVOKED")
    }

    fun toggleBackgroundDetection(granted: Boolean) {
        _backgroundDetection.value = granted
        addReportLog("System: Background BLE scanning service -> " + if (granted) "ACTIVE" else "STOPPED")
    }

    fun toggleBatteryOptimization(disabled: Boolean) {
        _batteryOptimization.value = disabled
        addReportLog("System: Battery optimization whitelist -> " + if (disabled) "WHITELISTED" else "OPTIMIZED")
    }

    fun toggleMobileDisplayPermission(granted: Boolean) {
        _mobileDisplayPermission.value = granted
        addReportLog("System: Mobile Display overlay permission -> " + if (granted) "GRANTED" else "DENIED")
    }

    fun toggleOfflineCache(enabled: Boolean) {
        _offlineCache.value = enabled
        addReportLog("System: Room DB offline database caching -> " + if (enabled) "ENABLED" else "DISABLED")
    }

    fun toggleLiveDistanceUpdates(enabled: Boolean) {
        _liveDistanceUpdates.value = enabled
        addReportLog("System: Live distance update broadcasts -> " + if (enabled) "ENABLED" else "PAUSED")
    }

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            // Start light distance simulation in background
            startDistanceSimulation()
        }
    }

    fun setRoute(route: String) {
        _currentRoute.value = route
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun setChatPartner(partner: UserProfile?) {
        _activeChatPartner.value = partner
        // Reset local AI caches for new screen
        _aiIcebreakers.value = emptyList()
        _aiCompatibility.value = null
    }

    fun setRadarRadius(radius: Float) {
        _radarRadius.value = radius
    }

    fun toggleInvisibleMode(enabled: Boolean) {
        _privacyInvisibleMode.value = enabled
        viewModelScope.launch {
            val current = repository.getProfile("current_user")
            if (current != null) {
                repository.updateProfile(current.copy(isIncognito = enabled))
            }
        }
    }

    fun toggleHideDistance(enabled: Boolean) {
        _privacyHideDistance.value = enabled
    }

    fun purchasePremium() {
        _premiumUnlocked.value = true
        viewModelScope.launch {
            repository.addNotification(
                title = "Premium Unlimited HeartSync Unlocked!",
                body = "You now enjoy unlimited daily super likes, VIP radar profiles, and custom neon styles.",
                type = "SYSTEM"
            )
        }
    }

    fun reportProfile(profileId: String, reason: String) {
        viewModelScope.launch {
            val profile = repository.getProfile(profileId)
            if (profile != null) {
                // Remove profile locally
                repository.updateProfile(profile.copy(isFake = true))
                _reportsLog.update { current ->
                    listOf("Report processed for $profileId: '$reason'", *current.toTypedArray())
                }
                repository.addNotification(
                    title = "Moderation Report Registered",
                    body = "We have received your report regarding ${profile.name}. The profile has been temporarily isolated.",
                    type = "SYSTEM"
                )
            }
        }
    }

    // --- Main Interactions ---
    fun toggleLike(profileId: String) {
        viewModelScope.launch {
            val profile = repository.getProfile(profileId) ?: return@launch
            val wasLiked = profile.liked
            val newLiked = !wasLiked

            val updated = profile.copy(liked = newLiked)
            repository.updateProfile(updated)

            if (newLiked) {
                repository.addNotification(
                    title = "Attraction Sent!",
                    body = "You liked ${profile.name}. If they like you back, HeartSync activates!",
                    type = "SYSTEM"
                )

                // Check if they already liked back (mutual match)
                if (updated.likedBack) {
                    delay(800) // Delay for build-up animation
                    _showMatchCelebration.value = updated
                    repository.addNotification(
                        title = "HeartSync Trigger Match!",
                        body = "You and ${profile.name} are in mutual sync range! Open your chat room now.",
                        type = "MATCH"
                    )
                }
            }
        }
    }

    fun dismissMatchCelebration() {
        _showMatchCelebration.value = null
    }

    // --- Chat Actions ---
    fun sendChatMessage(text: String, replyToText: String? = null) {
        val partner = _activeChatPartner.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            // Save current message
            repository.sendMessage("current_user", partner.id, text, replyToText)

            // Simulate typing and automated matched user response
            delay(1500)
            val responseText = getSimulatedResponseText(partner, text)
            repository.sendMessage(partner.id, "current_user", responseText)
        }
    }

    fun addEmojiReaction(msg: ChatMessage, emoji: String) {
        viewModelScope.launch {
            repository.addReaction(msg, emoji)
        }
    }

    fun deleteChatMessage(msgId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(msgId)
        }
    }

    // --- BLE / Proximity Manual Trigger Scanner ---
    fun startBleRadarScan() {
        if (_isScanning.value) return
        _isScanning.value = true
        _radarPingsCount.value = 0

        viewModelScope.launch {
            repository.addNotification(
                title = "Scanning Proximity BLE...",
                body = "Broadcasting anonymous Bluetooth Low Energy HeartSync pings...",
                type = "NEARBY"
            )

            // Simulate 5 pings
            for (i in 1..4) {
                delay(1200)
                _radarPingsCount.value = i
                // Move profiles randomly closer
                simulateShiftingDistances(scanPower = true)
            }

            delay(1000)
            _isScanning.value = false

            // Trigger potential love alarms
            checkLoveAlarms()
        }
    }

    // --- Admin Commands ---
    fun adminMockNearbyUser(profileId: String, distance: Double) {
        viewModelScope.launch {
            val profile = repository.getProfile(profileId) ?: return@launch
            val updated = profile.copy(distance = distance, isOnline = true)
            repository.updateProfile(updated)

            repository.addNotification(
                title = "Admin Teleport Triggered",
                body = "Admin forced ${profile.name} to be ${String.format("%.1f", distance)}m away.",
                type = "SYSTEM"
            )

            checkLoveAlarms()
        }
    }

    fun adminResetDatabase() {
        viewModelScope.launch {
            db.clearAllTables()
            repository.seedDatabaseIfEmpty()
            repository.addNotification(
                title = "Database Restored",
                body = "Mock dataset successfully hard-reset to factory state.",
                type = "SYSTEM"
            )
        }
    }

    // --- AI Matching Workflows ---
    fun fetchAiIcebreakers(partner: UserProfile) {
        val currentMeName = currentUserProfile.value?.name ?: "Aria"
        viewModelScope.launch {
            _aiIcebreakersLoading.value = true
            val starters = GeminiService.getIceBreakers(
                currentName = currentMeName,
                partnerName = partner.name,
                partnerBio = partner.bio,
                partnerInterests = partner.interests
            )
            _aiIcebreakers.value = starters
            _aiIcebreakersLoading.value = false
        }
    }

    fun fetchAiCompatibility(partner: UserProfile) {
        val me = currentUserProfile.value ?: return
        viewModelScope.launch {
            _aiCompatibilityLoading.value = true
            val result = GeminiService.getCompatibilityScoreAndAnalysis(
                meBio = me.bio,
                meInterests = me.interests,
                partnerName = partner.name,
                partnerBio = partner.bio,
                partnerInterests = partner.interests
            )
            _aiCompatibility.value = result
            _aiCompatibilityLoading.value = false
        }
    }

    fun loadAiInsightsForProfile(partnerId: String, bio: String, interests: String) {
        if (_aiInsights.value.containsKey(partnerId)) return
        viewModelScope.launch {
            val insight = GeminiService.getPersonalityInsights(partnerId, bio, interests)
            _aiInsights.update { current ->
                current + (partnerId to insight)
            }
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    // --- Background Simulators ---
    private fun startDistanceSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(15000) // update distances every 15 seconds silently
                if (!_isScanning.value && !_privacyInvisibleMode.value && _liveDistanceUpdates.value) {
                    simulateShiftingDistances(scanPower = false)
                    checkLoveAlarms()
                }
            }
        }
    }

    private suspend fun simulateShiftingDistances(scanPower: Boolean) {
        val currentProfiles = allProfiles.value
        for (profile in currentProfiles) {
            val drift = if (scanPower) {
                // Attracted closer during active BLE scan!
                Random.nextDouble(-8.0, 2.0)
            } else {
                Random.nextDouble(-3.0, 3.0)
            }
            val newDistance = (profile.distance + drift).coerceAtLeast(3.2)
            repository.updateProfile(profile.copy(distance = newDistance))
        }
    }

    private suspend fun checkLoveAlarms() {
        if (_privacyInvisibleMode.value) return

        val activeRadius = _radarRadius.value
        val profiles = allProfiles.value

        for (profile in profiles) {
            // If inside love alarm radius and is active/online
            if (profile.distance <= activeRadius && profile.isOnline) {
                // If we like them, and they liked back, trigger alarm!
                if (profile.liked && profile.likedBack) {
                    val welcomeLog = "PING! Proximity Attraction Sync! HeartSync Alarm triggered for ${profile.name} inside ${activeRadius.toInt()}m!"
                    repository.addNotification(
                        title = "HeartSync Alarm Triggered! 💓",
                        body = "You are within ${String.format("%.1f", profile.distance)}m of a mutual match (${profile.name})!",
                        type = "MATCH"
                    )
                } else if (profile.likedBack) {
                    // Anonymous attraction notification: someone who liked you is inside your radius!
                    // This is the core "Love Alarm" tv show concept: notification rings, saying "someone inside your 10m radius likes you!"
                    repository.addNotification(
                        title = "Love Alarm Sync Pulse!",
                        body = "An anonymous match is within ${activeRadius.toInt()}m of you! Tap radar to discover.",
                        type = "NEARBY"
                    )
                }
            }
        }
    }

    private fun getSimulatedResponseText(partner: UserProfile, message: String): String {
        val lower = message.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> {
                listOf(
                    "Hey there! 😊 So excited our HeartSync radar pulsed together!",
                    "Hi! Hope your day is going great. What are you up to?",
                    "Hello! It's so cool matching with you. I love your profile bio!"
                ).random()
            }
            lower.contains("coffee") || lower.contains("matcha") || lower.contains("drink") -> {
                "Oh I'm always down for coffee or matcha! Let's definitely plan that. Have you been to the cozy spots downtown?"
            }
            lower.contains("hike") || lower.contains("trail") || lower.contains("nature") -> {
                "Hiking is the best escape! I've been wanting to try the ridge trail this weekend. Do you hike often?"
            }
            lower.contains("ai") || lower.contains("tech") || lower.contains("code") -> {
                "Coding and building AI apps is my absolute jam! I love working on creative projects. What kind of tech are you exploring?"
            }
            lower.contains("music") || lower.contains("song") || lower.contains("vinyl") -> {
                "Music gives life color! I'm currently listening to some smooth vintage jazz. What's your go-to song when you need pure focus?"
            }
            lower.contains("ice") || lower.contains("starter") || lower.contains("question") -> {
                "Aha, using the AI Icebreakers? I love it! That's actually a super fun question to think about."
            }
            else -> {
                listOf(
                    "That's so interesting! Tell me more about that? ✨",
                    "Absolutely! I feel like we have a super unique compatibility score. Do you agree?",
                    "Haha love that! Let's meet up soon if our proximity radar says we're nearby!",
                    "That sounds amazing. What are your plans for the rest of the day?"
                ).random()
            }
        }
    }
}
