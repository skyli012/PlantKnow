package com.hailong.plantknow.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hailong.plantknow.R

@Composable
fun WelcomeContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 上半部分内容
        Column(
            modifier = Modifier.fillMaxWidth(),


            ) {
            Text(
                text = "你好！",
                color = Color(0xFF364858),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 20.dp, bottom = 24.dp, start = 5.dp)
            )

            Text(
                text = "欢迎使用PlantKnow",
                color = Color(0xFF364858),
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp, start = 5.dp)
            )

            Text(
                text = "拍摄或选择一张植物照片，AI将为您识别解答。",
                color = Color(0xFF666666),
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 20.dp, start = 5.dp)
            )

            Text(
                text = "学习与探索。",
                color = Color(0xFF666666),
                fontSize = 12.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(bottom = 40.dp, start = 5.dp)
            )
        }

        // 使用weight让图片在剩余空间中居中
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.home_flower), // 替换为您的花朵图片资源ID
                contentDescription = "装饰花朵",
                modifier = Modifier
                    .size(180.dp), // 调整图片大小
                contentScale = ContentScale.Fit
            )
        }
    }
}