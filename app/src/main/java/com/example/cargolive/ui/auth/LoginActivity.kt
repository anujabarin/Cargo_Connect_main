package com.example.cargolive.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cargolive.MainActivity
import com.example.cargolive.data.repository.AuthRepository
import com.example.cargolive.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            authRepository = AuthRepository(this)

            // Check if user is already logged in
            if (authRepository.isLoggedIn()) {
                navigateToMainActivity()
                return
            }

            setupClickListeners()
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing app. Please restart.", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun setupClickListeners() {
        // Login button click listener
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateLoginInputs(username, password)) {
                performLogin(username, password)
            }
        }

        // Register button click listener
        binding.btnRegister.setOnClickListener {
            try {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error navigating to RegisterActivity", e)
                Toast.makeText(this, "Error opening registration", Toast.LENGTH_SHORT).show()
            }
        }

        // Demo login button click listener
        binding.btnDemoLogin.setOnClickListener {
            // Pre-fill form w/ demo credentials
            binding.etUsername.setText(AuthRepository.DEMO_EMAIL)
            binding.etPassword.setText(AuthRepository.DEMO_PASSWORD)

            // Perform login w/ demo credentials
            performLogin(AuthRepository.DEMO_EMAIL, AuthRepository.DEMO_PASSWORD)
        }
    }

    private fun validateLoginInputs(username: String, password: String): Boolean {
        var isValid = true

        // Clear all errors
        binding.tilUsername.error = null
        binding.tilPassword.error = null

        if (username.trim().isEmpty()) {
            binding.tilUsername.error = "Email required"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password required"
            isValid = false
        }

        return isValid
    }

    private fun performLogin(username: String, password: String) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val result = authRepository.login(username, password)

                if (result.isSuccess) {
                    // Login successful; go to main screen
                    Log.d("LoginActivity", "Login successful for $username")
                    navigateToMainActivity()
                } else {
                    val exception = result.exceptionOrNull()
                    // Show error message
                    val errorMsg = when {
                        exception?.message?.contains("401") == true -> "Invalid username or password"
                        exception?.message?.contains("404") == true -> "Account not found"
                        exception?.message?.contains("network") == true -> "Network error. Check your connection"
                        else -> exception?.message ?: "Login failed"
                    }

                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error during login", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Something went wrong. Try again later.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun navigateToMainActivity() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error navigating to MainActivity", e)
            Toast.makeText(
                this,
                "Error navigating to main screen. Please restart the app.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.btnRegister.isEnabled = !show
        binding.btnDemoLogin.isEnabled = !show
    }
}