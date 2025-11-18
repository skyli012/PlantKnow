package com.hailong.plantknow.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hailong.plantknow.model.confidencePercent
import com.hailong.plantknow.viewmodel.FavoriteViewModel

@Composable
fun PlantDetailsWithStickyHeader(
    plantWithDetails: com.hailong.plantknow.model.PlantWithDetails,
    favoriteViewModel: FavoriteViewModel,
    selectedImage: Any? // 添加 selectedImage 参数
) {
    // 检查是否已收藏 - 使用 Flow 和 collectAsState
    val favoritePlants by favoriteViewModel.favoritePlants.collectAsState(initial = emptyList())
    val isFavorited = favoritePlants.any { it.plantName == plantWithDetails.basicInfo.plantName }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
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

                    // 收藏按钮 - 现在与ViewModel关联
                    FavoriteButton(
                        isFavorited = isFavorited,
                        onFavoriteClick = {
                            if (isFavorited) {
                                favoriteViewModel.removeFavorite(plantWithDetails.basicInfo.plantName)
                            } else {
                                // 直接传递图片对象，让 Repository 处理图片保存
                                favoriteViewModel.addFavorite(
                                    plantWithDetails = plantWithDetails,
                                    image = selectedImage // 直接传递图片对象，而不是 URI 字符串
                                )
                            }
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
 * 分段渲染植物描述，标题使用大字体
 */
@Composable
fun PlantDescriptionWithTitles(description: String) {
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
    Log.d("PlantParser", "Input: $description")

    var currentContent = StringBuilder()

    lines.forEach { line ->
        val match = Regex("^(.+?)[:：]\\s*(.*)").find(line)
        if (match != null) {
            // 如果之前有内容，先保存
            if (currentContent.isNotEmpty()) {
                sections.add(
                    DescriptionSection(SectionType.CONTENT, currentContent.toString().trim())
                )
                currentContent = StringBuilder()
            }
            // 添加标题和内容
            val title = match.groupValues[1].trim()
            val content = match.groupValues[2].trim()
            sections.add(DescriptionSection(SectionType.TITLE, title))
            sections.add(DescriptionSection(SectionType.CONTENT, content))
        } else {
            // 普通内容行
            currentContent.append(line)
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