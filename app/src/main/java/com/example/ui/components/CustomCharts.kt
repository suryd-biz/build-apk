package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.ScreenUtils

@Composable
fun FinanceLineTrendChart(
    dataPoints: List<Double>,
    labels: List<String>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    isMasked: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        if (dataPoints.isEmpty() || dataPoints.all { it == 0.0 }) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada data grafik",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            return@Box
        }

        if (isMasked) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Rp •••••••",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = lineColor.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Sensor Aktif",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            return@Box
        }

        val maxVal = dataPoints.maxOrNull() ?: 1.0
        val minVal = dataPoints.minOrNull() ?: 0.0
        val range = if (maxVal == minVal) 1.0 else (maxVal - minVal)

        val animationProgress by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800),
            label = "chartAnim"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val paddingX = 40f
            val paddingY = 40f
            val chartWidth = width - 2 * paddingX
            val chartHeight = height - 2 * paddingY

            // Draw Y Grid lines
            val gridLines = 3
            for (i in 0..gridLines) {
                val fraction = i.toFloat() / gridLines
                val y = paddingY + fraction * chartHeight
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    start = Offset(paddingX, y),
                    end = Offset(width - paddingX, y),
                    strokeWidth = 2f
                )
            }

            // Calculate point offsets
            val points = dataPoints.mapIndexed { index, value ->
                val x = if (dataPoints.size > 1) {
                    paddingX + (index.toFloat() / (dataPoints.size - 1)) * chartWidth
                } else {
                    paddingX + chartWidth / 2f
                }
                val rawY = paddingY + chartHeight - (((value - minVal) / range) * chartHeight).toFloat()
                // Apply animation adjustment
                val y = chartHeight + paddingY - (chartHeight + paddingY - rawY) * animationProgress
                Offset(x, y)
            }

            // Draw Bezier Curve
            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        val pPrev = points[i - 1]
                        val pCurr = points[i]
                        val controlX1 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                        val controlY1 = pPrev.y
                        val controlX2 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                        val controlY2 = pCurr.y
                        cubicTo(controlX1, controlY1, controlX2, controlY2, pCurr.x, pCurr.y)
                    }
                }

                // Draw solid line
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )

                // Draw alpha gradient under line
                val gradientPath = Path().apply {
                    addPath(path)
                    lineTo(points.last().x, height - paddingY)
                    lineTo(points.first().x, height - paddingY)
                    close()
                }

                drawPath(
                    path = gradientPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                // Circles on points
                points.forEach { point ->
                    drawCircle(
                        color = Color.White,
                        radius = 8f,
                        center = point
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 8f,
                        center = point,
                        style = Stroke(width = 4f)
                    )
                }
            } else if (points.size == 1) {
                drawCircle(
                    color = lineColor,
                    radius = 12f,
                    center = points[0]
                )
            }
        }
    }
}

@Composable
fun FinanceDonutDistribution(
    categoryMap: Map<String, Double>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    isMasked: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (categoryMap.isEmpty() || categoryMap.values.all { it == 0.0 }) {
            Text(
                text = "Belum ada distribusi data",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            return@Box
        }

        val total = categoryMap.values.sum()

        Canvas(modifier = Modifier.fillMaxSize(0.85f)) {
            val minDim = size.minDimension
            val strokeWidth = minDim * 0.22f
            val adjustedRadius = (minDim - strokeWidth) / 2f
            val centerOffset = Offset(size.width / 2f, size.height / 2f)

            if (isMasked) {
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    radius = adjustedRadius,
                    center = centerOffset,
                    style = Stroke(width = strokeWidth)
                )
                return@Canvas
            }

            var startAngle = -90f
            categoryMap.entries.forEachIndexed { index, entry ->
                val fraction = if (total > 0) entry.value / total else 0.0
                val sweepAngle = (fraction * 360f).toFloat()
                val color = colors[index % colors.size]

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = centerOffset - Offset(adjustedRadius, adjustedRadius),
                    size = Size(adjustedRadius * 2, adjustedRadius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                startAngle += sweepAngle
            }
        }

        // Inner Total Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "TOTAL",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = if (isMasked) "Rp •••••" else ScreenUtils.formatToIdr(total),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
