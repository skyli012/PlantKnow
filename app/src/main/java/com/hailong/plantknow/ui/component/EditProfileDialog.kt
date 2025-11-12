// EditProfileDialog.kt
package com.hailong.plantknow.ui.component

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hailong.plantknow.data.entity.UserProfile
import com.hailong.plantknow.utils.ImageSaver

@Composable
fun EditProfileDialog(
    userProfile: UserProfile?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(userProfile?.name ?: "") }
    var bio by remember { mutableStateOf(userProfile?.bio ?: "") }
    var avatarUri by remember { mutableStateOf(userProfile?.avatarUri ?: "") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imageSaver = remember { ImageSaver(context) }

    // 直接在这里创建图片选择器
    val coroutineScope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectedUri ->
            // 在回调里使用协程做异步保存，避免在非 Composable lambda 中调用 LaunchedEffect
            coroutineScope.launch {
                isLoading = true
                try {
                    // 在 IO 线程执行保存操作
                    val savedImagePath = withContext(Dispatchers.IO) {
                        imageSaver.saveAvatarToInternalStorage(selectedUri)
                    }
                    savedImagePath?.let { path ->
                        avatarUri = path
                        // 删除旧的头像文件（如果是内部存储路径）
                        userProfile?.avatarUri?.let { oldPath ->
                            if (oldPath.isNotEmpty() && oldPath.startsWith("/")) {
                                // 删除操作也可以在 IO 线程，但这里 deleteOldAvatar 已在 ImageSaver 中处理线程
                                imageSaver.deleteOldAvatar(oldPath)
                            }
                        }
                        println("头像保存成功: $path")
                    } ?: run {
                        // 如果保存失败，使用临时URI
                        avatarUri = selectedUri.toString()
                        println("头像保存失败，使用临时URI")
                    }
                } catch (e: Exception) {
                    // 如果异常，使用临时URI
                    avatarUri = selectedUri.toString()
                    println("保存过程中出现异常: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "编辑资料",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
        },
        text = {
            Column {
                // 头像编辑区域
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5))
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            if (!isLoading) {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        // 加载中显示进度指示器
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = Color(0xFF4CAF50)
                        )
                    } else if (avatarUri.isNotEmpty() && avatarUri.startsWith("/")) {
                        // 显示保存的内部存储图片
                        val imageFile = imageSaver.getImageFileFromPath(avatarUri)
                        if (imageSaver.isImageFileExists(avatarUri)) {
                            AsyncImage(
                                model = imageFile,
                                contentDescription = "用户头像",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // 文件不存在，显示默认图标
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "用户头像",
                                tint = Color(0xFF7F8C8D),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    } else if (avatarUri.isNotEmpty()) {
                        // 临时URI，直接显示
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "用户头像",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // 没有头像
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "用户头像",
                                tint = Color(0xFF7F8C8D),
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "点击选择",
                                fontSize = 10.sp,
                                color = Color(0xFF7F8C8D),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // 相机图标叠加
                    if (avatarUri.isNotEmpty() && !isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "更换头像",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (isLoading) "正在保存图片..." else "点击更换头像",
                    fontSize = 12.sp,
                    color = Color(0xFF7F8C8D),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp, bottom = 16.dp)
                )

                // 姓名输入框
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    singleLine = true,
                    enabled = !isLoading
                )

                // 个性签名输入框
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("个性签名") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    maxLines = 3,
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text("取消", color = Color(0xFF7F8C8D))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onSave(name, bio, avatarUri) },
                    enabled = !isLoading
                ) {
                    Text("保存")
                }
            }
        }
    )
}