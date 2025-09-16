package com.example.cargolive

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cargolive.data.repository.AuthRepository
import com.example.cargolive.ui.auth.LoginActivity
import com.example.cargolive.ui.cargo.CargoItemScreen
import com.example.cargolive.ui.schedule.ScheduleListScreen
import com.example.cargolive.ui.theme.CargoLiveTheme
import com.example.cargolive.utils.LocalNotificationHelper
import com.jakewharton.threetenabp.AndroidThreeTen

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Initialize authentication repository
            authRepository = AuthRepository(this)

            // Check if user is logged in
            if (!authRepository.isLoggedIn()) {
                navigateToLogin()
                return
            }

            AndroidThreeTen.init(this)
            setContent {
                CargoLiveTheme {
                    val context = this
                    val navController = rememberNavController()

                    // 🔐 Handle POST_NOTIFICATIONS permission
                    var permissionGranted by remember { mutableStateOf(false) }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        permissionGranted = isGranted
                        if (isGranted) {
                            LocalNotificationHelper.showTestNotification(context)
                        }
                    }

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val granted = ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED

                            if (granted) {
                                LocalNotificationHelper.showTestNotification(context)
                            } else {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            LocalNotificationHelper.showTestNotification(context)
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(navController = navController, startDestination = "schedules") {
                            composable("schedules") {
                                ScheduleListScreen(
                                    onScheduleClick = { scheduleId ->
                                        try {
                                            navController.navigate("cargo/$scheduleId")
                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "Navigation error: ${e.message}")
                                        }
                                    },
                                    onLogout = {
                                        // Handle logout action
                                        logout()
                                    }
                                )
                            }
                            composable("cargo/{scheduleId}") { backStackEntry ->
                                val scheduleId = backStackEntry.arguments?.getString("scheduleId") ?: ""
                                CargoItemScreen(
                                    scheduleId = scheduleId,
                                    onBackClick = {
                                        try {
                                            navController.popBackStack()
                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "Back navigation error: ${e.message}")
                                            finish()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate: ${e.message}")
            navigateToLogin()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        // Log the user out via repository
        authRepository.logout()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}