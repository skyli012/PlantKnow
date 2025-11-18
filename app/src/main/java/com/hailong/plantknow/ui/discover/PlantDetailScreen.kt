// PlantDetailScreen.kt
package com.hailong.plantknow.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.hailong.plantknow.ui.discover.PlantPost

@Composable
fun PlantDetailScreen(
    plantPost: PlantPost,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部图片区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(plantPost.img),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 返回按钮
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(24.dp)
                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.Black
                )
            }
        }

        // 内容区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题和描述
            Text(
                text = plantPost.desc,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(16.dp))

            // 作者信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "作者：${plantPost.author}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "点赞",
                        tint = Color(0xFFFF7B7B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = plantPost.likes.toString(),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.size(16.dp))

                    Icon(
                        Icons.Default.Share,
                        contentDescription = "分享",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 详细内容
            Text(
                text = "这里是植物的详细描述信息。可以包含植物的生长习性、养护方法、特点等详细信息。用户可以通过这个页面了解更多关于该植物的知识。",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(16.dp))

            // 更多图片展示
            Text(
                text = "更多图片",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(8.dp))

            // 模拟更多图片
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    Image(
                        painter = rememberAsyncImagePainter("${plantPost.img}?size=small$index"),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}