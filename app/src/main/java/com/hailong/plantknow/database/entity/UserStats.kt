package com.hailong.plantknow.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1, // 固定为1，只有一个用户数据
    val recognitionCount: Int = 0,
    val learningDays: Int = 0
)