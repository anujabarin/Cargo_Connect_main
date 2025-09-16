package com.example.cargolive.data.models

import android.util.Log
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.util.*

data class Schedule(
    @SerializedName("cargoId")
    val cargoId: String,
    
    @SerializedName("pickupLocation")
    val pickupLocation: String,
    
    @SerializedName("dropoffLocation")
    val dropoffLocation: String,
    
    @SerializedName("criticality")
    val criticality: String,
    
    @SerializedName("pickupTime")
    val pickupTime: String,
    
    @SerializedName("weight")
    val weight: Double,
    
    @SerializedName("description")
    val description: String,
) {
    companion object {
        private const val TAG = "Schedule"
        
        // Helper method to safely format pickup time
        fun formatPickupTime(pickupTime: String): String {
            return try {
                // Try several date format patterns
                if (pickupTime.contains("T")) {
                    // Looks like ISO format
                    val dateTime = LocalDateTime.parse(pickupTime)
                    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy - h:mm a")
                    dateTime.format(formatter)
                } else if (pickupTime.contains(",")) {
                    // Already in a readable format like "Apr 17th, 14:00"
                    pickupTime
                } else {
                    // Unknown format, return as-is
                    pickupTime
                }
            } catch (e: DateTimeParseException) {
                Log.e(TAG, "Could not parse pickup time: $pickupTime", e)
                pickupTime
            } catch (e: Exception) {
                Log.e(TAG, "Error formatting pickup time: $pickupTime", e)
                pickupTime
            }
        }
        
        // Helper method to safely extract coordinates from location string
        fun extractCoordinates(location: String): String {
            return try {
                if (location.contains("(") && location.contains(")")) {
                    location.substringAfter("(").substringBefore(")")
                } else if (location.contains("_")) {
                    location.split("_").getOrNull(1) ?: "0.0,0.0"
                } else {
                    "0.0,0.0" // Default coordinates
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting coordinates from: $location", e)
                "0.0,0.0" // Default on error
            }
        }
        
        // Helper method to safely extract location name
        fun extractLocationName(location: String): String {
            return try {
                if (location.contains("(")) {
                    location.substringBefore("(")
                } else if (location.contains("_")) {
                    location.split("_").getOrNull(0) ?: location
                } else {
                    location
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting location name from: $location", e)
                location
            }.replace("-", " ").replace("_", "").trim().uppercase()
        }
    }
}

enum class ScheduleStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}