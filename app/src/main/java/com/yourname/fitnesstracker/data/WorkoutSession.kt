package com.yourname.fitnesstracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutSession(
    @PrimaryKey val id: Long,
    val date: String,
    val durationSeconds: Int,
    val steps: Int,
    val distanceMeters: Float,
    val caloriesBurned: Float
)
