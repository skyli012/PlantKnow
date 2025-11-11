package com.hailong.plantknow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hailong.plantknow.model.PlantWithDetails
import com.hailong.plantknow.repository.FavoriteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    val favoritePlants = favoriteRepository.getAllFavorites()

    val favoriteCount: StateFlow<Int> = favoriteRepository.getFavoriteCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun addFavorite(plantWithDetails: PlantWithDetails, image: Any?) {
        viewModelScope.launch {
            favoriteRepository.addToFavorites(plantWithDetails, image)
        }
    }

    fun removeFavorite(plantName: String) {
        viewModelScope.launch {
            favoriteRepository.removeFromFavorites(plantName)
        }
    }

    fun isFavorite(plantName: String): StateFlow<Boolean> {
        return favoritePlants.map { plants ->
            plants.any { it.plantName == plantName }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    }
}