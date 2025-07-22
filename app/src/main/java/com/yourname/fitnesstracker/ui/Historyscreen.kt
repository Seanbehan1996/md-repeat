package com.yourname.fitnesstracker.ui

import com.yourname.fitnesstracker.utils.formatDuration
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourname.fitnesstracker.data.WorkoutSession
import com.yourname.fitnesstracker.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class WorkoutStats(
    val totalWorkouts: Int,
    val totalSteps: Int,
    val totalDistance: Float,
    val totalDuration: Int,
    val totalCalories: Float,
    val averageSteps: Int,
    val averageDistance: Float,
    val averageDuration: Int
)

@Composable
fun HistoryScreen(
    mainViewModel: MainViewModel = viewModel(),
    navController: NavController? = null
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showExportDialog by remember { mutableStateOf(false) }
    var exportSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mainViewModel.loadData()
    }

    // Calculate statistics
    val stats = remember(uiState.workouts) {
        if (uiState.workouts.isEmpty()) {
            WorkoutStats(0, 0, 0f, 0, 0f, 0, 0f, 0)
        } else {
            val workouts = uiState.workouts
            WorkoutStats(
                totalWorkouts = workouts.size,
                totalSteps = workouts.sumOf { it.steps },
                totalDistance = workouts.sumOf { it.distanceMeters.toDouble() }.toFloat(),
                totalDuration = workouts.sumOf { it.durationSeconds },
                totalCalories = workouts.sumOf { it.caloriesBurned.toDouble() }.toFloat(),
                averageSteps = if (workouts.isNotEmpty()) workouts.sumOf { it.steps } / workouts.size else 0,
                averageDistance = if (workouts.isNotEmpty()) workouts.sumOf { it.distanceMeters.toDouble() }.toFloat() / workouts.size else 0f,
                averageDuration = if (workouts.isNotEmpty()) workouts.sumOf { it.durationSeconds } / workouts.size else 0
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            navController?.let {
                IconButton(onClick = { it.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
            Text(
                text = "Workout History",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Statistics Overview
            if (uiState.workouts.isNotEmpty()) {
                StatsOverviewCard(stats = stats)

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export CSV")
                    }

                    OutlinedButton(
                        onClick = { navController?.navigate("goals") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set Goals")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Workout List
            if (uiState.workouts.isEmpty()) {
                EmptyStateCard(
                    onStartWorkout = { navController?.navigate("workout") }
                )
            } else {
                Text(
                    text = "Recent Workouts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.workouts) { workout ->
                        EnhancedWorkoutItem(workout = workout)
                    }
                }
            }
        }

        // Error handling
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
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

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onConfirm = {
                exportWorkoutsToCSV(context, uiState.workouts)
                showExportDialog = false
                exportSuccess = true
            }
        )
    }

    // Export Success Message
    if (exportSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            exportSuccess = false
        }

        LaunchedEffect(exportSuccess) {
            // Show snackbar or toast here if needed
        }
    }
}

@Composable
fun StatsOverviewCard(stats: WorkoutStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Total Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.FitnessCenter,
                    value = "${stats.totalWorkouts}",
                    label = "Workouts"
                )
                StatItem(
                    icon = Icons.Default.DirectionsWalk,
                    value = "${stats.totalSteps}",
                    label = "Total Steps"
                )
                StatItem(
                    icon = Icons.Default.Route,
                    value = String.format("%.1f km", stats.totalDistance / 1000),
                    label = "Distance"
                )
                StatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "${stats.totalCalories.toInt()}",
                    label = "Calories"
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Average Stats
            Text(
                text = "Averages per Workout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.DirectionsWalk,
                    value = "${stats.averageSteps}",
                    label = "Avg Steps",
                    isSmall = true
                )
                StatItem(
                    icon = Icons.Default.Route,
                    value = String.format("%.1f km", stats.averageDistance / 1000),
                    label = "Avg Distance",
                    isSmall = true
                )
                StatItem(
                    icon = Icons.Default.Timer,
                    value = formatDuration(stats.averageDuration),
                    label = "Avg Duration",
                    isSmall = true
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    isSmall: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(if (isSmall) 20.dp else 24.dp)
        )
        Text(
            text = value,
            style = if (isSmall) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
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
fun EnhancedWorkoutItem(workout: WorkoutSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Date and duration header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatWorkoutDate(workout.date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = formatDuration(workout.durationSeconds),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Workout metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WorkoutMetricSmall(
                    icon = Icons.Default.DirectionsWalk,
                    value = "${workout.steps}",
                    label = "Steps"
                )
                WorkoutMetricSmall(
                    icon = Icons.Default.Route,
                    value = String.format("%.2f km", workout.distanceMeters / 1000),
                    label = "Distance"
                )
                WorkoutMetricSmall(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "${workout.caloriesBurned.toInt()}",
                    label = "Calories"
                )
            }
        }
    }
}

@Composable
fun WorkoutMetricSmall(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyStateCard(onStartWorkout: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No workouts yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Start your first workout to see your progress here!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            onStartWorkout?.let {
                Button(onClick = it) {
                    Text("Start First Workout")
                }
            }
        }
    }
}

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Workout Data") },
        text = {
            Text("Export your workout history as a CSV file? This will save the file to your device and you can share it from there.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun exportWorkoutsToCSV(context: Context, workouts: List<WorkoutSession>) {
    try {
        val fileName = "fitness_tracker_export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)

        val header = "Date,Duration (minutes),Steps,Distance (km),Calories\n"
        val data = workouts.joinToString("\n") { workout ->
            val date = workout.date
            val duration = workout.durationSeconds / 60f
            val distance = workout.distanceMeters / 1000f
            "$date,${String.format("%.1f", duration)},${workout.steps},${String.format("%.2f", distance)},${String.format("%.1f", workout.caloriesBurned)}"
        }

        file.writeText(header + data)

        // Create share intent
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "text/csv"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share workout data"))

    } catch (e: Exception) {
        // Handle error - could show a toast or snackbar
        e.printStackTrace()
    }
}

fun formatWorkoutDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}