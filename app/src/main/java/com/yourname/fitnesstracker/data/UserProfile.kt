// File: app/src/main/java/com/yourname/fitnesstracker/data/UserProfile.kt
package com.yourname.fitnesstracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single user profile
    val name: String = "",
    val age: Int = 25,
    val weight: Float = 70.0f, // in kg
    val height: Float = 170.0f, // in cm
    val gender: String = "Not specified", // "Male", "Female", "Other", "Not specified"
    val activityLevel: String = "Moderate", // "Sedentary", "Light", "Moderate", "Active", "Very Active"
    val fitnessGoal: String = "General Fitness", // "Weight Loss", "Muscle Gain", "Endurance", "General Fitness"
    val profileImagePath: String? = null,
    val createdDate: String = "",
    val lastUpdated: String = ""
) {
    // Calculate BMI
    fun calculateBMI(): Float {
        val heightInMeters = height / 100
        return weight / (heightInMeters * heightInMeters)
    }

    // Get BMI category
    fun getBMICategory(): String {
        val bmi = calculateBMI()
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    // Calculate recommended daily calories (simplified Mifflin-St Jeor equation)
    fun calculateRecommendedCalories(): Int {
        val bmr = when (gender.lowercase()) {
            "male" -> (10 * weight) + (6.25 * height) - (5 * age) + 5
            "female" -> (10 * weight) + (6.25 * height) - (5 * age) - 161
            else -> (10 * weight) + (6.25 * height) - (5 * age) - 78 // Average
        }

        val activityMultiplier = when (activityLevel) {
            "Sedentary" -> 1.2f
            "Light" -> 1.375f
            "Moderate" -> 1.55f
            "Active" -> 1.725f
            "Very Active" -> 1.9f
            else -> 1.55f
        }

        return (bmr * activityMultiplier).toInt()
    }

    // Calculate recommended daily steps based on fitness goal
    fun getRecommendedSteps(): Int {
        return when (fitnessGoal) {
            "Weight Loss" -> 12000
            "Muscle Gain" -> 8000
            "Endurance" -> 15000
            "General Fitness" -> 10000
            else -> 10000
        }
    }

    // Calculate recommended workout duration (minutes)
    fun getRecommendedWorkoutDuration(): Int {
        return when (activityLevel) {
            "Sedentary" -> 20
            "Light" -> 25
            "Moderate" -> 30
            "Active" -> 45
            "Very Active" -> 60
            else -> 30
        }
    }

    // Get fitness assessment
    fun getFitnessAssessment(): FitnessAssessment {
        val bmi = calculateBMI()
        val bmiScore = when {
            bmi in 18.5..24.9 -> 4
            bmi in 25.0..29.9 -> 3
            bmi in 17.0..18.4 -> 3
            bmi in 30.0..34.9 -> 2
            else -> 1
        }

        val ageScore = when {
            age in 18..25 -> 5
            age in 26..35 -> 4
            age in 36..45 -> 3
            age in 46..55 -> 2
            else -> 1
        }

        val activityScore = when (activityLevel) {
            "Very Active" -> 5
            "Active" -> 4
            "Moderate" -> 3
            "Light" -> 2
            "Sedentary" -> 1
            else -> 3
        }

        val totalScore = bmiScore + ageScore + activityScore
        val level = when {
            totalScore >= 12 -> "Excellent"
            totalScore >= 10 -> "Good"
            totalScore >= 8 -> "Average"
            totalScore >= 6 -> "Below Average"
            else -> "Poor"
        }

        return FitnessAssessment(
            overallScore = totalScore,
            fitnessLevel = level,
            bmiScore = bmiScore,
            ageScore = ageScore,
            activityScore = activityScore,
            recommendations = generateRecommendations(level, fitnessGoal)
        )
    }

    private fun generateRecommendations(level: String, goal: String): List<String> {
        val recommendations = mutableListOf<String>()

        when (level) {
            "Poor", "Below Average" -> {
                recommendations.add("Start with 10-15 minute daily walks")
                recommendations.add("Focus on building consistency before intensity")
                recommendations.add("Consider consulting a fitness professional")
            }
            "Average" -> {
                recommendations.add("Aim for 150 minutes of moderate activity per week")
                recommendations.add("Include 2-3 strength training sessions")
                recommendations.add("Gradually increase workout intensity")
            }
            "Good", "Excellent" -> {
                recommendations.add("Maintain your current activity level")
                recommendations.add("Challenge yourself with varied workouts")
                recommendations.add("Consider training for specific events")
            }
        }

        when (goal) {
            "Weight Loss" -> {
                recommendations.add("Create a moderate calorie deficit")
                recommendations.add("Combine cardio with strength training")
                recommendations.add("Track your food intake")
            }
            "Muscle Gain" -> {
                recommendations.add("Focus on progressive strength training")
                recommendations.add("Ensure adequate protein intake")
                recommendations.add("Allow proper recovery between sessions")
            }
            "Endurance" -> {
                recommendations.add("Gradually increase workout duration")
                recommendations.add("Include interval training")
                recommendations.add("Focus on cardiovascular activities")
            }
        }

        return recommendations
    }
}

// Fitness Assessment data class
data class FitnessAssessment(
    val overallScore: Int,
    val fitnessLevel: String,
    val bmiScore: Int,
    val ageScore: Int,
    val activityScore: Int,
    val recommendations: List<String>
)