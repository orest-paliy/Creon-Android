package com.orestpalii.diploma.ui.helper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AIPointerView(
    confidence: Int,
    scale: Float,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    cardColor: Color = MaterialTheme.colorScheme.surface
) {
    val arcWidth: Dp = 20.dp * scale
    val circleSize = 200.dp * scale
    val halfCircleSize = 160.dp * scale
    val pointerSize = 20.dp * scale

    Box(
        modifier = modifier
            .size(circleSize)
            .wrapContentSize(Alignment.Center)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = arcWidth.toPx()
            val radius = size.minDimension / 2
            val arcRect = Rect(Offset.Zero, Size(size.width, size.height))

            // Arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(primaryColor, backgroundColor)
                ),
                startAngle = 180f,
                sweepAngle = 180f * 0.9f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Half Circle Background
            drawRoundRect(
                color = cardColor,
                topLeft = Offset((size.width - halfCircleSize.toPx()) / 2f, (size.height - halfCircleSize.toPx()) / 2f),
                size = Size(halfCircleSize.toPx(), halfCircleSize.toPx()),
                cornerRadius = CornerRadius(halfCircleSize.toPx() / 2f)
            )

            // Pointer
            val angle = angleForConfidence(confidence)
            val pointerLength = radius - strokeWidth / 2
            val pointerX = radius + pointerLength * cos(Math.toRadians(angle.toDouble())).toFloat()
            val pointerY = radius + pointerLength * sin(Math.toRadians(angle.toDouble())).toFloat()

            drawCircle(
                color = cardColor,
                radius = pointerSize.toPx() / 2,
                center = Offset(pointerX, pointerY)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center).offset(y = (-17.5 * scale).dp)
        ) {
            Text("AI", style = MaterialTheme.typography.titleMedium)
            Text("$confidence%", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun angleForConfidence(confidence: Int): Float {
    val percent = confidence.coerceIn(0, 100) / 100f
    return (-90f + (percent * 180f)) * 0.9f
}
