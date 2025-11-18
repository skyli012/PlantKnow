package com.hailong.plantknow.ui.discover.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.hailong.plantknow.ui.detail.Comment
import com.hailong.plantknow.ui.discover.PlantPost


@Composable
fun CommentSection(
    plantPost: PlantPost,
    commentText: MutableState<String>
) {
    val comments = remember {
        listOf(
            Comment(
                id = "1",
                userAvatar = plantPost.img,
                userName = "绿植爱好者",
                content = "这个植物真漂亮！我也养了一盆，确实很好养护",
                publishTime = "2小时前",
                likes = 12
            ),
            Comment(
                id = "2",
                userAvatar = plantPost.img,
                userName = "园艺达人",
                content = "感谢分享养护经验，学到了很多新知识",
                publishTime = "5小时前",
                likes = 8
            ),
            Comment(
                id = "3",
                userAvatar = plantPost.img,
                userName = "植物新手",
                content = "请问这个需要多少阳光？适合放在卧室吗？",
                publishTime = "1天前",
                likes = 5
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 评论标题
        Text(
            text = "评论 (${comments.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 评论输入
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(plantPost.img),
                contentDescription = "我的头像",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            TextField(
                value = commentText.value,
                onValueChange = { commentText.value = it },
                placeholder = {
                    Text(
                        text = "写下你的评论...",
                        fontSize = 14.sp,
                        color = Color(0xFF999999)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF2196F3),
                    unfocusedIndicatorColor = Color(0xFFE0E0E0)
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (commentText.value.isNotEmpty()) {
                        commentText.value = ""
                    }
                },
                enabled = commentText.value.isNotEmpty()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "发送",
                    tint = if (commentText.value.isNotEmpty()) Color(0xFF2196F3) else Color(0xFFBDBDBD)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 评论列表
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            comments.forEach { comment ->
                CommentItem(comment = comment)
            }
        }
    }
}


@Composable
private fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = rememberAsyncImagePainter(comment.userAvatar),
                contentDescription = "用户头像",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 用户信息
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = comment.publishTime,
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 评论内容
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 互动按钮
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { /* 点赞评论 */ }
                    ) {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = "点赞",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = comment.likes.toString(),
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "回复",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.clickable { /* 回复操作 */ }
                    )
                }
            }
        }

        // 分隔线
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFF0F0F0))
        )
    }
}