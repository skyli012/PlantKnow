package com.hailong.plantknow.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.hailong.plantknow.database.dao.FavoritePlantDao
import com.hailong.plantknow.model.FavoritePlant

@Database(
    entities = [FavoritePlant::class],
    version = 1,
    exportSchema = false
)
abstract class FavoritePlantDatabase : RoomDatabase() {
    abstract fun favoritePlantDao(): FavoritePlantDao

    companion object {
        @Volatile
        private var INSTANCE: FavoritePlantDatabase? = null

        fun getInstance(context: Context): FavoritePlantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavoritePlantDatabase::class.java,
                    "favorite_plants_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}