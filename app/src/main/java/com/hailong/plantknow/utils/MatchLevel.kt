package com.hailong.plantknow.utils

import androidx.compose.ui.graphics.Color

enum class MatchLevel(
    val label: String,
    val color: Color
) {
    HIGH("HIGH MATCH", Color(0xFF2ECC71)),
    MEDIUM("MEDIUM MATCH", Color(0xFFF39C12)),
    LOW("LOW MATCH", Color(0xFFE74C3C))
}
fun confidenceToMatchLevel(confidence: Int): MatchLevel {
    return when {
        confidence >= 85 -> MatchLevel.HIGH
        confidence >= 60 -> MatchLevel.MEDIUM
        else -> MatchLevel.LOW
    }
}
