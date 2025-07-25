package com.yourname.fitnesstracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourname.fitnesstracker.data.Achievement

@Database(
    entities = [WorkoutSession::class, UserProfile::class, Achievement::class],
    version = 3,
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun achievementDao(): AchievementDao

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

        // Migration from version 2 to 3 (adding Achievement table)
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Re-create the table using Room's expected structure
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `achievements_new` (
                `iconName` TEXT NOT NULL,
                `targetValue` INTEGER NOT NULL,
                `achievedDate` TEXT,
                `achievementId` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `isUnlocked` INTEGER NOT NULL,
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `title` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `points` INTEGER NOT NULL
            )
        """.trimIndent())

                // If an old 'achievements' table exists, drop it
                database.execSQL("DROP TABLE IF EXISTS achievements")

                // Rename the new table to match the expected name
                database.execSQL("ALTER TABLE achievements_new RENAME TO achievements")

                // Recreate the index
                database.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS `index_achievements_achievementId` 
            ON `achievements` (`achievementId`)
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}