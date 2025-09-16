package com.example.cargolive.services

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

object MockApiManager {
    private var useDaliMockApi = true
//    val DaliURL = "https://8447-129-110-242-17.ngrok-free.app"
//    val AirportURL = "https://417c-129-110-242-17.ngrok-free.app"
//    val DALI_MOCK_URL = "${DaliURL}/driver/location"
//    val AIRPORT_MOCK_URL = "${AirportURL}/get-cargo-status"
    private var accidentAlreadySent = false

    fun sendLocationToDaliMock(DaliEndURL: String, agentName: String, agentLatLng: LatLng, client: OkHttpClient): DaliResponse {
        val DaliFullURL = "${DaliEndURL}/driver/location"

        // IF DALI is not available
        if (!useDaliMockApi) {
            val dummyMessages = mutableListOf(
                "Speed up! Green signal ahead",
                "Caution ahead",
                "Bad Weather",
                "Maintain lane for 1 mile",
            )
            if (!accidentAlreadySent) {
                dummyMessages.add("Accident Ahead")  // Add accident only once
            }

            val message = dummyMessages.random()

            if (message == "Accident Ahead") {
                accidentAlreadySent = true // Mark it so it won't be added next time
            }

            // For mock data, determine state based on message content
            val state = when {
                message.contains("Speed up") || message.contains("Green") -> "G"
                message.contains("Accident") || message.contains("Bad Weather") -> "R"
                else -> "Y"
            }

            return DaliResponse(message, state)
        }

        // Use DALI MOCK
        val json = JSONObject().apply {
            put("next_agent_code", agentName)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(DaliFullURL)
            .post(requestBody)
            .build()
        Log.d("Current Agent Name:", "$json")

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.let { body ->
                        val jsonResponse = JSONObject(body)
                        val message = jsonResponse.optString("message", "No message")
                        val agentState = jsonResponse.optString("agent_state", "Y") // Default to Yellow if not specified
                        DaliResponse(message, agentState)
                    } ?: DaliResponse("Empty response", "Y")
                } else {
                    // Read error body (if any) and log everything
                    val errorBody = response.body?.string().orEmpty()
                    Log.e(
                        "DALI_API_ERROR",
                        "HTTP ${response.code} ${json}. Response body: $errorBody"
                    )
                    DaliResponse("Failed with code ${response.code}: ${response.message}", "Y")
                }
            }
        } catch (e: Exception) {
            Log.e("DALI_API_EXCEPTION", "Network call failed: ${e.localizedMessage}", e)
            DaliResponse("Network error: ${e.localizedMessage}", "Y")
        }
    }

    // Data class to hold both message and state
    data class DaliResponse(val message: String, val agentState: String)

    fun sendLocationToAirportMock(airportEndURL: String, position: LatLng, client: OkHttpClient, cargo_ID: String, terminalName: String): String{
        val AirportFullURL = "${airportEndURL}/get-cargo-status"
        val fullUrl = "$AirportFullURL?cargo_id=${cargo_ID}&terminal=${terminalName}"

        val request = Request.Builder()
            .url(fullUrl)
            .get()
            .build()

         return try {
             val response = client.newCall(request).execute()
             response.use {
                 if (it.isSuccessful) {
                     val body = it.body?.string()
                     if (body != null) {
                         val json = JSONObject(body)
                         json.optString("message", "No message")
                     } else {
                         "Empty response from Airport Mock"
                     }
                 } else {
                     "Failed with code: ${it.code}"
                 }
             }
         } catch (e: Exception) {
             Log.e("Airport_Mock_API", "Network call failed", e)
             "Network error"
         }
    }
}