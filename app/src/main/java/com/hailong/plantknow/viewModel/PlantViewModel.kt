package com.hailong.plantknow.viewModel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.hailong.plantknow.model.PlantResult
import com.hailong.plantknow.model.PlantWithDetails
import com.hailong.plantknow.repository.PlantRecognitionRepository
import com.hailong.plantknow.util.Result

class PlantViewModel(private val plantRecognitionRepository: PlantRecognitionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PlantRecognitionState())
    val uiState: StateFlow<PlantRecognitionState> = _uiState.asStateFlow()

    data class PlantRecognitionState(
        val isLoading: Boolean = false,
        val result: PlantResult? = null,
        val plantWithDetails: PlantWithDetails? = null,
        val error: String? = null,
        val selectedImage: Any? = null,
        val recognitionStep: String = "准备识别"
    )

    fun selectImageFromGallery(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedImage = uri,
            result = null,
            plantWithDetails = null,
            error = null,
            recognitionStep = "准备识别"
        )
    }

    fun takePhoto(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            selectedImage = bitmap,
            result = null,
            plantWithDetails = null,
            error = null,
            recognitionStep = "准备识别"
        )
    }

    /**
     * 完整的植物识别流程：百度识别 + 阿里云详情补充（推荐使用）
     */
    fun recognizePlantWithDetails() {
        val selectedImage = _uiState.value.selectedImage
        if (selectedImage == null) {
            _uiState.value = _uiState.value.copy(error = "未选择图片")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                result = null,
                plantWithDetails = null,
                error = null,
                recognitionStep = "识别植物中..."
            )

            try {
                val result = when (selectedImage) {
                    is Uri -> {
                        Log.d("PlantViewModel", "从URI开始完整识别")
                        plantRecognitionRepository.recognizePlantWithDetailsFromUri(selectedImage)
                    }
                    is Bitmap -> {
                        Log.d("PlantViewModel", "从Bitmap开始完整识别")
                        plantRecognitionRepository.recognizePlantWithDetails(selectedImage)
                    }
                    else -> throw IllegalArgumentException("不支持的图片类型")
                }

                when (result) {
                    is Result.Success -> {
                        Log.d("PlantViewModel", "完整识别成功: ${result.data.basicInfo.plantName}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            plantWithDetails = result.data,
                            result = result.data.basicInfo, // 保持兼容
                            recognitionStep = "识别完成"
                        )
                    }
                    is Result.Error -> {
                        Log.e("PlantViewModel", "识别失败", result.exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.exception.message ?: "识别失败",
                            recognitionStep = "识别失败"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PlantViewModel", "发生意外错误", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "发生意外错误",
                    recognitionStep = "识别失败"
                )
            }
        }
    }

    /**
     * 仅使用百度识别（保持向后兼容）
     */
    fun recognizePlant() {
        val selectedImage = _uiState.value.selectedImage
        if (selectedImage == null) {
            _uiState.value = _uiState.value.copy(error = "未选择图片")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                result = null,
                plantWithDetails = null,
                error = null,
                recognitionStep = "识别植物中..."
            )

            try {
                val result = when (selectedImage) {
                    is Uri -> plantRecognitionRepository.recognizePlantFromUri(selectedImage)
                    is Bitmap -> plantRecognitionRepository.recognizePlantFromBitmap(selectedImage)
                    else -> throw IllegalArgumentException("不支持的图片类型")
                }

                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            result = result.data,
                            recognitionStep = "识别完成"
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.exception.message ?: "识别失败",
                            recognitionStep = "识别失败"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "发生意外错误",
                    recognitionStep = "识别失败"
                )
            }
        }
    }

    /**
     * 刷新植物详细信息
     */
    fun refreshPlantDetails(plantName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                recognitionStep = "获取详细信息..."
            )

            try {
                val result = plantRecognitionRepository.refreshPlantDetails(plantName)

                when (result) {
                    is Result.Success -> {
                        val currentDetails = _uiState.value.plantWithDetails
                        if (currentDetails != null) {
                            val updatedDetails = currentDetails.copy(
                                detailedDescription = result.data
                            )
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                plantWithDetails = updatedDetails,
                                recognitionStep = "详情已更新"
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "刷新失败: ${result.exception.message}",
                            recognitionStep = "刷新失败"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "刷新失败: ${e.message}",
                    recognitionStep = "刷新失败"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = PlantRecognitionState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}