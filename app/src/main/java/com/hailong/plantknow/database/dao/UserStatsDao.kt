// data/dao/UserStatsDao.kt
package com.hailong.plantknow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hailong.plantknow.database.entity.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: UserStats)

    @Update
    suspend fun updateStats(stats: UserStats)

    // 专门的方法来增加计数
    @Query("UPDATE user_stats SET recognitionCount = recognitionCount + 1 WHERE id = 1")
    suspend fun incrementRecognitionCount()

    @Query("UPDATE user_stats SET learningDays = learningDays + 1 WHERE id = 1")
    suspend fun incrementLearningDays()

    // 设置具体数值
    @Query("UPDATE user_stats SET recognitionCount = :count WHERE id = 1")
    suspend fun setRecognitionCount(count: Int)

    @Query("UPDATE user_stats SET learningDays = :days WHERE id = 1")
    suspend fun setLearningDays(days: Int)
}