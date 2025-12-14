package com.hailong.plantknow.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
/**
 * 加载内容
 */
@Composable
fun LoadingContent(
    recognitionStep: String,
    isFullScreen: Boolean = false,
    selectedImage: Any? = null
) {
    if (isFullScreen) {
        // 全屏加载模式
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 图片预览（如果提供了图片）
            if (selectedImage != null) {
                // 这里可以添加图片预览组件
                Spacer(modifier = Modifier.height(40.dp))
            }

            // 加载动画
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = Color(0xFF4CAF50),
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 加载文本
            Text(
                text = recognitionStep,
                color = Color(0xFF364858),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = TextAlign.Center
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF364858),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = recognitionStep,
                    color = Color(0xFF666666),
                    fontSize = 16.sp
                )
            }
        }
    }
}