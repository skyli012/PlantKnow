package com.hailong.plantknow.ui.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hailong.plantknow.data.entity.UserProfile

// UserProfileDialog.kt - 修改版本
@Composable
fun UserProfileDialog(
    userProfile: UserProfile?,
    onDismiss: () -> Unit,
    onEdit: () -> Unit  // 新增编辑回调
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 在 UserProfileDialog 中检查并修复
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5)) // 使用单一颜色
                        .clickable { /* 如果有点击功能的话 */ },
                    contentAlignment = Alignment.Center
                ) {
                    if (!userProfile?.avatarUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = userProfile?.avatarUri,
                            contentDescription = "用户头像",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "用户头像",
                            tint = Color(0xFF7F8C8D),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userProfile?.name ?: "未设置昵称",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )
                }
            }
        },
        text = {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userProfile?.bio ?: "这个人很懒，还没有填写个性签名～",
                    fontSize = 14.sp,
                    color = if (userProfile?.bio.isNullOrEmpty()) Color(0xFFBDC3C7) else Color(0xFF2C3E50),
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )

//                Spacer(modifier = Modifier.height(16.dp))

//                // 添加编辑提示
//                Text(
//                    text = "提示：点击下方按钮可编辑资料",
//                    fontSize = 12.sp,
//                    color = Color(0xFF7F8C8D),
//                    modifier = Modifier.fillMaxWidth()
//                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) {
                    Text("关闭", color = Color(0xFF7F8C8D))
                }

                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC6A9F8)
                    )
                ) {
                    Text("编辑资料", color = Color.White)
                }
            }
        }
    )
}