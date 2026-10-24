package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqFrequencies
import com.example.ui.theme.*

@Composable
fun EqCurveVisualizer(
    bands: List<Float>,
    onBandChange: (index: Int, newGain: Float) -> Unit,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true
) {
    val labels = EqFrequencies.labels
    var draggedBandIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EQ FREQUENCY RESPONSE",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Range: ±12 dB",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkBackground)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isInteractive) {
                            Modifier.pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val bandWidth = size.width / (bands.size + 1)
                                        val closest = bands.indices.minByOrNull { idx ->
                                            val x = bandWidth * (idx + 1)
                                            kotlin.math.abs(x - offset.x)
                                        }
                                        draggedBandIndex = closest
                                        closest?.let { idx ->
                                            val normalizedY = (offset.y / size.height).coerceIn(0f, 1f)
                                            val db = (1f - normalizedY) * 24f - 12f
                                            onBandChange(idx, (db * 10).toInt() / 10f)
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        draggedBandIndex?.let { idx ->
                                            change.consume()
                                            val normalizedY = (change.position.y / size.height).coerceIn(0f, 1f)
                                            val db = (1f - normalizedY) * 24f - 12f
                                            onBandChange(idx, (db * 10).toInt() / 10f)
                                        }
                                    },
                                    onDragEnd = { draggedBandIndex = null },
                                    onDragCancel = { draggedBandIndex = null }
                                )
                            }
                        } else Modifier
                    )
            ) {
                val w = size.width
                val h = size.height

                // Draw Grid Lines (+12, +6, 0, -6, -12 dB)
                val dbLevels = listOf(12f, 6f, 0f, -6f, -12f)
                dbLevels.forEach { db ->
                    val y = h * (1f - (db + 12f) / 24f)
                    val isZero = db == 0f
                    drawLine(
                        color = if (isZero) DarkBorder.copy(alpha = 0.8f) else DarkBorder.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = if (isZero) 1.5f else 1f,
                        pathEffect = if (!isZero) PathEffect.dashPathEffect(floatArrayOf(6f, 6f)) else null
                    )
                }

                // Compute points for spline
                val bandCount = bands.size
                val stepX = w / (bandCount + 1)
                val points = mutableListOf<Offset>()

                // Anchor left edge at first band height
                val firstY = h * (1f - (bands[0] + 12f) / 24f)
                points.add(Offset(0f, firstY))

                for (i in 0 until bandCount) {
                    val x = stepX * (i + 1)
                    val y = h * (1f - (bands[i] + 12f) / 24f)
                    points.add(Offset(x, y))
                }

                // Anchor right edge at last band height
                val lastY = h * (1f - (bands.last() + 12f) / 24f)
                points.add(Offset(w, lastY))

                // Build smooth cubic Bezier curve
                val curvePath = Path()
                val fillPath = Path()

                curvePath.moveTo(points[0].x, points[0].y)
                fillPath.moveTo(points[0].x, points[0].y)

                for (i in 0 until points.size - 1) {
                    val p0 = points[maxOf(i - 1, 0)]
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val p3 = points[minOf(i + 2, points.size - 1)]

                    val cp1x = p1.x + (p2.x - p0.x) / 6f
                    val cp1y = p1.y + (p2.y - p0.y) / 6f
                    val cp2x = p2.x - (p3.x - p1.x) / 6f
                    val cp2y = p2.y - (p3.y - p1.y) / 6f

                    curvePath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                    fillPath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                }

                fillPath.lineTo(w, h)
                fillPath.lineTo(0f, h)
                fillPath.close()

                // Draw gradient under curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.28f),
                            NeonCyanGlow.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )

                // Draw main curve line
                drawPath(
                    path = curvePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(NeonCyan, NeonPurple, NeonViolet)
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw control nodes
                for (i in 0 until bandCount) {
                    val pt = points[i + 1]
                    val isDragged = draggedBandIndex == i

                    // Outer halo
                    drawCircle(
                        color = (if (isDragged) NeonCyan else NeonCyanGlow).copy(alpha = if (isDragged) 0.5f else 0.25f),
                        radius = (if (isDragged) 10.dp else 6.dp).toPx(),
                        center = pt
                    )
                    // Inner node
                    drawCircle(
                        color = if (isDragged) Color.White else NeonCyan,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Frequency labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { idx, label ->
                val isSelected = draggedBandIndex == idx
                Text(
                    text = label,
                    fontSize = 9.sp,
                    color = if (isSelected) NeonCyan else TextSecondary,
                    modifier = Modifier.width(28.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
