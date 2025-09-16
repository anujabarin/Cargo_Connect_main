package com.example.cargolive.data.models

data class Location(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val contactPerson: String? = null,
    val contactPhone: String? = null
)