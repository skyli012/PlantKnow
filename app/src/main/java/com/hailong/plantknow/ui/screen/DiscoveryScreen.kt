package com.hailong.plantknow.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hailong.plantknow.ui.discover.BeautyImagesContent
import com.hailong.plantknow.ui.discover.KnowledgeContent
import com.hailong.plantknow.ui.discover.WaterfallContent

// 发现界面 - 包含推荐、美图、知识的切换
@Composable
fun DiscoveryScreen(
    onSwipeToHome: () -> Unit = {},
    onSwipeToFollowing: () -> Unit = {},
    initialPage: Int = 0
) {
    val tabs = listOf("推荐", "美图", "知识")
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { tabs.size })
    var selectedTab by remember { mutableStateOf(tabs[initialPage]) }
    var triggerPageChange by remember { mutableStateOf<Int?>(null) }

    // 监听页面变化，更新选中的标签
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = tabs[pagerState.currentPage]
    }

    // 监听标签点击，切换页面
    LaunchedEffect(selectedTab) {
        val index = tabs.indexOf(selectedTab)
        if (index != -1 && index != pagerState.currentPage) {
            pagerState.animateScrollToPage(index)
        }
    }

    // 监听触发的页面变化
    LaunchedEffect(triggerPageChange) {
        triggerPageChange?.let { targetPage ->
            pagerState.animateScrollToPage(targetPage)
            triggerPageChange = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    when {
                        // 从左往右滑动：返回上一个页面
                        dragAmount > 40 -> {
                            when (pagerState.currentPage) {
                                0 -> onSwipeToHome() // 推荐页面返回主页
                                1 -> triggerPageChange = 0 // 美图页面返回推荐
                                2 -> triggerPageChange = 1 // 知识页面返回美图
                            }
                        }
                        // 从右往左滑动：前往下一个页面
                        dragAmount < -40 -> {
                            when (pagerState.currentPage) {
                                0 -> triggerPageChange = 1 // 推荐页面前往美图
                                1 -> triggerPageChange = 2 // 美图页面前往知识
                                2 -> onSwipeToFollowing() // 知识页面前往关注
                            }
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SecondaryTabs(selectedTab, tabs) { selectedTab = it }
            Spacer(Modifier.height(12.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> WaterfallContent()
                    1 -> BeautyImagesContent()
                    2 -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE9F0F8)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "植物知识",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF364858)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "丰富的植物知识库正在建设中",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
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

