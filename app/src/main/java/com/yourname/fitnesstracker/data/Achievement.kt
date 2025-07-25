package com.yourname.fitnesstracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

//CREATE THE ACHIEVEMENT OBJECTS
@Entity(
    tableName = "achievements",
    indices = [Index(value = ["achievementId"], unique = true)]
)
data class Achievement(
    val iconName: String,
    val targetValue: Int,
    val achievedDate: String?,
    val achievementId: String,
    val description: String,
    val isUnlocked: Boolean,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String,
    val points: Int
)
