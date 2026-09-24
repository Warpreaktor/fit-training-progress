package ru.trainingapp.feature.progress

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.trainingapp.core.ui.component.EmptyState

@Composable
fun WorkoutProgressRoute(
    onBack: () -> Unit,
    viewModel: WorkoutProgressViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutProgressScreen(
        uiState = uiState,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutProgressScreen(
    uiState: WorkoutProgressUiState,
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

            uiState.exercises.isEmpty() && uiState.archivedExercises.isEmpty() -> {
                EmptyState(
                    title = uiState.emptyTitle,
                    message = uiState.emptyMessage,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            else -> {
                WorkoutProgressContent(
                    exercises = uiState.exercises,
                    archivedExercises = uiState.archivedExercises,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }
        }
    }
}

@Composable
private fun WorkoutProgressContent(
    exercises: List<WorkoutProgressExerciseUi>,
    archivedExercises: List<WorkoutProgressExerciseUi>,
    modifier: Modifier = Modifier,
) {
    var archivedExpanded by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = exercises,
            key = { exercise -> exercise.workoutExerciseId },
        ) { exercise ->
            WorkoutExerciseProgressCard(exercise = exercise)
        }

        if (archivedExercises.isNotEmpty()) {
            item(key = "archived_header") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = "Удалённые упражнения (${archivedExercises.size})",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = "Их прогресс сохранён, но они больше не входят в тренировку",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        TextButton(
                            onClick = { archivedExpanded = !archivedExpanded },
                        ) {
                            Text(if (archivedExpanded) "Скрыть" else "Показать")
                        }
                    }
                }
            }

            if (archivedExpanded) {
                items(
                    items = archivedExercises,
                    key = { exercise -> "archived_${exercise.workoutExerciseId}" },
                ) { exercise ->
                    WorkoutExerciseProgressCard(exercise = exercise)
                }
            }
        }
    }
}

@Composable
private fun WorkoutExerciseProgressCard(
    exercise: WorkoutProgressExerciseUi,
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
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = exercise.exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    WorkoutProgressSeriesLegend(series = exercise.series)
                }

                Text(
                    text = exercise.latestResultLabel,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            ProgressLineChart(
                series = exercise.series,
                points = exercise.points,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                seriesColor = ::workoutProgressSeriesColor,
            )

            HorizontalDivider()

            Text(
                text = "Точек: ${exercise.points.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WorkoutProgressSeriesLegend(
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
                            color = workoutProgressSeriesColor(index),
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
private fun workoutProgressSeriesColor(
    index: Int,
): Color {
    return when (index) {
        0 -> MaterialTheme.colorScheme.primary
        1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }
}
