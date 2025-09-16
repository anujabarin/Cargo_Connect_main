// TrafficAlertManager --- V2
package com.example.cargolive.utils

import com.google.android.gms.maps.model.LatLng

data class TrafficAlert(
    val id: Int,
    val latLng: LatLng,
    val message: String,
    val title: String
)

object TrafficAlertManager {
    // Predefined navigation messages to show during the simulation
    val navigationMessages = listOf(
        "School zone ahead, reduce speed",
        "Heavy traffic ahead",
        "Accident reported - Rerouting!",
        "Construction zone, Beware of Surrounding",
        "Speed limit changed to 55 mph",
        "Weather alert: Rain detected",
        "Toll booth ahead",
        "Speed camera ahead",
        "Rest area in 2 miles",
        "Traffic signal turning green",
        "Pedestrian crossing ahead",
        "Sharp turn ahead, reduce speed",
    )

    // Generate equally spaced alerts along the route
    fun generateEquallySpacedAlerts(route: MutableList<LatLng>, enableAccident: Boolean = true): MutableList<TrafficAlert>{
        if (route.size < navigationMessages.size) return mutableListOf()

        val alerts = mutableListOf<TrafficAlert>()
        val segmentSize = route.size / navigationMessages.size

        for (i in navigationMessages.indices) {
            val index = if (i == navigationMessages.size - 1) {
                // Place the accident alert near the end but not at the very end
                (route.size * 0.9).toInt()
            } else {
                i * segmentSize
            }

            val message = navigationMessages[i]
            if (!enableAccident && message.contains("Accident")) continue // ✅ Skip accident if not high

            val title = when {
                message.contains("Accident") -> "Accident Alert"
                else -> "Traffic Alert"
            }

            alerts.add(TrafficAlert(
                id = i + 1,
                latLng = route[index],
                message = message,
                title = title
            ))
        }

        return alerts
    }

    // Generate the remaining alerts after a reroute
    fun generateRemainingAlerts(
        remainingRoute: MutableList<LatLng>,
        remainingMessages: List<String>
    ): MutableList<TrafficAlert> {
        if (remainingRoute.size < remainingMessages.size) return mutableListOf()

        val alerts = mutableListOf<TrafficAlert>()
        val segmentSize = remainingRoute.size / remainingMessages.size

        for (i in remainingMessages.indices) {
            val index = i * segmentSize
            val message = remainingMessages[i]
            val title = if (message.contains("Accident")) "Accident Alert" else "Traffic Alert"

            alerts.add(
                TrafficAlert(
                    id = i + 100, // shift ID range
                    latLng = remainingRoute[index],
                    message = message,
                    title = title
                )
            )
        }
        return alerts
    }
}