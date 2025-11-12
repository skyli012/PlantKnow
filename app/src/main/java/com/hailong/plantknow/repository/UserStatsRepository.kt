// repository/UserStatsRepository.kt
package com.hailong.plantknow.repository

import android.content.Context
import com.hailong.plantknow.database.UserDatabase
import com.hailong.plantknow.database.entity.UserStats
import com.hailong.plantknow.utils.LearningDaysManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserStatsRepository(
    private val database: UserDatabase,
    private val context: Context // 添加Context参数
) {

    private val userStatsDao = database.userStatsDao()
    private val learningDaysManager = LearningDaysManager.getInstance(context)

    fun getRecognitionCount(): Flow<Int> {
        return userStatsDao.getUserStats().map { it?.recognitionCount ?: 0 }
    }

    fun getLearningDays(): Flow<Int> {
        return userStatsDao.getUserStats().map { it?.learningDays ?: 0 }
    }

    // 获取用户统计信息的完整Flow
    fun getUserStats(): Flow<UserStats?> {
        return userStatsDao.getUserStats()
    }

    suspend fun initializeIfNeeded() {
        val currentStats = userStatsDao.getUserStats()
        // 如果还没有数据，插入初始数据
        if (currentStats == null) {
            userStatsDao.insertOrUpdateStats(
                UserStats(id = 1, recognitionCount = 0, learningDays = 0)
            )
        }
    }

    suspend fun incrementRecognitionCount() {
        userStatsDao.incrementRecognitionCount()
    }

    suspend fun incrementLearningDays() {
        userStatsDao.incrementLearningDays()
    }

    /**
     * 记录学习活动 - 核心方法
     * 检查今天是否已经学习过，如果没有则增加学习天数
     * @return 如果增加了学习天数返回true，否则返回false
     */
    suspend fun recordLearningActivity(): Boolean {
        // 检查是否需要增加学习天数
        if (learningDaysManager.shouldIncrementLearningDays()) {
            // 增加学习天数
            incrementLearningDays()
            return true
        }
        return false
    }

    /**
     * 检查今天是否已经学习过
     */
    fun hasLearnedToday(): Boolean {
        return learningDaysManager.hasLearnedToday()
    }

    /**
     * 获取最后学习日期
     */
    fun getLastLearningDate(): String {
        return learningDaysManager.getLastLearningDate()
    }
}