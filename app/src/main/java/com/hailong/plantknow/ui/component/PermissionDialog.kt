package com.hailong.plantknow.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * 权限请求对话框
 * @param permissionType 权限类型（用于显示不同的说明文本）
 * @param onAllowClick 允许权限回调
 * @param onDenyClick 拒绝权限回调
 * @param onDismissRequest 对话框关闭回调
 */
@Composable
fun PermissionDialog(
    permissionType: String = "相机",
    onAllowClick: () -> Unit,
    onDenyClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFE9F0F8),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = "需要${permissionType}权限",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF364858),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 说明文本
                Text(
                    text = getPermissionDescription(permissionType),
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Start,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // 按钮区域
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 拒绝按钮
                    OutlinedButton(
                        onClick = onDenyClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF666666)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "拒绝",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // 允许按钮
                    Button(
                        onClick = onAllowClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF364858)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "允许",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 权限被拒绝后的解释对话框
 */
@Composable
fun PermissionExplanationDialog(
    permissionType: String = "相机",
    onGoToSettings: () -> Unit,
    onCancel: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFE9F0F8),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = "权限未开启",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF364858),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 说明文本
                Text(
                    text = "${permissionType}权限已被拒绝，无法使用相关功能。\n\n请前往系统设置中手动开启权限。",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Start,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // 按钮区域
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 取消按钮
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF666666)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "取消",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // 去设置按钮
                    Button(
                        onClick = onGoToSettings,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "去设置",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 根据权限类型获取说明文本
 */
@Composable
private fun getPermissionDescription(permissionType: String): String {
    return when (permissionType) {
        "相机" -> "植物识别需要使用相机拍摄植物照片：\n" +
                "拍摄清晰的植物照片用于识别\n" +
                "照片仅用于植物识别分析\n" +
                "不会存储或分享您的照片"

        "存储" -> "植物识别需要访问相册来选择植物照片：\n" +
                "从相册选择植物照片用于识别\n" +
                "仅读取选中的照片\n" +
                "不会访问其他照片或数据"

        else -> "应用需要此权限来提供完整的植物识别功能。"
    }
}