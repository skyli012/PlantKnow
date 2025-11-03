package com.hailong.plantknow.repository

import com.hailong.plantknow.database.dao.FavoritePlantDao
import com.hailong.plantknow.model.FavoritePlant
import com.hailong.plantknow.model.PlantWithDetails
import com.hailong.plantknow.model.confidencePercent
import com.hailong.plantknow.utils.ImageSaver
import kotlinx.coroutines.flow.Flow

class FavoriteRepository(
    private val favoritePlantDao: FavoritePlantDao,
    private val imageSaver: ImageSaver
) {

    fun getAllFavorites(): Flow<List<FavoritePlant>> = favoritePlantDao.getAllFavorites()

    suspend fun addToFavorites(plantWithDetails: PlantWithDetails, image: Any?) {
        // 保存图片并获取永久 URI
        val savedImageUri = imageSaver.saveImageForFavorite(image)

        val favoritePlant = FavoritePlant(
            plantName = plantWithDetails.basicInfo.plantName,
            confidence = plantWithDetails.basicInfo.confidencePercent,
            description = plantWithDetails.detailedDescription,
            imageUri = savedImageUri
        )
        favoritePlantDao.insertFavorite(favoritePlant)
    }

    suspend fun removeFromFavorites(plantName: String) {
        favoritePlantDao.deleteFavorite(plantName)
    }

    suspend fun isFavorite(plantName: String): Boolean {
        return favoritePlantDao.isFavorite(plantName)
    }
}