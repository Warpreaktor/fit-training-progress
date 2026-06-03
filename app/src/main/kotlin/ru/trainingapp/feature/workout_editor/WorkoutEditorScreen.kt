package ru.trainingapp.feature.workout_editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.WorkoutExercise
import ru.trainingapp.core.model.WorkoutExerciseSet
import kotlin.collections.indexOfFirst

@Composable
fun WorkoutEditorRoute(
    workoutId: Long,
    onBack: () -> Unit,
    viewModel: WorkoutEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    WorkoutEditorScreen(
        uiState = uiState,
        onBack = onBack,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutEditorScreen(
    uiState: WorkoutEditorUiState,
    onBack: () -> Unit,
    onAction: (WorkoutEditorAction) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short,
        )

        onAction(WorkoutEditorAction.ErrorMessageShown)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.title.ifBlank { "Тренировка" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAction(WorkoutEditorAction.AddExerciseClick)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить упражнение",
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                uiState.exercises.isEmpty() -> {
                    EmptyWorkoutEditorContent(
                        onAddExerciseClick = {
                            onAction(WorkoutEditorAction.AddExerciseClick)
                        },
                    )
                }

                else -> {
                    WorkoutExerciseList(
                        exercises = uiState.exercises,
                        onAction = onAction,
                    )
                }
            }
        }

        if (uiState.isAddExerciseDialogVisible) {
            AddExerciseDialog(
                exercises = uiState.availableExercises,
                onDismiss = {
                    onAction(WorkoutEditorAction.DismissAddExerciseDialog)
                },
                onExerciseClick = { exerciseDefinitionId ->
                    onAction(
                        WorkoutEditorAction.ExerciseSelected(
                            exerciseDefinitionId = exerciseDefinitionId,
                        )
                    )
                },
            )
        }
    }
}

@Composable
private fun WorkoutExerciseList(
    exercises: List<WorkoutExercise>,
    onAction: (WorkoutEditorAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = exercises,
            key = { exercise -> exercise.id },
        ) { exercise ->
            val index = exercises.indexOfFirst { it.id == exercise.id }

            WorkoutExerciseCard(
                exercise = exercise,
                canMoveUp = index > 0,
                canMoveDown = index < exercises.lastIndex,
                onMoveUpClick = {
                    onAction(
                        WorkoutEditorAction.MoveExerciseUpClick(
                            workoutExerciseId = exercise.id,
                        )
                    )
                },
                onMoveDownClick = {
                    onAction(
                        WorkoutEditorAction.MoveExerciseDownClick(
                            workoutExerciseId = exercise.id,
                        )
                    )
                },
                onArchiveClick = {
                    onAction(
                        WorkoutEditorAction.ArchiveExerciseClick(
                            workoutExerciseId = exercise.id,
                        )
                    )
                },
                onAddSetClick = {
                    onAction(
                        WorkoutEditorAction.AddSetClick(
                            workoutExerciseId = exercise.id,
                        )
                    )
                },
                onRemoveSetClick = { setId ->
                    onAction(
                        WorkoutEditorAction.RemoveSetClick(
                            workoutExerciseSetId = setId,
                        )
                    )
                },
            )
        }
    }
}

@Composable
private fun WorkoutExerciseCard(
    exercise: WorkoutExercise,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onAddSetClick: () -> Unit,
    onRemoveSetClick: (Long) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = exercise.exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = "Подходов: ${exercise.sets.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(
                    onClick = onMoveUpClick,
                    enabled = canMoveUp,
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Поднять упражнение",
                    )
                }

                IconButton(
                    onClick = onMoveDownClick,
                    enabled = canMoveDown,
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Опустить упражнение",
                    )
                }

                IconButton(
                    onClick = onArchiveClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить упражнение из тренировки",
                    )
                }
            }

            if (exercise.sets.isEmpty()) {
                EmptySetsContent()
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    exercise.sets.forEach { set ->
                        WorkoutExerciseSetRow(
                            set = set,
                            onRemoveClick = {
                                onRemoveSetClick(set.id)
                            },
                        )
                    }
                }
            }

            TextButton(
                onClick = onAddSetClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )

                Text(
                    text = "Добавить подход",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun WorkoutExerciseSetRow(
    set: WorkoutExerciseSet,
    onRemoveClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    top = 10.dp,
                    end = 4.dp,
                    bottom = 10.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Подход ${set.setNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )

                Text(
                    text = buildSetDescription(set),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = onRemoveClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить подход",
                )
            }
        }
    }
}

@Composable
private fun EmptyWorkoutEditorContent(
    onAddExerciseClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "В тренировке пока нет упражнений",
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = "Добавь первое упражнение из справочника.",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            modifier = Modifier.padding(top = 20.dp),
            onClick = onAddExerciseClick,
        ) {
            Text("Добавить упражнение")
        }
    }
}

@Composable
private fun EmptySetsContent() {
    Text(
        text = "Подходов пока нет. Добавь первый подход.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AddExerciseDialog(
    exercises: List<ExerciseDefinition>,
    onDismiss: () -> Unit,
    onExerciseClick: (Long) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Добавить упражнение")
        },
        text = {
            if (exercises.isEmpty()) {
                Text(
                    text = "Справочник упражнений пуст. Сначала создай упражнение в справочнике.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(
                        items = exercises,
                        key = { exercise -> exercise.id },
                    ) { exercise ->
                        ExerciseDefinitionListItem(
                            exercise = exercise,
                            onClick = {
                                onExerciseClick(exercise.id)
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text("Отмена")
            }
        },
    )
}

@Composable
private fun ExerciseDefinitionListItem(
    exercise: ExerciseDefinition,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = exercise.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

private fun buildSetDescription(
    set: WorkoutExerciseSet,
): String {
    val reps = set.reps?.toString() ?: "—"
    val weight = buildWeightText(set)
    val duration = buildDurationText(set.durationSeconds)

    return "Повторы: $reps · Вес: $weight · Время: $duration"
}

private fun buildWeightText(
    set: WorkoutExerciseSet,
): String {
    val value = set.weightValue ?: return "—"
    val unit = set.weightUnit?.name.orEmpty()

    return if (unit.isBlank()) {
        value.toString()
    } else {
        "$value $unit"
    }
}

private fun buildDurationText(
    durationSeconds: Int?,
): String {
    val seconds = durationSeconds ?: return "—"

    return "${seconds}с"
}