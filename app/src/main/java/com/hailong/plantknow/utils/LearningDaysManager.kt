// utils/LearningDaysManager.kt
package com.hailong.plantknow.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class LearningDaysManager private constructor(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val PREFS_NAME = "learning_days_prefs"
        private const val KEY_LAST_LEARNING_DATE = "last_learning_date"

        @Volatile
        private var instance: LearningDaysManager? = null

        fun getInstance(context: Context): LearningDaysManager {
            return instance ?: synchronized(this) {
                instance ?: LearningDaysManager(
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                ).also { instance = it }
            }
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * 检查并记录今天的学习活动
     * @return 如果今天是新的一天且需要增加学习天数，返回true
     */
    fun shouldIncrementLearningDays(): Boolean {
        val today = getTodayDateString()
        val lastLearningDate = sharedPreferences.getString(KEY_LAST_LEARNING_DATE, "")

        // 如果今天已经记录过，不需要增加天数
        if (lastLearningDate == today) {
            return false
        }

        // 记录今天的学习日期
        sharedPreferences.edit()
            .putString(KEY_LAST_LEARNING_DATE, today)
            .apply()

        return true
    }

    /**
     * 获取最后学习日期
     */
    fun getLastLearningDate(): String {
        return sharedPreferences.getString(KEY_LAST_LEARNING_DATE, "") ?: ""
    }

    /**
     * 检查今天是否已经学习过
     */
    fun hasLearnedToday(): Boolean {
        val today = getTodayDateString()
        val lastLearningDate = sharedPreferences.getString(KEY_LAST_LEARNING_DATE, "")
        return lastLearningDate == today
    }

    /**
     * 重置学习记录（用于测试）
     */
    fun resetLearningRecord() {
        sharedPreferences.edit()
            .remove(KEY_LAST_LEARNING_DATE)
            .apply()
    }

    private fun getTodayDateString(): String {
        return dateFormat.format(Date())
    }
}