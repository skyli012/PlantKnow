package com.hailong.plantknow.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
    val mainTabs = listOf("发现", "关注")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { mainTabs.size })
    var selectedTab by remember { mutableStateOf(mainTabs[0]) }
    var triggerMainPageChange by remember { mutableStateOf<Int?>(null) }

    // 监听主页面变化
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = mainTabs[pagerState.currentPage]
    }

    // 监听触发的主页面变化
    LaunchedEffect(triggerMainPageChange) {
        triggerMainPageChange?.let { targetPage ->
            pagerState.animateScrollToPage(targetPage)
            triggerMainPageChange = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE9F0F8))
            .padding(horizontal = 12.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        TopBar(selectedTab) { tab ->
            val index = mainTabs.indexOf(tab)
            if (index != -1) {
                triggerMainPageChange = index
            }
        }

        Spacer(Modifier.height(4.dp))

        // 主 HorizontalPager - 统一管理发现和关注的滑动
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> DiscoveryScreen(
                    onSwipeToHome = onBackClick,
                    // 移除关注相关的边界滑动，因为现在由外层处理
                    onSwipeToFollowing = {},
                    initialPage = 0
                )
                1 -> FollowingScreen(
                    onSwipeToDiscovery = {
                        // 直接滑动到发现页面
                        triggerMainPageChange = 0
                    }
                )
            }
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onTabSelected(tab) }
                    ) {
                        Text(
                            text = tab,
                            fontSize = 20.sp,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        if (selectedTab == tab)
                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(40.dp)
                                    .background(Color(0xFFFF9800))
                            )
                    }
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



