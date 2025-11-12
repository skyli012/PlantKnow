package com.hailong.plantknow.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hailong.plantknow.data.entity.UserProfile
import kotlinx.coroutines.flow.Flow

// UserProfileDao.kt
@Dao
interface UserProfileDao {

    // 一次性获取（suspend）
    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfile(): UserProfile?

    // 观察数据库变化，推荐用于 UI 层的实时显示
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)

    @Query("UPDATE user_profile SET name = :name, bio = :bio, avatarUri = :avatarUri WHERE id = 1")
    suspend fun updateUserInfo(name: String, bio: String, avatarUri: String)
}