package com.example.cargolive.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cargolive.R
import com.example.cargolive.databinding.ActivityMapsBinding
import com.example.cargolive.services.RouteManager
import com.example.cargolive.services.NotificationProcessor
import com.example.cargolive.services.MockApiManager
import com.example.cargolive.utils.Constants
import com.example.cargolive.utils.TrafficAlert
import com.example.cargolive.utils.TrafficAlertManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import android.animation.ValueAnimator
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.model.BitmapDescriptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import androidx.core.graphics.toColorInt
import androidx.core.graphics.createBitmap
import com.example.cargolive.utils.EmojiUtils
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = "MapsActivity"
    private var baseInterval = 300L
    private var currentInterval = baseInterval
    private var currentSpeed = "NORMAL"

    private lateinit var binding: ActivityMapsBinding
    private lateinit var googleMap: GoogleMap
    private var marker: Marker? = null
    private val handler = Handler(Looper.getMainLooper())
    private var simulationRunnable: Runnable? = null

    private var pathPoints = mutableListOf<LatLng>()
    private var stepIndex = 0
    private val triggeredNotifications = mutableSetOf<Int>()
    private var speedIncreased = false
    private var speedDecreased = false

    private var alertPoints = mutableListOf<TrafficAlert>()

    private val previouslyTraveled = mutableListOf<LatLng>()
    private lateinit var criticality: String
    private lateinit var cargo_ID: String
    private lateinit var terminalName: String

    // Original route and location parameters
    private val startLatLng = LatLng(32.984774220294895, -96.74775350068155)
    private var pickupLocation = LatLng(32.88981687057822, -97.03790148285134)
    private var dropoffLocation: LatLng? = null

    // Navigation phase tracking with debug string
    private enum class NavigationPhase(val debugName: String) {
        TO_PICKUP("TO_PICKUP"),
        AT_PICKUP("AT_PICKUP"),
        TO_DROPOFF("TO_DROPOFF"),
        COMPLETED("COMPLETED")
    }

    private var currentPhase = NavigationPhase.TO_PICKUP

    private val shownMessages = mutableSetOf<String>()
    private var accidentAlreadyHandled = false

    // Track if we've reached the destination
    private var destinationReached = false
    private var trafficNotificationsDisabled = false

    private var parkingSpotAssigned = false

    // This is for for API calls
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    private var isVehiclePaused = false

    // Enum for notification types
    enum class NotificationType { POSITIVE, NEUTRAL, NEGATIVE }

    private var agentList = mutableListOf<Pair<String, LatLng>>()

    private var currentAgentIndex = 0
    private var agentInitialDistance = 0.0
    private var agentJustUpdated = true
    private val triggeredAirportMilestones = mutableSetOf<Double>()
    private var lastMockTriggered: String = "" // "DALI" or "AIRPORT"
    private var lastDaliAgentIndex = -1
    private var lastNotificationTime = 0L
    private val MIN_NOTIFICATION_INTERVAL = 1000L

    // Track processed agents to skip after rerouting
    private val processedAgentNames = mutableSetOf<String>()
    private var accidentAgent: String? = null

    private val allProcessedAgentMap = mutableMapOf<String, LatLng>()

    // Flag to determine if camera should follow the vehicle automatically
    private var isCameraFollowing = true

    private lateinit var mapsApiKey: String
    private lateinit var daliEndpoint: String
    private lateinit var airportEndpoint: String

    val terminalParkingLocations = mapOf(
        "A" to LatLng(32.90631503886106, -97.03792673073337),
        "B" to LatLng(32.90484059614922, -97.04399519025002),
        "C" to LatLng(32.89760968949179, -97.03761569547815),
        "D" to LatLng(32.89786612394736, -97.04292974334649),
        "E" to LatLng(32.89226253523804, -97.0378871915226)
    )

    fun adjustVehicleSpeed(type: NotificationType) {
        // If we're paused, don't change speed
        if (isVehiclePaused) return

        // Remove any pending callbacks to avoid speed conflicts
        simulationRunnable?.let { handler.removeCallbacks(it) }

        // Set new interval based on notification type
        val oldInterval = currentInterval
        currentInterval = when (type) {
            NotificationType.NEGATIVE -> baseInterval * 2  // Red - Slow speed (500ms)
            NotificationType.NEUTRAL -> baseInterval       // Yellow - Normal speed (300ms)
            NotificationType.POSITIVE -> baseInterval / 2  // Green - Fast speed (125ms)
        }

        // Update speed indicator
        val speedLabel = when (type) {
            NotificationType.NEGATIVE -> "SLOW"
            NotificationType.NEUTRAL -> "NORMAL"
            NotificationType.POSITIVE -> "FAST"
        }
        updateSpeedIndicator(speedLabel)

        Log.d(TAG, "Speed adjusted: $oldInterval ms → $currentInterval ms ($speedLabel)")

        // Continue simulation with new speed if not at destination
        if (!destinationReached && stepIndex < pathPoints.size && !isVehiclePaused) {
            simulationRunnable?.let {
                handler.postDelayed(it, currentInterval)
            }
        }
    }

    @SuppressLint("SetTextI18s")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Maps Activity
        mapsApiKey = getString(R.string.google_maps_key)
        daliEndpoint = getString(R.string.DALI_ENDPOINT)
        airportEndpoint = getString(R.string.AIRPORT_ENDPOINT)


        Log.d("API_KEY_CHECK", "Using Google Maps API Key: $mapsApiKey")

        // Get intent extras
        criticality = intent.getStringExtra(Constants.CRITICALITY)?.uppercase() ?: "LOW"

        val temp_terminalName =
            intent.getStringExtra(Constants.EXTRA_TERMINAL_NAME)?.uppercase() ?: "A"
        terminalName = temp_terminalName.trim().last().toString()
        cargo_ID = intent.getStringExtra(Constants.EXTRA_SCHEDULE_ID)
            ?: "b2e0b559-4fe9-44b9-bd39-5946e4bc810e"

        // Parse pickup location from intent
        parsePickupLocation()

        // Parse dropoff location from intent (might be null initially)
        parseDropoffLocation()

        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationProcessor.createNotificationChannel(this)

        // Adjust notification area to take up 1/5 of screen height
        val notificationsLayout = findViewById<ConstraintLayout>(R.id.notifications_layout)
        val params = notificationsLayout.layoutParams
        params.height = resources.displayMetrics.heightPixels / 5
        notificationsLayout.layoutParams = params

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupButtonListeners()
    }

    private fun parsePickupLocation() {
        val pickupLngStr = intent.getStringExtra(Constants.EXTRA_PICKUP_LOC)
        pickupLocation = pickupLngStr?.split(",")?.let { parts ->
            if (parts.size == 2) {
                val lat = parts[0].trim().toDouble()
                val lng = parts[1].trim().toDouble()
                LatLng(lat, lng)
            } else {
                LatLng(32.88981687057822, -97.03790148285134)
            }
        } ?: LatLng(32.88981687057822, -97.03790148285134)
    }

    private fun parseDropoffLocation() {
        val dropoffLngStr = intent.getStringExtra(Constants.EXTRA_DROP_OFF_LOC)
        dropoffLocation = dropoffLngStr
            ?.replace("(", "")    // Remove the opening bracket
            ?.replace(")", "")    // Remove the closing bracket
            ?.split(",")          // Split at comma
            ?.let { parts ->
                if (parts.size == 2) {
                    val lat = parts[0].trim().toDouble()
                    val lng = parts[1].trim().toDouble()
                    LatLng(lat, lng)
                } else {
                    LatLng(32.88981687057822, -97.03790148285134) // Default dropoff location
                }
            } ?: LatLng(32.88981687057822, -97.03790148285134)
    }

    // Updated button click handler
    private fun setupButtonListeners() {
        binding.btnStartNavigation.setOnClickListener {
            Log.d(TAG, "Button clicked in phase: ${currentPhase.debugName}")

            when (currentPhase) {
                NavigationPhase.TO_PICKUP -> {
                    resetSimulation()
                    startSimulation()
                    binding.btnStartNavigation.isEnabled = false
                    binding.btnStartNavigation.text = "Navigation to Pickup..."
                }

                NavigationPhase.AT_PICKUP -> {
                    // Show the improved dialog for dropoff confirmation
                    showFixedDropoffNavigationAlert()
                }

                else -> {
                    Log.d(TAG, "Button clicked in ignored phase: ${currentPhase.debugName}")
                }
            }
        }

        binding.btnToggleSimulation.setOnClickListener {
            toggleVehiclePause()
        }

        // Add button to toggle camera following behavior
        binding.btnToggleFollow.setOnClickListener {
            toggleCameraFollow()
        }
    }

    // New method to toggle camera following behavior
    private fun toggleCameraFollow() {
        isCameraFollowing = !isCameraFollowing

        if (isCameraFollowing) {
            binding.btnToggleFollow.text = "DISABLE AUTO-FOLLOW"
            // If enabling follow, immediately move camera to marker position
            marker?.position?.let { position ->
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(position))
            }
        } else {
            binding.btnToggleFollow.text = "ENABLE AUTO-FOLLOW"
        }
    }

    // New improved dialog method with fixed navigation state management
    private fun showFixedDropoffNavigationAlert() {
        Log.d(
            TAG,
            "Showing dropoff navigation confirmation dialog. Current phase: ${currentPhase.debugName}"
        )

        val dropoff = dropoffLocation
        if (dropoff == null) {
            Log.e(TAG, "Cannot start dropoff navigation: Dropoff location is null!")
            // Show error notification
            val notificationText = findViewById<TextView>(R.id.notification_text)
            NotificationProcessor.displayNotification(
                notificationText = notificationText,
                message = "Error: No dropoff location available",
                type = NotificationType.NEGATIVE,
                isVehiclePaused = isVehiclePaused,
                adjustVehicleSpeed = this::adjustVehicleSpeed,
            )
            return
        }

        // First set the phase to TO_DROPOFF
        // Do this BEFORE showing the dialog to ensure state is correct
        currentPhase = NavigationPhase.TO_DROPOFF

        // Reset navigation state
        destinationReached = false
        trafficNotificationsDisabled = false

        AlertDialog.Builder(this)
            .setTitle("Continue to Dropoff")
            .setMessage("Pickup completed! Continue to dropoff location?")
            .setPositiveButton("Start Navigation") { dialog, _ ->
                dialog.dismiss()

                Log.d(TAG, "User confirmed dropoff navigation. Phase is: ${currentPhase.debugName}")

                // Double-check phase is still correct
                if (currentPhase != NavigationPhase.TO_DROPOFF) {
                    Log.e(TAG, "Phase changed during dialog! Resetting to TO_DROPOFF")
                    currentPhase = NavigationPhase.TO_DROPOFF
                }

                // Update UI
                binding.btnStartNavigation.isEnabled = false
                binding.btnStartNavigation.text = "Navigation to Dropoff..."

                // Start dropoff navigation
                Log.d(TAG, "Starting navigation from pickup: $pickupLocation to dropoff: $dropoff")
                drawRouteToDropoff(pickupLocation, dropoff)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Log.d(TAG, "User cancelled dropoff navigation. Resetting to AT_PICKUP phase")
// Reset back to AT_PICKUP if user cancels
                currentPhase = NavigationPhase.AT_PICKUP
                binding.btnStartNavigation.isEnabled = true
                binding.btnStartNavigation.text = "Start Dropoff Navigation"
            }
            .setCancelable(false)
            .show()
    }

    fun plotIntersectionMarkers(googleMap: GoogleMap, points: Map<String, LatLng>) {
        val emojiIcon = EmojiUtils.createEmojiBitmap("🚦", size = 80f)

        for ((agentName, latLng) in points) {
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(agentName)
                    .icon(emojiIcon)
            )
        }
    }

    // Specialized method just for drawing dropoff route
    private fun drawRouteToDropoff(startLatLng: LatLng, destinationLatLng: LatLng) {
        RouteManager.fetchRouteFromApi(
            mapsApiKey,
            startLatLng,
            destinationLatLng,
            avoidHighways = false,
            googleMap
        ) { route, intersections -> // Clear the map and path points
            googleMap.clear()
            pathPoints.clear()
            pathPoints.addAll(route)

            // Reset agent numbering when starting fresh dropoff route
            var agentNumber = 1
            val renamedIntersections = intersections.map {
                "Agent${agentNumber++}" to it.value
            }.toMap()

            plotIntersectionMarkers(googleMap, renamedIntersections)

            // FIX: Use renamed intersections instead of original
            stepIndex = 0
            agentList.clear()
            agentList.addAll(renamedIntersections.toList()) // Changed from intersections
            currentAgentIndex = 0

            // NEW: Clear processed agents when starting fresh dropoff route
            processedAgentNames.clear()

            // Draw the route polyline
            googleMap.addPolyline(
                PolylineOptions()
                    .addAll(route)
                    .width(8f)
                    .color("#cc00cc".toColorInt())
            )

            // Add truck marker at start position
            marker = googleMap.addMarker(
                MarkerOptions()
                    .position(startLatLng)
                    .title("Simulated Truck")
                    .flat(true)
                    .icon(getBlackCircleIcon())
            )

            // Add dropoff marker
            googleMap.addMarker(
                MarkerOptions()
                    .position(destinationLatLng)
                    .title("Dropoff Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )

            // Generate alerts and move camera
            alertPoints = TrafficAlertManager.generateEquallySpacedAlerts(
                route.toMutableList(),
                enableAccident = (criticality == "HIGH")
            )

            val cameraPosition = com.google.android.gms.maps.model.CameraPosition.Builder()
                .target(startLatLng)
                .zoom(17f)
                .build()

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            // Double-check that we're in the correct phase before starting simulation
            if (currentPhase != NavigationPhase.TO_DROPOFF) {
                currentPhase = NavigationPhase.TO_DROPOFF
            }

            // Start the simulation for dropoff
            startSimulation()
        }
    }

    private fun updateSpeedIndicator(speedLabel: String) {
        currentSpeed = speedLabel
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        // Set up map click listener to disable auto-follow when user interacts with the map
        googleMap.setOnMapClickListener {
            if (isCameraFollowing) {
                toggleCameraFollow()
            }
        }

        // MAP_TYPE_SATELLITE, MAP_TYPE_NORMAL, MAP_TYPE_HYBRID,
        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else {
            googleMap.isMyLocationEnabled = true
        }

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setPadding(50, 50, 50, 200)

        // Set a camera change listener to disable auto-follow when user zooms
        googleMap.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE && isCameraFollowing) {
                toggleCameraFollow()
            }
        }

        drawInitialRoute(startLatLng, pickupLocation)
    }

    private fun drawInitialRoute(startLatLng: LatLng, destinationLatLng: LatLng) {
        RouteManager.fetchRouteFromApi(
            mapsApiKey,
            startLatLng,
            destinationLatLng,
            avoidHighways = false,
            googleMap
        ) { route, intersections ->

            var agentNumber = 1
            val renamedIntersections = intersections.map {
                "Agent${agentNumber++}" to it.value
            }.toMap()

            agentList.clear()
            agentList.addAll(renamedIntersections.toList()) // Use renamed agents
            currentAgentIndex = 0

            plotIntersectionMarkers(googleMap, intersections)
            // NEW: Clear processed agents on new route
            processedAgentNames.clear()

            googleMap.clear()
            pathPoints.clear()
            pathPoints.addAll(route)

            val addPolyline = googleMap.addPolyline(
                PolylineOptions()
                    .addAll(route)
                    .width(8f)
                    .color(Color.parseColor("#cc00cc"))
            )

            marker = googleMap.addMarker(
                MarkerOptions()
                    .position(startLatLng)
                    .title("Simulated Truck")
                    .flat(true)
                    .icon(getBlackCircleIcon())
            )

            // Add destination marker
            googleMap.addMarker(
                MarkerOptions().position(destinationLatLng).title("Pickup Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )

            alertPoints = TrafficAlertManager.generateEquallySpacedAlerts(
                route.toMutableList(),
                enableAccident = (criticality == "HIGH")
            )
            val cameraPosition = com.google.android.gms.maps.model.CameraPosition.Builder()
                .target(startLatLng)
                .zoom(17f)
                .build()

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    private fun getBlackCircleIcon(): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this, R.drawable.black_circle)!!
        val bitmap = createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val canvas = android.graphics.Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun rerouteFromCurrentLocation(currentPos: LatLng, destinationLatLng: LatLng) {
        val wasPaused = isVehiclePaused
        handler.removeCallbacks(simulationRunnable!!)

        // Save and mark current agent as processed
        if (currentAgentIndex < agentList.size) {
            val (agentName, _) = agentList[currentAgentIndex]
            processedAgentNames.add(agentName)
            accidentAgent = agentName
        }

        for (agentName in processedAgentNames) {
            if (!allProcessedAgentMap.containsKey(agentName)) {
                val latLng = agentList.find { it.first == agentName }?.second
                if (latLng != null) {
                    allProcessedAgentMap[agentName] = latLng
                }
            }
        }

        RouteManager.fetchRouteFromApi(
            mapsApiKey,
            currentPos,
            destinationLatLng,
            avoidHighways = true,
            googleMap
        ) { newRoute, newIntersections ->
            // Save previously traveled path
            val traveledSoFar = pathPoints.subList(0, stepIndex).toMutableList()
            previouslyTraveled.clear()
            previouslyTraveled.addAll(traveledSoFar)

            pathPoints.clear()
            pathPoints.addAll(previouslyTraveled)
            pathPoints.addAll(newRoute)

            googleMap.clear()
            googleMap.addPolyline(
                PolylineOptions()
                    .addAll(pathPoints)
                    .width(8f)
                    .color("#cc00cc".toColorInt())
            )

//            // 🔵 Plot previously processed agents with emoji
            val emojiIcon = EmojiUtils.createEmojiBitmap("🚦", size = 80f)
            val agentNamesToPlot = processedAgentNames.toList().dropLast(1)  // skip the last accident-triggered
            for (agentName in agentNamesToPlot) {
                val latLng = allProcessedAgentMap[agentName]
                if (latLng != null) {
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(agentName)
                            .icon(emojiIcon)
                    )
                }
            }

            // 🔢 Reassign new agent names starting after the last one
            val lastAgentName = processedAgentNames.lastOrNull()
            val lastAgentNumber = lastAgentName?.let {
                Regex("Agent(\\d+)").find(it)?.groupValues?.get(1)?.toIntOrNull()
            } ?: 0
            var nextAgentNumber = lastAgentNumber + 1

            agentList.clear()
            currentAgentIndex = 0
            for ((_, latLng) in newIntersections) {
                val newAgentName = "Agent$nextAgentNumber"
                agentList.add(Pair(newAgentName, latLng))
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(newAgentName)
                        .icon(emojiIcon)
                )
                nextAgentNumber++
            }

            // ⚫️ Vehicle marker
            marker = googleMap.addMarker(
                MarkerOptions()
                    .position(currentPos)
                    .title("Simulated Truck")
                    .flat(true)
                    .icon(getBlackCircleIcon())
            )

            // 🟢 Destination marker
            val destinationTitle =
                if (currentPhase == NavigationPhase.TO_PICKUP) "Pickup Location" else "Dropoff Location"
            googleMap.addMarker(
                MarkerOptions()
                    .position(destinationLatLng)
                    .title(destinationTitle)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )

            val remainingMessages =
                TrafficAlertManager.navigationMessages.filter { it !in shownMessages }
            alertPoints = TrafficAlertManager.generateRemainingAlerts(
                newRoute.toMutableList(),
                remainingMessages
            )

            stepIndex = previouslyTraveled.size
            lastMockTriggered = ""
            agentJustUpdated = true
            startSimulation(wasPaused, false)
        }
    }

    private fun animateMarker(startPosition: LatLng, endPosition: LatLng, duration: Long) {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = duration

        valueAnimator.addUpdateListener { animation ->
            val v = animation.animatedFraction
            val lng = v * endPosition.longitude + (1 - v) * startPosition.longitude
            val lat = v * endPosition.latitude + (1 - v) * startPosition.latitude
            val newPosition = LatLng(lat, lng)
            marker?.position = newPosition

            // Only update camera if auto-follow is enabled
            if (isCameraFollowing) {
                // Get current zoom and bearing
                val currentZoom = googleMap.cameraPosition.zoom
                val currentBearing = googleMap.cameraPosition.bearing

                // Create a camera position that maintains zoom and bearing
                val cameraUpdate = CameraUpdateFactory.newCameraPosition(
                    com.google.android.gms.maps.model.CameraPosition.Builder()
                        .target(newPosition)
                        .zoom(currentZoom)
                        .bearing(currentBearing)
                        .build()
                )

                // Move camera without animation but preserve zoom level
                googleMap.moveCamera(cameraUpdate)
            }
        }

        valueAnimator.start()
    }

    private fun startSimulation(wasPaused: Boolean = false, isFirstTime: Boolean = true) {
        Log.d(TAG, "Starting simulation in phase: ${currentPhase.debugName}, paused: $wasPaused")

        // Always initialize with the default slow speed (300ms)
        baseInterval = 300L
        currentInterval = baseInterval

        binding.btnToggleSimulation.visibility = View.VISIBLE
        binding.btnToggleSimulation.text = "STOP VEHICLE"
        binding.btnToggleSimulation.setBackgroundColor(Color.rgb(180, 0, 0)) // Red

        // Show the follow toggle button
        binding.btnToggleFollow.visibility = View.VISIBLE
        if (isCameraFollowing) {
            binding.btnToggleFollow.text = "DISABLE AUTO-FOLLOW"
        } else {
            binding.btnToggleFollow.text = "ENABLE AUTO-FOLLOW"
        }

        // Initialize speed indicator to NORMAL by default
        updateSpeedIndicator("NORMAL")
        isVehiclePaused = wasPaused
        if (isFirstTime) {
            val notificationText = findViewById<TextView>(R.id.notification_text)
            NotificationProcessor.processNotification(
                message = "🚦 Starting Navigation",
                notificationText = notificationText,
                adjustVehicleSpeed = this::adjustVehicleSpeed
            )
        }

        simulationRunnable = object : Runnable {
            @SuppressLint("DefaultLocale")
            override fun run() {
                if (stepIndex >= pathPoints.size - 1) {  // Notice -1 because we're using end point
                    handleDestinationReached()
                    return
                }

                val start = pathPoints[stepIndex]
                val end = pathPoints[stepIndex + 1]

                animateMarker(start, end, currentInterval)

                if (!trafficNotificationsDisabled) {
                    val current = pathPoints[stepIndex]

                    // Track if we should process airport notifications this step
                    val processAirport = currentPhase == NavigationPhase.TO_PICKUP

                    Log.d("Agent List", "$agentList")
                    // === DALI MOCK Based on % Distance to Agent ===
                    if (currentAgentIndex < agentList.size) {
                        val (agentName, agentLatLng) = agentList[currentAgentIndex]
                        val distanceToAgent = calculateDistance(current, agentLatLng)

                        // When switching to a new agent, reset base distance
                        if (agentJustUpdated) {
                            agentInitialDistance = distanceToAgent
                            agentJustUpdated = false
                        }

                        Log.d(
                            "AGENT_DEBUG",
                            "Processing agent: $agentName at index $currentAgentIndex"
                        )
                        Log.d("AGENT_DEBUG Calculations", "Processing agent: $distanceToAgent")

                        if (distanceToAgent <= 30.00) {
                            currentAgentIndex++
                            agentJustUpdated = true
                        }

                        if (stepIndex % 15 == 0) {
                            handleDaliApiCalls(agentName, agentLatLng, current)
                            lastDaliAgentIndex = currentAgentIndex
                            processedAgentNames.add(agentName)
                            Log.d(TAG, "Agent processed: $agentName")
                        }
                    }

                    // === AIRPORT MOCK (milestone-based) ===
                    // Only process if we're in pickup phase and no DALI notification was shown
                    if (processAirport && currentPhase == NavigationPhase.TO_PICKUP) {
                        val progress = (stepIndex.toDouble() / pathPoints.size).coerceIn(0.0, 1.0)
                        val roundedProgress = String.format("%.1f", progress).toDouble()

                        // New airport call milestones (15%, 30%, 45%, 60%, 75%, 90%)
                        val airportMilestones = setOf(0.15, 0.3, 0.45, 0.6, 0.75, 0.9)

                        if (airportMilestones.contains(roundedProgress) &&
                            !triggeredAirportMilestones.contains(roundedProgress) &&
                            System.currentTimeMillis() - lastNotificationTime > MIN_NOTIFICATION_INTERVAL
                        ) {

                            handleAirportApiCalls(current)
                            triggeredAirportMilestones.add(roundedProgress)
                            lastMockTriggered = "AIRPORT"
                            lastNotificationTime = System.currentTimeMillis()
                        }
                    }
                }

                stepIndex++
                // Only post new callback if not paused
                if (!isVehiclePaused) {
                    handler.postDelayed(this, currentInterval)
                }
            }
        }

// Only start simulation if not paused
        if (!isVehiclePaused) {
            handler.postDelayed(simulationRunnable!!, currentInterval)
        }
    }

    private fun toggleVehiclePause() {
        isVehiclePaused = !isVehiclePaused

        if (isVehiclePaused) {
            // Pause the vehicle
            handler.removeCallbacks(simulationRunnable!!)
            binding.btnToggleSimulation.text = "RESUME VEHICLE"
            binding.btnToggleSimulation.setBackgroundColor(Color.rgb(0, 150, 0)) // Green

            // Make sure to set DALI as source and include clear debug logs
            Log.d(TAG, "Displaying VEHICLE STOPPED notification with DALI source")
            updateSpeedIndicator("PAUSED")
        } else {
            // Resume the vehicle with current interval
            binding.btnToggleSimulation.text = "STOP VEHICLE"
            binding.btnToggleSimulation.setBackgroundColor(Color.rgb(180, 0, 0)) // Red

            // Make sure to set DALI as source and include clear debug logs
            Log.d(TAG, "Displaying VEHICLE MOVING notification with DALI source")

            // Re-apply last speed if there was one, otherwise use normal speed
            val lastType = NotificationProcessor.getLastNotificationType()
            if (lastType != null) {
                adjustVehicleSpeed(lastType)
            } else {
                // Default to normal speed if no last notification type
                currentInterval = baseInterval
                updateSpeedIndicator("NORMAL")
                handler.postDelayed(simulationRunnable!!, currentInterval)
            }
        }
    }

    private fun handleDestinationReached() {
        if (!destinationReached) {
            destinationReached = true
            trafficNotificationsDisabled = true

            Log.d(TAG, "Destination reached in phase: ${currentPhase.debugName}")

            val notificationText = findViewById<TextView>(R.id.notification_text)

            when (currentPhase) {
                NavigationPhase.TO_PICKUP -> {
                    // We've reached the pickup location
                    Log.d(TAG, "PICKUP COMPLETED - Transitioning to AT_PICKUP phase")
                    currentPhase = NavigationPhase.AT_PICKUP
                    binding.btnStartNavigation.isEnabled = true
                    binding.btnStartNavigation.text = "Start Dropoff Navigation"

                    // Show notification with AIRPORT source since it's a destination notification
                    NotificationProcessor.displayNotification(
                        notificationText = notificationText,
                        message = "Pickup destination reached! Ready for dropoff.",
                        type = NotificationType.POSITIVE,
                        isVehiclePaused = isVehiclePaused,
                        adjustVehicleSpeed = this::adjustVehicleSpeed,
                    )

                    binding.btnToggleSimulation.visibility = View.INVISIBLE
                    binding.btnToggleSimulation.text = "At the Docking Port"
                    binding.btnToggleFollow.visibility = View.INVISIBLE
                }

                NavigationPhase.TO_DROPOFF -> {
                    // We've reached the final destination
                    Log.d(TAG, "DROPOFF COMPLETED - Navigation complete")
                    currentPhase = NavigationPhase.COMPLETED

                    // Make the delivery completed button enabled and visible
                    binding.btnStartNavigation.isEnabled = true
                    binding.btnStartNavigation.text = "Delivery Completed"
                    // Make the stop vehicle button GONE (not just invisible)
                    binding.btnToggleSimulation.visibility = View.GONE
                    binding.btnToggleFollow.visibility = View.GONE

                    // Show notification with AIRPORT source since it's a destination notification
                    NotificationProcessor.displayNotification(
                        notificationText = notificationText,
                        message = "Dropoff destination reached! Delivery completed.",
                        type = NotificationType.POSITIVE,
                        isVehiclePaused = isVehiclePaused,
                        adjustVehicleSpeed = this::adjustVehicleSpeed,
                    )
                }

                else -> {
                    binding.btnStartNavigation.isEnabled = false
                    binding.btnStartNavigation.text = "Destination Reached"
                }
            }
        }
    }

    private fun handleAirportApiCalls(current: LatLng) {
        Thread {
            val airportMessage =
                MockApiManager.sendLocationToAirportMock(airportEndpoint, current, client, cargo_ID, terminalName)

            runOnUiThread {
                val notificationText = findViewById<TextView>(R.id.notification_text)
                NotificationProcessor.processNotification(
                    message = "✈️ $airportMessage",
                    notificationText = notificationText,
                    onParkingAvailable = {
                        val newDestinationLocation = terminalParkingLocations[terminalName]
                        if (newDestinationLocation != null) {
                            rerouteFromCurrentLocation(current, newDestinationLocation)
                        } else {
                            Log.e("PARKING_REROUTE", "Null dropoff location")
                        }
                    },
                    onPullOver = {
                        handler.removeCallbacks(simulationRunnable!!)
                        isVehiclePaused = true
                        binding.btnToggleSimulation.text = "RESUME VEHICLE"
                        binding.btnToggleSimulation.setBackgroundColor(Color.rgb(0, 150, 0))
                    },
                    currentPosition = current,
                    adjustVehicleSpeed = this::adjustVehicleSpeed
                )
            }
        }.start()
    }

    private fun calculateDistance(p1: LatLng, p2: LatLng): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            results
        )
        return results[0].toDouble()
    }

    private fun handleDaliApiCalls(agentName: String, agentLatLng: LatLng, current: LatLng) {
        Thread {

            val daliResponse = MockApiManager.sendLocationToDaliMock(daliEndpoint, agentName, agentLatLng, client)

            runOnUiThread {
                val notificationText = findViewById<TextView>(R.id.notification_text)
                NotificationProcessor.processNotification(
                    message = "🚦 ${daliResponse.message}",
                    agentState = daliResponse.agentState,
                    notificationText = notificationText,
                    onAccidentReroute = { position ->
                        val destination =
                            if (currentPhase == NavigationPhase.TO_PICKUP) pickupLocation else dropoffLocation
                        destination?.let { rerouteFromCurrentLocation(position, it) }
                    },
                    currentPosition = current,
                    adjustVehicleSpeed = this::adjustVehicleSpeed
                )
            }
        }.start()
    }

    private fun resetSimulation(keepHistory: Boolean = false) {
        stepIndex = 0
        currentAgentIndex = 0
        speedIncreased = false
        speedDecreased = false
        triggeredNotifications.clear()
        if (!keepHistory) {
            previouslyTraveled.clear()
        }
        destinationReached = false
        trafficNotificationsDisabled = false
        parkingSpotAssigned = false
        accidentAlreadyHandled = false
        isVehiclePaused = false
        baseInterval = 300L
        currentInterval = baseInterval
        currentSpeed = "NORMAL"
    }

    override fun onDestroy() {
        simulationRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }
}

