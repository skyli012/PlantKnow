package com.hailong.plantknow.ui.screen

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.hailong.plantknow.R
import com.hailong.plantknow.model.FavoritePlant
import com.hailong.plantknow.utils.MatchLevel
import com.hailong.plantknow.utils.confidenceToMatchLevel

// 新增：结构化解析数据类（与识别界面保持一致）
data class PlantFullData(
    val basicInfo: Map<String, String>, // 植物简介/科属分类等基础信息
    val careData: List<CareItemData>    // 水/阳光等养护数据
)

// 养护项数据类
data class CareItemData(
    val title: String,
    val icon: Int,
    val description: String
)

@Composable
fun FavoriteDetailScreen(
    favoritePlant: FavoritePlant,
    onBackClick: () -> Unit
) {
    // 调试日志：检查数据结构
    Log.d("FavoriteDetailScreen", "📊 接收到的收藏数据:")
    Log.d("FavoriteDetailScreen", "植物名称: ${favoritePlant.plantName}")
    Log.d("FavoriteDetailScreen", "置信度: ${favoritePlant.confidence}%")
    Log.d("FavoriteDetailScreen", "描述长度: ${favoritePlant.description.length}")
    Log.d("FavoriteDetailScreen", "描述前200字符: '${favoritePlant.description.take(200)}...'")

    // 解析收藏的描述文本为结构化数据（使用增强版的解析函数）
    val plantFullData = remember(favoritePlant.description) {
        parseFavoritePlantData(favoritePlant.description)
    }

    // 获取植物简介
    val plantIntroduction = remember(plantFullData.basicInfo) {
        plantFullData.basicInfo["植物简介"] ?: "暂无植物简介"
    }

    // 计算匹配等级 - 直接从收藏的置信度字段获取
    val matchLevel = confidenceToMatchLevel(favoritePlant.confidence)

    Box(modifier = Modifier.fillMaxSize()) {
        // 图片头部区域（与识别界面完全一致）
        PlantImageHeader(
            imageUri = favoritePlant.imageUri,
            plantName = favoritePlant.plantName,
            matchLevel = matchLevel,
            onBackClick = onBackClick
        )

        // 详情内容区域（与识别界面完全一致）
        PlantDetailSheet(
            plantFullData = plantFullData,
            plantIntroduction = plantIntroduction
        )
    }
}

// 🌿 1. 图片头部组件（与识别界面完全一致）
@Composable
fun PlantImageHeader(
    imageUri: String?,
    plantName: String,
    matchLevel: MatchLevel,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        // 显示植物图片
        if (!imageUri.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(Uri.parse(imageUri)),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // 默认背景
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.identify_water),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        // 返回按钮（与识别界面相同样式）
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

        // 分享按钮（与识别界面相同样式）
        IconButton(
            onClick = { /* 分享功能，后续可添加 */ },
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

        // 底部渐变和植物名称（与识别界面相同样式）
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
                .padding(start = 16.dp, bottom = 40.dp, end = 8.dp, top = 8.dp)
        ) {
            Column {
                MatchBadge(matchLevel)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = plantName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun PlantDetailSheet(
    plantFullData: PlantFullData,
    plantIntroduction: String
) {
    // 记录当前选中的Tab
    var selectedTab by remember { mutableStateOf("植物护理") }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 220.dp),
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
            // 植物简介部分（不显示标题，直接显示内容）
            Spacer(modifier = Modifier.height(12.dp))

            if (plantIntroduction.isNotBlank()) {
                Text(
                    text = plantIntroduction,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tab切换区域
            Column {
                PlantTabs(
                    selectedTab = selectedTab,
                    onTabSelect = { selectedTab = it }
                )
                // Tab底部全局下划线
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp)
                        .height(1.dp)
                        .background(Color(0xFFEEEEEE))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 根据选中的Tab展示对应的内容
            when (selectedTab) {
                "植物护理" -> CareGuideContent(careData = plantFullData.careData)
                "植物学百科" -> BotanyFactsContent(basicInfo = plantFullData.basicInfo)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// 🏷️ 3. Tab切换组件（与识别界面完全一致）
@Composable
fun PlantTabs(
    selectedTab: String,
    onTabSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabItem(
            text = "植物护理",
            selected = selectedTab == "植物护理",
            onSelect = { onTabSelect("植物护理") }
        )
        TabItem(
            text = "植物学百科",
            selected = selectedTab == "植物学百科",
            onSelect = { onTabSelect("植物学百科") }
        )
    }
}

@Composable
fun TabItem(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                enabled = true, // 保持可点击
                onClick = onSelect,
                indication = null, // ✅ 关键：取消涟漪效果
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 8.dp)
    ) {
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

// 🎯 4. 匹配等级徽章（与识别界面完全一致）
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

// 💧 5. 养护指南内容区域（与识别界面完全一致）
@Composable
fun CareGuideContent(careData: List<CareItemData>) {
    Log.d("CareGuideContent", "渲染养护数据，数量: ${careData.size}")
    if (careData.isEmpty()) {
        Text(
            text = "暂无养护信息",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
    } else {
        careData.forEach { item ->
            Log.d("CareGuideContent", "养护项: ${item.title} - ${item.description.take(30)}...")
            CareItemWithIcon(data = item)
        }
    }
}

@Composable
fun CareItemWithIcon(data: CareItemData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.Top // 让Row整体向上对齐
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter // 让图标向上居中
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = when (data.title) {
                                "水" -> Color(0xFFE1F5FE)
                                "阳光" -> Color(0xFFFFF8E1)
                                "土壤" -> Color(0xFFE8F5E9)
                                "温度" -> Color(0xFFFFEBEE)
                                "肥料" -> Color(0xFFF3E5F5)
                                else -> Color.LightGray
                            },
                            shape = RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = data.icon),
                        contentDescription = data.title,
                        modifier = Modifier.size(20.dp),
                        tint = when (data.title) {
                            "水" -> Color(0xFF2196F3)
                            "阳光" -> Color(0xFFFFC107)
                            "土壤" -> Color(0xFF4CAF50)
                            "温度" -> Color(0xFFF44336)
                            "肥料" -> Color(0xFF9C27B0)
                            else -> Color.DarkGray
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = data.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.description,
                    fontSize = 14.sp,
                    color = Color(0xFF555555),
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color(0xFFF5F3F3))
        )
    }
}

// 🌱 6. 植物学百科内容区域（与识别界面完全一致）
@Composable
fun BotanyFactsContent(basicInfo: Map<String, String>) {
    Log.d("BotanyFactsContent", "渲染植物学百科，基础信息数量: ${basicInfo.size}")

    Column {
        // 1. 分类信息卡片（科属分类）
        basicInfo["科属分类"]?.let { taxonomy ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "分类学",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Log.d("BotanyFactsContent", "科属分类数据: $taxonomy")
                    val taxoParts = taxonomy.split(" ", limit = 2)
                    if (taxoParts.size >= 2) {
                        TaxonomyRow(label = "科", value = taxoParts[0])
                        TaxonomyRow(label = "属", value = taxoParts[1])
                    } else {
                        TaxonomyRow(label = "科属", value = taxonomy)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. 形态特征卡片
        basicInfo["形态特征"]?.let { feature ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "形态特征",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = feature,
                        fontSize = 14.sp,
                        color = Color(0xFF555555),
                        lineHeight = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. 植物文化卡片
        basicInfo["植物文化"]?.let { culture ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "植物文化",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = culture,
                        fontSize = 14.sp,
                        color = Color(0xFF555555),
                        lineHeight = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 4. 趣味知识卡片
        basicInfo["趣味知识"]?.let { funFact ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "你知道吗?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = funFact,
                        fontSize = 14.sp,
                        color = Color(0xFF555555),
                        lineHeight = 20.sp
                    )
                }
            }
        } ?: run {
            // 如果没有趣味知识，显示提示
            Text(
                text = "暂无更多植物学信息",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun TaxonomyRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF555555),
            fontWeight = FontWeight.Medium
        )
    }
}

// 🎯 核心：增强版的解析函数，处理收藏数据的各种格式
private fun parseFavoritePlantData(aiText: String): PlantFullData {
    Log.d("PlantParser", "🎯 开始解析收藏的AI结构化数据")
    Log.d("PlantParser", "输入数据长度: ${aiText.length}")
    Log.d("PlantParser", "输入数据前200字符: '${aiText.take(200)}...'")

    val basicInfo = mutableMapOf<String, String>()
    val careData = mutableListOf<CareItemData>()

    // 图标映射（与识别界面相同）
    val careIconMap = mapOf(
        "水" to R.drawable.identify_water,
        "阳光" to R.drawable.identify_sunlight,
        "土壤" to R.drawable.identify_soil,
        "温度" to R.drawable.identify_temperature,
        "肥料" to R.drawable.identify_fertilizer
    )

    // 按行解析
    val lines = aiText.lines().map { it.trim() }.filter { it.isNotBlank() }
    Log.d("PlantParser", "总行数: ${lines.size}")

    var parsedCount = 0
    lines.forEachIndexed { index, line ->
        Log.d("PlantParser", "解析行[$index]: '$line'")

        // 尝试多种分隔符格式：支持冒号（中英文）、破折号等
        val separators = listOf(
            Pair(":", line.indexOf(':')),
            Pair("：", line.indexOf('：')),
            Pair("-", line.indexOf('-')),
            Pair("： ", line.indexOf("： "))
        )

        // 找到第一个有效的分隔符
        val validSeparator = separators.firstOrNull { it.second > 0 && it.second < line.length - 1 }

        if (validSeparator != null) {
            val separatorIndex = validSeparator.second
            val title = line.substring(0, separatorIndex).trim()
            val content = line.substring(separatorIndex + validSeparator.first.length).trim()

            Log.d("PlantParser", "✅ 解析成功行[${index + 1}]: '$title' -> '${content.take(30)}...'")

            // 扩展的标题映射，处理可能的多种标题名称
            when {
                // 基础信息：植物简介/简介/植物介绍
                title in listOf("植物简介", "简介", "植物介绍", "介绍") -> {
                    basicInfo["植物简介"] = content
                    parsedCount++
                }
                // 科属分类/分类/科属
                title in listOf("科属分类", "分类", "科属", "科属信息") -> {
                    basicInfo["科属分类"] = content
                    parsedCount++
                }
                // 形态特征/特征/形态
                title in listOf("形态特征", "特征", "形态", "外形特征") -> {
                    basicInfo["形态特征"] = content
                    parsedCount++
                }
                // 植物文化/文化
                title in listOf("植物文化", "文化", "文化背景") -> {
                    basicInfo["植物文化"] = content
                    parsedCount++
                }
                // 趣味知识/趣味/小知识
                title in listOf("趣味知识", "趣味", "小知识", "趣闻") -> {
                    basicInfo["趣味知识"] = content
                    parsedCount++
                }
                // 养护信息：水/浇水/水分
                title in listOf("水", "浇水", "水分", "水分需求") -> {
                    careData.add(CareItemData(
                        title = "水",
                        icon = careIconMap["水"] ?: R.drawable.identify_water,
                        description = content
                    ))
                    parsedCount++
                }
                // 阳光/光照/日照
                title in listOf("阳光", "光照", "日照", "光照需求") -> {
                    careData.add(CareItemData(
                        title = "阳光",
                        icon = careIconMap["阳光"] ?: R.drawable.identify_sunlight,
                        description = content
                    ))
                    parsedCount++
                }
                // 土壤/土质/培土
                title in listOf("土壤", "土质", "培土", "土壤需求") -> {
                    careData.add(CareItemData(
                        title = "土壤",
                        icon = careIconMap["土壤"] ?: R.drawable.identify_soil,
                        description = content
                    ))
                    parsedCount++
                }
                // 温度/气温/适温
                title in listOf("温度", "气温", "适温", "温度需求") -> {
                    careData.add(CareItemData(
                        title = "温度",
                        icon = careIconMap["温度"] ?: R.drawable.identify_temperature,
                        description = content
                    ))
                    parsedCount++
                }
                // 肥料/施肥/营养
                title in listOf("肥料", "施肥", "营养", "肥料需求") -> {
                    careData.add(CareItemData(
                        title = "肥料",
                        icon = careIconMap["肥料"] ?: R.drawable.identify_fertilizer,
                        description = content
                    ))
                    parsedCount++
                }
                else -> {
                    Log.w("PlantParser", "⚠️ 未知标题: '$title'")
                }
            }
        } else {
            Log.w("PlantParser", "❌ 格式不正确行[${index + 1}]: '$line'")

            // 兜底处理：如果没有分隔符，将整行内容作为植物简介的一部分
            if (basicInfo["植物简介"] == null) {
                basicInfo["植物简介"] = line
            } else {
                basicInfo["植物简介"] = basicInfo["植物简介"] + "\n" + line
            }
        }
    }

    // 打印解析结果
    Log.d("PlantParser", "✅ 解析完成:")
    Log.d("PlantParser", "成功解析行数: $parsedCount")
    Log.d("PlantParser", "基础信息数量: ${basicInfo.size}")
    basicInfo.forEach { (key, value) ->
        Log.d("PlantParser", "  $key: ${value.take(30)}...")
    }
    Log.d("PlantParser", "养护数据数量: ${careData.size}")
    careData.forEach { item ->
        Log.d("PlantParser", "  ${item.title}: ${item.description.take(30)}...")
    }

    // 兜底：如果养护数据不完整，补充默认值
    if (careData.size < 5) {
        Log.w("PlantParser", "⚠️ 养护数据不完整，补充默认值")
        ensureAllCareItems(careData)
    }

    // 确保植物简介不为空
    if (basicInfo["植物简介"].isNullOrEmpty() && aiText.isNotBlank()) {
        basicInfo["植物简介"] = aiText.take(200) // 使用前200字符作为简介
    }

    return PlantFullData(basicInfo, careData)
}

// 确保所有养护项都存在（与识别界面相同）
private fun ensureAllCareItems(careData: MutableList<CareItemData>) {
    val requiredTitles = listOf("水", "阳光", "土壤", "温度", "肥料")
    val existingTitles = careData.map { it.title }.toSet()

    requiredTitles.forEach { title ->
        if (!existingTitles.contains(title)) {
            val icon = when (title) {
                "水" -> R.drawable.identify_water
                "阳光" -> R.drawable.identify_sunlight
                "土壤" -> R.drawable.identify_soil
                "温度" -> R.drawable.identify_temperature
                "肥料" -> R.drawable.identify_fertilizer
                else -> R.drawable.identify_water
            }
            careData.add(CareItemData(title, icon, "待补充"))
            Log.d("PlantParser", "补充默认养护项: $title")
        }
    }

    // 按顺序排序
    careData.sortBy { requiredTitles.indexOf(it.title) }
}