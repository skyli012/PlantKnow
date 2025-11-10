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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import com.hailong.plantknow.model.FavoritePlant
import com.hailong.plantknow.ui.component.ErrorCard
import com.hailong.plantknow.ui.component.LoadingContent
import com.hailong.plantknow.ui.component.PermissionDialog
import com.hailong.plantknow.ui.component.PermissionExplanationDialog
import com.hailong.plantknow.ui.component.PlantBasicInfoWithStickyHeader
import com.hailong.plantknow.ui.component.PlantDetailsWithStickyHeader
import com.hailong.plantknow.ui.component.WelcomeContent
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

    // 页面状态 - 只保留个人主页显示状态
    var showProfile by remember { mutableStateOf(false) }
    // 添加滑动启用状态
    var swipeEnabled by remember { mutableStateOf(true) }

    // 权限相关状态
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var showPermissionExplanation by remember { mutableStateOf(false) }

    // 权限检查器
    val permissionChecker = remember { PermissionChecker(context) }

    // 滑动相关状态
    val slideOffset = remember { Animatable(0f) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxSlideOffsetPx = with(LocalDensity.current) { screenWidth.toPx() }
    val maxSlideOffset = maxSlideOffsetPx
    val coroutineScope = rememberCoroutineScope()

    // 计算进度 (0f - 1f)
    val progress by remember { derivedStateOf { slideOffset.value / maxSlideOffset } }

    // 是否显示识别结果
    val hasRecognitionResult = remember {
        derivedStateOf {
            uiState.plantWithDetails != null || uiState.result != null || uiState.error != null
        }
    }

    // 处理返回键 - 只处理个人主页和识别结果
    BackHandler(enabled = showProfile || hasRecognitionResult.value) {
        when {
            showProfile -> {
                // 如果正在显示个人主页，先滑动回主页再关闭
                coroutineScope.launch {
                    slideOffset.animateTo(0f, animationSpec = tween(300))
                    showProfile = false
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
                .pointerInput(uiState.isLoading, hasRecognitionResult.value, showProfile, swipeEnabled) {
                    // 根据 swipeEnabled 决定是否启用滑动
                    if (!swipeEnabled) return@pointerInput

                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                val currentOffset = slideOffset.value
                                val threshold = maxSlideOffset * 0.1f

                                if (currentOffset > threshold &&
                                    !showProfile &&
                                    !hasRecognitionResult.value &&
                                    !uiState.isLoading) {
                                    // 向右滑动超过阈值，切换到个人主页
                                    slideOffset.animateTo(maxSlideOffset, animationSpec = tween(145))
                                    showProfile = true
                                }
                                else if (currentOffset < (maxSlideOffset - threshold) && showProfile) {
                                    // 向左滑动超过阈值，切换回主页面
                                    slideOffset.animateTo(0f, animationSpec = tween(145))
                                    showProfile = false
                                }
                                else {
                                    // 滑动距离不足，回到原位置
                                    if (showProfile) {
                                        slideOffset.animateTo(maxSlideOffset, animationSpec = tween(300))
                                    } else {
                                        slideOffset.animateTo(0f, animationSpec = tween(300))
                                    }
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        if (hasRecognitionResult.value || uiState.isLoading) {
                            return@detectHorizontalDragGestures
                        }

                        val newOffset = when {
                            showProfile -> {
                                (slideOffset.value + dragAmount).coerceIn(0f, maxSlideOffset)
                            }
                            else -> {
                                (slideOffset.value + dragAmount).coerceIn(0f, maxSlideOffset)
                            }
                        }

                        // 使用 snapTo 而不是直接赋值
                        coroutineScope.launch {
                            slideOffset.snapTo(newOffset)
                        }
                    }
                }
        ) {
            // ========== 页面渲染部分 ==========

            // 个人主页容器 - 从左侧滑入
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = -maxSlideOffset + slideOffset.value
                    }
            ) {
                // 只要滑动进度大于1%就显示个人主页
                if (progress > 0.01f) {
                    // 个人主页 - 收藏功能完全由 ProfileScreen 内部管理
                    ProfileScreen(
                        onBackClick = {
                            // 返回按钮点击：动画滑动回主页面
                            coroutineScope.launch {
                                slideOffset.animateTo(0f, animationSpec = tween(300))
                                showProfile = false
                            }
                        },
                        onSwipeEnabledChange = { enabled ->
                            swipeEnabled = enabled
                        }
                    )
                }
            }

            // 主页面容器 - 向右移动并逐渐消失
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
                        // 用户图标点击：显示个人主页
                        coroutineScope.launch {
                            slideOffset.animateTo(maxSlideOffset, animationSpec = tween(300))
                            showProfile = true
                        }
                    }
                )
            }

            // 边缘检测区域
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(20.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            // 只在滑动启用时检测边缘手势
                            if (swipeEnabled && dragAmount > 0 && !showProfile && !hasRecognitionResult.value && !uiState.isLoading) {
                                coroutineScope.launch {
                                    val newOffset = (slideOffset.value + dragAmount).coerceIn(0f, maxSlideOffset)
                                    slideOffset.snapTo(newOffset)

                                    if (newOffset > maxSlideOffset * 0.3f) {
                                        slideOffset.animateTo(maxSlideOffset, animationSpec = tween(300))
                                        showProfile = true
                                    }
                                }
                            }
                        }
                    }
            )
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
    favoriteViewModel: FavoriteViewModel,
    pickImageLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Uri?>,
    onTakePictureClick: () -> Unit,
    hasRecognitionResult: Boolean,
    paddingValues: PaddingValues,
    onUserIconClick: () -> Unit
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 顶部区域 - 只显示用户图标在左侧
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // 用户图标 - 只在初始状态显示（没有选择图片、没有加载、没有识别结果）
                if (uiState.selectedImage == null && !uiState.isLoading && !hasRecognitionResult) {
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
                            imageVector = Icons.Default.Person,
                            contentDescription = "个人主页",
                            tint = Color(0xFF364858),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 占位空间，保持布局平衡
                Spacer(modifier = Modifier.weight(1f))
            }

            // 图片预览区域（仅在选择了图片时显示）
            if (uiState.selectedImage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(bottom = 20.dp)
                ) {
                    // 显示选中的图片
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

            // 中部：主要文本展示区域 - 使用Box包装以便添加毛玻璃效果
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
                        // 未选择图片时显示欢迎文本
                        uiState.selectedImage == null -> {
                            WelcomeContent()
                        }

                        // 加载中显示加载指示器
                        uiState.isLoading -> {
                            LoadingContent(uiState.recognitionStep)
                        }

                        // 优先显示完整识别结果（百度+阿里云）
                        uiState.plantWithDetails != null -> {
                            PlantDetailsWithStickyHeader(
                                plantWithDetails = uiState.plantWithDetails!!,
                                favoriteViewModel = favoriteViewModel,
                                selectedImage = uiState.selectedImage
                            )
                        }

                        // 显示仅百度识别结果（兼容旧版本）
                        uiState.result != null -> {
                            PlantBasicInfoWithStickyHeader(plant = uiState.result!!)
                        }

                        // 显示错误信息
                        uiState.error != null -> {
                            ErrorCard(message = uiState.error!!)
                        }
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
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { pickImageLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_gallery),
                            contentDescription = "相册",
                            tint = Color(0xFF364858)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = onTakePictureClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription = "拍照",
                            tint = Color(0xFF364858)
                        )
                    }
                }
            }
        }
    }
}