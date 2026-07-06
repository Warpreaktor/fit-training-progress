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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import ru.trainingapp.core.model.Tag
import ru.trainingapp.core.model.Workout
import ru.trainingapp.core.ui.component.EmptyState

@Composable
fun WorkoutListRoute(
    onOpenWorkout: (Long) -> Unit,
    onOpenExerciseCatalog: () -> Unit,
    onOpenWorkoutProgress: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: WorkoutListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutListScreen(
        uiState = uiState,
        onOpenWorkout = onOpenWorkout,
        onOpenExerciseCatalog = onOpenExerciseCatalog,
        onOpenWorkoutProgress = onOpenWorkoutProgress,
        onOpenSettings = onOpenSettings,
        onCreateWorkoutClick = viewModel::onCreateWorkoutClick,
        onArchiveWorkoutClick = viewModel::onArchiveWorkoutClick,
        onWorkoutNameChange = viewModel::onWorkoutNameChange,
        onWorkoutDescriptionChange = viewModel::onWorkoutDescriptionChange,
        onDismissEditor = viewModel::onDismissEditor,
        onSaveWorkoutClick = viewModel::onSaveWorkoutClick,
        onFilterTagClick = viewModel::onFilterTagClick,
        onClearFilterClick = viewModel::onClearFilterClick,
        onEditWorkoutTagsClick = viewModel::onEditWorkoutTagsClick,
        onDismissTagEditor = viewModel::onDismissTagEditor,
        onToggleTagSelection = viewModel::onToggleTagSelection,
        onNewTagNameChange = viewModel::onNewTagNameChange,
        onCreateTagClick = viewModel::onCreateTagClick,
        onSaveWorkoutTagsClick = viewModel::onSaveWorkoutTagsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    uiState: WorkoutListUiState,
    onOpenWorkout: (Long) -> Unit,
    onOpenExerciseCatalog: () -> Unit,
    onOpenWorkoutProgress: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onCreateWorkoutClick: () -> Unit,
    onArchiveWorkoutClick: (Long) -> Unit,
    onWorkoutNameChange: (String) -> Unit,
    onWorkoutDescriptionChange: (String) -> Unit,
    onDismissEditor: () -> Unit,
    onSaveWorkoutClick: () -> Unit,
    onFilterTagClick: (Long) -> Unit,
    onClearFilterClick: () -> Unit,
    onEditWorkoutTagsClick: (Workout) -> Unit,
    onDismissTagEditor: () -> Unit,
    onToggleTagSelection: (Long) -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onCreateTagClick: () -> Unit,
    onSaveWorkoutTagsClick: () -> Unit,
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

                Button(onClick = onOpenSettings) {
                    Text("#")
                }
            }

            if (uiState.allTags.isNotEmpty()) {
                TagFilterRow(
                    tags = uiState.allTags,
                    selectedTagIds = uiState.selectedFilterTagIds,
                    onTagClick = onFilterTagClick,
                    onClearClick = onClearFilterClick,
                )
            }

            if (uiState.workouts.isEmpty()) {
                val hasFilter = uiState.selectedFilterTagIds.isNotEmpty()

                EmptyState(
                    title = if (hasFilter) {
                        "Ничего не найдено"
                    } else {
                        "Пока нет тренировок"
                    },
                    message = if (hasFilter) {
                        "Под выбранные теги нет тренировок."
                    } else {
                        "Создай первую тренировку!"
                    },
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
                            onProgressClick = { onOpenWorkoutProgress(workout.id) },
                            onEditTagsClick = { onEditWorkoutTagsClick(workout) },
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

    if (uiState.tagEditor.isVisible) {
        WorkoutTagsDialog(
            tagEditor = uiState.tagEditor,
            allTags = uiState.allTags,
            onToggleTag = onToggleTagSelection,
            onNewTagNameChange = onNewTagNameChange,
            onCreateTagClick = onCreateTagClick,
            onDismiss = onDismissTagEditor,
            onSave = onSaveWorkoutTagsClick,
        )
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    onOpenClick: () -> Unit,
    onProgressClick: () -> Unit,
    onEditTagsClick: () -> Unit,
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

            if (workout.tags.isNotEmpty()) {
                WorkoutTagChipsRow(tags = workout.tags)
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
                    Text("Архив")
                }

                Spacer(modifier = Modifier.width(4.dp))

                TextButton(onClick = onEditTagsClick) {
                    Text("Теги")
                }

                Spacer(modifier = Modifier.width(4.dp))

                TextButton(onClick = onProgressClick) {
                    Text("Прогресс")
                }

                Spacer(modifier = Modifier.width(4.dp))

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

@Composable
private fun TagFilterRow(
    tags: List<Tag>,
    selectedTagIds: Set<Long>,
    onTagClick: (Long) -> Unit,
    onClearClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Фильтр по тегам",
            style = MaterialTheme.typography.labelLarge,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = selectedTagIds.isEmpty(),
                    onClick = onClearClick,
                    label = { Text("Все") },
                )
            }

            items(
                items = tags,
                key = { tag -> tag.id },
            ) { tag ->
                FilterChip(
                    selected = tag.id in selectedTagIds,
                    onClick = { onTagClick(tag.id) },
                    label = { Text("#${tag.name}") },
                )
            }
        }
    }
}

@Composable
private fun WorkoutTagChipsRow(
    tags: List<Tag>,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = tags,
            key = { tag -> tag.id },
        ) { tag ->
            AssistChip(
                onClick = {},
                label = { Text("#${tag.name}") },
            )
        }
    }
}

@Composable
private fun WorkoutTagsDialog(
    tagEditor: WorkoutTagEditorState,
    allTags: List<Tag>,
    onToggleTag: (Long) -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onCreateTagClick: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Теги: ${tagEditor.workoutName}")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (allTags.isEmpty()) {
                    Text(
                        text = "Тегов пока нет. Создай первый тег ниже.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(
                            items = allTags,
                            key = { tag -> tag.id },
                        ) { tag ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = tag.id in tagEditor.selectedTagIds,
                                    onCheckedChange = {
                                        onToggleTag(tag.id)
                                    },
                                )

                                Text(text = "#${tag.name}")
                            }
                        }
                    }
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = tagEditor.newTagName,
                    onValueChange = onNewTagNameChange,
                    label = { Text("Новый тег") },
                    isError = tagEditor.newTagNameError != null,
                    supportingText = {
                        tagEditor.newTagNameError?.let { Text(it) }
                    },
                    singleLine = true,
                )

                TextButton(
                    onClick = onCreateTagClick,
                ) {
                    Text("Добавить тег")
                }
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