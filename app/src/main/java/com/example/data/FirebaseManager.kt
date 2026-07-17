package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

    private val _isRealFirebase = MutableStateFlow(false)
    val isRealFirebase = _isRealFirebase.asStateFlow()

    private val _statusMessage = MutableStateFlow("Firebase: Offline / No active configuration")
    val statusMessage = _statusMessage.asStateFlow()

    // Saved manually configured credentials
    private val _customApiKey = MutableStateFlow("")
    val customApiKey = _customApiKey.asStateFlow()

    private val _customProjectId = MutableStateFlow("")
    val customProjectId = _customProjectId.asStateFlow()

    private val _customAppId = MutableStateFlow("")
    val customAppId = _customAppId.asStateFlow()

    fun initialize(context: Context) {
        try {
            // First check if there is an existing google-services.json context (default)
            val app = FirebaseApp.initializeApp(context)
            if (app != null) {
                _isInitialized.value = true
                _isRealFirebase.value = true
                _statusMessage.value = "Firebase: Cloud Connected (via Google Services config)"
                Log.d(TAG, "Firebase default app initialized successfully.")
            } else {
                initializeSimulationMode()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase default initialization bypassed or failed: ${e.message}")
            initializeSimulationMode()
        }
    }

    private fun initializeSimulationMode() {
        _isInitialized.value = true
        _isRealFirebase.value = false
        _statusMessage.value = "Firebase: Simulated Mode (Local database active)"
    }

    fun initializeWithManualOptions(
        context: Context,
        apiKey: String,
        projectId: String,
        appId: String
    ): Boolean {
        if (apiKey.isBlank() || projectId.isBlank() || appId.isBlank()) {
            _statusMessage.value = "Error: API Key, Project ID, and App ID cannot be empty."
            return false
        }

        return try {
            // Delete existing default app if any to avoid collision
            try {
                FirebaseApp.getInstance().delete()
            } catch (e: Exception) {
                // Ignore if not initialized
            }

            val options = FirebaseOptions.Builder()
                .setApiKey(apiKey.trim())
                .setProjectId(projectId.trim())
                .setApplicationId(appId.trim())
                .build()

            val app = FirebaseApp.initializeApp(context, options)
            if (app != null) {
                _customApiKey.value = apiKey
                _customProjectId.value = projectId
                _customAppId.value = appId
                _isInitialized.value = true
                _isRealFirebase.value = true
                _statusMessage.value = "Firebase: Successfully Connected via manual config!"
                Log.d(TAG, "Firebase custom options initialized successfully.")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            val errMsg = e.localizedMessage ?: "Unknown initialization error"
            _statusMessage.value = "Firebase Error: $errMsg"
            Log.e(TAG, "Failed manual initialization", e)
            false
        }
    }

    fun resetToSimulation(context: Context) {
        try {
            FirebaseApp.getInstance().delete()
        } catch (e: Exception) {
            // Ignore
        }
        _customApiKey.value = ""
        _customProjectId.value = ""
        _customAppId.value = ""
        initializeSimulationMode()
    }
}
