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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
    onDismiss: () -> Unit,  // 这个用于返回到资料对话框
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(userProfile?.name ?: "") }
    var bio by remember { mutableStateOf(userProfile?.bio ?: "") }
    var avatarUri by remember { mutableStateOf(userProfile?.avatarUri ?: "") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imageSaver = remember { ImageSaver(context) }

    val coroutineScope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectedUri ->
            coroutineScope.launch {
                isLoading = true
                try {
                    val savedImagePath = withContext(Dispatchers.IO) {
                        imageSaver.saveAvatarToInternalStorage(selectedUri)
                    }
                    savedImagePath?.let { path ->
                        avatarUri = path
                        // 删除旧的头像文件
                        userProfile?.avatarUri?.let { oldPath ->
                            if (oldPath.isNotEmpty() && oldPath.startsWith("/")) {
                                imageSaver.deleteOldAvatar(oldPath)
                            }
                        }
                        println("头像保存成功: $path")
                    } ?: run {
                        avatarUri = selectedUri.toString()
                        println("头像保存失败，使用临时URI")
                    }
                } catch (e: Exception) {
                    avatarUri = selectedUri.toString()
                    println("保存过程中出现异常: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,  // 点击空白处或返回键返回到资料对话框
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
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = Color(0xFF4CAF50)
                        )
                    } else if (avatarUri.isNotEmpty() && avatarUri.startsWith("/")) {
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
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "用户头像",
                                tint = Color(0xFF7F8C8D),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    } else if (avatarUri.isNotEmpty()) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "用户头像",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
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
                    onClick = onDismiss,  // 取消按钮返回到资料对话框
                    enabled = !isLoading
                ) {
                    Text("取消", color = Color(0xFF7F8C8D))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onSave(name, bio, avatarUri) },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC6A9F8)
                    )
                ) {
                    Text("保存")
                }
            }
        }
    )
}