// PlantCardContent.kt
package com.hailong.plantknow.ui.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun PlantCardContent(
    post: PlantPost,
    onItemClick: (PlantPost) -> Unit // 添加点击回调
) {
    Column(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(7.dp))
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onItemClick(post) } // 添加点击事件
    ) {
        Image(
            painter = rememberAsyncImagePainter(post.img),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(post.randomHeight.dp)
                .clip(RoundedCornerShape(7.dp, 7.dp, 0.dp, 0.dp))
                .background(Color.LightGray, RoundedCornerShape(7.dp, 7.dp, 0.dp, 0.dp)),
            contentScale = ContentScale.Crop
        )
        // 文本内容区域 - 添加左右和下边距
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(post.desc, fontSize = 14.sp, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(post.author, color = Color.Gray, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFF7B7B),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(post.likes.toString(), fontSize = 12.sp)
                }
            }
        }
    }
}