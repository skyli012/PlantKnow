package com.hailong.plantknow.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hailong.plantknow.R
import com.hailong.plantknow.database.FavoritePlantDatabase
import com.hailong.plantknow.model.PlantWithDetails
import com.hailong.plantknow.repository.FavoriteRepository
import com.hailong.plantknow.ui.component.ErrorCard
import com.hailong.plantknow.ui.component.LoadingContent
import com.hailong.plantknow.ui.component.PermissionDialog
import com.hailong.plantknow.ui.component.PermissionExplanationDialog
import com.hailong.plantknow.ui.component.WelcomeContent
import com.hailong.plantknow.ui.discover.PlantPost
import com.hailong.plantknow.ui.screen.ProfileScreen
import com.hailong.plantknow.utils.AppScreen
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
    val bgColor1 = Color.White

    // 设置状态栏颜色，并在组件销毁时清理
    DisposableEffect(Unit) {
        systemUiController.setStatusBarColor(
            color = bgColor1,
            darkIcons = true
        )
        // 设置底部导航栏
        systemUiController.setNavigationBarColor(
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

    // 页面导航状态
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }
    var showProfile by remember { mutableStateOf(false) }

    // 植物详情数据
    var plantDetailData by remember {
        mutableStateOf<Pair<PlantWithDetails, Any?>?>(null)
    }

    // 选择的图片（用于加载页面显示）
    var selectedImageForLoading by remember {
        mutableStateOf<Any?>(null)
    }

    // 滑动启用状态 - 简化管理
    var isSwipeEnabled by remember { mutableStateOf(true) }

    // 权限相关状态
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var showPermissionExplanation by remember { mutableStateOf(false) }

    // 权限检查器
    val permissionChecker = remember { PermissionChecker(context) }

    // 滑动相关状态 - 现在只处理向右滑动（显示个人主页）
    val slideOffset = remember { Animatable(0f) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxSlideOffsetPx = with(LocalDensity.current) { screenWidth.toPx() }
    val coroutineScope = rememberCoroutineScope()

    // 计算滑动进度 (0f 到 1f，因为现在只向右滑动)
    val progress by remember {
        derivedStateOf {
            slideOffset.value / maxSlideOffsetPx
        }
    }

    // 添加植物详情相关状态
    var selectedPlantPost by remember { mutableStateOf<PlantPost?>(null) }
    var showPlantDetail by remember { mutableStateOf(false) }

    // 判断是否允许滑动 - 只在主页和个人主页之间滑动
    val isSwipeAllowed = remember {
        derivedStateOf {
            // 只有在主页面、未加载中且滑动启用时，才允许向右滑动
            currentScreen == AppScreen.MAIN && !uiState.isLoading && isSwipeEnabled && slideOffset.value >= 0f
        }
    }

    // 处理返回键
    BackHandler(enabled = currentScreen != AppScreen.MAIN || showProfile || showPlantDetail) {
        when (currentScreen) {
            AppScreen.LOADING -> {
                // 不允许在加载时返回
            }
            AppScreen.PLANT_DETAIL -> {
                currentScreen = AppScreen.MAIN
                viewModel.clearRecognitionResult()
                plantDetailData = null
            }
            else -> {
                if (showProfile) {
                    coroutineScope.launch {
                        slideOffset.animateTo(0f, animationSpec = tween(300))
                        showProfile = false
                    }
                } else if (showPlantDetail) {
                    showPlantDetail = false
                    selectedPlantPost = null
                }
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
            selectedImageForLoading = it
            currentScreen = AppScreen.LOADING
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
            selectedImageForLoading = bmp
            currentScreen = AppScreen.LOADING
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

    // 监听识别状态变化
    LaunchedEffect(uiState.isLoading, uiState.plantWithDetails, uiState.error) {
        when {
            // 正在加载中，保持在加载页面
            uiState.isLoading -> {
                if (currentScreen != AppScreen.LOADING && uiState.selectedImage != null) {
                    currentScreen = AppScreen.LOADING
                }
            }

            // 识别完成，跳转到详情页面
            uiState.plantWithDetails != null && currentScreen == AppScreen.LOADING -> {
                plantDetailData = Pair(uiState.plantWithDetails!!, uiState.selectedImage)
                currentScreen = AppScreen.PLANT_DETAIL
                selectedImageForLoading = null
            }

            // 识别错误，返回主页面显示错误
            uiState.error != null && currentScreen == AppScreen.LOADING -> {
                currentScreen = AppScreen.MAIN
                selectedImageForLoading = null
            }
        }
    }

    // 根据当前屏幕渲染不同内容
    when (currentScreen) {
        AppScreen.MAIN -> {
            Scaffold(
                containerColor = Color.White
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .pointerInput(isSwipeAllowed.value) {
                            // 只有在允许滑动时才启用滑动检测
                            if (!isSwipeAllowed.value) return@pointerInput

                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    coroutineScope.launch {
                                        val currentOffset = slideOffset.value
                                        val threshold = maxSlideOffsetPx * 0.15f // 15% 阈值

                                        when {
                                            // 向右滑动显示个人主页（只允许向右滑动）
                                            currentOffset > threshold && !showProfile -> {
                                                slideOffset.animateTo(maxSlideOffsetPx, animationSpec = tween(300))
                                                showProfile = true
                                            }
                                            // 从个人主页滑回主页
                                            currentOffset < (maxSlideOffsetPx - threshold) && showProfile -> {
                                                slideOffset.animateTo(0f, animationSpec = tween(300))
                                                showProfile = false
                                            }
                                            else -> {
                                                // 滑动距离不足，回到原位置
                                                if (showProfile) {
                                                    slideOffset.animateTo(maxSlideOffsetPx, animationSpec = tween(300))
                                                } else {
                                                    slideOffset.animateTo(0f, animationSpec = tween(300))
                                                }
                                            }
                                        }
                                    }
                                }
                            ) { change, dragAmount ->
                                // 只允许向右滑动（dragAmount > 0）
                                val newOffset = when {
                                    showProfile -> (slideOffset.value + dragAmount).coerceIn(0f, maxSlideOffsetPx)
                                    dragAmount > 0 -> (slideOffset.value + dragAmount).coerceIn(0f, maxSlideOffsetPx)
                                    else -> slideOffset.value // 不允许向左滑动
                                }

                                coroutineScope.launch {
                                    slideOffset.snapTo(newOffset)
                                }
                            }
                        }
                ) {
                    // ========== 页面渲染部分 ==========

                    // 1. 个人主页容器 - 从左侧滑入
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                // 个人主页在左侧，向右滑动时进入
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

                    // 2. 主页面容器 - 跟随滑动向右移动
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = slideOffset.value.coerceAtLeast(0f) // 只允许向右移动
                            }
                    ) {
                        // 主页面内容 - 只负责图片选择
                        MainContent(
                            uiState = uiState,
                            onPickImage = { pickImageLauncher.launch("image/*") },
                            onTakePictureClick = {
                                if (permissionChecker.hasCameraPermission()) {
                                    takePictureLauncher.launch(null)
                                } else {
                                    showCameraPermissionDialog = true
                                }
                            },
                            onUserIconClick = {
                                coroutineScope.launch {
                                    slideOffset.animateTo(maxSlideOffsetPx, animationSpec = tween(300))
                                    showProfile = true
                                }
                            }
                        )
                    }
                }
            }
        }

        AppScreen.LOADING -> {
            // 独立的加载页面
            LoadingScreen(
                recognitionStep = uiState.recognitionStep,
                selectedImage = selectedImageForLoading,
                error = uiState.error,
                onBackClick = {
                    if (uiState.error != null) {
                        currentScreen = AppScreen.MAIN
                        viewModel.clearRecognitionResult()
                        selectedImageForLoading = null
                    }
                }
            )
        }

        AppScreen.PLANT_DETAIL -> {
            plantDetailData?.let { (plantWithDetails, selectedImage) ->
                PlantDetailScreen(
                    plantWithDetails = plantWithDetails,
                    selectedImage = selectedImage,
                    favoriteViewModel = favoriteViewModel,
                    onBackClick = {
                        currentScreen = AppScreen.MAIN
                        viewModel.clearRecognitionResult()
                        plantDetailData = null
                    },
                    onReturnHomeClick = {
                        // 当用户点击"重新识别"按钮时，返回主页
                        currentScreen = AppScreen.MAIN
                        viewModel.clearRecognitionResult()
                        plantDetailData = null
                        // 你可以在这里添加其他清理或重置逻辑
                    }
                )
            }
        }

        AppScreen.PROFILE -> {
            // 个人主页作为独立页面，通过滑动显示
            currentScreen = AppScreen.MAIN
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

@Composable
private fun MainContent(
    uiState: PlantViewModel.PlantRecognitionState,
    onPickImage: () -> Unit,
    onTakePictureClick: () -> Unit,
    onUserIconClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // 顶部区域
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                IconButton(
                    onClick = onUserIconClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.home_user),
                        contentDescription = "个人主页",
                        tint = Color(0xFF364858),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = {
                        // TODO: 点击事件
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.home_information), // 使用自定义图片
                        contentDescription = "消息提醒",
                        tint = Color.Unspecified, // 保持原图颜色
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 中部：欢迎内容 - 只显示欢迎内容或错误
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
                        uiState.error != null -> {
                            // 错误提示
                            ErrorCard(message = uiState.error!!)
                        }
                        else -> WelcomeContent()
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
                                    Color.White.copy(alpha = 0.8f),
                                    Color.White.copy(alpha = 0.9f)
                                )
                            )
                        )
                )
            }

            // 底部：操作按钮区域 - 始终显示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPickImage,
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

/**
 * 独立的加载页面
 */
@Composable
fun LoadingScreen(
    recognitionStep: String,
    selectedImage: Any?,
    error: String?,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 返回按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (error != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_community), // 需要返回图标
                            contentDescription = "返回",
                            tint = Color(0xFF364858)
                        )
                    }
                    Text(
                        text = "返回",
                        color = Color(0xFF364858),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // 加载内容
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LoadingContent(
                    recognitionStep = recognitionStep,
                    isFullScreen = true,
                    selectedImage = selectedImage
                )
            }
        }
    }
}