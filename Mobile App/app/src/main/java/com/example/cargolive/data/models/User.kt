package com.example.cargolive.data.models

// Represents user in the system
data class User(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val isGoogleAccount: Boolean = false // Keeping for backward compatibility
) {
    // Ensure all properties are valid
    fun isValid(): Boolean {
        return id.isNotEmpty() && email.isNotEmpty() && fullName.isNotEmpty()
    }
    
    // Companion object for utility methods
    companion object {
        // Factory method to create user w/ null safety
        fun fromMapSafely(userData: Map<*, *>?): User {
            if (userData == null) return User()
            
            return User(
                id = userData["id"] as? String ?: "",
                email = userData["email"] as? String ?: "",
                fullName = userData["fullName"] as? String ?: userData["full_name"] as? String ?: "",
                isGoogleAccount = false // Google Sign-In removed, but keeping field for compatibility
            )
        }
    }
}