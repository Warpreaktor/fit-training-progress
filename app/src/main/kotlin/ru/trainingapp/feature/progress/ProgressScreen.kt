package ru.trainingapp.feature.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.trainingapp.core.ui.component.EmptyState

@Composable
fun ProgressRoute(
    onBack: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProgressScreen(
        uiState = uiState,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    uiState: ProgressUiState,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.title)
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.points.isEmpty() -> {
                EmptyState(
                    title = uiState.emptyTitle,
                    message = uiState.emptyMessage,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            else -> {
                ProgressContent(
                    uiState = uiState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }
        }
    }
}

@Composable
private fun ProgressContent(
    uiState: ProgressUiState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ProgressChartCard(uiState = uiState)
        }

        item {
            Text(
                text = "Точки прогресса",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        items(
            items = uiState.points,
            key = { point -> point.dateKey },
        ) { point ->
            ListItem(
                headlineContent = {
                    Text(point.resultLabel)
                },
                supportingContent = {
                    Text(point.dateLabel)
                },
            )

            HorizontalDivider()
        }
    }
}

@Composable
private fun ProgressChartCard(
    uiState: ProgressUiState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = uiState.summaryTitle,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    ProgressSeriesLegend(series = uiState.series)
                }

                Text(
                    text = uiState.summaryValue,
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            ExerciseProgressLineChart(
                series = uiState.series,
                points = uiState.points,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
        }
    }
}

@Composable
private fun ProgressSeriesLegend(
    series: List<ProgressChartSeriesUi>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        series.forEachIndexed { index, item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = progressSeriesColor(index),
                            shape = CircleShape,
                        ),
                )

                Text(
                    text = "${item.title}: ${item.latestValueLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ExerciseProgressLineChart(
    series: List<ProgressChartSeriesUi>,
    points: List<ProgressChartPointUi>,
    modifier: Modifier = Modifier,
) {
    val seriesColors = series.mapIndexed { index, _ -> progressSeriesColor(index) }
    val axisColor = MaterialTheme.colorScheme.outline
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(
        modifier = modifier,
    ) {
        if (points.isEmpty() || series.isEmpty()) {
            return@Canvas
        }

        val left = 12.dp.toPx()
        val right = size.width - 12.dp.toPx()
        val top = 12.dp.toPx()
        val bottom = size.height - 24.dp.toPx()

        fun x(index: Int): Float {
            if (points.size == 1) {
                return (left + right) / 2f
            }

            val progress = index.toFloat() / points.lastIndex.toFloat()
            return left + ((right - left) * progress)
        }

        repeat(4) { index ->
            val gridY = top + ((bottom - top) / 3f * index)

            drawLine(
                color = gridColor,
                start = Offset(left, gridY),
                end = Offset(right, gridY),
                strokeWidth = 1.dp.toPx(),
            )
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

        series.forEachIndexed { seriesIndex, item ->
            val indexedValues = points.mapIndexedNotNull { index, point ->
                val value = point.valueBySeriesType(item.type) ?: return@mapIndexedNotNull null
                index to value
            }

            if (indexedValues.isEmpty()) {
                return@forEachIndexed
            }

            val minValue = indexedValues.minOf { (_, value) -> value }
            val maxValue = indexedValues.maxOf { (_, value) -> value }
            val isFlatLine = minValue == maxValue
            val valueRange = (maxValue - minValue).takeIf { range -> range > 0.0 } ?: 1.0

            fun y(value: Double): Float {
                if (isFlatLine) {
                    return (top + bottom) / 2f
                }

                val progress = ((value - minValue) / valueRange).toFloat()
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

@Composable
private fun progressSeriesColor(
    index: Int,
): Color {
    return when (index) {
        0 -> MaterialTheme.colorScheme.primary
        1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
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