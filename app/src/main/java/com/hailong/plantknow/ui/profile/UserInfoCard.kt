package com.hailong.plantknow.ui.profile

import androidx.compose.foundation.background
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
import com.hailong.plantknow.ui.component.UserProfileDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hailong.plantknow.viewmodel.FavoriteViewModel
import com.hailong.plantknow.viewmodel.UserStatsViewModel
import com.hailong.plantknow.viewmodel.UserStatsViewModelFactory
import com.hailong.plantknow.viewmodel.UserProfileViewModel
import com.hailong.plantknow.viewmodel.UserProfileViewModelFactory

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
    val showProfileDialog = remember { mutableStateOf(false) }
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
                .clickable { showProfileDialog.value = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 用户头像
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6BBD6E),
                                Color(0xFF4CAF50)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "用户头像",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
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
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = userProfile?.bio ?: "热爱大自然，喜欢探索各种植物奥秘",
                    fontSize = 13.sp,
                    color = Color(0xFF7F8C8D)
                )
            }
        }

        // 弹窗：展示用户完整信息（抽到组件中）
        if (showProfileDialog.value) {
            UserProfileDialog(userProfile = userProfile) {
                showProfileDialog.value = false
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 第二板块：统计数据 - 每个项都有白框
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BoxedStatItem(
                count = recognitionCount.toString(), // 从Room数据库读取
                label = "识别次数",
                modifier = Modifier.weight(1f)
            )
            BoxedStatItem(
                count = learningDays.toString(), // 从Room数据库读取
                label = "学习天数",
                modifier = Modifier.weight(1f)
            )
            BoxedStatItem(
                count = favoriteCount.toString(), // ✅ 使用数据库实时值
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