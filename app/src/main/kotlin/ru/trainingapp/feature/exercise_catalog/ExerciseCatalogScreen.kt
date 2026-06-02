package ru.trainingapp.feature.exercise_catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
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
import ru.trainingapp.core.model.ExerciseDefinition

@Composable
fun ExerciseCatalogRoute(
    onBack: () -> Unit,
    viewModel: ExerciseCatalogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ExerciseCatalogScreen(
        uiState = uiState,
        onBack = onBack,
        onAddExerciseClick = viewModel::onAddExerciseClick,
        onEditExerciseClick = viewModel::onEditExerciseClick,
        onArchiveExerciseClick = viewModel::onArchiveExerciseClick,
        onEditorNameChange = viewModel::onEditorNameChange,
        onEditorDescriptionChange = viewModel::onEditorDescriptionChange,
        onDismissEditor = viewModel::onDismissEditor,
        onSaveExerciseClick = viewModel::onSaveExerciseClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCatalogScreen(
    uiState: ExerciseCatalogUiState,
    onBack: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onEditExerciseClick: (ExerciseDefinition) -> Unit,
    onArchiveExerciseClick: (Long) -> Unit,
    onEditorNameChange: (String) -> Unit,
    onEditorDescriptionChange: (String) -> Unit,
    onDismissEditor: () -> Unit,
    onSaveExerciseClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Справочник упражнений") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExerciseClick,
            ) {
                Text("Добавить")
            }
        },
    ) { padding ->
        if (uiState.exercises.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Пока нет упражнений",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Добавь первое упражнение в справочник.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = uiState.exercises,
                    key = { it.id },
                ) { exercise ->
                    ExerciseCatalogItem(
                        exercise = exercise,
                        onEditClick = { onEditExerciseClick(exercise) },
                        onArchiveClick = { onArchiveExerciseClick(exercise.id) },
                    )
                }
            }
        }
    }

    if (uiState.editor.isVisible) {
        ExerciseEditorDialog(
            editor = uiState.editor,
            onNameChange = onEditorNameChange,
            onDescriptionChange = onEditorDescriptionChange,
            onDismiss = onDismissEditor,
            onSave = onSaveExerciseClick,
        )
    }
}

@Composable
private fun ExerciseCatalogItem(
    exercise: ExerciseDefinition,
    onEditClick: () -> Unit,
    onArchiveClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onEditClick) {
                    Text("Изменить")
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = onArchiveClick) {
                    Text("Архивировать")
                }
            }
        }
    }
}

@Composable
private fun ExerciseEditorDialog(
    editor: ExerciseEditorState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (editor.isEditing) {
                    "Изменить упражнение"
                } else {
                    "Новое упражнение"
                }
            )
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
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}