package com.hailong.plantknow.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hailong.plantknow.R
import com.hailong.plantknow.database.FavoritePlantDatabase
import com.hailong.plantknow.repository.FavoriteRepository
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.hailong.plantknow.model.FavoritePlant
import com.hailong.plantknow.ui.component.ErrorCard
import com.hailong.plantknow.ui.component.LoadingContent
import com.hailong.plantknow.ui.component.PermissionDialog
import com.hailong.plantknow.ui.component.PermissionExplanationDialog
import com.hailong.plantknow.ui.component.PlantBasicInfoWithStickyHeader
import com.hailong.plantknow.ui.component.PlantDetailsWithStickyHeader
import com.hailong.plantknow.ui.component.WelcomeContent
import com.hailong.plantknow.ui.screen.CommunityScreen
import com.hailong.plantknow.ui.screen.FavoriteDetailScreen
import com.hailong.plantknow.ui.screen.FavoriteListScreen
import com.hailong.plantknow.ui.screen.ProfileScreen
import com.hailong.plantknow.utils.ImageSaver
import com.hailong.plantknow.utils.PermissionChecker
import com.hailong.plantknow.viewmodel.FavoriteViewModel
import com.hailong.plantknow.viewmodel.FavoriteViewModelFactory
import com.hailong.plantknow.viewmodel.PlantViewModel
import com.hailong.plantknow.viewmodel.PlantViewModelFactory
import kotlinx.coroutines.launch

/**
 * 主屏幕Composable函数
 * 负责图片选择、预览和AI植物识别结果显示
 * @param viewModel 植物识别ViewModel，负责状态管理和业务逻辑
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PlantViewModel = viewModel(
        factory = PlantViewModelFactory(LocalContext.current)
    )
) {
    // 系统UI控制器，用于设置状态栏颜色
    val systemUiController = rememberSystemUiController()
    val bgColor1 = Color(0xFFE9F0F8) // 浅蓝色背景

    // 设置状态栏颜色，并在组件销毁时清理
    DisposableEffect(Unit) {
        systemUiController.setStatusBarColor(
            color = bgColor1,
            darkIcons = true
        )
        onDispose {}
    }

    // 观察UI状态
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 添加必要的依赖
    val imageSaver = remember { ImageSaver(context) }
    val favoriteViewModel: FavoriteViewModel = viewModel(
        factory = FavoriteViewModelFactory(
            FavoriteRepository(
                FavoritePlantDatabase.getInstance(context).favoritePlantDao(),
                imageSaver
            )
        )
    )

    // 页面状态
    var showProfile by remember { mutableStateOf(false) }
    var showCommunity by remember { mutableStateOf(false) }

    // 滑动启用状态 - 简化管理
    var isSwipeEnabled by remember { mutableStateOf(true) }

    // 权限相关状态
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var showPermissionExplanation by remember { mutableStateOf(false) }

    // 权限检查器
    val permissionChecker = remember { PermissionChecker(context) }

    // 滑动相关状态
    val slideOffset = remember { Animatable(0f) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxSlideOffsetPx = with(LocalDensity.current) { screenWidth.toPx() }
    val coroutineScope = rememberCoroutineScope()

    // 计算滑动进度 (-1f 到 1f)
    val progress by remember {
        derivedStateOf {
            slideOffset.value / maxSlideOffsetPx
        }
    }

    // 是否显示识别结果
    val hasRecognitionResult = remember {
        derivedStateOf {
            uiState.plantWithDetails != null || uiState.result != null || uiState.error != null
        }
    }

    // 判断是否在三个主页面之间（个人主页、主页面、社区页面）
    val isOnMainThreeScreens = remember {
        derivedStateOf {
            // 在个人主页、主页面、社区页面之间可以滑动
            // 只有在显示识别结果或加载中时禁用滑动
            !hasRecognitionResult.value && !uiState.isLoading && isSwipeEnabled
        }
    }

    // 处理返回键
    BackHandler(enabled = showProfile || showCommunity || hasRecognitionResult.value) {
        when {
            showProfile -> {
                coroutineScope.launch {
                    slideOffset.animateTo(0f, animationSpec = tween(300))
                    showProfile = false
                }
            }
            showCommunity -> {
                coroutineScope.launch {
                    slideOffset.animateTo(0f, animationSpec = tween(300))
                    showCommunity = false
                }
            }
            hasRecognitionResult.value -> {
                viewModel.clearRecognitionResult()
            }
        }
    }

    /**
     * 创建相册选择启动器
     */
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.selectImageFromGallery(it)
            viewModel.recognizePlantWithDetails()
        }
    }

    /**
     * 创建拍照启动器
     */
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        bmp?.let {
            viewModel.takePhoto(it)
            viewModel.recognizePlantWithDetails()
        }
    }

    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            showPermissionExplanation = true
        }
        showCameraPermissionDialog = false
    }

    // 使用Scaffold布局作为根容器
    Scaffold(
        containerColor = Color(0xFFE9F0F8)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isOnMainThreeScreens.value) {
                    // 只有在三个主页面之间才允许滑动
                    if (!isOnMainThreeScreens.value) return@pointerInput

                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                val currentOffset = slideOffset.value
                                val threshold = maxSlideOffsetPx * 0.1f // 20% 阈值

                                when {
                                    // 向右滑动显示个人主页
                                    currentOffset > threshold && !showProfile -> {
                                        slideOffset.animateTo(maxSlideOffsetPx, animationSpec = tween(300))
                                        showProfile = true
                                        showCommunity = false
                                    }
                                    // 向左滑动显示社区
                                    currentOffset < -threshold && !showCommunity -> {
                                        slideOffset.animateTo(-maxSlideOffsetPx, animationSpec = tween(300))
                                        showCommunity = true
                                        showProfile = false
                                    }
                                    // 从个人主页滑回主页
                                    currentOffset < (maxSlideOffsetPx - threshold) && showProfile -> {
                                        slideOffset.animateTo(0f, animationSpec = tween(300))
                                        showProfile = false
                                    }
                                    // 从社区滑回主页
                                    currentOffset > (-maxSlideOffsetPx + threshold) && showCommunity -> {
                                        slideOffset.animateTo(0f, animationSpec = tween(300))
                                        showCommunity = false
                                    }
                                    else -> {
                                        // 滑动距离不足，回到原位置
                                        when {
                                            showProfile -> slideOffset.animateTo(maxSlideOffsetPx, animationSpec = tween(300))
                                            showCommunity -> slideOffset.animateTo(-maxSlideOffsetPx, animationSpec = tween(300))
                                            else -> slideOffset.animateTo(0f, animationSpec = tween(300))
                                        }
                                    }
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        val newOffset = when {
                            showProfile -> (slideOffset.value + dragAmount).coerceIn(0f, maxSlideOffsetPx)
                            showCommunity -> (slideOffset.value + dragAmount).coerceIn(-maxSlideOffsetPx, 0f)
                            else -> (slideOffset.value + dragAmount).coerceIn(-maxSlideOffsetPx, maxSlideOffsetPx)
                        }

                        coroutineScope.launch {
                            slideOffset.snapTo(newOffset)
                        }
                    }
                }
        ) {
            // ========== 页面渲染部分 ==========

            // 1. 社区页面容器 - 从右侧滑入
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 修复：社区页面在右侧，向左滑动时进入
                        translationX = maxSlideOffsetPx + slideOffset.value
                    }
            ) {
                // 滑动超过10%才显示社区页面
                if (progress < 0f) {
                    CommunityScreen(
                        onBackClick = {
                            coroutineScope.launch {
                                slideOffset.animateTo(0f, animationSpec = tween(300))
                                showCommunity = false
                            }
                        },
                        onSwipeEnabledChange = { enabled ->
                            isSwipeEnabled = enabled
                        }
                    )
                }
            }

            // 2. 个人主页容器 - 从左侧滑入
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 修复：个人主页在左侧，向右滑动时进入
                        translationX = -maxSlideOffsetPx + slideOffset.value
                    }
            ) {
                // 滑动超过30%才显示个人主页
                if (progress > 0f) {
                    ProfileScreen(
                        onBackClick = {
                            coroutineScope.launch {
                                slideOffset.animateTo(0f, animationSpec = tween(300))
                                showProfile = false
                            }
                        },
                        onSwipeEnabledChange = { enabled ->
                            isSwipeEnabled = enabled
                        }
                    )
                }
            }

            // 3. 主页面容器 - 跟随滑动移动
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = slideOffset.value
                    }
            ) {
                // 主页面内容 - 植物识别功能
                MainContent(
                    uiState = uiState,
                    favoriteViewModel = favoriteViewModel,
                    pickImageLauncher = pickImageLauncher,
                    onTakePictureClick = {
                        if (permissionChecker.hasCameraPermission()) {
                            takePictureLauncher.launch(null)
                        } else {
                            showCameraPermissionDialog = true
                        }
                    },
                    hasRecognitionResult = hasRecognitionResult.value,
                    paddingValues = paddingValues,
                    onUserIconClick = {
                        coroutineScope.launch {
                            slideOffset.animateTo(maxSlideOffsetPx, animationSpec = tween(300))
                            showProfile = true
                            showCommunity = false
                        }
                    },
                    onCommunityIconClick = {
                        coroutineScope.launch {
                            slideOffset.animateTo(-maxSlideOffsetPx, animationSpec = tween(300))
                            showCommunity = true
                            showProfile = false
                        }
                    }
                )
            }
        }
    }

    // 权限请求对话框
    if (showCameraPermissionDialog) {
        PermissionDialog(
            permissionType = "相机",
            onAllowClick = {
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            },
            onDenyClick = {
                showCameraPermissionDialog = false
            },
            onDismissRequest = {
                showCameraPermissionDialog = false
            }
        )
    }

    // 权限解释对话框
    if (showPermissionExplanation) {
        PermissionExplanationDialog(
            permissionType = "相机",
            onGoToSettings = {
                permissionChecker.openAppSettings()
                showPermissionExplanation = false
            },
            onCancel = {
                showPermissionExplanation = false
            },
            onDismissRequest = {
                showPermissionExplanation = false
            }
        )
    }
}

// MainContent 函数保持不变...

@Composable
private fun MainContent(
    uiState: PlantViewModel.PlantRecognitionState,
    favoriteViewModel: FavoriteViewModel,
    pickImageLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Uri?>,
    onTakePictureClick: () -> Unit,
    hasRecognitionResult: Boolean,
    paddingValues: PaddingValues,
    onUserIconClick: () -> Unit,
    onCommunityIconClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE9F0F8))
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // 顶部区域 - 只在初始状态显示
            if (uiState.selectedImage == null && !uiState.isLoading && !hasRecognitionResult) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    // 用户图标
                    IconButton(
                        onClick = onUserIconClick,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFFE9F0F8).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.home_user),
                            contentDescription = "个人主页",
                            tint = Color(0xFF364858),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // PlantKnow 文字
                    Text(
                        text = "PlantKnow",
                        color = Color(0xFF364858),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onCommunityIconClick()
                            }
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    )
                }
            }

            // 图片预览区域
            if (uiState.selectedImage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(top = 30.dp , bottom = 20.dp)
                ) {
                    when (val image = uiState.selectedImage) {
                        is Bitmap -> {
                            Image(
                                bitmap = image.asImageBitmap(),
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        is Uri -> {
                            Image(
                                painter = rememberAsyncImagePainter(image),
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = when {
                            uiState.isLoading -> uiState.recognitionStep
                            uiState.error != null -> "识别失败"
                            uiState.plantWithDetails != null -> "识别完成"
                            else -> "已选择图片"
                        },
                        color = Color(0xFF666666),
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 中部：主要文本展示区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                ) {
                    when {
                        uiState.selectedImage == null -> WelcomeContent()
                        uiState.isLoading -> LoadingContent(uiState.recognitionStep)
                        uiState.plantWithDetails != null -> PlantDetailsWithStickyHeader(
                            plantWithDetails = uiState.plantWithDetails!!,
                            favoriteViewModel = favoriteViewModel,
                            selectedImage = uiState.selectedImage
                        )
                        uiState.result != null -> PlantBasicInfoWithStickyHeader(plant = uiState.result!!)
                        uiState.error != null -> ErrorCard(message = uiState.error!!)
                    }
                }

                // 毛玻璃效果层
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFE9F0F8).copy(alpha = 0.8f),
                                    Color(0xFFE9F0F8).copy(alpha = 0.9f)
                                )
                            )
                        )
                )
            }

            // 底部：操作按钮区域
            if (uiState.selectedImage == null || hasRecognitionResult) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { pickImageLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.home_photo),
                            contentDescription = "相册",
                            tint = Color(0xFF364858)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = onTakePictureClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.home_camera),
                            contentDescription = "拍照",
                            tint = Color(0xFF364858)
                        )
                    }
                }
            }
        }
    }
}