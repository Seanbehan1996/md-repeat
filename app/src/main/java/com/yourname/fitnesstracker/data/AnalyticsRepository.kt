// File: app/src/main/java/com/yourname/fitnesstracker/data/AnalyticsRepository.kt
package com.yourname.fitnesstracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsRepository(
    private val workoutDao: WorkoutDao,
    private val achievementDao: AchievementDao
) {

    fun getWorkoutAnalytics(): Flow<WorkoutAnalytics> {
        return workoutDao.getAllWorkoutsFlow().map { workouts: List<WorkoutSession> ->
            if (workouts.isEmpty()) {
                WorkoutAnalytics()
            } else {
                val totalSteps: Int = workouts.sumOf { workout -> workout.steps }
                val totalDistance: Float = workouts.sumOf { workout -> workout.distanceMeters.toDouble() }.toFloat()
                val totalDuration: Int = workouts.sumOf { workout -> workout.durationSeconds }
                val totalCalories: Float = workouts.sumOf { workout -> workout.caloriesBurned.toDouble() }.toFloat()

                WorkoutAnalytics(
                    totalWorkouts = workouts.size,
                    totalSteps = totalSteps,
                    totalDistance = totalDistance,
                    totalDuration = totalDuration,
                    totalCalories = totalCalories,
                    averageSteps = (totalSteps.toFloat() / workouts.size),
                    averageDistance = (totalDistance / 1000) / workouts.size, // Convert to km
                    averageDuration = (totalDuration.toFloat() / 60) / workouts.size, // Convert to minutes
                    averageCalories = totalCalories / workouts.size,
                    longestWorkout = workouts.maxOfOrNull { workout -> workout.durationSeconds } ?: 0,
                    mostStepsInSession = workouts.maxOfOrNull { workout -> workout.steps } ?: 0,
                    longestDistance = (workouts.maxOfOrNull { workout -> workout.distanceMeters } ?: 0f) / 1000, // Convert to km
                    currentStreak = calculateCurrentStreak(workouts),
                    bestStreak = calculateBestStreak(workouts)
                )
            }
        }
    }

    suspend fun getStepsChartData(days: Int = 30): List<ChartDataPoint> {
        val workouts = workoutDao.getAllWorkouts()
        return generateDailyStepsChart(workouts, days)
    }

    suspend fun getDistanceChartData(days: Int = 30): List<ChartDataPoint> {
        val workouts = workoutDao.getAllWorkouts()
        return generateDailyDistanceChart(workouts, days)
    }

    suspend fun initializeAchievements() {
        // Insert all predefined achievements if they don't exist
        achievementDao.insertAllAchievements(AchievementDefinitions.allAchievements)
    }

    suspend fun checkAndUnlockAchievements(workout: WorkoutSession) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Check single workout achievements
        checkStepsAchievements(workout.steps, currentDate)
        checkDistanceAchievements(workout.distanceMeters, currentDate)
        checkDurationAchievements(workout.durationSeconds, currentDate)

        // Check cumulative achievements
        val allWorkouts = workoutDao.getAllWorkouts()
        checkWorkoutCountAchievements(allWorkouts.size, currentDate)
    }

    private suspend fun checkStepsAchievements(steps: Int, date: String) {
        val stepMilestones = mapOf(1000 to "steps_1000", 5000 to "steps_5000", 10000 to "steps_10000")

        stepMilestones.forEach { (target, achievementId) ->
            if (steps >= target) {
                val achievement = achievementDao.getAchievementById(achievementId)
                if (achievement != null && !achievement.isUnlocked) {
                    achievementDao.unlockAchievement(achievementId, date)
                }
            }
        }
    }

    private suspend fun checkDistanceAchievements(distanceMeters: Float, date: String) {
        val distanceMilestones = mapOf(1000f to "distance_1km", 5000f to "distance_5km", 10000f to "distance_10km")

        distanceMilestones.forEach { (target, achievementId) ->
            if (distanceMeters >= target) {
                val achievement = achievementDao.getAchievementById(achievementId)
                if (achievement != null && !achievement.isUnlocked) {
                    achievementDao.unlockAchievement(achievementId, date)
                }
            }
        }
    }

    private suspend fun checkDurationAchievements(durationSeconds: Int, date: String) {
        val durationMilestones = mapOf(1800 to "duration_30min", 3600 to "duration_60min")

        durationMilestones.forEach { (target, achievementId) ->
            if (durationSeconds >= target) {
                val achievement = achievementDao.getAchievementById(achievementId)
                if (achievement != null && !achievement.isUnlocked) {
                    achievementDao.unlockAchievement(achievementId, date)
                }
            }
        }
    }

    private suspend fun checkWorkoutCountAchievements(totalWorkouts: Int, date: String) {
        val workoutMilestones = mapOf(1 to "first_workout", 5 to "workouts_5", 25 to "workouts_25", 100 to "workouts_100")

        workoutMilestones.forEach { (target, achievementId) ->
            if (totalWorkouts >= target) {
                val achievement = achievementDao.getAchievementById(achievementId)
                if (achievement != null && !achievement.isUnlocked) {
                    achievementDao.unlockAchievement(achievementId, date)
                }
            }
        }
    }

    private fun calculateCurrentStreak(workouts: List<WorkoutSession>): Int {
        if (workouts.isEmpty()) return 0

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val workoutDates = workouts.map { workout ->
            try {
                format.format(format.parse(workout.date.split(" ")[0]) ?: Date())
            } catch (e: Exception) {
                ""
            }
        }.distinct().sorted().reversed()

        var streak = 0
        val today = format.format(Date())
        val calendar = Calendar.getInstance()

        for (i in workoutDates.indices) {
            val expectedDate = if (i == 0) today else {
                calendar.time = format.parse(today) ?: Date()
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                format.format(calendar.time)
            }

            if (i < workoutDates.size && workoutDates[i] == expectedDate) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

    private fun calculateBestStreak(workouts: List<WorkoutSession>): Int {
        if (workouts.isEmpty()) return 0

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val workoutDates = workouts.mapNotNull { workout ->
            try {
                format.format(format.parse(workout.date.split(" ")[0]) ?: Date())
            } catch (e: Exception) {
                null
            }
        }.distinct().sorted()

        if (workoutDates.isEmpty()) return 0

        var maxStreak = 1
        var currentStreak = 1

        for (i in 1 until workoutDates.size) {
            try {
                val prevDate = format.parse(workoutDates[i-1]) ?: continue
                val currDate = format.parse(workoutDates[i]) ?: continue

                val diffInDays = ((currDate.time - prevDate.time) / (1000 * 60 * 60 * 24)).toInt()

                if (diffInDays == 1) {
                    currentStreak++
                    maxStreak = maxOf(maxStreak, currentStreak)
                } else {
                    currentStreak = 1
                }
            } catch (e: Exception) {
                // Handle parsing error, continue with next iteration
                currentStreak = 1
            }
        }

        return maxStreak
    }

    private fun generateDailyStepsChart(workouts: List<WorkoutSession>, days: Int): List<ChartDataPoint> {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val chartData = mutableListOf<ChartDataPoint>()

        // Generate data for the last 'days' days
        for (i in days - 1 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = format.format(calendar.time)

            val dayWorkouts = workouts.filter { workout ->
                workout.date.startsWith(date)
            }
            val totalSteps = dayWorkouts.sumOf { workout -> workout.steps }

            chartData.add(ChartDataPoint(
                label = SimpleDateFormat("MMM dd", Locale.getDefault()).format(calendar.time),
                value = totalSteps.toFloat()
            ))
        }

        return chartData
    }

    private fun generateDailyDistanceChart(workouts: List<WorkoutSession>, days: Int): List<ChartDataPoint> {
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
            val totalDistance = dayWorkouts.sumOf { workout -> workout.distanceMeters.toDouble() }.toFloat() / 1000 // Convert to km

            chartData.add(ChartDataPoint(
                label = SimpleDateFormat("MMM dd", Locale.getDefault()).format(calendar.time),
                value = totalDistance
            ))
        }

        return chartData
    }
}