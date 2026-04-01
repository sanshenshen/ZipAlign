package com.lingxing.zipalign.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PanelCard(
    title: String,
    modifier: Modifier = Modifier,
    kicker: String? = null,
    content: @Composable () -> Unit,
) {
    val containerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f),
        ),
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(containerBrush)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (!kicker.isNullOrBlank()) {
                Text(
                    text = kicker.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = if (kicker == null) 0.dp else 4.dp, bottom = 14.dp),
            )
            content()
        }
    }
}

@Composable
fun ConsoleSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.96f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                    ),
                ),
            )
    ) {
        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.04f))
                .padding(18.dp)
        ) {
            content()
        }
    }
}
