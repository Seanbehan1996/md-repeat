package com.yourname.fitnesstracker.ui

import com.yourname.fitnesstracker.utils.formatDuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourname.fitnesstracker.data.UserGoals
import com.yourname.fitnesstracker.viewmodel.MainViewModel

/**
 * Screen for setting and managing user fitness goals.
 * Allows users to set daily targets for steps, distance, and exercise duration.
 */
@Composable
fun GoalScreen(
    mainViewModel: MainViewModel = viewModel(),
    navController: NavController
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val currentGoals = uiState.userGoals

    // Local state for form inputs
    var stepsGoal by remember { mutableStateOf(currentGoals.steps.toString()) }
    var distanceGoal by remember { mutableStateOf(currentGoals.distance.toString()) }
    var durationGoal by remember { mutableStateOf((currentGoals.duration / 60).toString()) } // Convert to minutes for display

    var showSuccessMessage by remember { mutableStateOf(false) }

    // Update local state when goals change from viewmodel
    LaunchedEffect(currentGoals) {
        stepsGoal = currentGoals.steps.toString()
        distanceGoal = currentGoals.distance.toString()
        durationGoal = (currentGoals.duration / 60).toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header with back button and title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Fitness Goals",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Current goals overview card showing saved values
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Current Goals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Display current goals in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GoalSummaryItem(
                        icon = Icons.Default.DirectionsWalk,
                        value = "${currentGoals.steps}",
                        label = "Steps"
                    )
                    GoalSummaryItem(
                        icon = Icons.Default.Route,
                        value = "${currentGoals.distance} km",
                        label = "Distance"
                    )
                    GoalSummaryItem(
                        icon = Icons.Default.Timer,
                        value = "${currentGoals.duration / 60} min",
                        label = "Duration"
                    )
                }
            }
        }

        // Goal input section with text fields
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Set Your Goals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Steps goal input
                GoalInputField(
                    value = stepsGoal,
                    onValueChange = { stepsGoal = it },
                    label = "Daily Steps Goal",
                    icon = Icons.Default.DirectionsWalk,
                    suffix = "steps",
                    keyboardType = KeyboardType.Number
                )

                // Distance goal input
                GoalInputField(
                    value = distanceGoal,
                    onValueChange = { distanceGoal = it },
                    label = "Daily Distance Goal",
                    icon = Icons.Default.Route,
                    suffix = "km",
                    keyboardType = KeyboardType.Decimal
                )

                // Duration goal input
                GoalInputField(
                    value = durationGoal,
                    onValueChange = { durationGoal = it },
                    label = "Daily Exercise Duration",
                    icon = Icons.Default.Timer,
                    suffix = "minutes",
                    keyboardType = KeyboardType.Number
                )
            }
        }

        // Preset goals card with quick selection buttons
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Choose a fitness level to set default goals:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Three preset buttons for different fitness levels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetButton(
                        text = "Beginner",
                        onClick = {
                            stepsGoal = "5000"
                            distanceGoal = "2.5"
                            durationGoal = "15"
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PresetButton(
                        text = "Moderate",
                        onClick = {
                            stepsGoal = "8000"
                            distanceGoal = "4.0"
                            durationGoal = "25"
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PresetButton(
                        text = "Active",
                        onClick = {
                            stepsGoal = "12000"
                            distanceGoal = "6.0"
                            durationGoal = "45"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Action buttons for reset and save
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reset button to restore current saved values
            OutlinedButton(
                onClick = {
                    // Reset to current saved goals
                    stepsGoal = currentGoals.steps.toString()
                    distanceGoal = currentGoals.distance.toString()
                    durationGoal = (currentGoals.duration / 60).toString()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset")
            }

            // Save button with validation
            Button(
                onClick = {
                    // Validate and save goals
                    val steps = stepsGoal.toIntOrNull()
                    val distance = distanceGoal.toFloatOrNull()
                    val durationMinutes = durationGoal.toIntOrNull()

                    if (steps != null && distance != null && durationMinutes != null &&
                        steps > 0 && distance > 0 && durationMinutes > 0) {

                        val newGoals = UserGoals(
                            steps = steps,
                            distance = distance,
                            duration = durationMinutes * 60 // Convert to seconds
                        )

                        mainViewModel.updateGoals(newGoals)
                        showSuccessMessage = true
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Goals")
            }
        }

        // Success message that auto-dismisses after 2 seconds
        if (showSuccessMessage) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showSuccessMessage = false
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Goals saved successfully!",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Error message display with dismiss button
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

/**
 * Individual goal summary item displaying icon, value, and label.
 */
@Composable
fun GoalSummaryItem(
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
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Reusable input field for goal values with icon and suffix.
 */
@Composable
fun GoalInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    suffix: String,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        suffix = { Text(suffix) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

/**
 * Preset button for quick goal selection based on fitness level.
 */
@Composable
fun PresetButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text)
    }
}