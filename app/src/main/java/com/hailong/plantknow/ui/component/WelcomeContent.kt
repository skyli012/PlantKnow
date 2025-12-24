package com.hailong.plantknow.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hailong.plantknow.R

@Composable
fun WelcomeContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 上半部分内容
        Column(
            modifier = Modifier.fillMaxWidth(),


            ) {
            Text(
                text = "Hello, Plant Lover! \uD83D\uDC4B",
                color = Color(0xFF000000),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 20.dp, start = 5.dp)
            )

            Text(
                text = "Identify plants in seconds",
                color = Color(0xFF000000),
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp, start = 5.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.christmas),
                contentDescription = "Christmas trees",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp)
            )

        }
    }
}