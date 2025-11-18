package com.hailong.plantknow.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 关注界面 - 空白页面
// 简化 FollowingScreen，移除手势检测
@Composable
fun FollowingScreen(
    onSwipeToDiscovery: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE9F0F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "暂无关注内容",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "关注用户后，他们的动态会显示在这里",
                fontSize = 14.sp,
                color = Color.Gray.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "← 向右滑动返回发现页面",
                fontSize = 12.sp,
                color = Color.Gray.copy(alpha = 0.5f)
            )
        }
    }
}