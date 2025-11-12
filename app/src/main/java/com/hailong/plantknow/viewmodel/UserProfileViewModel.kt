package com.hailong.plantknow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hailong.plantknow.data.entity.UserProfile
import com.hailong.plantknow.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(private val repository: UserProfileRepository) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        viewModelScope.launch {
            // 如果没有数据，插入一行默认数据
            if (repository.getUserProfile() == null) {
                repository.saveUserProfile(UserProfile())
            }
            // 订阅数据库变化
            repository.observeUserProfile().collect { profile ->
                _userProfile.value = profile
            }
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun updateUserInfo(name: String, bio: String, avatarUri: String) {
        viewModelScope.launch {
            repository.updateUserInfo(name, bio, avatarUri)
        }
    }
}
