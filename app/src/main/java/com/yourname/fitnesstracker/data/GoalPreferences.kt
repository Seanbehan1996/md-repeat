package com.yourname.fitnesstracker.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore


private val Context.dataStore by preferencesDataStore(name = "user_goals")

class GoalPreferences(private val context: Context) {

    companion object {
        private val STEPS_KEY = intPreferencesKey("goal_steps")
        private val DISTANCE_KEY = floatPreferencesKey("goal_distance")
        private val DURATION_KEY = intPreferencesKey("goal_duration")
    }

    val userGoals: Flow<UserGoals> = context.dataStore.data.map { preferences ->
        UserGoals(
            steps = preferences[STEPS_KEY] ?: 10000,
            distance = preferences[DISTANCE_KEY] ?: 5000f,
            duration = preferences[DURATION_KEY] ?: 1800
        )
    }

    suspend fun saveGoals(goals: UserGoals) {
        context.dataStore.edit { prefs ->
            prefs[STEPS_KEY] = goals.steps
            prefs[DISTANCE_KEY] = goals.distance
            prefs[DURATION_KEY] = goals.duration
        }
    }
}
