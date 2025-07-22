// File: app/src/main/java/com/yourname/fitnesstracker/viewmodel/UserProfileViewModel.kt
package com.yourname.fitnesstracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.fitnesstracker.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFirstTimeUser: Boolean = true,
    val fitnessAssessment: FitnessAssessment? = null,
    val showAssessment: Boolean = false
)

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WorkoutDatabase.getDatabase(application)
    private val repository = UserProfileRepository(database.userProfileDao())

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                repository.getUserProfile().collect { profile ->
                    val isFirstTime = profile == null
                    val assessment = profile?.getFitnessAssessment()

                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false,
                        isFirstTimeUser = isFirstTime,
                        fitnessAssessment = assessment
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load profile: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun createOrUpdateProfile(profile: UserProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                repository.createOrUpdateProfile(profile)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save profile: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun updateProfileImage(imagePath: String) {
        val currentProfile = _uiState.value.profile
        if (currentProfile != null) {
            val updatedProfile = currentProfile.copy(profileImagePath = imagePath)
            createOrUpdateProfile(updatedProfile)
        }
    }

    fun calculateAndShowAssessment() {
        val profile = _uiState.value.profile
        if (profile != null) {
            val assessment = profile.getFitnessAssessment()
            _uiState.value = _uiState.value.copy(
                fitnessAssessment = assessment,
                showAssessment = true
            )
        }
    }

    fun hideAssessment() {
        _uiState.value = _uiState.value.copy(showAssessment = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun deleteProfile() {
        viewModelScope.launch {
            try {
                repository.deleteProfile()
                _uiState.value = ProfileUiState(isFirstTimeUser = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete profile: ${e.message}"
                )
            }
        }
    }

    // Helper functions for UI
    fun getBMIColor(bmi: Float): androidx.compose.ui.graphics.Color {
        return when {
            bmi < 18.5 -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue - Underweight
            bmi < 25.0 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green - Normal
            bmi < 30.0 -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange - Overweight
            else -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red - Obese
        }
    }

    fun getFitnessLevelColor(level: String): androidx.compose.ui.graphics.Color {
        return when (level) {
            "Excellent" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            "Good" -> androidx.compose.ui.graphics.Color(0xFF8BC34A) // Light Green
            "Average" -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
            "Below Average" -> androidx.compose.ui.graphics.Color(0xFFFF5722) // Deep Orange
            "Poor" -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
            else -> androidx.compose.ui.graphics.Color(0xFF9E9E9E) // Grey
        }
    }

    // Get personalized workout recommendations based on profile
    fun getPersonalizedRecommendations(): List<String> {
        val profile = _uiState.value.profile ?: return emptyList()
        val assessment = profile.getFitnessAssessment()

        return buildList {
            add("Daily Steps Goal: ${profile.getRecommendedSteps()} steps")
            add("Recommended Workout: ${profile.getRecommendedWorkoutDuration()} minutes")
            add("Daily Calorie Target: ${profile.calculateRecommendedCalories()} calories")
            addAll(assessment.recommendations)
        }
    }
}