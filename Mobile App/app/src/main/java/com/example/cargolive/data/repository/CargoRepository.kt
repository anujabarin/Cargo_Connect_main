package com.example.cargolive.data.repository

import com.example.cargolive.data.api.RetrofitClient
import com.example.cargolive.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CargoRepository {
    private val TAG = "CargoRepository"
    private val apiService = RetrofitClient.apiService

    suspend fun getSchedules(): List<Schedule> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching all schedules from API")
            val response = apiService.getSchedules()
            
            if (response.isSuccessful) {
                val schedules = response.body() ?: emptyList()
                Log.d(TAG, "Successfully fetched ${schedules.size} schedules")
                return@withContext schedules
            } else {
                val errorMessage = "HTTP Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                // Fall back to mock data in case of error
                Log.d(TAG, "Returning mock schedules as fallback")
                return@withContext getMockSchedules()
            }
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Network error: Cannot reach server - check connection", e)
            return@withContext getMockSchedules()
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Network timeout: Server took too long to respond", e)
            return@withContext getMockSchedules()
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error: ${e.code()} - ${e.message()}", e)
            return@withContext getMockSchedules()
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception: ${e.message}", e)
            return@withContext getMockSchedules()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error fetching schedules", e)
            return@withContext getMockSchedules()
        }
    }

    suspend fun getCargoItemById(id: String): Schedule? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching schedule with ID: $id")
            val response = apiService.getScheduleById(id)
            
            if (response.isSuccessful) {
                val schedule = response.body()
                if (schedule != null) {
                    Log.d(TAG, "Successfully fetched schedule: ${schedule.cargoId}")
                    Log.d(TAG, "Pickup location: ${schedule.pickupLocation}")
                    Log.d(TAG, "Dropoff location: ${schedule.dropoffLocation}")
                    Log.d(TAG, "Pickup time: ${schedule.pickupTime}")
                    return@withContext schedule
                } else {
                    Log.e(TAG, "API returned null for schedule ID: $id")
                    // Fall back to mock data
                    return@withContext getMockSchedules().find { it.cargoId == id }
                }
            } else {
                val errorMessage = "HTTP Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                // Fall back to mock data
                Log.d(TAG, "Returning mock schedule as fallback for ID: $id")
                return@withContext getMockSchedules().find { it.cargoId == id }
            }
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Network error: Cannot reach server - check connection", e)
            return@withContext getMockSchedules().find { it.cargoId == id }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Network timeout: Server took too long to respond", e)
            return@withContext getMockSchedules().find { it.cargoId == id }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error: ${e.code()} - ${e.message()}", e)
            return@withContext getMockSchedules().find { it.cargoId == id }
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception: ${e.message}", e)
            return@withContext getMockSchedules().find { it.cargoId == id }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error fetching schedule with ID: $id", e)
            return@withContext getMockSchedules().find { it.cargoId == id }
        }
    }

    suspend fun updateLocation(scheduleId: String, latitude: Double, longitude: Double): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating location for schedule: $scheduleId - Lat: $latitude, Long: $longitude")
            
            val locationUpdate = mapOf(
                "scheduleId" to scheduleId,
                "latitude" to latitude,
                "longitude" to longitude,
                "timestamp" to System.currentTimeMillis()
            )
            
            try {
                val response = apiService.updateLocation(locationUpdate)
                val success = response.isSuccessful
                Log.d(TAG, "Location update result: $success")
                return@withContext success
            } catch (e: Exception) {
                Log.e(TAG, "Error updating location", e)
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in updateLocation", e)
            return@withContext false
        }
    }

    private fun getMockSchedules(): List<Schedule> {
        Log.d(TAG, "Generating mock schedules")
        
        return listOf(
            Schedule(
                cargoId = "sch1",
                pickupLocation = "UTD Terminal A (32.9851,-96.7501)",
                dropoffLocation = "DFW Airport Terminal C_32.8998,-97.0403",
                criticality = "Medium",
                pickupTime = "2023-04-17T14:00:00",
                weight = 600.00,
                description = "Test cargo"
            ),
            Schedule(
                cargoId = "sch2",
                pickupLocation = "UTD Terminal B (32.9851,-96.7501)",
                dropoffLocation = "DFW Airport Terminal D_32.8998,-97.0403",
                criticality = "High",
                pickupTime = "2023-04-19T06:00:00",
                weight = 1100.00,
                description = "Emergency medical supplies"
            )
        )
    }
}