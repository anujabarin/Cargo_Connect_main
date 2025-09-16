package com.example.cargolive.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.cargolive.data.repository.CargoRepository
import com.example.cargolive.utils.Constants
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class LocationSimulatorService : Service() {
    private val TAG = "LocationSimulatorService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val repository = CargoRepository()

    private var scheduleId: String? = null
    private var startLat: Double = 0.0
    private var startLng: Double = 0.0
    private var destLat: Double = 0.0
    private var destLng: Double = 0.0

    private var currentLat: Double = 0.0
    private var currentLng: Double = 0.0

    private var isRunning = false
    private var updateJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        scheduleId = intent.getStringExtra(Constants.EXTRA_SCHEDULE_ID)
        startLat = intent.getDoubleExtra("start_lat", 0.0)
        startLng = intent.getDoubleExtra("start_lng", 0.0)
        destLat = intent.getDoubleExtra(Constants.EXTRA_PICKUP_LOC, 0.0)
        destLng = intent.getDoubleExtra(Constants.EXTRA_DROP_OFF_LOC, 0.0)

        if (scheduleId == null || startLat == 0.0 || startLng == 0.0 || destLat == 0.0 || destLng == 0.0) {
            Log.e(TAG, "Missing required parameters")
            stopSelf()
            return START_NOT_STICKY
        }

        // Initialize with starting position
        currentLat = startLat
        currentLng = startLng

        startLocationUpdates()

        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (isRunning) return

        isRunning = true
        updateJob = serviceScope.launch {
            try {
                while (isActive) {
                    // Update location by moving towards destination
                    simulateMovement()

                    // Send location update to the backend
                    val success = repository.updateLocation(
                        scheduleId ?: return@launch,
                        currentLat,
                        currentLng
                    )

                    if (success) {
                        Log.d(TAG, "Location updated: $currentLat, $currentLng")
                    } else {
                        Log.e(TAG, "Failed to update location")
                    }

                    // Check if we've reached destination (within small threshold)
                    if (isNearDestination()) {
                        Log.d(TAG, "Destination reached")
                        stopSelf()
                        break
                    }

                    delay(Constants.LOCATION_UPDATE_INTERVAL)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in location updates", e)
            }
        }
    }

    private fun simulateMovement() {
        // Simple linear interpolation between start and destination
        // In a real app, you might want to follow roads, etc.
        val totalDistance = calculateDistance(startLat, startLng, destLat, destLng)
        val stepSize = 0.0005 // Roughly ~50-60 meters depending on latitude

        // Direction vector
        val dirLat = destLat - currentLat
        val dirLng = destLng - currentLng
        val dirLength = calculateDistance(currentLat, currentLng, destLat, destLng)

        if (dirLength > 0) {
            // Normalize and scale by step size
            val moveLat = dirLat / dirLength * stepSize
            val moveLng = dirLng / dirLength * stepSize

            // Update current position
            currentLat += moveLat
            currentLng += moveLng

            // Add some randomness for realism
            currentLat += (Random().nextDouble() - 0.5) * 0.00002
            currentLng += (Random().nextDouble() - 0.5) * 0.00002
        }
    }

    private fun isNearDestination(): Boolean {
        val distance = calculateDistance(currentLat, currentLng, destLat, destLng)
        return distance < 0.0005 // About 50 meters
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        // Simple Euclidean distance - in a real app, you'd use Haversine formula
        val latDiff = lat2 - lat1
        val lngDiff = lng2 - lng1
        return Math.sqrt(latDiff * latDiff + lngDiff * lngDiff)
    }

    override fun onDestroy() {
        isRunning = false
        updateJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}