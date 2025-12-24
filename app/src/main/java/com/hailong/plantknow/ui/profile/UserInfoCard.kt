package com.hailong.plantknow.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.hailong.plantknow.ui.component.EditProfileDialog
import com.hailong.plantknow.ui.component.UserProfileDialog
import com.hailong.plantknow.viewmodel.FavoriteViewModel
import com.hailong.plantknow.viewmodel.UserProfileViewModel
import com.hailong.plantknow.viewmodel.UserProfileViewModelFactory
import com.hailong.plantknow.viewmodel.UserStatsViewModel
import com.hailong.plantknow.viewmodel.UserStatsViewModelFactory

@Composable
fun UserInfoCard(
    favoriteViewModel: FavoriteViewModel = viewModel(),
    userStatsViewModel: UserStatsViewModel = viewModel(
        factory = UserStatsViewModelFactory(LocalContext.current)
    )
) {
    val userProfileViewModel: UserProfileViewModel = viewModel(
        factory = UserProfileViewModelFactory(LocalContext.current)
    )
    val userProfile by userProfileViewModel.userProfile.collectAsState(initial = null)

    // 使用简单的布尔状态管理
    val showProfileDialog = remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf(false) }

    val favoriteCount by favoriteViewModel.favoriteCount.collectAsState()
    val recognitionCount by userStatsViewModel.recognitionCount.collectAsState(initial = 128)
    val learningDays by userStatsViewModel.learningDays.collectAsState(initial = 42)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 第一板块：头像和基本信息（可点击，打开用户信息弹窗）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null  // 禁用点击反馈效果
                ) { showProfileDialog.value = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 用户头像
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
                    .clickable { showProfileDialog.value = true },
                contentAlignment = Alignment.Center
            ) {
                if (!userProfile?.avatarUri.isNullOrEmpty()) {
                    // 有头像：显示数据库中的图片
                    AsyncImage(
                        model = userProfile?.avatarUri,
                        contentDescription = "用户头像",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 没有头像：显示默认图标
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "用户头像",
                        tint = Color(0xFF7F8C8D),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 用户信息（来自数据库）
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userProfile?.name ?: "Skyyy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = userProfile?.bio ?: "热爱大自然，喜欢探索各种植物奥秘",
                    fontSize = 13.sp,
                    color = Color(0xFF000000)
                )
            }
        }

        // 展示弹窗 - 使用条件判断确保正确的层级
        if (showProfileDialog.value && !showEditDialog.value) {
            UserProfileDialog(
                userProfile = userProfile,
                onDismiss = {
                    // 直接返回到个人主页
                    showProfileDialog.value = false
                },
                onEdit = {
                    // 进入编辑对话框，但不关闭资料对话框
                    showEditDialog.value = true
                }
            )
        }

        // 编辑弹窗
        if (showEditDialog.value) {
            EditProfileDialog(
                userProfile = userProfile,
                onDismiss = {
                    // 只能返回到资料对话框，不能直接到个人主页
                    showEditDialog.value = false
                },
                onSave = { name, bio, avatarUri ->
                    userProfileViewModel.updateUserInfo(name, bio, avatarUri)
                    // 保存后返回到资料对话框
                    showEditDialog.value = false
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 第二板块：统计数据 - 每个项都有白框
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BoxedStatItem(
                count = recognitionCount.toString(),
                label = "识别次数",
                modifier = Modifier.weight(1f)
            )
            BoxedStatItem(
                count = learningDays.toString(),
                label = "学习天数",
                modifier = Modifier.weight(1f)
            )
            BoxedStatItem(
                count = favoriteCount.toString(),
                label = "收藏数量",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BoxedStatItem(
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFFFFF))
            .padding(vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = count,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF95A5A6)
            )
        }
    }
}