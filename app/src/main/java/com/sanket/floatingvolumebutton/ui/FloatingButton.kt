package com.sanket.floatingvolumebutton.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun FloatingButton(
    size: Int,
    opacity: Float,
    outsideColor: Color,
    insideColor: Color,
    onClick: () -> Unit,
    onDrag: (dx: Int, dy: Int) -> Unit,
    onDragEnd: () -> Unit,
    onDismiss: () -> Unit,
    screenHeight: Int
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { /* Optional: handle start */ },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x.roundToInt(), dragAmount.y.roundToInt())
                    },
                    onDragEnd = {
                        onDragEnd()
                    }
                )
            }
            .background(outsideColor.copy(alpha = opacity), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Volume",
                tint = insideColor,
                modifier = Modifier.size((size * 0.6).dp)
            )
        }
    }
}

@Preview
@Composable
fun FloatingButtonPreview() {
    FloatingButton(
        size = 60,
        opacity = 0.7f,
        outsideColor = Color.Blue,
        insideColor = Color.White,
        onClick = {},
        onDrag = { _, _ -> },
        onDragEnd = {},
        onDismiss = {},
        screenHeight = 2000
    )
}
