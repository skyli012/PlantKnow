package com.hailong.plantknow.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hailong.plantknow.data.entity.UserProfile
import com.hailong.plantknow.database.dao.UserProfileDao

@Database(
    entities = [UserProfile::class],
    version = 1,
    exportSchema = false
)
abstract class UserProfileDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: UserProfileDatabase? = null

        fun getInstance(context: Context): UserProfileDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserProfileDatabase::class.java,
                    "user_profile_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
