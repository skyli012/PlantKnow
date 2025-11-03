package com.hailong.plantknow.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hailong.plantknow.model.FavoritePlant
import com.hailong.plantknow.ui.component.FavoriteListItem
import com.hailong.plantknow.viewmodel.FavoriteViewModel

@Composable
fun FavoriteListScreen(
    favoriteViewModel: FavoriteViewModel,
    onBackClick: () -> Unit,
    onItemClick: (FavoritePlant) -> Unit
) {
    val favoritePlants by favoriteViewModel.favoritePlants.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, "返回")
            }
            Text(
                text = "我的收藏",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // 收藏列表
        if (favoritePlants.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("还没有收藏的植物", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(favoritePlants) { favorite ->
                    FavoriteListItem(
                        favorite = favorite,
                        onClick = { onItemClick(favorite) },
                        onRemoveClick = {
                            favoriteViewModel.removeFavorite(favorite.plantName)
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}