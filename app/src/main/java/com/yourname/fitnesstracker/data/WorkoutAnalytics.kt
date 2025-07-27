package com.yourname.fitnesstracker.data

data class WorkoutAnalytics(
    val totalWorkouts: Int = 0,
    val totalSteps: Int = 0,
    val totalDistance: Float = 0f, // in meters
    val totalDuration: Int = 0, // in seconds
    val totalCalories: Float = 0f,
    val averageSteps: Float = 0f,
    val averageDistance: Float = 0f, // in km
    val averageDuration: Float = 0f, // in minutes
    val averageCalories: Float = 0f,
    val longestWorkout: Int = 0, // in seconds
    val mostStepsInSession: Int = 0,
    val longestDistance: Float = 0f, // in km
    val currentStreak: Int = 0, // consecutive days with workouts
    val bestStreak: Int = 0
)

data class WeeklyProgress(
    val weekStart: String,
    val weekEnd: String,
    val totalSteps: Int,
    val totalDistance: Float, // in km
    val totalDuration: Int, // in minutes
    val totalCalories: Float,
    val workoutCount: Int,
    val averagePerDay: Float
)

data class MonthlyProgress(
    val month: String, // "2025-07"
    val monthName: String, // "July 2025"
    val totalSteps: Int,
    val totalDistance: Float, // in km
    val totalDuration: Int, // in minutes
    val totalCalories: Float,
    val workoutCount: Int,
    val averagePerDay: Float,
    val bestDay: String?,
    val bestDaySteps: Int
)

data class ChartDataPoint(
    val label: String, // Date or week label
    val value: Float,
    val secondaryValue: Float = 0f // For dual-axis charts
)

// Achievement definitions
object AchievementDefinitions {
    val allAchievements = listOf(
        Achievement(
            achievementId = "first_workout",
            title = "First Steps",
            description = "Complete your first workout",
            iconName = "first_workout",
            category = "workouts",
            targetValue = 1,
            points = 10,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "steps_1000",
            title = "Walker",
            description = "Take 1,000 steps in a single workout",
            iconName = "steps_bronze",
            category = "steps",
            targetValue = 1000,
            points = 15,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "steps_5000",
            title = "Strider",
            description = "Take 5,000 steps in a single workout",
            iconName = "steps_silver",
            category = "steps",
            targetValue = 5000,
            points = 25,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "steps_10000",
            title = "Step Master",
            description = "Take 10,000 steps in a single workout",
            iconName = "steps_gold",
            category = "steps",
            targetValue = 10000,
            points = 50,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "distance_1km",
            title = "Distance Rookie",
            description = "Walk 1 kilometer in a single workout",
            iconName = "distance_bronze",
            category = "distance",
            targetValue = 1,
            points = 15,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "distance_5km",
            title = "Distance Warrior",
            description = "Walk 5 kilometers in a single workout",
            iconName = "distance_silver",
            category = "distance",
            targetValue = 5,
            points = 30,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "distance_10km",
            title = "Distance Champion",
            description = "Walk 10 kilometers in a single workout",
            iconName = "distance_gold",
            category = "distance",
            targetValue = 10,
            points = 60,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "duration_30min",
            title = "Endurance Builder",
            description = "Exercise for 30 minutes continuously",
            iconName = "duration_bronze",
            category = "duration",
            targetValue = 1800,
            points = 20,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "duration_60min",
            title = "Endurance Master",
            description = "Exercise for 1 hour continuously",
            iconName = "duration_silver",
            category = "duration",
            targetValue = 3600,
            points = 40,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "workouts_5",
            title = "Consistency Starter",
            description = "Complete 5 total workouts",
            iconName = "consistency_bronze",
            category = "workouts",
            targetValue = 5,
            points = 25,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "workouts_25",
            title = "Consistency Builder",
            description = "Complete 25 total workouts",
            iconName = "consistency_silver",
            category = "workouts",
            targetValue = 25,
            points = 50,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "workouts_100",
            title = "Consistency Master",
            description = "Complete 100 total workouts",
            iconName = "consistency_gold",
            category = "workouts",
            targetValue = 100,
            points = 100,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "streak_3",
            title = "3-Day Streak",
            description = "Work out for 3 consecutive days",
            iconName = "streak_bronze",
            category = "streak",
            targetValue = 3,
            points = 30,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "streak_7",
            title = "Week Warrior",
            description = "Work out for 7 consecutive days",
            iconName = "streak_silver",
            category = "streak",
            targetValue = 7,
            points = 60,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "streak_30",
            title = "Month Master",
            description = "Work out for 30 consecutive days",
            iconName = "streak_gold",
            category = "streak",
            targetValue = 30,
            points = 150,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "calories_500",
            title = "Calorie Burner",
            description = "Burn 500 calories in a single workout",
            iconName = "calories_bronze",
            category = "calories",
            targetValue = 500,
            points = 25,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "early_bird",
            title = "Early Bird",
            description = "Complete a workout before 8 AM",
            iconName = "early_bird",
            category = "special",
            targetValue = 1,
            points = 20,
            achievedDate = null,
            isUnlocked = false
        ),
        Achievement(
            achievementId = "night_owl",
            title = "Night Owl",
            description = "Complete a workout after 9 PM",
            iconName = "night_owl",
            category = "special",
            targetValue = 1,
            points = 20,
            achievedDate = null,
            isUnlocked = false
        )
    )
}
