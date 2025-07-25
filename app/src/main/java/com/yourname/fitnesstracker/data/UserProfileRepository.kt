package com.yourname.fitnesstracker.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
/**
 * Repository class for managing user profile data operations.
 */
class UserProfileRepository(private val userProfileDao: UserProfileDao) {
    /**
     * Gets the user profile as a Flow for reactive updates.
     */
    fun getUserProfile(): Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun getUserProfileOnce(): UserProfile? = userProfileDao.getUserProfileOnce()
    /**
     * Creates a new profile or updates an existing one with timestamps.
     */
    suspend fun createOrUpdateProfile(profile: UserProfile) {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val updatedProfile = if (profile.createdDate.isEmpty()) {
            profile.copy(createdDate = currentTime, lastUpdated = currentTime)
        } else {
            profile.copy(lastUpdated = currentTime)
        }
        userProfileDao.insertOrUpdateUserProfile(updatedProfile)
    }
    /**
     * Checks if a user profile has been created.
     */
    suspend fun isProfileCreated(): Boolean {
        return userProfileDao.getUserProfileCount() > 0
    }
    /**
     * Deletes the user profile from the database.
     */
    suspend fun deleteProfile() = userProfileDao.deleteUserProfile()
}