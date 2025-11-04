package com.hailong.plantknow

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.hailong.plantknow.network.AuthHelper
import com.hailong.plantknow.ui.MainScreen
import com.hailong.plantknow.ui.screen.SplashScreen  // 导入您的SplashScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "应用启动")

        // 初始化 AuthHelper
        Log.d(TAG, "初始化 AuthHelper")
        AuthHelper.initialize(applicationContext)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppContent()
                }
            }
        }

        Log.d(TAG, "界面初始化完成")
    }
}

@Composable
fun AppContent() {
    var showSplash by remember { mutableStateOf(true) }
    val splashAlpha = remember { Animatable(1f) }
    val mainScreenAlpha = remember { Animatable(0f) }
    val backgroundColor = Color(0xFFE9F0F8)

    // ✅ 新增：控制 MainScreen 是否真正可见
    var isMainScreenReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // ✅ 提前准备 MainScreen：触发组合，但不显示
        // 你可以在这里预加载数据
//         delay(100) // 模拟初始化
        isMainScreenReady = true
    }

    LaunchedEffect(showSplash) {
        if (!showSplash) {
            launch { splashAlpha.animateTo(0f, tween(400)) }
            launch { mainScreenAlpha.animateTo(1f, tween(400)) }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)) {

        // ✅ 关键：提前组合 MainScreen，但先隐藏
        if (isMainScreenReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(mainScreenAlpha.value)
                    .graphicsLayer()
                // .visible(mainScreenAlpha.value > 0f) // 可选：完全隐藏直到淡入
            ) {
                MainScreen()
            }
        } else {
            // 可选：显示一个极简的占位 UI（如 Logo + 背景），避免白屏
            Box(modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor))
        }

        // 开屏界面
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(splashAlpha.value)
        ) {
            if (showSplash) {
                SplashScreen(onAnimationComplete = { showSplash = false })
            }
        }
    }
}