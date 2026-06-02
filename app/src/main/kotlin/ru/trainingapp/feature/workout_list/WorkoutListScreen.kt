package ru.trainingapp.feature.workout_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.trainingapp.core.model.Workout
import ru.trainingapp.core.ui.component.EmptyState

@Composable
fun WorkoutListRoute(
    onOpenWorkout: (Long) -> Unit,
    onOpenExerciseCatalog: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: WorkoutListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutListScreen(
        uiState = uiState,
        onOpenWorkout = onOpenWorkout,
        onOpenExerciseCatalog = onOpenExerciseCatalog,
        onOpenProgress = onOpenProgress,
        onOpenSettings = onOpenSettings,
        onCreateWorkoutClick = viewModel::onCreateWorkoutClick,
        onArchiveWorkoutClick = viewModel::onArchiveWorkoutClick,
        onWorkoutNameChange = viewModel::onWorkoutNameChange,
        onWorkoutDescriptionChange = viewModel::onWorkoutDescriptionChange,
        onDismissEditor = viewModel::onDismissEditor,
        onSaveWorkoutClick = viewModel::onSaveWorkoutClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    uiState: WorkoutListUiState,
    onOpenWorkout: (Long) -> Unit,
    onOpenExerciseCatalog: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenSettings: () -> Unit,
    onCreateWorkoutClick: () -> Unit,
    onArchiveWorkoutClick: (Long) -> Unit,
    onWorkoutNameChange: (String) -> Unit,
    onWorkoutDescriptionChange: (String) -> Unit,
    onDismissEditor: () -> Unit,
    onSaveWorkoutClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Тренировки") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateWorkoutClick,
            ) {
                Text("Создать")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenExerciseCatalog) {
                    Text("Упражнения")
                }

                Button(onClick = onOpenProgress) {
                    Text("Прогресс")
                }

                Button(onClick = onOpenSettings) {
                    Text("Настройки")
                }
            }

            if (uiState.workouts.isEmpty()) {
                EmptyState(
                    title = "Пока нет тренировок",
                    message = "Создай первую тренировку!",
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = uiState.workouts,
                        key = { it.id },
                    ) { workout ->
                        WorkoutCard(
                            workout = workout,
                            onOpenClick = { onOpenWorkout(workout.id) },
                            onArchiveClick = { onArchiveWorkoutClick(workout.id) },
                        )
                    }
                }
            }
        }
    }

    if (uiState.editor.isVisible) {
        CreateWorkoutDialog(
            editor = uiState.editor,
            onNameChange = onWorkoutNameChange,
            onDescriptionChange = onWorkoutDescriptionChange,
            onDismiss = onDismissEditor,
            onSave = onSaveWorkoutClick,
        )
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    onOpenClick: () -> Unit,
    onArchiveClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = workout.name,
                style = MaterialTheme.typography.titleMedium,
            )

            workout.description
                ?.takeIf { it.isNotBlank() }
                ?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("${workout.exercisesCount} упражнений") },
                )

                AssistChip(
                    onClick = {},
                    label = { Text("${workout.checkedExercisesCount} отмечено") },
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onArchiveClick) {
                    Text("Архивировать")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = onOpenClick) {
                    Text("Открыть")
                }
            }
        }
    }
}

@Composable
private fun CreateWorkoutDialog(
    editor: WorkoutEditorState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Новая тренировка")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editor.name,
                    onValueChange = onNameChange,
                    label = { Text("Название") },
                    isError = editor.nameError != null,
                    supportingText = {
                        editor.nameError?.let { Text(it) }
                    },
                    singleLine = true,
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editor.description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Описание") },
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}