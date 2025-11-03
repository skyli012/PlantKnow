package com.hailong.plantknow.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 收藏按钮组件
 * @param isFavorited 当前收藏状态
 * @param onFavoriteClick 点击回调
 * @param modifier 修饰符
 */

@Composable
fun FavoriteButton(
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tintColor = if (isFavorited) {
        Color(0xFFFF5252) // 红色 - 已收藏
    } else {
        Color(0xFF9E9E9E) // 灰色 - 未收藏
    }

    val iconRes = if (isFavorited) {
        Icons.Filled.Favorite // 实心爱心
    } else {
        Icons.Outlined.FavoriteBorder // 空心爱心
    }

    Box(
        modifier = modifier
            .size(48.dp) // 保持合适的点击区域
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // 关键：移除涟漪效果
            ) {
                onFavoriteClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconRes,
            contentDescription = if (isFavorited) "取消收藏" else "收藏",
            tint = tintColor,
            modifier = Modifier.size(28.dp)
        )
    }
}