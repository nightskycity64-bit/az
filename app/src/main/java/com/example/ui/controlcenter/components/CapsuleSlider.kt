package com.example.ui.controlcenter.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CapsuleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    width: Dp = 72.dp,
    height: Dp = 152.dp,
    testTag: String = "capsule_slider",
    onDragEnd: () -> Unit = {}
) {
    val shape = RoundedCornerShape(26.dp)
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = 800f),
        label = "slider_fill"
    )

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .testTag(testTag)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x382A2A32),
                        Color(0x281C1C22)
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x35FFFFFF),
                        Color(0x10FFFFFF)
                    )
                ),
                shape = shape
            )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val h = size.height
                        if (h > 0) {
                            val newValue = 1f - (offset.y / h)
                            onValueChange(newValue.coerceIn(0f, 1f))
                            onDragEnd()
                        }
                    }
                }
                .pointerInput(Unit) {
                    var currentY = 0f
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentY = offset.y
                        },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val h = size.height
                            if (h > 0) {
                                val delta = -dragAmount.y / h
                                val next = (value + delta).coerceIn(0f, 1f)
                                onValueChange(next)
                            }
                        }
                    )
                }
        ) {
            val totalHeight = maxHeight

            // Fill Layer rising from bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(animatedValue)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFE8E8ED)
                            )
                        )
                    )
            )

            // Bottom Icon
            val isFilledOverIcon = animatedValue > 0.28f
            val iconTint = if (isFilledOverIcon) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)

            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .size(26.dp)
            )
        }
    }
}
