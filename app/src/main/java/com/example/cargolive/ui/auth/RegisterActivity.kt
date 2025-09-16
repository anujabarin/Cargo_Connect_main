package com.example.cargolive.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cargolive.MainActivity
import com.example.cargolive.databinding.ActivityRegisterBinding
import com.example.cargolive.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityRegisterBinding.inflate(layoutInflater)
            setContentView(binding.root)

            authRepository = AuthRepository(this)

            setupClickListeners()
        } catch (e: Exception) {
            Log.e("RegisterActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing app. Please restart.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        // Register button click listener
        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateRegistrationInputs(fullName, email, password, confirmPassword)) {
                performRegistration(fullName, email, password)
            }
        }

        // Login button click listener (to navigate back to login)
        binding.btnLogin.setOnClickListener {
            finish() // Close this activity and go back to login
        }
    }

    private fun validateRegistrationInputs(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        // Clear all errors
        binding.tilFullName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Name required"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password required"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Please confirm password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun performRegistration(fullName: String, email: String, password: String) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val result = authRepository.register(fullName, email, password)

                if (result.isSuccess) {
                    // Registration successful
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration successful!",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToMainActivity()
                } else {
                    val exception = result.exceptionOrNull()
                    // Show error message
                    val errorMsg = when {
                        exception?.message?.contains("409") == true -> "Email already registered"
                        exception?.message?.contains("network") == true -> "Network error. Check your connection"
                        else -> exception?.message ?: "Registration failed"
                    }

                    Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("RegisterActivity", "Error during registration", e)
                Toast.makeText(
                    this@RegisterActivity,
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
            Log.e("RegisterActivity", "Error navigating to MainActivity", e)
            Toast.makeText(this, "Error navigating to main screen.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
        binding.btnLogin.isEnabled = !show
    }

    // Handle back button in action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}