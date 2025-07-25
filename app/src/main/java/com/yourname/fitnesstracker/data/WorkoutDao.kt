package com.yourname.fitnesstracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insertWorkout(workout: WorkoutSession)

    @Query("SELECT * FROM workouts ORDER BY date DESC")
    suspend fun getAllWorkouts(): List<WorkoutSession>

    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkoutsFlow(): Flow<List<WorkoutSession>>

}