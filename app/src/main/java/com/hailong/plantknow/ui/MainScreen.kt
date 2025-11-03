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
import com.hailong.plantknow.ui.screen.FavoriteListScreen
import com.hailong.plantknow.ui.screen.FavoriteDetailScreen
import com.hailong.plantknow.viewmodel.FavoriteViewModel
import com.hailong.plantknow.viewmodel.FavoriteViewModelFactory
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.collectAsState
import com.hailong.plantknow.ui.component.ErrorCard
import com.hailong.plantknow.ui.component.LoadingContent
import com.hailong.plantknow.ui.component.PlantBasicInfoWithStickyHeader
import com.hailong.plantknow.ui.component.PlantDetailsWithStickyHeader
import com.hailong.plantknow.ui.component.WelcomeContent
import com.hailong.plantknow.utils.ImageSaver
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
    var showFavorites by remember { mutableStateOf(false) }
    var selectedFavorite by remember { mutableStateOf<com.hailong.plantknow.model.FavoritePlant?>(null) }

    // 滑动相关状态
    val slideOffset = remember { Animatable(0f) }
    val maxSlideOffset = 1000f // 最大滑动距离
    val coroutineScope = rememberCoroutineScope()

    // 计算进度 (0f - 1f)
    val progress by remember { derivedStateOf { slideOffset.value / maxSlideOffset } }

    // 是否显示识别结果（用于控制返回键行为）
    val hasRecognitionResult = remember {
        derivedStateOf {
            uiState.plantWithDetails != null || uiState.result != null || uiState.error != null
        }
    }

    // 处理返回键
    BackHandler(enabled = showFavorites || hasRecognitionResult.value) {
        when {
            selectedFavorite != null -> {
                selectedFavorite = null
            }
            showFavorites -> {
                coroutineScope.launch {
                    slideOffset.animateTo(0f, animationSpec = tween(300))
                    showFavorites = false
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

    // 使用Scaffold布局（移除topBar参数）
    Scaffold(
        containerColor = Color(0xFFE9F0F8)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(hasRecognitionResult.value) { // 添加依赖，当识别结果状态变化时重新创建
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                // 只有在没有识别结果时才允许切换到收藏页面
                                if (slideOffset.value > maxSlideOffset * 0.6f && !showFavorites && !hasRecognitionResult.value) {
                                    // 滑动超过60%，切换到收藏页面
                                    slideOffset.animateTo(maxSlideOffset, animationSpec = tween(300))
                                    showFavorites = true
                                } else if (slideOffset.value < maxSlideOffset * 0.4f && showFavorites) {
                                    // 滑动不足40%，回到主页面
                                    slideOffset.animateTo(0f, animationSpec = tween(300))
                                    showFavorites = false
                                } else {
                                    // 保持当前状态
                                    if (showFavorites) {
                                        slideOffset.animateTo(maxSlideOffset, animationSpec = tween(300))
                                    } else {
                                        slideOffset.animateTo(0f, animationSpec = tween(300))
                                    }
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        // 在有识别结果时禁用向右滑动
                        if (hasRecognitionResult.value && dragAmount > 0) {
                            // 有识别结果且向右滑动，不处理
                            return@detectHorizontalDragGestures
                        }

                        // 优化：实时更新showFavorites状态，让主界面能实时显示
                        val newOffset = when {
                            !showFavorites -> (slideOffset.value + dragAmount).coerceIn(0f, maxSlideOffset)
                            else -> (slideOffset.value + dragAmount).coerceIn(0f, maxSlideOffset)
                        }

                        coroutineScope.launch {
                            slideOffset.snapTo(newOffset)

                            // 实时更新页面状态，让界面能及时响应
                            if (newOffset > maxSlideOffset * 0.5f && !showFavorites) {
                                showFavorites = true
                            } else if (newOffset < maxSlideOffset * 0.5f && showFavorites) {
                                showFavorites = false
                            }
                        }
                    }
                }
        ) {
            // 收藏页面 - 从左侧进入
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 优化：收藏页面从左侧滑入，实时跟随手指移动
                        translationX = -maxSlideOffset + slideOffset.value
                        alpha = progress
                    }
            ) {
                // 优化：只要进度大于0就显示收藏页面，确保滑动时能实时看到
                if (progress > 0.01f) {
                    if (selectedFavorite != null) {
                        FavoriteDetailScreen(
                            favoritePlant = selectedFavorite!!,
                            onBackClick = { selectedFavorite = null }
                        )
                    } else {
                        FavoriteListScreen(
                            favoriteViewModel = favoriteViewModel,
                            onBackClick = {
                                coroutineScope.launch {
                                    slideOffset.animateTo(0f, animationSpec = tween(300))
                                    showFavorites = false
                                }
                            },
                            onItemClick = { favorite -> selectedFavorite = favorite }
                        )
                    }
                }
            }

            // 主页面 - 向右移动并逐渐消失
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 优化：主页面实时跟随手指移动
                        translationX = slideOffset.value * 0.3f
                        alpha = 1f - progress
                        scaleX = 1f - progress * 0.1f
                        scaleY = 1f - progress * 0.1f
                    }
            ) {
                // 优化：只要进度小于0.99就显示主页面，确保滑动时能实时看到
                if (progress < 0.99f) {
                    MainContent(
                        uiState = uiState,
                        favoriteViewModel = favoriteViewModel,
                        pickImageLauncher = pickImageLauncher,
                        takePictureLauncher = takePictureLauncher,
                        hasRecognitionResult = hasRecognitionResult.value,
                        paddingValues = paddingValues,
                        onUserIconClick = {
                            coroutineScope.launch {
                                slideOffset.animateTo(maxSlideOffset, animationSpec = tween(300))
                                showFavorites = true
                            }
                        }
                    )
                }
            }

            // 滑动提示 - 只在主页面显示且没有识别结果时显示
            if (progress > 0.1f && progress < 0.9f && !showFavorites && !hasRecognitionResult.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(progress * 0.7f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (progress > 0.5f) "" else "",
                        color = Color(0xFF364858),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .background(
                                color = Color(0xFFE9F0F8).copy(alpha = 0.8f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    )
                }
            }

            // 从收藏页面返回的滑动提示
            if (progress > 0.1f && progress < 0.9f && showFavorites) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha((1f - progress) * 0.7f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (progress < 0.5f) "" else "",
                        color = Color(0xFF364858),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .background(
                                color = Color(0xFFE9F0F8).copy(alpha = 0.8f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    uiState: com.hailong.plantknow.viewmodel.PlantViewModel.PlantRecognitionState,
    favoriteViewModel: FavoriteViewModel,
    pickImageLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Uri?>,
    takePictureLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Void?, Bitmap?>,
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
                            contentDescription = "查看收藏",
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
                        onClick = { takePictureLauncher.launch(null) },
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