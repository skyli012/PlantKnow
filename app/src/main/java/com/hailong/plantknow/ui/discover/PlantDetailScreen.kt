// PlantDetailScreen.kt
package com.hailong.plantknow.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hailong.plantknow.ui.discover.PlantPost
import com.hailong.plantknow.ui.discover.component.CommentSection
import com.hailong.plantknow.ui.discover.component.TextDescriptionSection

// 评论数据类
data class Comment(
    val id: String,
    val userAvatar: String,
    val userName: String,
    val content: String,
    val publishTime: String,
    val likes: Int
)

@Composable
fun PlantDetailScreen(
    plantPost: PlantPost,
    onBackClick: () -> Unit
) {
    val isLiked = remember { mutableStateOf(false) }
    val commentText = remember { mutableStateOf("") }

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = remember { mutableStateOf(true) }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = useDarkIcons.value
        )
        // 设置底部导航栏
        systemUiController.setNavigationBarColor(
            color = Color.White,
            darkIcons = useDarkIcons.value
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部栏
        TopAppBar(onBackClick = onBackClick)

        // 1. 图片展示板块
        ImageDisplaySection(plantPost = plantPost)

        // 2. 文本描述板块
        TextDescriptionSection(
            plantPost = plantPost,
            isLiked = isLiked
        )

        // 3. 评论板块
        CommentSection(
            plantPost = plantPost,
            commentText = commentText
        )
    }
}

@Composable
private fun TopAppBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "植物详情",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ImageDisplaySection(plantPost: PlantPost) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(plantPost.img),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}




