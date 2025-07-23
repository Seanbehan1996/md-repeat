package com.yourname.fitnesstracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.fitnesstracker.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val analytics: WorkoutAnalytics = WorkoutAnalytics(),
    val achievements: List<Achievement> = emptyList(),
    val unlockedAchievements: List<Achievement> = emptyList(),
    val totalPoints: Int = 0,
    val unlockedCount: Int = 0,
    val totalAchievementCount: Int = 0,
    val stepsChartData: List<ChartDataPoint> = emptyList(),
    val distanceChartData: List<ChartDataPoint> = emptyList(),
    val weeklyProgress: List<WeeklyProgress> = emptyList(),
    val monthlyProgress: List<MonthlyProgress> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedChartPeriod: ChartPeriod = ChartPeriod.LAST_30_DAYS,
    val selectedMetric: ChartMetric = ChartMetric.STEPS
)

enum class ChartPeriod(val days: Int, val label: String) {
    LAST_7_DAYS(7, "7 Days"),
    LAST_30_DAYS(30, "30 Days"),
    LAST_90_DAYS(90, "90 Days")
}

enum class ChartMetric(val label: String) {
    STEPS("Steps"),
    DISTANCE("Distance"),
    DURATION("Duration"),
    CALORIES("Calories")
}

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WorkoutDatabase.getDatabase(application)
    private val repository = AnalyticsRepository(database.workoutDao(), database.achievementDao())

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        try {
            initializeData()
            observeAnalytics()
            observeAchievements()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Failed to initialize analytics: ${e.message}"
            )
        }
    }

    private fun initializeData() {
        viewModelScope.launch {
            try {
                repository.initializeAchievements()
                loadChartData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to initialize analytics: ${e.message}"
                )
            }
        }
    }

    private fun observeAnalytics() {
        viewModelScope.launch {
            repository.getWorkoutAnalytics().collect { analytics ->
                _uiState.value = _uiState.value.copy(analytics = analytics)
            }
        }
    }

    private fun observeAchievements() {
        viewModelScope.launch {
            try {
                // Observe all achievements
                database.achievementDao().getAllAchievements().collect { achievements: List<Achievement> ->
                    _uiState.value = _uiState.value.copy(achievements = achievements)
                }
            } catch (e: Exception) {
                // Ignore achievement errors for now
            }
        }

        viewModelScope.launch {
            try {
                // Observe unlocked achievements
                database.achievementDao().getUnlockedAchievements().collect { unlocked: List<Achievement> ->
                    _uiState.value = _uiState.value.copy(unlockedAchievements = unlocked)
                }
            } catch (e: Exception) {
                // Ignore achievement errors for now
            }
        }

        viewModelScope.launch {
            try {
                // Get achievement stats
                val totalPoints = database.achievementDao().getTotalPoints() ?: 0
                val unlockedCount = database.achievementDao().getUnlockedCount()
                val totalCount = database.achievementDao().getTotalCount()

                _uiState.value = _uiState.value.copy(
                    totalPoints = totalPoints,
                    unlockedCount = unlockedCount,
                    totalAchievementCount = totalCount
                )
            } catch (e: Exception) {
                // Ignore achievement errors for now
            }
        }
    }

    fun changeChartPeriod(period: ChartPeriod) {
        _uiState.value = _uiState.value.copy(selectedChartPeriod = period)
        loadChartData()
    }

    fun changeChartMetric(metric: ChartMetric) {
        _uiState.value = _uiState.value.copy(selectedMetric = metric)
        loadChartData()
    }

    private fun loadChartData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val period = _uiState.value.selectedChartPeriod
                val metric = _uiState.value.selectedMetric

                val chartData = when (metric) {
                    ChartMetric.STEPS -> repository.getStepsChartData(period.days)
                    ChartMetric.DISTANCE -> repository.getDistanceChartData(period.days)
                    ChartMetric.DURATION -> getDurationChartData(period.days)
                    ChartMetric.CALORIES -> getCaloriesChartData(period.days)
                }

                _uiState.value = _uiState.value.copy(
                    stepsChartData = if (metric == ChartMetric.STEPS) chartData else _uiState.value.stepsChartData,
                    distanceChartData = if (metric == ChartMetric.DISTANCE) chartData else _uiState.value.distanceChartData,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load chart data: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun getDurationChartData(days: Int): List<ChartDataPoint> {
        val workouts = database.workoutDao().getAllWorkouts()
        return generateDailyDurationChart(workouts, days)
    }

    private suspend fun getCaloriesChartData(days: Int): List<ChartDataPoint> {
        val workouts = database.workoutDao().getAllWorkouts()
        return generateDailyCaloriesChart(workouts, days)
    }

    private fun generateDailyDurationChart(workouts: List<WorkoutSession>, days: Int): List<ChartDataPoint> {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()
        val chartData = mutableListOf<ChartDataPoint>()

        for (i in days - 1 downTo 0) {
            calendar.time = java.util.Date()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val date = format.format(calendar.time)

            val dayWorkouts = workouts.filter { workout ->
                workout.date.startsWith(date)
            }
            val totalDuration = dayWorkouts.sumOf { workout -> workout.durationSeconds }.toFloat() / 60 // Convert to minutes

            chartData.add(ChartDataPoint(
                label = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(calendar.time),
                value = totalDuration
            ))
        }

        return chartData
    }

    private fun generateDailyCaloriesChart(workouts: List<WorkoutSession>, days: Int): List<ChartDataPoint> {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()
        val chartData = mutableListOf<ChartDataPoint>()

        for (i in days - 1 downTo 0) {
            calendar.time = java.util.Date()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val date = format.format(calendar.time)

            val dayWorkouts = workouts.filter { workout ->
                workout.date.startsWith(date)
            }
            val totalCalories = dayWorkouts.sumOf { workout -> workout.caloriesBurned.toDouble() }.toFloat()

            chartData.add(ChartDataPoint(
                label = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(calendar.time),
                value = totalCalories
            ))
        }

        return chartData
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                loadChartData()

                // Refresh achievement stats
                try {
                    val totalPoints = database.achievementDao().getTotalPoints() ?: 0
                    val unlockedCount = database.achievementDao().getUnlockedCount()
                    val totalCount = database.achievementDao().getTotalCount()

                    _uiState.value = _uiState.value.copy(
                        totalPoints = totalPoints,
                        unlockedCount = unlockedCount,
                        totalAchievementCount = totalCount,
                        isLoading = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to refresh data: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getAchievementProgress(): Float {
        val state = _uiState.value
        return if (state.totalAchievementCount > 0) {
            state.unlockedCount.toFloat() / state.totalAchievementCount.toFloat()
        } else {
            0f
        }
    }

    fun getAchievementsByCategory(): Map<String, List<Achievement>> {
        return _uiState.value.achievements.groupBy { it.category }
    }

    fun getRecentAchievements(limit: Int = 5): List<Achievement> {
        return _uiState.value.unlockedAchievements
            .sortedByDescending { it.achievedDate }
            .take(limit)
    }

    // Call this when a new workout is completed
    fun checkAchievements(workout: WorkoutSession) {
        viewModelScope.launch {
            try {
                repository.checkAndUnlockAchievements(workout)
            } catch (e: Exception) {
                // Handle silently or log error
            }
        }
    }
}