// viewmodel/UserStatsViewModel.kt
package com.hailong.plantknow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hailong.plantknow.repository.UserStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserStatsViewModel(private val repository: UserStatsRepository) : ViewModel() {

    private val _recognitionCount = MutableStateFlow(0)
    val recognitionCount: StateFlow<Int> = _recognitionCount.asStateFlow()

    private val _learningDays = MutableStateFlow(0)
    val learningDays: StateFlow<Int> = _learningDays.asStateFlow()

    init {
        // 初始化时从 repository 同步最新数据
        viewModelScope.launch {
            repository.initializeIfNeeded()

            // 监听数据库/仓库变化
            launch {
                repository.getRecognitionCount().collect { count ->
                    _recognitionCount.value = count
                }
            }
            launch {
                repository.getLearningDays().collect { days ->
                    _learningDays.value = days
                }
            }
        }
    }

    fun incrementRecognitionCount() {
        viewModelScope.launch {
            repository.incrementRecognitionCount()
        }
    }

    fun incrementLearningDays() {
        viewModelScope.launch {
            repository.incrementLearningDays()
        }
    }
}
