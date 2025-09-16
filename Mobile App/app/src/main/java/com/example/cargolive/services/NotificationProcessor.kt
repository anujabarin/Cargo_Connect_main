package com.example.cargolive.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.cargolive.ui.maps.MapsActivity.NotificationType
import com.google.android.gms.maps.model.LatLng

/**
 * Handles processing and displaying notifications in the CargoLive app,
 * with smooth vehicle speed transitions
 */
object NotificationProcessor {
    private const val CHANNEL_ID = "vehicle_alerts"
    private const val TAG = "NotificationProcessor"

    private var trafficNotificationsDisabled = false
    private var lastNotificationType: NotificationType? = null

    // Handler for delaying speed changes to prevent jarring transitions
    private val handler = Handler(Looper.getMainLooper())
    private var pendingSpeedChange: Runnable? = null

    // Track ongoing notification to prevent rapid changes
    private var lastNotificationTimestamp = 0L
    private const val NOTIFICATION_DEBOUNCE_MS = 1000 // 1 second minimum between notification processing

    /**
     * Process notification message and update UI accordingly.
     * Implements debouncing to prevent rapid notification changes that could cause jerky movement.
     *
     * @param message The notification message to process
     * @param notificationText TextView to update with the message
     * @param onAccidentReroute Callback for accident rerouting
     * @param onParkingAvailable Callback for parking availability
     * @param onPullOver Callback for pull over instructions
     * @param currentPosition Current vehicle position
     * @param adjustVehicleSpeed Callback to adjust vehicle speed
     */
    fun processNotification(
        message: String,
        notificationText: TextView,
        agentState: String? = null,
        onAccidentReroute: ((LatLng) -> Unit)? = null,
        onParkingAvailable: (() -> Unit)? = null,
        onPullOver: (() -> Unit)? = null,
        currentPosition: LatLng? = null,
        adjustVehicleSpeed: ((NotificationType) -> Unit)? = null
    ) {
        if (trafficNotificationsDisabled) return

        val currentTime = System.currentTimeMillis()

        // Apply debouncing to prevent rapid notification changes
        if (currentTime - lastNotificationTimestamp < NOTIFICATION_DEBOUNCE_MS) {
            Log.d(TAG, "Notification debounced: $message")
            return
        }

        lastNotificationTimestamp = currentTime

        // Update notification text
        notificationText.text = message
        Log.d(TAG, "Processing notification: $message with state: $agentState")

        // Determine notification type based on agent_state
        val type = if (agentState != null) {
            when (agentState) {
                "G" -> NotificationType.POSITIVE
                "R" -> NotificationType.NEGATIVE
                "Y" -> NotificationType.NEUTRAL
                else -> determineNotificationType(message) // Fallback to message-based determination
            }
        } else {
            determineNotificationType(message)
        }

        // Handle special cases with grace periods
        handleSpecialCases(message, notificationText, onAccidentReroute,
            onParkingAvailable, onPullOver, currentPosition)

        // Apply visual styling based on notification type
        applyNotificationStyle(notificationText, type)

        // Schedule speed adjustment with a small delay to prevent abrupt changes
        cancelPendingSpeedChanges()
        pendingSpeedChange = Runnable {
            adjustVehicleSpeed?.invoke(type)
            pendingSpeedChange = null
        }
        handler.postDelayed(pendingSpeedChange!!, 300) // Small delay for smooth transition

        // Remember last notification type
        lastNotificationType = type
    }

    /**
     * Determine the type of notification based on message content
     */
    private fun determineNotificationType(message: String): NotificationType {
        return when {
            message.contains("accident", ignoreCase = true) ||
                    message.contains("delay", ignoreCase = true) ||
                    message.contains("traffic", ignoreCase = true) ||
                    message.contains("camera", ignoreCase = true) ||
                    message.contains("reduce speed", ignoreCase = true) ||
                    message.contains("weather", ignoreCase = true) -> {
                Log.d(TAG, "NEGATIVE notification: $message")
                NotificationType.NEGATIVE
            }
            message.contains("speed up", ignoreCase = true) ||
                    message.contains("green", ignoreCase = true) ||
                    message.contains("clear road", ignoreCase = true) ||
                    message.contains("faster", ignoreCase = true) -> {
                Log.d(TAG, "POSITIVE notification: $message")
                NotificationType.POSITIVE
            }
            else -> {
                Log.d(TAG, "NEUTRAL notification: $message")
                NotificationType.NEUTRAL
            }
        }
    }

    /**
     * Handle special notification cases that trigger actions
     */
    private fun handleSpecialCases(
        message: String,
        notificationText: TextView,
        onAccidentReroute: ((LatLng) -> Unit)?,
        onParkingAvailable: (() -> Unit)?,
        onPullOver: (() -> Unit)?,
        currentPosition: LatLng?
    ) {
        if (message.contains("Accident", ignoreCase = true)) {
            notificationText.text = "Accident Ahead! Rerouting!"
            // Delay the reroute slightly for UI feedback
            handler.postDelayed({
                currentPosition?.let { onAccidentReroute?.invoke(it) }
            }, 1500)
        }

        if (message.contains("Parking available", ignoreCase = false)) {
            handler.postDelayed({
                onParkingAvailable?.invoke()
            }, 800)
        }

        if (message.contains("Please pull over near rest area!", ignoreCase = false)) {
            onPullOver?.invoke()
        }
    }

    /**
     * Apply visual styling to notification based on type
     */
    private fun applyNotificationStyle(notificationText: TextView, type: NotificationType) {
        when (type) {
            NotificationType.POSITIVE -> {
                // Soft green
                notificationText.setBackgroundColor(Color.rgb(144, 238, 144))  // light green
                notificationText.setTextColor(Color.BLACK)
            }
            NotificationType.NEUTRAL -> {
                // Soft amber
                notificationText.setBackgroundColor(Color.rgb(255, 236, 179))  // pale amber
                notificationText.setTextColor(Color.BLACK)
            }
            NotificationType.NEGATIVE -> {
                // Soft red (salmon/light red)
                notificationText.setBackgroundColor(Color.rgb(255, 204, 203))  // light red
                notificationText.setTextColor(Color.BLACK)
            }
        }
    }


    /**
     * Cancel any pending speed changes to prevent conflicting adjustments
     */
    private fun cancelPendingSpeedChanges() {
        pendingSpeedChange?.let {
            handler.removeCallbacks(it)
            pendingSpeedChange = null
        }
    }

    /**
     * Display a notification directly with specified type
     * Used for manual notifications triggered by the app rather than external events
     */
    fun displayNotification(
        notificationText: TextView,
        message: String,
        type: NotificationType,
        isVehiclePaused: Boolean = false,
        adjustVehicleSpeed: ((NotificationType) -> Unit)? = null
    ) {
        if (trafficNotificationsDisabled) return

        Log.d(TAG, "Displaying notification: $message (Type: $type, Paused: $isVehiclePaused)")

        // Update UI
        notificationText.text = message
        applyNotificationStyle(notificationText, type)

        // Handle speed adjustment if vehicle is not paused
        if (!isVehiclePaused) {
            cancelPendingSpeedChanges()

            // Smooth transition with a small delay
            pendingSpeedChange = Runnable {
                adjustVehicleSpeed?.invoke(type)
                pendingSpeedChange = null
            }
            handler.postDelayed(pendingSpeedChange!!, 200)
        }

        lastNotificationType = type
    }

    /**
     * Enable/disable traffic notifications
     */
    fun setTrafficNotificationsEnabled(enabled: Boolean) {
        trafficNotificationsDisabled = !enabled
        Log.d(TAG, "Traffic notifications ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Get the last notification type
     */
    fun getLastNotificationType(): NotificationType? {
        return lastNotificationType
    }

    /**
     * Create notification channel for Android O and above
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vehicle Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Vehicle Simulation Alerts"
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    /**
     * Clean up resources when app is destroyed
     */
    fun cleanup() {
        cancelPendingSpeedChanges()
    }
}