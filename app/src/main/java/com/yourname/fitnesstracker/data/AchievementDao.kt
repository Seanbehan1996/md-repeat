package com.yourname.fitnesstracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, points DESC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY achievedDate DESC")
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 0 ORDER BY points ASC")
    fun getLockedAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE achievementId = :achievementId")
    suspend fun getAchievementById(achievementId: String): Achievement?

    @Query("SELECT SUM(points) FROM achievements WHERE isUnlocked = 1")
    suspend fun getTotalPoints(): Int?

    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    suspend fun getUnlockedCount(): Int

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("UPDATE achievements SET isUnlocked = 1, achievedDate = :date WHERE achievementId = :achievementId")
    suspend fun unlockAchievement(achievementId: String, date: String)

    @Query("DELETE FROM achievements")
    suspend fun deleteAllAchievements()
}
