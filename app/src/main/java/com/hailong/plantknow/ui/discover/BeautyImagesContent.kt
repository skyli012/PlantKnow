package com.hailong.plantknow.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 美图内容 - 空白页面
@Composable
fun BeautyImagesContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "美图专区",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF364858)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "精彩植物美图即将上线",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}