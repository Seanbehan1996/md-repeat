package com.yourname.fitnesstracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WorkoutSession::class, UserProfile::class],
    version = 2, // Increment version for new entity
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        // Migration from version 1 to 2 (adding UserProfile table)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_profile` (
                        `id` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `age` INTEGER NOT NULL,
                        `weight` REAL NOT NULL,
                        `height` REAL NOT NULL,
                        `gender` TEXT NOT NULL,
                        `activityLevel` TEXT NOT NULL,
                        `fitnessGoal` TEXT NOT NULL,
                        `profileImagePath` TEXT,
                        `createdDate` TEXT NOT NULL,
                        `lastUpdated` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                )
                    .addMigrations(MIGRATION_1_2) // Add migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}