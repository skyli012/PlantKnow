package com.hailong.plantknow.database.dao

import androidx.room.*
import com.hailong.plantknow.model.FavoritePlant
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePlantDao {
    @Query("SELECT * FROM favorite_plants ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoritePlant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(plant: FavoritePlant)

    @Query("DELETE FROM favorite_plants WHERE plantName = :plantName")
    suspend fun deleteFavorite(plantName: String)

    @Query("SELECT COUNT(*) FROM favorite_plants WHERE plantName = :plantName")
    suspend fun isFavorite(plantName: String): Boolean
}