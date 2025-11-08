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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.hailong.plantknow.ui.component.ErrorCard
import com.hailong.plantknow.ui.component.LoadingContent
import com.hailong.plantknow.ui.component.PermissionDialog
import com.hailong.plantknow.ui.component.PermissionExplanationDialog
import com.hailong.plantknow.ui.component.PlantBasicInfoWithStickyHeader
import com.hailong.plantknow.ui.component.PlantDetailsWithStickyHeader
import com.hailong.plantknow.ui.component.WelcomeContent
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
            darkIcons = true  // 因为背景色较浅，所以使用深色图标
        )
        onDispose {}
    }

    // 观察UI状态 - 使用正确的类型
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
    var showProfile by remember { mutableStateOf(false) } // 个人主页显示状态

    // 权限相关状态
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var showPermissionExplanation by remember { mutableStateOf(false) }

    // 权限检查器
    val permissionChecker = remember { PermissionChecker(context) }

    // 滑动相关状态
    val slideOffset = remember { Animatable(0f) }
    // 获取屏幕宽度
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxSlideOffsetPx = with(LocalDensity.current) { screenWidth.toPx() }
    val maxSlideOffset = maxSlideOffsetPx // 最大滑动距离
    val coroutineScope = rememberCoroutineScope()

    // 计算进度 (0f - 1f)
    val progress by remember { derivedStateOf { slideOffset.value / maxSlideOffset } }

    // 是否显示识别结果（用于控制返回键行为）
    val hasRecognitionResult = remember {
        derivedStateOf {
            uiState.plantWithDetails != null || uiState.result != null || uiState.error != null
        }
    }

    // 处理返回键 - 简化处理逻辑
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
     * 使用ActivityResultContractes.GetContent()契约来获取图片内容
     */
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 通知ViewModel选择图片并自动开始完整识别流程
            viewModel.selectImageFromGallery(it)
            viewModel.recognizePlantWithDetails() // 改为使用完整识别流程
        }
    }

    /**
     * 创建拍照启动器
     * 使用 ActivityResultContracts.TakePicturePreview() 契约来获取拍照预览
     */
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        bmp?.let {
            // 通知ViewModel拍照并自动开始完整识别流程
            viewModel.takePhoto(it)
            viewModel.recognizePlantWithDetails() // 改为使用完整识别流程
        }
    }

    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限已授予，执行拍照
            takePictureLauncher.launch(null)
        } else {
            // 权限被拒绝，显示解释对话框
            showPermissionExplanation = true
        }
        // 关闭权限请求对话框
        showCameraPermissionDialog = false
    }

    // 使用Scaffold布局作为根容器
    Scaffold(
        containerColor = Color(0xFFE9F0F8) // 设置背景色为浅蓝色
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.isLoading, hasRecognitionResult.value, showProfile) {
                    // 添加依赖：当加载状态、识别结果或个人主页状态变化时重新创建手势检测
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                // ========== 滑动结束处理逻辑 ==========

                                // 条件1：从主页面切换到个人主页（向右滑动）
                                // 要求：滑动超过60% + 当前不在个人主页 + 没有识别结果 + 不在加载状态
                                if (slideOffset.value > maxSlideOffset * 0.6f &&
                                    !showProfile &&
                                    !hasRecognitionResult.value &&
                                    !uiState.isLoading) {
                                    // 执行切换到个人主页的动画
                                    slideOffset.animateTo(maxSlideOffset, animationSpec = tween(300))
                                    showProfile = true
                                }
                                // 条件2：从个人主页切换回主页面（向左滑动）
                                // 要求：滑动不足40% + 当前在个人主页
                                else if (slideOffset.value < maxSlideOffset * 0.4f && showProfile) {
                                    // 执行切换回主页面的动画
                                    slideOffset.animateTo(0f, animationSpec = tween(300))
                                    showProfile = false
                                }
                                // 条件3：保持当前状态（滑动距离在40%-60%之间）
                                else {
                                    // 根据当前页面状态弹回对应的位置
                                    if (showProfile) {
                                        slideOffset.animateTo(maxSlideOffset, animationSpec = tween(300))
                                    } else {
                                        slideOffset.animateTo(0f, animationSpec = tween(300))
                                    }
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        // ========== 滑动过程实时处理逻辑 ==========

                        // 保护条件：在有识别结果或加载中时禁用所有滑动
                        if (hasRecognitionResult.value || uiState.isLoading) {
                            return@detectHorizontalDragGestures
                        }

                        // 计算新的滑动偏移量，限制在有效范围内 [0f, maxSlideOffset]
                        val newOffset = (slideOffset.value + dragAmount).coerceIn(0f, maxSlideOffset)

                        coroutineScope.launch {
                            // 立即更新滑动位置（无动画），实现手指实时跟随效果
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
                        // 个人主页动画效果：
                        // 初始位置：-maxSlideOffset（完全在左侧屏幕外）
                        // 结束位置：0（完全显示在屏幕上）
                        translationX = -maxSlideOffset + slideOffset.value
                    }
            ) {
                // 优化：只要滑动进度大于1%就显示个人主页，确保滑动时能实时看到
                // 避免页面闪烁，提供流畅的视觉反馈
                if (progress > 0.01f) {
                    // 个人主页 - 显示用户信息和功能入口
                    ProfileScreen(
                        onBackClick = {
                            // 返回按钮点击：动画滑动回主页面
                            coroutineScope.launch {
                                slideOffset.animateTo(0f, animationSpec = tween(300))
                                showProfile = false
                            }
                        },
                        onFavoritesClick = {
                            // 收藏按钮点击：这里可以添加收藏功能入口
                            // 暂时先关闭个人主页
                            coroutineScope.launch {
                                slideOffset.animateTo(0f, animationSpec = tween(300))
                                showProfile = false
                            }
                        }
                    )
                }
            }

            // 主页面容器 - 向右移动并逐渐消失
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 主页面动画效果：
                        // 初始位置：0（完全显示在屏幕上）
                        // 结束位置：maxSlideOffset（完全在右侧屏幕外）
                        translationX = slideOffset.value
                    }
            ) {
                // 主页面内容 - 植物识别功能
                MainContent(
                    uiState = uiState,
                    favoriteViewModel = favoriteViewModel,
                    pickImageLauncher = pickImageLauncher,
                    onTakePictureClick = {
                        // 处理拍照按钮点击
                        if (permissionChecker.hasCameraPermission()) {
                            // 已有权限，直接拍照
                            takePictureLauncher.launch(null)
                        } else {
                            // 没有权限，显示权限请求对话框
                            showCameraPermissionDialog = true
                        }
                    },
                    hasRecognitionResult = hasRecognitionResult.value,
                    paddingValues = paddingValues,
                    onUserIconClick = {
                        // 用户图标点击：显示个人主页并执行滑动动画
                        coroutineScope.launch {
                            slideOffset.animateTo(maxSlideOffset, animationSpec = tween(300))
                            showProfile = true
                        }
                    }
                )
            }

            // 修复：添加边缘检测区域，让左侧边缘滑动更容易触发
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(20.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            // 只在左侧边缘且向右滑动时触发
                            if (dragAmount > 0 && !showProfile && !hasRecognitionResult.value && !uiState.isLoading) {
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
                // 请求相机权限
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            },
            onDenyClick = {
                showCameraPermissionDialog = false
                // 可以在这里记录用户拒绝权限
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
                // 跳转到应用设置页面
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