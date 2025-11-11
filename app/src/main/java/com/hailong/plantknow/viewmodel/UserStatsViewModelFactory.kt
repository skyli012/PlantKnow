package com.hailong.plantknow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import com.hailong.plantknow.repository.UserStatsRepository
import com.hailong.plantknow.database.UserDatabase

class UserStatsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserStatsViewModel::class.java)) {
            val database = UserDatabase.getInstance(context)
            val repository = UserStatsRepository(database)
            return UserStatsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
