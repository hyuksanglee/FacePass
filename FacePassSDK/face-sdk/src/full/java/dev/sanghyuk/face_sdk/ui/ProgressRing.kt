package dev.sanghyuk.face_sdk.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun ProgressRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 8f
    ){
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "ringProgress"
    )

    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(durationMillis = 400),
        label = "ringColor"
    )

    Canvas(modifier = modifier.size(200.dp)) {
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        val inset = strokeWidth / 2
        val arcSize = Size(
            size.width - strokeWidth,
            size.height - strokeWidth
            )
        val topLeft = Offset(inset, inset)

        drawArc(
            color = Color.White.copy(0.15f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )

        drawArc(
            color = animatedColor,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )
    }
}

@Preview
@Composable
private fun ProgressRingPreview() {
    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        ProgressRing(progress = 0.66f, color = Color(0xFFF0A835))
    }
}