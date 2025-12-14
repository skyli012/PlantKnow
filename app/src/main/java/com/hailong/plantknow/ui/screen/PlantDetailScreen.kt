package com.hailong.plantknow.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.hailong.plantknow.R
import com.hailong.plantknow.model.PlantWithDetails
import com.hailong.plantknow.model.confidencePercent
import com.hailong.plantknow.model.description
import com.hailong.plantknow.ui.component.FavoriteButton
import com.hailong.plantknow.utils.MatchLevel
import com.hailong.plantknow.utils.confidenceToMatchLevel
import com.hailong.plantknow.viewmodel.FavoriteViewModel

@Composable
fun PlantDetailScreen(
    plantWithDetails: PlantWithDetails,
    selectedImage: Any?,
    favoriteViewModel: FavoriteViewModel,
    onBackClick: () -> Unit
) {
    val favoritePlants by favoriteViewModel.favoritePlants.collectAsState(initial = emptyList())
    val isFavorited = favoritePlants.any { it.plantName == plantWithDetails.basicInfo.plantName }

    // ✅ 只在这里算一次
    val matchLevel = confidenceToMatchLevel(
        plantWithDetails.basicInfo.confidencePercent
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // 1️⃣ 顶部图片 Header
        PlantImageHeader(
            image = selectedImage,
            plantName = plantWithDetails.basicInfo.plantName,
            matchLevel = matchLevel,
            onBackClick = onBackClick
        )

        // 2️⃣ 白色内容 Sheet
        PlantDetailSheet(
            plantWithDetails = plantWithDetails,
            matchLevel = matchLevel,
            isFavorited = isFavorited,
            favoriteViewModel = favoriteViewModel,
            selectedImage = selectedImage
        )

        // 3️⃣ 底部固定按钮
        ScanAnotherPlantButton(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun PlantImageHeader(
    image: Any?,
    plantName: String,
    matchLevel: MatchLevel,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {

        when (image) {
            is Bitmap -> Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            is Uri -> Image(
                painter = rememberAsyncImagePainter(image),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 返回按钮
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(0.35f), RoundedCornerShape(50))
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.identify_back),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // 分享按钮
        IconButton(
            onClick = { },
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopEnd)
                .background(Color.Black.copy(0.35f), RoundedCornerShape(50))
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.identify_share),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // 底部渐变 + 文本 - 添加底部边距和左边距
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(start = 16.dp)  // 添加左边距
                .padding(bottom = 40.dp)  // 底部边距，避免被卡片遮挡
                .padding(end = 8.dp)      // 保持右侧内边距
                .padding(top = 8.dp)      // 保持顶部内边距
        ) {
            Column {
                MatchBadge(matchLevel)

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = plantName,
                    color = Color.White,
                    fontSize = 20.sp,  // 字体稍微大一点（从18.sp改为20.sp）
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



@Composable
fun PlantDetailSheet(
    plantWithDetails: PlantWithDetails,
    matchLevel: MatchLevel,
    isFavorited: Boolean,
    favoriteViewModel: FavoriteViewModel,
    selectedImage: Any?
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 220.dp),  // 减小顶部间距，让卡片向上延伸
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 收藏按钮对齐到右侧
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FavoriteButton(
                    isFavorited = isFavorited,
                    onFavoriteClick = {
                        if (isFavorited) {
                            favoriteViewModel.removeFavorite(
                                plantWithDetails.basicInfo.plantName
                            )
                        } else {
                            favoriteViewModel.addFavorite(
                                plantWithDetails,
                                selectedImage
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = plantWithDetails.basicInfo.description,
                fontSize = 14.sp,
                color = Color(0xFF555555),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            PlantTabs()

            Spacer(modifier = Modifier.height(12.dp))

            CareItem("Water", "Requires consistently moist, well-drained soil...")
            CareItem("Sunlight", "Prefers full sun to partial shade...")

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun PlantTabs() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabItem("Care Guide", true)
        TabItem("Botany & Facts", false)
    }
}

@Composable
fun TabItem(text: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color(0xFF2ECC71) else Color.Gray
        )
        if (selected) {
            Spacer(
                modifier = Modifier
                    .height(2.dp)
                    .width(36.dp)
                    .background(Color(0xFF2ECC71))
            )
        }
    }
}
@Composable
fun ScanAnotherPlantButton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Scan Another Plant",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MatchBadge(matchLevel: MatchLevel) {
    Box(
        modifier = Modifier
            .background(
                color = matchLevel.color,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 1.dp)
    ) {
        Text(
            text = matchLevel.label,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun CareItem(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF555555),
                lineHeight = 20.sp
            )
        }
    }
}
