package com.example.cargolive.services

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.cargolive.utils.EmojiUtils
import com.google.android.gms.common.api.internal.ApiKey
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set
import com.google.android.gms.maps.GoogleMap

object RouteManager {

    fun fetchRouteFromApi(
        apiKey: String,
        origin: LatLng,
        dest: LatLng,
        avoidHighways: Boolean = false,
        googleMap: GoogleMap,
        callback: (List<LatLng>, Map<String, LatLng>) -> Unit
    ) {
        val intersectionPoints =
            mutableMapOf<String, LatLng>() // New dictionary to hold intersections
        val avoidParam = if (avoidHighways) "&avoid=highways" else ""
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}$avoidParam&key=${apiKey}"

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val routes = json.getJSONArray("routes")
                val route = routes.getJSONObject(0)
                val overviewPolyline = route.getJSONObject("overview_polyline").getString("points")
                val decodedPath = decodePolyline(overviewPolyline)

                val legs = route.getJSONArray("legs")
                if (legs.length() > 0) {
                    val steps = legs.getJSONObject(0).getJSONArray("steps")
                    var agentCounter = 1 // To create unique keys like Agent1, Agent2, etc.

                    for (i in 0 until steps.length()) {
                        val step = steps.getJSONObject(i)
                        val maneuver =
                            step.optString("maneuver", "none") // e.g., turn-right, turn-left
                        val startLocation = step.getJSONObject("start_location")
                        val lat = startLocation.getDouble("lat")
                        val lng = startLocation.getDouble("lng")

                        // Save only if maneuver is meaningful (skip "none" if you want)
                        if (maneuver != "none") {
                            val agentKey = "Agent$agentCounter"
                            val latLng = LatLng(lat, lng)

                            intersectionPoints[agentKey] = latLng
                            agentCounter++

                            // Log it
                            Log.d("INTERSECTION", "[$agentKey] Maneuver: $maneuver at ($lat, $lng)")
                        }
                    }
                }
                // Plot the markers on the map after everything is parsed
                Handler(Looper.getMainLooper()).post {
                    callback(decodedPath, intersectionPoints)
                }

            } catch (e: Exception) {
                Log.e("ROUTE_API FINAL", "Route fetch failed: ${e.message}", e)
                e.printStackTrace()
            }
        }.start()
    }

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            poly.add(LatLng(lat / 1e5, lng / 1e5))
        }
        return poly
    }
}