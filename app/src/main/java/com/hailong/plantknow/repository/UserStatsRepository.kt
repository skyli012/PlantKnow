// repository/UserStatsRepository.kt
package com.hailong.plantknow.repository

import com.hailong.plantknow.database.UserDatabase
import com.hailong.plantknow.database.entity.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserStatsRepository(private val database: UserDatabase) {

    private val userStatsDao = database.userStatsDao()

    fun getRecognitionCount(): Flow<Int> {
        return userStatsDao.getUserStats().map { it?.recognitionCount ?: 1 }
    }

    fun getLearningDays(): Flow<Int> {
        return userStatsDao.getUserStats().map { it?.learningDays ?: 1 }
    }

    suspend fun initializeIfNeeded() {
        val currentStats = userStatsDao.getUserStats()
        // 如果还没有数据，插入初始数据
        if (currentStats == null) { // 数据库为空
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
}