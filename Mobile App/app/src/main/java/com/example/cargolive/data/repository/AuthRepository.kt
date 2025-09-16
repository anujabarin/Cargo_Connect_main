package com.example.cargolive.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.cargolive.data.api.RetrofitClient
import com.example.cargolive.data.models.User
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {

    companion object {
        private const val PREF_NAME = "cargo_connect_prefs"
        private const val KEY_USER = "current_user"
        private const val KEY_TOKEN = "auth_token"

        const val DEMO_EMAIL = "demo@cargolive.com"
        const val DEMO_PASSWORD = "demo123"
        private const val KEY_IS_DEMO = "is_demo_account"
    }

    private val authService = RetrofitClient.authService
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            if (email == DEMO_EMAIL && password == DEMO_PASSWORD) {
                val demoUser = User(
                    id = "demo-123",
                    email = DEMO_EMAIL,
                    fullName = "Demo User",
                    isGoogleAccount = false
                )
                saveUserToPreferences(demoUser, "demo-token-123", isDemoAccount = true)
                return@withContext Result.success(demoUser)
            }

            val loginRequest = mapOf("email" to email, "password" to password)
            val response = authService.login(loginRequest)

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val token = responseBody["token"] as? String ?: ""
                    val userData = responseBody["user"] as? Map<*, *>
                    
                    // Use safe factory method to create user
                    val user = User.fromMapSafely(userData)
                    
                    Log.d("AuthRepository", "Login successful. User: $user")
                    
                    saveUserToPreferences(user, token, isDemoAccount = false)
                    return@withContext Result.success(user)
                } else {
                    return@withContext Result.failure(Exception("Empty response body"))
                }
            } else {
                return@withContext Result.failure(Exception("Login failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error", e)
            return@withContext Result.failure(e)
        }
    }

    suspend fun register(fullName: String, email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val registerRequest = mapOf("fullName" to fullName, "email" to email, "password" to password)
            val response = authService.register(registerRequest)

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    try {
                        val token = responseBody["token"] as? String ?: ""
                        val userData = responseBody["user"] as? Map<*, *>
                        
                        Log.d("AuthRepository", "Registration response: $responseBody")
                        
                        // Use safe factory method to create user
                        val user = User.fromMapSafely(userData)
                        
                        Log.d("AuthRepository", "Registered User: $user")
                        
                        saveUserToPreferences(user, token, isDemoAccount = false)
                        return@withContext Result.success(user)
                    } catch (e: Exception) {
                        Log.e("AuthRepository", "Error parsing user data", e)
                        return@withContext Result.failure(Exception("Error parsing response: ${e.message}"))
                    }
                } else {
                    return@withContext Result.failure(Exception("Empty response body"))
                }
            } else {
                return@withContext Result.failure(Exception("Registration failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Registration error", e)
            return@withContext Result.failure(e)
        }
    }

    fun getLoggedInUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Corrupted user JSON, clearing preferences", e)
            sharedPreferences.edit().remove(KEY_USER).apply()
            null
        }
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun isLoggedIn(): Boolean {
        return getLoggedInUser() != null && getAuthToken() != null
    }

    fun logout() {
        sharedPreferences.edit().apply {
            remove(KEY_USER)
            remove(KEY_TOKEN)
            apply()
        }
    }

    private fun saveUserToPreferences(user: User, token: String, isDemoAccount: Boolean = false) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().apply {
            putString(KEY_USER, userJson)
            putString(KEY_TOKEN, token)
            putBoolean(KEY_IS_DEMO, isDemoAccount)
            apply()
        }
    }

    fun isDemoAccount(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_DEMO, false)
    }
}