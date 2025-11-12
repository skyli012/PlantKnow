// ImagePickerUtils.kt
package com.hailong.plantknow.utils

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class ImagePicker(private val context: Context) {

    fun createImageFile(): String {
        // 这里可以创建临时文件，或者直接使用返回的 URI
        return ""
    }
}

@Composable
fun rememberImagePicker(): ImagePicker {
    val context = LocalContext.current
    return remember { ImagePicker(context) }
}

@Composable
fun rememberImagePickerLauncher(onImageSelected: (Uri?) -> Unit) =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onImageSelected(uri)
    }