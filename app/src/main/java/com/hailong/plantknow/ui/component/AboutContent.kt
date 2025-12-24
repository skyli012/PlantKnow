package com.hailong.plantknow.ui.component
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hailong.plantknow.R

@Composable
fun AboutContent(
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部栏 - 使用收藏列表的样式
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.identify_back),
                    contentDescription = "返回",
                    tint = Color(0xFF000000),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "关于我们",
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        // 内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 应用图标和名称
            Box(
                modifier = Modifier
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // 使用简单的植物emoji作为图标
                Image(
                    painter = painterResource(id = R.drawable.winter_app),
                    contentDescription = "应用图标",
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))


            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFFFFCCCC),
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Plant")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFFC0E889),
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Know")
                    }
                },
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "版本 1.0.0",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 30.dp)
            )

            // 关于我们内容
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "欢迎使用PlantKnow！ ᐢ. ֑ .ᐢ",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }


//                Text(
//                    text = "主要功能：",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    color = Color(0xFF2C3E50),
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
//
//                Text(
//                    text = "• 简洁易用的界面设计\n• 稳定可靠的性能表现\n• 持续的功能更新和优化\n• 贴心的用户体验",
//                    fontSize = 14.sp,
//                    color = Color(0xFF666666),
//                    lineHeight = 20.sp,
//                    modifier = Modifier.padding(bottom = 24.dp)
//                )

//                Text(
//                    text = "联系我们：",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    color = Color(0xFF2C3E50),
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
                Spacer(modifier = Modifier.height(228.dp))

            }

//            Box(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "plantknow@126.com",
//                    fontSize = 14.sp,
//                    color = Color(0xFF666666),
//                    lineHeight = 20.sp
//                )
//            }

            // 版权信息 - 固定在底部
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "© 2024 PlantKnow 版权所有",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun AboutScreenPreview() {
    AboutContent(onBackClick = {})
}