package com.yourname.fitnesstracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.fitnesstracker.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiState(
    val workouts: List<WorkoutSession> = emptyList(),
    val userGoals: UserGoals = UserGoals(),
    val currentWorkout: WorkoutState? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class WorkoutState(
    val isTracking: Boolean = false,
    val steps: Int = 0,
    val distanceMeters: Float = 0f,
    val durationSeconds: Int = 0,
    val caloriesBurned: Float = 0f,
    val startTime: Long = 0L
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = WorkoutDatabase.getDatabase(application).workoutDao()
    private val goalPreferences = GoalPreferences(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _workoutState = MutableStateFlow(WorkoutState())
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()

    init {
        loadData()
        observeGoals()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val workouts = dao.getAllWorkouts()
                _uiState.value = _uiState.value.copy(
                    workouts = workouts,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load workouts: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun observeGoals() {
        viewModelScope.launch {
            goalPreferences.userGoals.collect { goals ->
                _uiState.value = _uiState.value.copy(userGoals = goals)
            }
        }
    }

    fun startWorkout() {
        val startTime = System.currentTimeMillis()
        _workoutState.value = WorkoutState(
            isTracking = true,
            startTime = startTime
        )
        _uiState.value = _uiState.value.copy(
            currentWorkout = _workoutState.value
        )
    }

    fun stopWorkout() {
        val currentState = _workoutState.value
        if (currentState.isTracking) {
            val duration = ((System.currentTimeMillis() - currentState.startTime) / 1000).toInt()
            val calories = calculateCalories(
                currentState.steps,
                currentState.distanceMeters,
                duration
            )

            val workout = WorkoutSession(
                id = System.currentTimeMillis(),
                date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date()),
                durationSeconds = duration,
                steps = currentState.steps,
                distanceMeters = currentState.distanceMeters,
                caloriesBurned = calories
            )

            addWorkout(workout)
        }

        _workoutState.value = WorkoutState(isTracking = false)
        _uiState.value = _uiState.value.copy(currentWorkout = null)
    }

    fun updateWorkoutSteps(steps: Int) {
        _workoutState.value = _workoutState.value.copy(steps = steps)
        updateCurrentWorkoutInUiState()
    }

    fun updateWorkoutDistance(distanceMeters: Float) {
        _workoutState.value = _workoutState.value.copy(distanceMeters = distanceMeters)
        updateCurrentWorkoutInUiState()
    }

    private fun updateCurrentWorkoutInUiState() {
        if (_workoutState.value.isTracking) {
            val duration = if (_workoutState.value.startTime > 0) {
                ((System.currentTimeMillis() - _workoutState.value.startTime) / 1000).toInt()
            } else 0

            val calories = calculateCalories(
                _workoutState.value.steps,
                _workoutState.value.distanceMeters,
                duration
            )

            _workoutState.value = _workoutState.value.copy(
                durationSeconds = duration,
                caloriesBurned = calories
            )

            _uiState.value = _uiState.value.copy(currentWorkout = _workoutState.value)
        }
    }

    fun addWorkout(workout: WorkoutSession) {
        viewModelScope.launch {
            try {
                dao.insertWorkout(workout)

                // Check for new achievements
                try {
                    val database = WorkoutDatabase.getDatabase(getApplication())
                    val analyticsRepository = AnalyticsRepository(database.workoutDao(), database.achievementDao())
                    analyticsRepository.checkAndUnlockAchievements(workout)
                } catch (e: Exception) {
                    // Ignore achievement errors, just log them
                }

                loadData() // Reload workouts after insert
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save workout: ${e.message}"
                )
            }
        }
    }

    fun updateGoals(goals: UserGoals) {
        viewModelScope.launch {
            try {
                goalPreferences.saveGoals(goals)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save goals: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getGoalProgress(): GoalProgress {
        val goals = _uiState.value.userGoals
        val currentWorkout = _workoutState.value

        return if (currentWorkout.isTracking) {
            GoalProgress(
                stepsProgress = (currentWorkout.steps.toFloat() / goals.steps).coerceAtMost(1f),
                distanceProgress = ((currentWorkout.distanceMeters / 1000) / goals.distance).coerceAtMost(1f),
                durationProgress = (currentWorkout.durationSeconds.toFloat() / goals.duration).coerceAtMost(1f)
            )
        } else {
            GoalProgress()
        }
    }

    private fun calculateCalories(steps: Int, distanceMeters: Float, durationSeconds: Int): Float {
        // More sophisticated calorie calculation
        // Base: 0.04 calories per step + distance factor + time factor
        val stepCalories = steps * 0.04f
        val distanceCalories = (distanceMeters / 1000) * 50 // ~50 cal per km
        val timeCalories = (durationSeconds / 60f) * 5 // ~5 cal per minute base

        return stepCalories + distanceCalories + timeCalories
    }
}

data class GoalProgress(
    val stepsProgress: Float = 0f,
    val distanceProgress: Float = 0f,
    val durationProgress: Float = 0f
)