package com.hailong.plantknow.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.Icon
//import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun CommunityScreen(
    onBackClick: () -> Unit,
    onSwipeEnabledChange: (Boolean) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("发现") }
    var discoveryInitialPage by remember { mutableStateOf(0) } // 0=推荐, 2=知识

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE9F0F8))
            .padding(horizontal = 12.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        TopBar(selectedTab) {
            selectedTab = it
            // 点击标签时重置到推荐页面
            if (it == "发现") {
                discoveryInitialPage = 0
            }
        }
        Spacer(Modifier.height(4.dp))

        when (selectedTab) {
            "关注" -> FollowingScreen(
                onSwipeToDiscovery = {
                    selectedTab = "发现"
                    discoveryInitialPage = 2 // 设置发现页面初始显示知识页面
                }
            )
            "发现" -> DiscoveryScreen(
                onSwipeToHome = onBackClick,
                onSwipeToFollowing = { selectedTab = "关注" },
                initialPage = discoveryInitialPage
            )
        }
    }
}

@Composable
fun TopBar(selectedTab: String, onTabSelected: (String) -> Unit) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 左侧占位，与右侧搜索图标大小相同  为了使 关注和发现居中
            Spacer(modifier = Modifier.size(24.dp))

            // 居中的标签
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                listOf("发现", "关注").forEach { tab ->
                    Text(
                        text = tab,
                        fontSize = 20.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .clickable { onTabSelected(tab) }
                    )
                }
            }

            // 右侧的搜索图标
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        // 白色的分割线
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFDEE8EC))
        )
    }
}


data class PlantPost(
    val img: String,
    val desc: String,
    val author: String,
    val likes: Int,
    val randomHeight: Int
)



