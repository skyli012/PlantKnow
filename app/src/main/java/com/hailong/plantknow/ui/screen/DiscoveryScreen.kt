package com.hailong.plantknow.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.hailong.plantknow.ui.discover.BeautyImagesContent
import com.hailong.plantknow.ui.discover.KnowledgeContent
import com.hailong.plantknow.ui.discover.PlantPost
import com.hailong.plantknow.ui.discover.RecommendContent

// 发现界面 - 包含推荐、美图、知识的切换
// 修改 DiscoveryScreen，移除向右滑动到关注的逻辑
@Composable
fun DiscoveryScreen(
    onSwipeToHome: () -> Unit = {},
    onSwipeToFollowing: () -> Unit = {},
    initialPage: Int = 0,
    onPlantItemClick: (PlantPost) -> Unit // 新增：接收植物项点击事件
) {
    val tabs = listOf("推荐", "美图", "知识")
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { tabs.size })
    var selectedTab by remember { mutableStateOf(tabs[initialPage]) }
    var triggerPageChange by remember { mutableStateOf<Int?>(null) }

    // 监听页面变化，更新选中的标签
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = tabs[pagerState.currentPage]
    }

    // 监听触发的页面变化
    LaunchedEffect(triggerPageChange) {
        triggerPageChange?.let { targetPage ->
            pagerState.animateScrollToPage(targetPage)
            triggerPageChange = null
        }
    }

    // 只保留返回主页的边界滑动逻辑
    LaunchedEffect(pagerState.currentPageOffsetFraction) {
        val offset = pagerState.currentPageOffsetFraction

        // 在第一个页面且向右滑动超过阈值：返回主页
        if (pagerState.currentPage == 0 && offset > 0.5f) {
            onSwipeToHome()
            pagerState.scrollToPage(0) // 重置位置
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE9F0F8))
    ) {
        SecondaryTabs(
            selected = selectedTab,
            tabs = tabs
        ) { tab ->
            val index = tabs.indexOf(tab)
            if (index != -1) {
                triggerPageChange = index
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> RecommendContent(onItemClick = onPlantItemClick)
                1 -> BeautyImagesContent()
                2 -> KnowledgeContent()
            }
        }
    }
}


// 修改 SecondaryTabs，添加tabs列表参数
@Composable
fun SecondaryTabs(selected: String, tabs: List<String>, onTabSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        tabs.forEach { tab ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(tab) }
            ) {
                Text(
                    text = tab,
                    fontSize = 16.sp,
                    color = if (selected == tab) Color.Black else Color.Gray
                )
            }
        }
    }
}

