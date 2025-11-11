package com.hailong.plantknow.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hailong.plantknow.database.UserDatabase
import com.hailong.plantknow.repository.PlantRecognitionRepository
import com.hailong.plantknow.repository.UserStatsRepository

/**
 * PlantViewModel 的工厂类
 * 用于创建带有正确依赖的 PlantViewModel 实例
 */
class PlantViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantViewModel::class.java)) {

            // ✅ 1. 先获取数据库实例
            val userDatabase = UserDatabase.getDatabase(context)

            // ✅ 2. 传入数据库实例给仓库
            val userStatsRepository = UserStatsRepository(userDatabase)

            // ✅ 3. 植物识别仓库依赖用户统计仓库
            val plantRepository = PlantRecognitionRepository(context, userStatsRepository)

            @Suppress("UNCHECKED_CAST")
            return PlantViewModel(plantRepository, userStatsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
