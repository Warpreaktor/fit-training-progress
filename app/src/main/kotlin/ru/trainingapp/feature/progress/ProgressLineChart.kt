package ru.trainingapp.feature.progress

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun ProgressLineChart(
    series: List<ProgressChartSeriesUi>,
    points: List<ProgressChartPointUi>,
    modifier: Modifier = Modifier,
    seriesColor: @Composable (Int) -> Color,
) {
    val seriesColors = series.mapIndexed { index, _ -> seriesColor(index) }
    val axisColor = androidx.compose.material3.MaterialTheme.colorScheme.outline
    val gridColor = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
    val labelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        if (points.isEmpty() || series.isEmpty()) {
            return@Canvas
        }

        val hasRightAxis = series.size > 1
        val left = 44.dp.toPx()
        val right = size.width - if (hasRightAxis) 44.dp.toPx() else 12.dp.toPx()
        val top = 12.dp.toPx()
        val bottom = size.height - 34.dp.toPx()
        val tickCount = 4

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = labelColor.toArgb()
            textSize = 10.sp.toPx()
        }

        fun x(index: Int): Float {
            if (points.size == 1) {
                return (left + right) / 2f
            }

            val progress = index.toFloat() / points.lastIndex.toFloat()
            return left + ((right - left) * progress)
        }

        fun valuesFor(item: ProgressChartSeriesUi): List<Double> {
            return points.mapNotNull { point -> point.valueBySeriesType(item.type) }
        }

        fun scaleFor(item: ProgressChartSeriesUi): ChartScale? {
            val values = valuesFor(item)
            if (values.isEmpty()) return null

            val min = values.minOrNull() ?: return null
            val max = values.maxOrNull() ?: return null

            if (min == max) {
                val padding = when {
                    abs(min) >= 10.0 -> abs(min) * 0.1
                    else -> 1.0
                }
                return ChartScale(min = min - padding, max = max + padding)
            }

            val padding = (max - min) * 0.08
            return ChartScale(
                min = min - padding,
                max = max + padding,
            )
        }

        val scales = series.map { item -> scaleFor(item) }

        repeat(tickCount) { index ->
            val progress = index.toFloat() / (tickCount - 1).toFloat()
            val gridY = bottom - ((bottom - top) * progress)

            drawLine(
                color = gridColor,
                start = Offset(left, gridY),
                end = Offset(right, gridY),
                strokeWidth = 1.dp.toPx(),
            )

            scales.firstOrNull()?.let { scale ->
                val value = scale.min + ((scale.max - scale.min) * progress)
                labelPaint.textAlign = Paint.Align.RIGHT
                drawContext.canvas.nativeCanvas.drawText(
                    value.formatAxisValue(),
                    left - 6.dp.toPx(),
                    gridY + 3.dp.toPx(),
                    labelPaint,
                )
            }

            if (hasRightAxis) {
                scales.getOrNull(1)?.let { scale ->
                    val value = scale.min + ((scale.max - scale.min) * progress)
                    labelPaint.textAlign = Paint.Align.LEFT
                    drawContext.canvas.nativeCanvas.drawText(
                        value.formatAxisValue(),
                        right + 6.dp.toPx(),
                        gridY + 3.dp.toPx(),
                        labelPaint,
                    )
                }
            }
        }

        drawLine(
            color = axisColor,
            start = Offset(left, bottom),
            end = Offset(right, bottom),
            strokeWidth = 1.dp.toPx(),
        )

        drawLine(
            color = axisColor,
            start = Offset(left, top),
            end = Offset(left, bottom),
            strokeWidth = 1.dp.toPx(),
        )

        if (hasRightAxis) {
            drawLine(
                color = axisColor,
                start = Offset(right, top),
                end = Offset(right, bottom),
                strokeWidth = 1.dp.toPx(),
            )
        }

        val dateLabelIndexes = when (points.size) {
            1 -> listOf(0)
            2 -> listOf(0, 1)
            else -> listOf(0, points.lastIndex / 2, points.lastIndex).distinct()
        }

        dateLabelIndexes.forEachIndexed { labelIndex, pointIndex ->
            labelPaint.textAlign = when (labelIndex) {
                0 -> Paint.Align.LEFT
                dateLabelIndexes.lastIndex -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }

            val labelX = when (labelIndex) {
                0 -> left
                dateLabelIndexes.lastIndex -> right
                else -> x(pointIndex)
            }

            drawContext.canvas.nativeCanvas.drawText(
                points[pointIndex].dateLabel,
                labelX,
                size.height - 8.dp.toPx(),
                labelPaint,
            )
        }

        series.forEachIndexed { seriesIndex, item ->
            val scale = scales.getOrNull(seriesIndex) ?: return@forEachIndexed
            val indexedValues = points.mapIndexedNotNull { index, point ->
                val value = point.valueBySeriesType(item.type) ?: return@mapIndexedNotNull null
                index to value
            }

            fun y(value: Double): Float {
                val progress = ((value - scale.min) / (scale.max - scale.min)).toFloat()
                return bottom - ((bottom - top) * progress)
            }

            indexedValues.zipWithNext().forEach { pair ->
                drawLine(
                    color = seriesColors[seriesIndex],
                    start = Offset(
                        x = x(pair.first.first),
                        y = y(pair.first.second),
                    ),
                    end = Offset(
                        x = x(pair.second.first),
                        y = y(pair.second.second),
                    ),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            indexedValues.forEach { (index, value) ->
                drawCircle(
                    color = seriesColors[seriesIndex],
                    radius = 4.dp.toPx(),
                    center = Offset(
                        x = x(index),
                        y = y(value),
                    ),
                )
            }
        }
    }
}

private data class ChartScale(
    val min: Double,
    val max: Double,
)

private fun Double.formatAxisValue(): String {
    val rounded = roundToInt()
    return if (abs(this - rounded.toDouble()) < 0.05) {
        rounded.toString()
    } else {
        String.format("%.1f", this)
    }
}

private fun ProgressChartPointUi.valueBySeriesType(
    type: ProgressChartSeriesType,
): Double? {
    return when (type) {
        ProgressChartSeriesType.REPS -> repsValue
        ProgressChartSeriesType.WEIGHT -> weightValue
        ProgressChartSeriesType.DURATION -> durationValue
    }
}
