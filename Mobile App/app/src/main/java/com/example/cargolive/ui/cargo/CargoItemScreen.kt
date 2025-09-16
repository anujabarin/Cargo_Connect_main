package com.example.cargolive.ui.cargo

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cargolive.data.models.Schedule
import com.example.cargolive.ui.maps.MapsActivity
import com.example.cargolive.utils.Constants
import com.example.cargolive.utils.DateTimeUtils

private const val TAG = "CargoItemScreen"

@Composable
fun CargoItemScreen(
    scheduleId: String,
    onBackClick: () -> Unit,
    viewModel: CargoViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(scheduleId) {
        Log.d(TAG, "Loading schedule with ID: $scheduleId")
        viewModel.loadScheduleDetails(scheduleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is CargoItemUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is CargoItemUiState.Success -> {
                    // Use a remembered state to handle errors instead of try-catch
                    var errorState by remember { mutableStateOf<String?>(null) }
                    
                    // Process location data safely outside the composable
                    // Use mutable variables for data that might be modified in the try-catch block
                    var pickupCoordinates by remember { mutableStateOf("0.0,0.0") }
                    var pickupName by remember { mutableStateOf("") }
                    var dropoffName by remember { mutableStateOf("") }
                    var dropoffCoordinates by remember { mutableStateOf("0.0,0.0") }
                    
                    // Set up effect to process data when the schedule changes
                    val schedule = state.schedule
                    LaunchedEffect(schedule) {
                        try {
                            pickupCoordinates = Schedule.extractCoordinates(schedule.pickupLocation)
                            pickupName = Schedule.extractLocationName(schedule.pickupLocation)
                            dropoffName = Schedule.extractLocationName(schedule.dropoffLocation)
                            dropoffCoordinates = Schedule.extractCoordinates(schedule.dropoffLocation)
                            
                            Log.d(TAG, "Successfully parsed location data")
                            Log.d(TAG, "Pickup: $pickupName, Coordinates: $pickupCoordinates")
                            Log.d(TAG, "Dropoff: $dropoffName, Coordinates: $dropoffCoordinates")
                            
                            // Clear any previous errors
                            errorState = null
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing location data", e)
                            errorState = "Error processing schedule data: ${e.message}"
                            
                            // Default values are already set
                            pickupName = schedule.pickupLocation
                            dropoffName = schedule.dropoffLocation
                        }
                    }
                    
                    // Show error if we had problems
                    if (errorState != null) {
                        Text(
                            text = errorState!!,
                            color = Color.Red,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    } else {
                        // No errors, show the schedule details
                        ScheduleDetails(
                            schedule = schedule,
                            onStartNavigationClick = { clickedSchedule ->
                                val intent = Intent(context, MapsActivity::class.java).apply {
                                    putExtra(Constants.EXTRA_SCHEDULE_ID, clickedSchedule.cargoId)
                                    putExtra(Constants.CRITICALITY, clickedSchedule.criticality)
                                    putExtra(Constants.EXTRA_PICKUP_LOC, pickupCoordinates)
                                    putExtra(Constants.EXTRA_TERMINAL_NAME, pickupName)
                                    putExtra(Constants.EXTRA_DROP_OFF_LOC, dropoffCoordinates)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                is CargoItemUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadScheduleDetails(scheduleId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleDetails(
    schedule: Schedule,
    onStartNavigationClick: (Schedule) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cargo ID",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = schedule.cargoId,
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        val criticalityColor = when (schedule.criticality.uppercase()) {
            "HIGH" -> Color(0xFFFFCDD2) // Light Red
            "MEDIUM" -> Color(0xFFFFF9C4) // Light Yellow
            "LOW" -> Color(0xFFC8E6C9) // Light Green
            else -> Color.LightGray
        }

        val criticalityTextColor = when (schedule.criticality.uppercase()) {
            "HIGH" -> Color.Red
            "MEDIUM" -> Color(0xFFFFA000)
            "LOW" -> Color(0xFF388E3C)
            else -> Color.Gray
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Surface(
                color = criticalityColor,
                shape = MaterialTheme.shapes.small,
                elevation = 2.dp
            ) {
                Text(
                    text = "Criticality: ${schedule.criticality}",
                    color = criticalityTextColor,
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Route details
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            backgroundColor = criticalityColor.copy(alpha = 0.2f) // subtle background tint
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Route Details",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Process location data safely outside the composable
                    val displayPickup = remember(schedule.pickupLocation) {
                        try {
                            Schedule.extractLocationName(schedule.pickupLocation)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error formatting pickup location", e)
                            schedule.pickupLocation // Fall back to raw string
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pickup Address",
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            text = displayPickup,
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Process location data safely outside the composable
                    val displayDropoff = remember(schedule.dropoffLocation) {
                        try {
                            Schedule.extractLocationName(schedule.dropoffLocation)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error formatting dropoff location", e)
                            schedule.dropoffLocation // Fall back to raw string
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Delivery Address",
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            text = displayDropoff,
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Process time data safely outside the composable
                    val displayTime = remember(schedule.pickupTime) {
                        try {
                            DateTimeUtils.formatForDisplay(schedule.pickupTime)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error formatting pickup time", e)
                            schedule.pickupTime // Fall back to raw string
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pickup Time:",
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            text = displayTime,
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Transportation Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            backgroundColor = criticalityColor.copy(alpha = 0.2f) // Subtle background tint
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Transportation Details",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Vehicle ID:",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "TK-101",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cargo Weight:",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${schedule.weight} lb",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation button
        Button(
            onClick = { onStartNavigationClick(schedule) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Map, contentDescription = "Map")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Navigation")
            }
        }
    }
}