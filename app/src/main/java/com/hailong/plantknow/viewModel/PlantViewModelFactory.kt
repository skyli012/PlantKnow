package com.hailong.plantknow.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hailong.plantknow.repository.PlantRecognitionRepository

/**
 * PlantViewModel 的工厂类
 * 用于创建带有正确依赖的 PlantViewModel 实例
 */
class PlantViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantViewModel::class.java)) {
            val repository = PlantRecognitionRepository(context)
            @Suppress("UNCHECKED_CAST")
            return PlantViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
