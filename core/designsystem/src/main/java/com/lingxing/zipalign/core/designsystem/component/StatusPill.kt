package com.lingxing.zipalign.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusPill(
    label: String,
    tone: StatusTone,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when (tone) {
        StatusTone.Positive -> Color(0x1F2B8078)
        StatusTone.Caution -> Color(0x1FE2A93B)
        StatusTone.Critical -> Color(0x1FB7472A)
        StatusTone.Neutral -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }
    val contentColor = when (tone) {
        StatusTone.Positive -> Color(0xFF2B8078)
        StatusTone.Caution -> Color(0xFF9A6A0F)
        StatusTone.Critical -> MaterialTheme.colorScheme.error
        StatusTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

enum class StatusTone {
    Positive,
    Caution,
    Critical,
    Neutral,
}
