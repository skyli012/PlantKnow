package com.hailong.plantknow.ui

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hailong.plantknow.R
import com.hailong.plantknow.model.confidencePercent
import com.hailong.plantknow.model.description
import com.hailong.plantknow.viewModel.PlantViewModel
import com.hailong.plantknow.viewModel.PlantViewModelFactory
import kotlin.contracts.contract

/**
 * 主屏幕Composable函数
 * 负责图片选择、预览和AI植物识别结果显示
 * @param viewModel 植物识别ViewModel，负责状态管理和业务逻辑
 */
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

    // 观察UI状态
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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

    val bgColor = Color(0xFFE9F0F8) // 背景颜色

    // 主布局：使用Box布局来叠加毛玻璃效果
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // 顶部：图片预览区域（仅在选择了图片时显示）
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
                            PlantDetailsWithStickyHeader(plantWithDetails = uiState.plantWithDetails!!)
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

                // 毛玻璃效果层 - 在内容区域底部，位于内容和按钮之间
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
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
//                    .padding(top = 10.dp),
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

@Composable
private fun PlantDetailsWithStickyHeader(plantWithDetails: com.hailong.plantknow.model.PlantWithDetails) {
    // 本地收藏状态 - 使用 remember 管理UI状态
    var isFavorited by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE9F0F8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            // 固定标题部分 - 添加收藏按钮
            Column {
                // 植物名称和收藏按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plantWithDetails.basicInfo.plantName,
                        color = Color(0xFF364858),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .weight(1f)
                    )

                    // 收藏按钮
                    FavoriteButton(
                        isFavorited = isFavorited,
                        onFavoriteClick = {
                            isFavorited = !isFavorited
                            // 这里可以添加点击反馈，比如震动或Toast
                            Log.d("Favorite", "收藏状态: $isFavorited")
                        },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "识别置信度：",
                        color = Color(0xFF666666),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${plantWithDetails.basicInfo.confidencePercent}%",
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE0E0E0)
                )
            }

            // 可滚动内容部分 - 修改为分段显示
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // 使用分段渲染函数
                PlantDescriptionWithTitles(plantWithDetails.detailedDescription)

                Text(
                    text = "—— 信息由通义千问AI提供",
                    color = Color(0xFF999999),
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

/**
 * 收藏按钮组件
 * @param isFavorited 当前收藏状态
 * @param onFavoriteClick 点击回调
 * @param modifier 修饰符
 */
@Composable
private fun FavoriteButton(
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tintColor = if (isFavorited) {
        Color(0xFFFF5252) // 红色 - 已收藏
    } else {
        Color(0xFF9E9E9E) // 灰色 - 未收藏
    }

    val iconRes = if (isFavorited) {
        Icons.Filled.Favorite // 实心爱心
    } else {
        Icons.Outlined.FavoriteBorder // 空心爱心
    }

    Box(
        modifier = modifier
            .size(48.dp) // 保持合适的点击区域
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // 关键：移除涟漪效果
            ) {
                onFavoriteClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconRes,
            contentDescription = if (isFavorited) "取消收藏" else "收藏",
            tint = tintColor,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * 增强版收藏按钮 - 带动画效果
 */
@Composable
private fun AnimatedFavoriteButton(
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(targetState = isFavorited, label = "favoriteTransition")

    val tint by transition.animateColor(label = "tint") { favoriteState ->
        if (favoriteState) Color(0xFFFF5252) else Color(0xFF9E9E9E)
    }

    val scale by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 200, easing = FastOutSlowInEasing)
        },
        label = "scale"
    ) { favoriteState ->
        if (favoriteState) 1.2f else 1f
    }

    IconButton(
        onClick = onFavoriteClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorited) "取消收藏" else "收藏",
            tint = tint,
            modifier = Modifier
                .size(28.dp)
                .scale(scale)
        )
    }
}

/**
 * 分段渲染植物描述，标题使用大字体
 */
@Composable
private fun PlantDescriptionWithTitles(description: String) {
    // 按数字标题分割内容
    val sections = parseDescriptionIntoSections(description)

    Column {
        sections.forEach { section ->
            when (section.type) {
                SectionType.TITLE -> {
                    // 标题样式 - 大字体
                    Text(
                        text = section.content,
                        color = Color(0xFF364858),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                    )
                }
                SectionType.CONTENT -> {
                    // 内容样式 - 正常字体
                    Text(
                        text = section.content,
                        color = Color(0xFF666666),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }
    }
}

/**
 * 解析描述文本，分割为标题和内容
 */
private fun parseDescriptionIntoSections(description: String): List<DescriptionSection> {
    val sections = mutableListOf<DescriptionSection>()
    val lines = description.split("\n")

    var currentContent = StringBuilder()

    lines.forEach { line ->
        // 判断是否为标题行（以数字序号开头）
        if (line.matches(Regex("^\\d+\\..*"))) {
            // 如果之前有内容，先保存
            if (currentContent.isNotEmpty()) {
                sections.add(DescriptionSection(SectionType.CONTENT, currentContent.toString().trim()))
                currentContent = StringBuilder()
            }
            // 添加标题
            sections.add(DescriptionSection(SectionType.TITLE, line.trim()))
        } else {
            // 内容行
            currentContent.append(line).append("\n")
        }
    }

    // 添加最后的内容
    if (currentContent.isNotEmpty()) {
        sections.add(DescriptionSection(SectionType.CONTENT, currentContent.toString().trim()))
    }

    return sections
}

/**
 * 描述分段数据类
 */
private data class DescriptionSection(
    val type: SectionType,
    val content: String
)

/**
 * 分段类型枚举
 */
private enum class SectionType {
    TITLE, CONTENT
}

/**
 * 基础植物信息带固定标题
 */
@Composable
private fun PlantBasicInfoWithStickyHeader(plant: com.hailong.plantknow.model.PlantResult) {
    // 在函数开始处定义 descriptionText
    val descriptionText = runCatching { plant.description }.getOrElse { "" }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE9F0F8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            // 固定标题部分
            Column {
                Text(
                    text = plant.plantName,
                    color = Color(0xFF364858),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "识别置信度：",
                        color = Color(0xFF666666),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${plant.confidencePercent}%",
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // 只在有描述时显示分割线和标题
                if (descriptionText.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFE0E0E0)
                    )
                    Text(
                        text = "详细描述",
                        color = Color(0xFF364858),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // 可滚动内容部分 - 只在有描述时显示
            if (descriptionText.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = descriptionText,
                        color = Color(0xFF666666),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

/**
 * 欢迎内容
 */
@Composable
private fun WelcomeContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 上半部分内容
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "你好！",
                color = Color(0xFF364858),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 60.dp, bottom = 24.dp, start = 10.dp)
            )

            Text(
                text = "欢迎使用PlantKnow",
                color = Color(0xFF364858),
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp, start = 10.dp)
            )

            Text(
                text = "拍摄或选择一张植物照片，AI将为您识别解答。",
                color = Color(0xFF666666),
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 20.dp, start = 10.dp)
            )

            Text(
                text = "学习与探索。",
                color = Color(0xFF666666),
                fontSize = 12.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(bottom = 40.dp, start = 10.dp)
            )
        }

        // 使用weight让图片在剩余空间中居中
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.home_flower), // 替换为您的花朵图片资源ID
                contentDescription = "装饰花朵",
                modifier = Modifier
                    .size(180.dp), // 调整图片大小
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * 加载内容
 */
@Composable
private fun LoadingContent(recognitionStep: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF364858),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = recognitionStep,
                color = Color(0xFF666666),
                fontSize = 16.sp
            )
        }
    }
}

/**
 * 错误信息卡片组件
 */
@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "识别失败",
                color = Color(0xFFD32F2F),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = message,
                color = Color(0xFF666666),
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}