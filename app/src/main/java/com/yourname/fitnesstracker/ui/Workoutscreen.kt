package com.yourname.fitnesstracker.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourname.fitnesstracker.sensors.LocationTracker
import com.yourname.fitnesstracker.sensors.ConditionalStepCounter
import com.yourname.fitnesstracker.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp

/**
 * Main workout tracking screen with real-time sensor monitoring.
 * Handles permissions, tracks steps/location, displays progress, and manages workout sessions.
 */
@Composable
fun WorkoutScreen(
    mainViewModel: MainViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val uiState by mainViewModel.uiState.collectAsState()
    val workoutState by mainViewModel.workoutState.collectAsState()

    // Initialize sensor trackers
    val stepCounter = remember { ConditionalStepCounter(context) }
    val locationTracker = remember { LocationTracker(context) }

    // Location tracking state for distance calculation
    var lastLocation by remember { mutableStateOf<Location?>(null) }
    var totalDistance by remember { mutableStateOf(0f) }

    // Permission management state
    var permissionsGranted by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    var sensorAvailable by remember { mutableStateOf(true) }

    // Define required permissions based on Android version
    val requiredPermissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
        if (!permissionsGranted) {
            showPermissionRationale = true
        }
    }

    //Check sensor availability when screen loads
    LaunchedEffect(Unit) {
        sensorAvailable = stepCounter.isAccelerometerAvailable()

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            permissionsGranted = true
        }
    }

    // Manage sensor tracking based on workout state
    LaunchedEffect(workoutState.isTracking) {
        if (workoutState.isTracking && permissionsGranted && sensorAvailable) {
            // CHANGED: Start step counting with accelerometer
            stepCounter.startTracking { steps ->
                mainViewModel.updateWorkoutSteps(steps)
            }

            // Start location tracking for distance calculation
            locationTracker.startTracking { location ->
                lastLocation?.let { lastLoc ->
                    val distance = lastLoc.distanceTo(location)
                    totalDistance += distance
                    mainViewModel.updateWorkoutDistance(totalDistance)
                }
                lastLocation = location
            }
        } else {
            // Stop all tracking and reset state
            stepCounter.stopTracking()
            locationTracker.stopTracking()
            lastLocation = null
            totalDistance = 0f
        }
    }

    // Update workout duration every second during tracking
    LaunchedEffect(workoutState.isTracking) {
        while (workoutState.isTracking) {
            delay(1000)
            mainViewModel.updateWorkoutSteps(workoutState.steps) // Trigger UI update
        }
    }

    // Cleanup sensors when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            stepCounter.stopTracking()
            locationTracker.stopTracking()
        }
    }

    // Show permission rationale dialog if needed
    if (showPermissionRationale) {
        PermissionRationaleDialog(
            onDismiss = { showPermissionRationale = false },
            onRequestPermissions = {
                showPermissionRationale = false
                permissionLauncher.launch(requiredPermissions.toTypedArray())
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Screen header
        Text(
            text = "Workout Tracking",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        //Show sensor availability error if needed
        if (!sensorAvailable) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Accelerometer not available on this device. Step counting will not work.",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Show permission request or workout content
        if (!permissionsGranted) {
            PermissionRequiredCard(
                onRequestPermissions = {
                    permissionLauncher.launch(requiredPermissions.toTypedArray())
                }
            )
        } else {
            // Main workout status display
            WorkoutStatusCard(
                isTracking = workoutState.isTracking,
                steps = workoutState.steps,
                distance = workoutState.distanceMeters / 1000f, // Convert to km
                duration = workoutState.durationSeconds,
                calories = workoutState.caloriesBurned
            )

            // Goal progress indicators
            GoalProgressCard(
                progress = mainViewModel.getGoalProgress(),
                goals = uiState.userGoals
            )

            // Control buttons for workout management
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start/Stop workout button -Disable if no accelerometer
                Button(
                    onClick = {
                        if (workoutState.isTracking) {
                            mainViewModel.stopWorkout()
                        } else {
                            mainViewModel.startWorkout()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = sensorAvailable, //Only enable if sensor available
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (workoutState.isTracking)
                            MaterialTheme.colorScheme.error else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (workoutState.isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (workoutState.isTracking) "Stop" else "Start")
                }

                // History navigation button (disabled during tracking)
                OutlinedButton(
                    onClick = { navController.navigate("history") },
                    modifier = Modifier.weight(1f),
                    enabled = !workoutState.isTracking
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("History")
                }
            }
        }

        // Error message display with dismiss option
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { mainViewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutStatusCard(
    isTracking: Boolean,
    steps: Int,
    distance: Float,
    duration: Int,
    calories: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isTracking) Icons.Default.FitnessCenter else Icons.Default.Pause,
                    contentDescription = null,
                    tint = if (isTracking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTracking) "Tracking Active" else "Ready to Track",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WorkoutMetric(
                    icon = Icons.Default.DirectionsWalk,
                    value = steps.toString(),
                    label = "Steps"
                )
                WorkoutMetric(
                    icon = Icons.Default.Route,
                    value = String.format("%.2f km", distance),
                    label = "Distance"
                )
                WorkoutMetric(
                    icon = Icons.Default.Timer,
                    value = formatDuration(duration),
                    label = "Duration"
                )
                WorkoutMetric(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "${calories.toInt()}",
                    label = "Calories"
                )
            }
        }
    }
}

@Composable
fun WorkoutMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GoalProgressCard(
    progress: com.yourname.fitnesstracker.viewmodel.GoalProgress,
    goals: com.yourname.fitnesstracker.data.UserGoals
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Goal Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            ProgressItem(
                label = "Steps",
                progress = progress.stepsProgress,
                current = (progress.stepsProgress * goals.steps).toInt().toString(),
                goal = goals.steps.toString()
            )

            ProgressItem(
                label = "Distance",
                progress = progress.distanceProgress,
                current = String.format("%.1f km", progress.distanceProgress * goals.distance),
                goal = "${goals.distance} km"
            )

            ProgressItem(
                label = "Duration",
                progress = progress.durationProgress,
                current = formatDuration((progress.durationProgress * goals.duration).toInt()),
                goal = formatDuration(goals.duration)
            )
        }
    }
}

@Composable
fun ProgressItem(
    label: String,
    progress: Float,
    current: String,
    goal: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$current / $goal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = when {
                progress >= 1f -> MaterialTheme.colorScheme.primary
                progress >= 0.7f -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.secondary
            }
        )
    }
}

@Composable
fun PermissionRequiredCard(
    onRequestPermissions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Permissions Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "To track your workout, we need access to your location and activity data.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permissions")
            }
        }
    }
}

@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissions Required") },
        text = {
            Text("This app needs location and activity permissions to track your workouts accurately. Without these permissions, workout tracking won't work properly.")
        },
        confirmButton = {
            TextButton(onClick = onRequestPermissions) {
                Text("Grant Permissions")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, remainingSeconds)
        else -> String.format("%d:%02d", minutes, remainingSeconds)
    }
}