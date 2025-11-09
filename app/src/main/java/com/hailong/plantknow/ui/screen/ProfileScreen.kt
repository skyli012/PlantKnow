// ProfileScreen.kt
package com.hailong.plantknow.ui.screen
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hailong.plantknow.database.FavoritePlantDatabase
import com.hailong.plantknow.model.FavoritePlant
import com.hailong.plantknow.repository.FavoriteRepository
import com.hailong.plantknow.ui.profile.FavoriteEntryCard
import com.hailong.plantknow.ui.profile.OtherFeaturesSection
import com.hailong.plantknow.ui.profile.UserInfoCard
import com.hailong.plantknow.utils.ImageSaver
import com.hailong.plantknow.viewmodel.FavoriteViewModel
import com.hailong.plantknow.viewmodel.FavoriteViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    // 添加回调来控制是否允许滑动返回
    onSwipeEnabledChange: (Boolean) -> Unit = {}
) {
    // 获取上下文
    val context = LocalContext.current

    // 正确获取 FavoriteViewModel
    val favoriteViewModel: FavoriteViewModel = viewModel(
        factory = FavoriteViewModelFactory(
            FavoriteRepository(
                FavoritePlantDatabase.getInstance(context).favoritePlantDao(),
                ImageSaver(context)
            )
        )
    )

    // 在 ProfileScreen 内部管理收藏相关的状态 - 明确指定类型
    var showFavoriteList by remember { mutableStateOf(false) }
    var selectedFavorite by remember { mutableStateOf<FavoritePlant?>(null) }

    // 在 ProfileScreen 内部处理返回键逻辑
    BackHandler(enabled = showFavoriteList || selectedFavorite != null) {
        when {
            selectedFavorite != null -> {
                // 如果正在显示收藏详情，返回收藏列表
                selectedFavorite = null
            }
            showFavoriteList -> {
                // 如果正在显示收藏列表，返回个人主页
                showFavoriteList = false
            }
        }
    }

    // 监听页面变化，通知 MainScreen 是否允许滑动
    LaunchedEffect(showFavoriteList, selectedFavorite) {
        // 只有在显示个人主页时才允许滑动返回
        val allowSwipe = !showFavoriteList && selectedFavorite == null
        onSwipeEnabledChange(allowSwipe)
    }

    // 根据状态显示不同页面
    when {
        selectedFavorite != null -> {
            // 收藏详情页面 - 使用非空断言，因为前面已经检查了不为null
            FavoriteDetailScreen(
                favoritePlant = selectedFavorite!!,
                onBackClick = { selectedFavorite = null }
            )
        }
        showFavoriteList -> {
            // 收藏列表页面
            FavoriteListScreen(
                favoriteViewModel = favoriteViewModel,
                onBackClick = { showFavoriteList = false },
                onItemClick = { favorite ->
                    // 明确指定参数类型
                    selectedFavorite = favorite
                }
            )
        }
        else -> {
            // 个人主页内容
            PersonalProfileContent(
                onFavoritesClick = { showFavoriteList = true }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalProfileContent(
    onFavoritesClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "个人主页",
                        color = Color(0xFF364858),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFFE9F0F8)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 用户信息卡片
            item {
                UserInfoCard()
            }

            // 收藏入口卡片
            item {
                FavoriteEntryCard(onFavoritesClick = onFavoritesClick)
            }

            // 其他功能卡片
            item {
                OtherFeaturesSection()
            }

            // 底部间距
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}





