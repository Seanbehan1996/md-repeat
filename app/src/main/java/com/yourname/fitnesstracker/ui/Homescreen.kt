package com.yourname.fitnesstracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourname.fitnesstracker.viewmodel.UserProfileViewModel

/**
 * Main home screen of the fitness tracker app.
 * Displays personalized welcome message, daily goals, action buttons, and helpful tips.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    val profileUiState by userProfileViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header section with personalized welcome and profile button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // Personalized welcome message or generic title
                Text(
                    text = if (profileUiState.profile != null)
                        "Welcome back, ${profileUiState.profile!!.name.split(" ").first()}!"
                    else "Fitness Tracker",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                // Motivational subtitle for existing users
                if (profileUiState.profile != null) {
                    Text(
                        text = "Ready for your next workout?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Profile button that navigates to setup or existing profile
            IconButton(
                onClick = {
                    if (profileUiState.isFirstTimeUser) {
                        navController.navigate("profile_setup")
                    } else {
                        navController.navigate("profile")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Daily goals card (only shown if user has profile)
        profileUiState.profile?.let { profile ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Today's Goals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Row of personalized daily targets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickGoalItem(
                            icon = Icons.Default.DirectionsWalk,
                            target = "${profile.getRecommendedSteps()}",
                            label = "Steps",
                            unit = ""
                        )
                        QuickGoalItem(
                            icon = Icons.Default.Timer,
                            target = "${profile.getRecommendedWorkoutDuration()}",
                            label = "Minutes",
                            unit = "min"
                        )
                        QuickGoalItem(
                            icon = Icons.Default.LocalFireDepartment,
                            target = "${profile.calculateRecommendedCalories()}",
                            label = "Calories",
                            unit = "cal"
                        )
                    }
                }
            }
        }

        // Main navigation action buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Primary start workout button
            Button(
                onClick = { navController.navigate("workout") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Workout",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Secondary navigation buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("history") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("History", style = MaterialTheme.typography.bodySmall)
                }

                OutlinedButton(
                    onClick = { navController.navigate("goals") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Goals", style = MaterialTheme.typography.bodySmall)
                }

                OutlinedButton(
                    onClick = { navController.navigate("achievements") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Awards", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Profile setup prompt for first-time users
        if (profileUiState.isFirstTimeUser) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with icon and title
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Complete Your Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Description of profile benefits
                    Text(
                        text = "Set up your profile to get personalized workout recommendations and fitness assessments.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    // Call-to-action button
                    Button(
                        onClick = { navController.navigate("profile_setup") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set Up Profile")
                    }
                }
            }
        }

        // Daily fitness tip card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header with lightbulb icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Today's Tip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Random fitness tip selection
                val tip = remember {
                    listOf(
                        "Start with a 5-minute warm-up before intense exercise",
                        "Stay hydrated - drink water before, during, and after workouts",
                        "Listen to your body and rest when you need to",
                        "Consistency is key - even 10 minutes of activity counts!",
                        "Mix cardio and strength training for best results"
                    ).random()
                }

                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Individual goal item displaying icon, target value, and label.
 * Used in the daily goals card to show personalized targets.
 */
@Composable
fun QuickGoalItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    target: String,
    label: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = target,
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