package com.yourname.fitnesstracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.fitnesstracker.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Add this data class for achievement progress
data class AchievementProgressInfo(
    val currentValue: Float,
    val targetValue: Float,
    val progressText: String,
    val clampedProgress: Float = (currentValue / targetValue).coerceIn(0f, 1f)
)

// Add this data class for user statistics
data class UserStats(
    val totalSteps: Int,
    val totalDistance: Float,
    val totalDuration: Float,
    val totalWorkouts: Int,
    val currentStreak: Int,
    val earlyWorkouts: Int,
    val weekendWorkouts: Int
)

data class AnalyticsUiState(
    val analytics: WorkoutAnalytics = WorkoutAnalytics(),
    val achievements: List<Achievement> = emptyList(),
    val unlockedAchievements: List<Achievement> = emptyList(),
    val workouts: List<WorkoutSession> = emptyList(), // Added for progress calculation
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
            observeWorkouts() // Added to track workouts for progress
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

    // Added method to observe workouts for progress calculation
    private fun observeWorkouts() {
        viewModelScope.launch {
            try {
                database.workoutDao().getAllWorkoutsFlow().collect { workouts ->
                    _uiState.value = _uiState.value.copy(workouts = workouts)
                }
            } catch (e: Exception) {
                // Handle error if needed
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

    // NEW METHOD: Get progress information for a specific achievement
    fun getAchievementProgress(achievement: Achievement): AchievementProgressInfo {
        val currentStats = getCurrentUserStats()

        return when (achievement.category.lowercase()) {
            "steps" -> {
                val current = currentStats.totalSteps.toFloat()
                val target = achievement.targetValue.toFloat()
                AchievementProgressInfo(
                    currentValue = current,
                    targetValue = target,
                    progressText = "${currentStats.totalSteps.formatNumber()} / ${achievement.targetValue.formatNumber()} steps"
                )
            }
            "distance" -> {
                val current = currentStats.totalDistance
                val target = achievement.targetValue.toFloat()
                AchievementProgressInfo(
                    currentValue = current,
                    targetValue = target,
                    progressText = "${current.formatDistance()} / ${target.formatDistance()}"
                )
            }
            "duration" -> {
                val current = currentStats.totalDuration
                val target = achievement.targetValue.toFloat()
                AchievementProgressInfo(
                    currentValue = current,
                    targetValue = target,
                    progressText = "${(current/60).toInt()} / ${(target/60).toInt()} minutes"
                )
            }
            "workouts" -> {
                val current = currentStats.totalWorkouts.toFloat()
                val target = achievement.targetValue.toFloat()
                AchievementProgressInfo(
                    currentValue = current,
                    targetValue = target,
                    progressText = "${currentStats.totalWorkouts} / ${achievement.targetValue} workouts"
                )
            }
            "streak" -> {
                val current = currentStats.currentStreak.toFloat()
                val target = achievement.targetValue.toFloat()
                AchievementProgressInfo(
                    currentValue = current,
                    targetValue = target,
                    progressText = "${currentStats.currentStreak} / ${achievement.targetValue} days"
                )
            }
            "special" -> {
                // For special achievements, determine progress based on description
                when {
                    achievement.description.contains("early", ignoreCase = true) -> {
                        val current = currentStats.earlyWorkouts.toFloat()
                        val target = achievement.targetValue.toFloat()
                        AchievementProgressInfo(
                            currentValue = current,
                            targetValue = target,
                            progressText = "${currentStats.earlyWorkouts} / ${achievement.targetValue} early workouts"
                        )
                    }
                    achievement.description.contains("weekend", ignoreCase = true) -> {
                        val current = currentStats.weekendWorkouts.toFloat()
                        val target = achievement.targetValue.toFloat()
                        AchievementProgressInfo(
                            currentValue = current,
                            targetValue = target,
                            progressText = "${currentStats.weekendWorkouts} / ${achievement.targetValue} weekend workouts"
                        )
                    }
                    else -> {
                        AchievementProgressInfo(
                            currentValue = if (achievement.isUnlocked) 1f else 0f,
                            targetValue = 1f,
                            progressText = if (achievement.isUnlocked) "Completed" else "Not completed"
                        )
                    }
                }
            }
            else -> {
                AchievementProgressInfo(
                    currentValue = if (achievement.isUnlocked) 1f else 0f,
                    targetValue = 1f,
                    progressText = if (achievement.isUnlocked) "Completed" else "In progress"
                )
            }
        }
    }

    //Get current user statistics for progress calculation
    private fun getCurrentUserStats(): UserStats {
        val workouts = _uiState.value.workouts
        val totalSteps = workouts.sumOf { it.steps }
        val totalDistance = workouts.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f // Convert to km
        val totalDuration = workouts.sumOf { it.durationSeconds }.toFloat() // Keep in seconds
        val totalWorkouts = workouts.size

        // Calculate streaks and special stats
        val currentStreak = calculateCurrentStreak(workouts)
        val earlyWorkouts = calculateEarlyWorkouts(workouts)
        val weekendWorkouts = calculateWeekendWorkouts(workouts)

        return UserStats(
            totalSteps = totalSteps,
            totalDistance = totalDistance,
            totalDuration = totalDuration,
            totalWorkouts = totalWorkouts,
            currentStreak = currentStreak,
            earlyWorkouts = earlyWorkouts,
            weekendWorkouts = weekendWorkouts
        )
    }

    //Calculate current workout streak
    private fun calculateCurrentStreak(workouts: List<WorkoutSession>): Int {
        if (workouts.isEmpty()) return 0

        // Group workouts by date and calculate consecutive days
        val workoutDates = workouts.map {
            it.date.split(" ")[0] // Get just the date part
        }.distinct().sorted()

        if (workoutDates.isEmpty()) return 0

        var streak = 1
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in workoutDates.size - 2 downTo 0) {
            try {
                val currentDate = dateFormat.parse(workoutDates[i + 1])
                val previousDate = dateFormat.parse(workoutDates[i])

                if (currentDate != null && previousDate != null) {
                    val dayDiff = ((currentDate.time - previousDate.time) / (1000 * 60 * 60 * 24)).toInt()

                    if (dayDiff == 1) {
                        streak++
                    } else {
                        break
                    }
                }
            } catch (e: Exception) {
                break
            }
        }

        return streak
    }

    //Calculate early morning workouts
    private fun calculateEarlyWorkouts(workouts: List<WorkoutSession>): Int {
        return workouts.count { workout ->
            try {
                val time = workout.date.split(" ").getOrNull(1) ?: "12:00:00"
                val hour = time.split(":")[0].toIntOrNull() ?: 12
                hour in 5..8 // Early morning workouts (5 AM to 8 AM)
            } catch (e: Exception) {
                false
            }
        }
    }

    //Calculate weekend workouts
    private fun calculateWeekendWorkouts(workouts: List<WorkoutSession>): Int {
        return workouts.count { workout ->
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.parse(workout.date.split(" ")[0])
                val calendar = Calendar.getInstance()
                calendar.time = date
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
            } catch (e: Exception) {
                false
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
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val chartData = mutableListOf<ChartDataPoint>()

        for (i in days - 1 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = format.format(calendar.time)

            val dayWorkouts = workouts.filter { workout ->
                workout.date.startsWith(date)
            }
            val totalDuration = dayWorkouts.sumOf { workout -> workout.durationSeconds }.toFloat() / 60 // Convert to minutes

            chartData.add(ChartDataPoint(
                label = SimpleDateFormat("MMM dd", Locale.getDefault()).format(calendar.time),
                value = totalDuration
            ))
        }

        return chartData
    }

    private fun generateDailyCaloriesChart(workouts: List<WorkoutSession>, days: Int): List<ChartDataPoint> {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val chartData = mutableListOf<ChartDataPoint>()

        for (i in days - 1 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = format.format(calendar.time)

            val dayWorkouts = workouts.filter { workout ->
                workout.date.startsWith(date)
            }
            val totalCalories = dayWorkouts.sumOf { workout -> workout.caloriesBurned.toDouble() }.toFloat()

            chartData.add(ChartDataPoint(
                label = SimpleDateFormat("MMM dd", Locale.getDefault()).format(calendar.time),
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

// Extension functions for formatting
fun Int.formatNumber(): String {
    return "%,d".format(this)
}

fun Float.formatDistance(): String {
    return if (this < 1f) {
        "${(this * 1000).toInt()}m"
    } else {
        "%.1f km".format(this)
    }
}