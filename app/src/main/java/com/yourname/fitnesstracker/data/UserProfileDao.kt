// File: app/src/main/java/com/yourname/fitnesstracker/data/UserProfileDao.kt
package com.yourname.fitnesstracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun deleteUserProfile()

    @Query("SELECT COUNT(*) FROM user_profile")
    suspend fun getUserProfileCount(): Int
}
