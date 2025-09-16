package com.example.cargolive.data.api

import com.example.cargolive.data.models.Schedule
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Body

interface ApiService {
    /**
     * Get all schedules
     * 
     * @return List of schedules
     */
    @GET("schedule")
    suspend fun getSchedules(): Response<List<Schedule>>

    /**
     * Get a specific schedule by ID
     * Note: Although endpoint says "cargo/{id}", it returns a Schedule object
     * 
     * @param id The schedule/cargo ID
     * @return The schedule details
     */
    @GET("cargo/{id}")
    suspend fun getScheduleById(@Path("id") id: String): Response<Schedule>

    /**
     * Update location for a schedule
     * 
     * @param locationUpdate Map containing scheduleId, latitude, longitude, and timestamp
     * @return Response with status
     */
    @POST("location/update")
    suspend fun updateLocation(
        @Body locationUpdate: Map<String, Any>
    ): Response<Map<String, Any>>
}