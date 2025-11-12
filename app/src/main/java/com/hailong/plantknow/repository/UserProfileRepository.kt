package com.hailong.plantknow.repository

import com.hailong.plantknow.database.UserProfileDatabase
import com.hailong.plantknow.data.entity.UserProfile
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(
    private val database: UserProfileDatabase
) {
    private val userProfileDao = database.userProfileDao()

    fun observeUserProfile(): Flow<UserProfile?> {
        return userProfileDao.observeUserProfile()
    }

    suspend fun getUserProfile(): UserProfile? {
        return userProfileDao.getUserProfile()
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.insertUserProfile(profile)
    }

    suspend fun updateUserInfo(name: String, bio: String, avatarUri: String) {
        userProfileDao.updateUserInfo(name, bio, avatarUri)
    }
}
