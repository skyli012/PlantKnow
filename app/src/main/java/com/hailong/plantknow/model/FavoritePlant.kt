package com.hailong.plantknow.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "favorite_plants")
data class FavoritePlant(
    @PrimaryKey
    val plantName: String,
    val confidence: Int,
    val description: String,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)