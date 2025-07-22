// File: app/src/main/java/com/yourname/fitnesstracker/data/UserProfileRepository.kt
package com.yourname.fitnesstracker.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    fun getUserProfile(): Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun getUserProfileOnce(): UserProfile? = userProfileDao.getUserProfileOnce()

    suspend fun createOrUpdateProfile(profile: UserProfile) {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val updatedProfile = if (profile.createdDate.isEmpty()) {
            profile.copy(createdDate = currentTime, lastUpdated = currentTime)
        } else {
            profile.copy(lastUpdated = currentTime)
        }
        userProfileDao.insertOrUpdateUserProfile(updatedProfile)
    }

    suspend fun isProfileCreated(): Boolean {
        return userProfileDao.getUserProfileCount() > 0
    }

    suspend fun deleteProfile() = userProfileDao.deleteUserProfile()
}