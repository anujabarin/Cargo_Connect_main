package com.example.cargolive.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.cargolive.MainActivity
import com.example.cargolive.R
import com.example.cargolive.data.repository.AuthRepository
import com.example.cargolive.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize authentication repository
        authRepository = AuthRepository(this)

        // Show animation if you have any
        showSplashAnimation()

        // Delay for splash screen display
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if user is already logged in
            if (authRepository.isLoggedIn()) {
                // If logged in, go to main activity
                navigateToMain()
            } else {
                // If not logged in, go to login screen
                navigateToLogin()
            }
        }, SPLASH_DELAY)
    }

    private fun showSplashAnimation() {
        // Optional: Add animations to your logo or app name
        binding.ivLogo.visibility = View.VISIBLE
        binding.tvAppName.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val SPLASH_DELAY = 2000L // 2 seconds
    }
}