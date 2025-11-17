package com.hailong.plantknow.ui.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.hailong.plantknow.ui.screen.PlantPost

@Composable
fun PlantCardContent(post: PlantPost) {
    Column(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(10.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(post.img),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(post.randomHeight.dp)
                .background(Color.LightGray, RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(6.dp))
        Text(post.desc, fontSize = 14.sp, maxLines = 2)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(post.author, color = Color.Gray, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF7B7B),
                    modifier = Modifier.size(16.dp)
                )
                Text(post.likes.toString(), fontSize = 12.sp)
            }
        }
    }
}