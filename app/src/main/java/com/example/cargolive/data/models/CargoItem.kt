package com.example.cargolive.data.models

import java.util.*

data class CargoItem(
    val cargoId: String,
    val description: String,
    val Cargoweight: Double,
    val pickupLocation: String,
    val dropoffLocation: String,
    val criticality: String,
    val scheduledTime: String,
)

enum class CargoStatus {
    PENDING,
    IN_TRANSIT,
    DELIVERED,
    DELAYED
}