package com.hailong.plantknow.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hailong.plantknow.model.FavoritePlant

@Composable
fun FavoriteDetailScreen(
    favoritePlant: FavoritePlant,
    onBackClick: () -> Unit
) {
    // 解析描述文本为结构化数据
    val descriptionSections = parsePlantDescription(favoritePlant.description)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp), // 保留上左右下边距
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, "返回")
            }
            Text(
                text = "植物详情",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // 内容
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            AsyncImage(
                model = favoritePlant.imageUri,
                contentDescription = favoritePlant.plantName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = favoritePlant.plantName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "置信度: ${favoritePlant.confidence}%",
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 显示解析后的结构化描述
            if (descriptionSections.isNotEmpty()) {
                descriptionSections.forEach { section ->
                    PlantDescriptionSection(
                        title = section.title,
                        content = section.content
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                // 如果解析失败，显示原始文本
                Text(
                    text = favoritePlant.description,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

/**
 * 解析植物描述文本，提取小标题和内容
 * 使用白名单机制，只识别真正的小标题
 */
private fun parsePlantDescription(description: String): List<DescriptionSection> {
    val sections = mutableListOf<DescriptionSection>()

    if (description.isBlank()) return sections

    // 定义允许的小标题列表（白名单）
    val allowedTitles = setOf(
        "植物简介", "科属分类", "形态特征", "植物文化", "趣味知识",
        "生长环境", "分布范围", "主要价值", "栽培技术", "药用价值",
        "生态习性", "繁殖方式", "注意事项"
    )

    // 按换行符分割文本为段落
    val paragraphs = description.split('\n').map { it.trim() }.filter { it.isNotBlank() }

    var currentSection: DescriptionSection? = null

    for (paragraph in paragraphs) {
        // 检查是否是标题段落（以白名单中的标题开头）
        val matchedTitle = allowedTitles.firstOrNull { title ->
            paragraph.startsWith("$title:") // 植物详细页面小标题和内容划分   英文冒号
        }
        Log.d("PlantParser", "Input: $paragraph")

        if (matchedTitle != null) {
            // 保存前一个section
            currentSection?.let { sections.add(it) }

            // 创建新的section
            val content = paragraph.substringAfter(":").trim()  // 植物详细页面小标题和内容划分    英文冒号
            currentSection = DescriptionSection(matchedTitle, content)
        } else {
            // 如果不是标题段落
            if (currentSection == null) {
                // 如果是第一个段落，作为植物简介
                if (sections.isEmpty()) {
                    currentSection = DescriptionSection("植物简介", paragraph)
                } else {
                    // 添加到最后一个section
                    val lastIndex = sections.size - 1
                    sections[lastIndex] = sections[lastIndex].copy(
                        content = sections[lastIndex].content + "\n" + paragraph
                    )
                }
            } else {
                // 添加到当前section
                currentSection = currentSection.copy(
                    content = currentSection.content + "\n" + paragraph
                )
            }
        }
    }

    // 添加最后一个section
    currentSection?.let { sections.add(it) }

    // 如果没有找到任何section，使用原始文本作为简介
    if (sections.isEmpty()) {
        sections.add(DescriptionSection("植物简介", description.trim()))
    }

    return sections
}

/**
 * 植物描述段落组件
 */
@Composable
fun PlantDescriptionSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF364858)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            fontSize = 14.sp,
            color = Color(0xFF666666),
            lineHeight = 20.sp
        )
    }
}

/**
 * 描述段落数据类
 */
data class DescriptionSection(
    val title: String,
    val content: String
)