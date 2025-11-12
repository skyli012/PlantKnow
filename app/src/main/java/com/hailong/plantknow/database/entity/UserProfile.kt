// UserProfile.kt
package com.hailong.plantknow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1, // 只有一个用户，所以固定为1
    val name: String = "PlantKnow",
    val bio: String = "热爱大自然，喜欢探索各种植物奥秘",
    val avatarUri: String = "" // 存储图片的 URI 路径
)