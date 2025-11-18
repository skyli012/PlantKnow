package com.hailong.plantknow.ui.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 原来的 WaterfallContent 和 PlantCard 保持不变...
@Composable
fun RecommendContent() {
    val data = remember {
        (1..10).map {
            PlantPost(
                img = "https://picsum.photos/300/30$it",
                desc = "文本描述$it",
                author = "作者$it",
                likes = 65,
                randomHeight = (140..240).random()
            )
        }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 4.dp,    // 左边距
            top = 2.dp,
            end = 4.dp,      // 右边距
        ),
        verticalItemSpacing = 5.dp,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(data) { card ->
            PlantCardContent(card)
        }
    }
}
data class PlantPost(
    val img: String,
    val desc: String,
    val author: String,
    val likes: Int,
    val randomHeight: Int
)