package com.yourname.fitnesstracker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.yourname.fitnesstracker.data.UserProfile
import com.yourname.fitnesstracker.viewmodel.UserProfileViewModel

/**
 * Main profile screen displaying comprehensive user information and health metrics.
 * Shows profile details, BMI, fitness assessment, recommendations, and action buttons.
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    val uiState by userProfileViewModel.uiState.collectAsState()

    // Navigate to setup if no profile exists
    LaunchedEffect(uiState.isFirstTimeUser) {
        if (uiState.isFirstTimeUser) {
            navController.navigate("profile_setup")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with title and action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Action buttons for assessment and edit
            Row {
                IconButton(onClick = {
                    userProfileViewModel.calculateAndShowAssessment()
                }) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "View Assessment"
                    )
                }
                IconButton(onClick = {
                    navController.navigate("profile_setup")
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile"
                    )
                }
            }
        }

        // Loading state display
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Main profile content (only if profile exists)
            uiState.profile?.let { profile ->
                // Profile header with image and basic info
                ProfileHeaderCard(profile = profile, userProfileViewModel = userProfileViewModel)

                // Health metrics including BMI and fitness level
                HealthMetricsCard(profile = profile, userProfileViewModel = userProfileViewModel)

                // Personal details and account information
                PersonalDetailsCard(profile = profile)

                // Personalized fitness recommendations
                RecommendationsCard(
                    recommendations = userProfileViewModel.getPersonalizedRecommendations()
                )

                // Action buttons for profile management
                ActionsCard(
                    onEditProfile = { navController.navigate("profile_setup") },
                    onViewAssessment = { userProfileViewModel.calculateAndShowAssessment() },
                    onDeleteProfile = { userProfileViewModel.deleteProfile() }
                )
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
    }

    // Fitness assessment dialog overlay
    if (uiState.showAssessment && uiState.fitnessAssessment != null) {
        FitnessAssessmentDialog(
            assessment = uiState.fitnessAssessment!!,
            onDismiss = { userProfileViewModel.hideAssessment() }
        )
    }
}

/**
 * Card displaying profile header with image, name, age, and quick stats.
 */
@Composable
fun ProfileHeaderCard(
    profile: UserProfile,
    userProfileViewModel: UserProfileViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular profile image or default icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (profile.profileImagePath != null) {
                    Image(
                        painter = rememberAsyncImagePainter(profile.profileImagePath),
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User name and basic demographic info
            Text(
                text = profile.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${profile.age} years old • ${profile.gender}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick stats row showing key metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStatItem(
                    icon = Icons.Default.MonitorWeight,
                    value = "${profile.weight.toInt()} kg",
                    label = "Weight"
                )
                QuickStatItem(
                    icon = Icons.Default.Height,
                    value = "${profile.height.toInt()} cm",
                    label = "Height"
                )
                QuickStatItem(
                    icon = Icons.Default.Flag,
                    value = profile.fitnessGoal.split(" ").first(),
                    label = "Goal"
                )
            }
        }
    }
}

/**
 * Card displaying calculated health metrics including BMI, fitness level, and calorie targets.
 */
@Composable
fun HealthMetricsCard(
    profile: UserProfile,
    userProfileViewModel: UserProfileViewModel
) {
    // Calculate health metrics
    val bmi = profile.calculateBMI()
    val bmiCategory = profile.getBMICategory()
    val bmiColor = userProfileViewModel.getBMIColor(bmi)
    val assessment = profile.getFitnessAssessment()
    val levelColor = userProfileViewModel.getFitnessLevelColor(assessment.fitnessLevel)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Health Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // BMI section with calculated value and category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Body Mass Index",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = bmiCategory,
                        style = MaterialTheme.typography.bodySmall,
                        color = bmiColor
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = bmiColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = String.format("%.1f", bmi),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = bmiColor
                    )
                }
            }

            Divider()

            // Fitness level section with assessment score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fitness Level",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${assessment.overallScore}/15 points",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = levelColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = assessment.fitnessLevel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                }
            }

            Divider()

            // Daily calorie target recommendation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Calorie Target",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Based on your profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "${profile.calculateRecommendedCalories()}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Card displaying personal details and account information.
 */
@Composable
fun PersonalDetailsCard(profile: UserProfile) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Personal Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Activity level information
            DetailRow(
                icon = Icons.Default.DirectionsRun,
                label = "Activity Level",
                value = profile.activityLevel
            )

            // Fitness goal information
            DetailRow(
                icon = Icons.Default.Flag,
                label = "Fitness Goal",
                value = profile.fitnessGoal
            )

            // Account creation date
            DetailRow(
                icon = Icons.Default.Today,
                label = "Member Since",
                value = profile.createdDate.split(" ").firstOrNull() ?: "Unknown"
            )

            // Last update date (only if different from creation)
            if (profile.lastUpdated != profile.createdDate) {
                DetailRow(
                    icon = Icons.Default.Update,
                    label = "Last Updated",
                    value = profile.lastUpdated.split(" ").firstOrNull() ?: "Unknown"
                )
            }
        }
    }
}

/**
 * Card displaying personalized fitness recommendations.
 */
@Composable
fun RecommendationsCard(recommendations: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Personalized Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // List of personalized recommendations
            recommendations.forEach { recommendation ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Card containing action buttons for profile management.
 */
@Composable
fun ActionsCard(
    onEditProfile: () -> Unit,
    onViewAssessment: () -> Unit,
    onDeleteProfile: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Edit profile button
            OutlinedButton(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile")
            }

            // View assessment button
            OutlinedButton(
                onClick = onViewAssessment,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Fitness Assessment")
            }

            // Delete profile button (destructive action)
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Profile")
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Profile") },
            text = { Text("Are you sure you want to delete your profile? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfile()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Quick statistic item with icon, value, and label for profile header.
 */
@Composable
fun QuickStatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
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

/**
 * Detail row component displaying icon, label, and value for personal information.
 */
@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}