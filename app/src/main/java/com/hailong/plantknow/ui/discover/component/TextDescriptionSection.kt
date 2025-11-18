package com.hailong.plantknow.ui.discover.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
fun TextDescriptionSection(
    plantPost: PlantPost,
    isLiked: MutableState<Boolean>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 标题和作者信息
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = plantPost.desc,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(plantPost.authorAvatar ?: plantPost.img),
                    contentDescription = "作者头像",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = plantPost.author,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = plantPost.publishTime ?: "2024-01-15",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 点赞按钮
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isLiked.value = !isLiked.value }
                ) {
                    Icon(
                        if (isLiked.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "点赞",
                        tint = if (isLiked.value) Color(0xFFE53935) else Color(0xFF666666),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = plantPost.likes.toString(),
                        fontSize = 14.sp,
                        color = if (isLiked.value) Color(0xFFE53935) else Color(0xFF666666)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 详细内容
        Text(
            text = "植物简介\n\n" +
                    "这是一种非常受欢迎的室内观赏植物，以其独特的叶形和易于养护的特性受到广大植物爱好者的喜爱。\n\n" +
                    "养护要点\n" +
                    "• 光照：喜欢明亮的散射光，避免强烈直射\n" +
                    "• 浇水：保持土壤微湿，避免积水\n" +
                    "• 温度：适宜生长温度18-25°C\n" +
                    "• 施肥：生长季节每月施肥一次\n\n" +
                    "植物特点\n" +
                    "叶片肥厚有光泽，生长速度快，净化空气效果好，非常适合家居和办公室环境。",
            fontSize = 15.sp,
            color = Color(0xFF333333),
            lineHeight = 22.sp
        )
    }
}