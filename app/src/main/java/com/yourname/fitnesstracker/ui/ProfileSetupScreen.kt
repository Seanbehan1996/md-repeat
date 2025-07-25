package com.yourname.fitnesstracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.yourname.fitnesstracker.data.UserProfile
import com.yourname.fitnesstracker.viewmodel.UserProfileViewModel

/**
 * Profile setup screen for creating or editing user profiles.
 * Collects personal information, physical measurements, and fitness preferences.
 * Includes real-time BMI calculation and fitness assessment for new users.
 */
@Composable
fun ProfileSetupScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    val uiState by userProfileViewModel.uiState.collectAsState()

    // Form state variables for user input
    var name by remember { mutableStateOf(uiState.profile?.name ?: "") }
    var age by remember { mutableStateOf(uiState.profile?.age?.toString() ?: "") }
    var weight by remember { mutableStateOf(uiState.profile?.weight?.toString() ?: "") }
    var height by remember { mutableStateOf(uiState.profile?.height?.toString() ?: "") }
    var gender by remember { mutableStateOf(uiState.profile?.gender ?: "Not specified") }
    var activityLevel by remember { mutableStateOf(uiState.profile?.activityLevel ?: "Moderate") }
    var fitnessGoal by remember { mutableStateOf(uiState.profile?.fitnessGoal ?: "General Fitness") }

    // Update form fields when existing profile loads
    LaunchedEffect(uiState.profile) {
        uiState.profile?.let { profile ->
            name = profile.name
            age = profile.age.toString()
            weight = profile.weight.toString()
            height = profile.height.toString()
            gender = profile.gender
            activityLevel = profile.activityLevel
            fitnessGoal = profile.fitnessGoal
        }
    }

    // Available options for selection chips
    val genderOptions = listOf("Male", "Female", "Other", "Not specified")
    val activityOptions = listOf("Sedentary", "Light", "Moderate", "Active", "Very Active")
    val fitnessGoalOptions = listOf("Weight Loss", "Muscle Gain", "Endurance", "General Fitness")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with context-sensitive title
        Text(
            text = if (uiState.isFirstTimeUser) "Welcome! Let's set up your profile" else "Edit Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Basic information card with name, age, and gender
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Basic Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Name input field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Age input field with numeric validation
                OutlinedTextField(
                    value = age,
                    onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                    label = { Text("Age") },
                    leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Gender selection using filter chips
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(genderOptions.size) { index ->
                        val option = genderOptions[index]
                        FilterChip(
                            onClick = { gender = option },
                            label = { Text(option) },
                            selected = gender == option
                        )
                    }
                }
            }
        }

        // Physical measurements card with BMI calculation
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Physical Measurements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Weight and height input fields side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height (cm)") },
                        leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Real-time BMI calculation and display
                if (weight.isNotEmpty() && height.isNotEmpty()) {
                    val weightFloat = weight.toFloatOrNull()
                    val heightFloat = height.toFloatOrNull()
                    if (weightFloat != null && heightFloat != null && weightFloat > 0 && heightFloat > 0) {
                        val bmi = weightFloat / ((heightFloat / 100) * (heightFloat / 100))
                        val bmiCategory = when {
                            bmi < 18.5 -> "Underweight"
                            bmi < 25.0 -> "Normal weight"
                            bmi < 30.0 -> "Overweight"
                            else -> "Obese"
                        }

                        // BMI preview card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "BMI: ${String.format("%.1f", bmi)} ($bmiCategory)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Activity level and fitness goals card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Activity & Goals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Activity level selection chips
                Text(
                    text = "Activity Level",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activityOptions.size) { index ->
                        val option = activityOptions[index]
                        FilterChip(
                            onClick = { activityLevel = option },
                            label = { Text(option) },
                            selected = activityLevel == option
                        )
                    }
                }

                // Fitness goal selection chips
                Text(
                    text = "Primary Fitness Goal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(fitnessGoalOptions.size) { index ->
                        val option = fitnessGoalOptions[index]
                        FilterChip(
                            onClick = { fitnessGoal = option },
                            label = { Text(option) },
                            selected = fitnessGoal == option
                        )
                    }
                }
            }
        }

        // Save button with validation and loading state
        Button(
            onClick = {
                // Validate required fields before saving
                if (name.isNotEmpty() && age.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty()) {
                    val profile = UserProfile(
                        name = name,
                        age = age.toIntOrNull() ?: 25,
                        weight = weight.toFloatOrNull() ?: 70f,
                        height = height.toFloatOrNull() ?: 170f,
                        gender = gender,
                        activityLevel = activityLevel,
                        fitnessGoal = fitnessGoal
                    )
                    userProfileViewModel.createOrUpdateProfile(profile)

                    // Handle navigation based on user type
                    if (uiState.isFirstTimeUser) {
                        // Show fitness assessment for new users
                        userProfileViewModel.calculateAndShowAssessment()
                    } else {
                        // Return to previous screen for existing users
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotEmpty() && age.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (uiState.isFirstTimeUser) "Create Profile" else "Save Changes")
            }
        }

        // Skip option for first-time users
        if (uiState.isFirstTimeUser) {
            TextButton(
                onClick = { navController.navigate("home") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for now")
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
                TextButton(onClick = { userProfileViewModel.clearError() }) {
                    Text("Dismiss")
                }
            }
        }
    }

    // Fitness assessment dialog for new users
    if (uiState.showAssessment && uiState.fitnessAssessment != null) {
        FitnessAssessmentDialog(
            assessment = uiState.fitnessAssessment!!,
            onDismiss = {
                userProfileViewModel.hideAssessment()
                navController.navigate("home") {
                    popUpTo("profile_setup") { inclusive = true }
                }
            }
        )
    }
}

/**
 * Dialog displaying fitness assessment results for new users.
 * Shows calculated fitness level, score, and personalized recommendations.
 */
@Composable
fun FitnessAssessmentDialog(
    assessment: com.yourname.fitnesstracker.data.FitnessAssessment,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Dialog header with assessment icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Your Fitness Assessment")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Overall fitness score card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Fitness Level",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = assessment.fitnessLevel,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${assessment.overallScore}/15 points",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Top personalized recommendations
                if (assessment.recommendations.isNotEmpty()) {
                    Text(
                        text = "Top Recommendations:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Show first 3 recommendations as bullet points
                    assessment.recommendations.take(3).forEach { recommendation ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = recommendation,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Get Started!")
            }
        }
    )
}