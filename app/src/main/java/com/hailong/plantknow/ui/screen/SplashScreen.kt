package com.hailong.plantknow.ui.screen
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hailong.plantknow.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onAnimationComplete: () -> Unit
) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }
    val imageScale = remember { Animatable(0.5f) }
    val exitAlpha = remember { Animatable(1f) }

    val systemUiController = rememberSystemUiController()
    val backgroundColor = Color(0xFFE9F0F8)

    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(
            color = backgroundColor,
            darkIcons = true
        )
        systemUiController.setNavigationBarColor(
            color = backgroundColor
        )

        // 入场动画
        launch {
            scale.animateTo(1.1f, tween(600))
            scale.animateTo(1f, tween(200))
        }
        launch {
            alpha.animateTo(1f, tween(800))
        }
        launch {
            imageScale.animateTo(1f, tween(600))
        }

        // 显示内容
        delay(1500)

        // 淡出动画
        exitAlpha.animateTo(0f, tween(500))

        onAnimationComplete()
    }

    // 创建带样式的文本
    val coloredText = buildAnnotatedString {
        // "Plant" 部分 - 粉色
        withStyle(style = SpanStyle(color = Color(0xFFFFCCCC))) {
            append("Plant")
        }
        // "Know" 部分 - 绿色
        withStyle(style = SpanStyle(color = Color(0xFFC0E889))) {
            append("Know")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .alpha(exitAlpha.value),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-40).dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.home_flower),
                contentDescription = "装饰花朵",
                modifier = Modifier
                    .size(150.dp)
                    .scale(imageScale.value)
                    .alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 使用带样式的文本
            Text(
                text = coloredText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
            )
        }
    }
}