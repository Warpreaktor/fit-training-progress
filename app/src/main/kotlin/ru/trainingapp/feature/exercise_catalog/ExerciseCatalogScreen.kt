package ru.trainingapp.feature.exercise_catalog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.ExerciseImage
import ru.trainingapp.core.model.Tag

@Composable
fun ExerciseCatalogRoute(
    onBack: () -> Unit,
    viewModel: ExerciseCatalogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        viewModel.onImagesSelected(
            uriStrings = uris.map { uri -> uri.toString() },
        )
    }

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
        onFilterTagClick = viewModel::onFilterTagClick,
        onClearFilterClick = viewModel::onClearFilterClick,
        onEditExerciseTagsClick = viewModel::onEditExerciseTagsClick,
        onDismissTagEditor = viewModel::onDismissTagEditor,
        onToggleTagSelection = viewModel::onToggleTagSelection,
        onNewTagNameChange = viewModel::onNewTagNameChange,
        onCreateTagClick = viewModel::onCreateTagClick,
        onSaveExerciseTagsClick = viewModel::onSaveExerciseTagsClick,
        onEditExerciseAlternativesClick = viewModel::onEditExerciseAlternativesClick,
        onDismissAlternativeEditor = viewModel::onDismissAlternativeEditor,
        onToggleAlternativeSelection = viewModel::onToggleAlternativeSelection,
        onSaveExerciseAlternativesClick = viewModel::onSaveExerciseAlternativesClick,
        onAddImagesClick = { exercise, reopenViewerAfterSelection ->
            viewModel.onPrepareAddImagesClick(
                exercise = exercise,
                reopenViewerAfterSelection = reopenViewerAfterSelection,
            )

            imagePickerLauncher.launch("image/*")
        },
        onOpenImageViewer = viewModel::onOpenImageViewer,
        onDismissImageViewer = viewModel::onDismissImageViewer,
        onSetCoverImageClick = viewModel::onSetCoverImageClick,
        onDeleteExerciseImageClick = viewModel::onDeleteExerciseImageClick,
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
    onFilterTagClick: (Long) -> Unit,
    onClearFilterClick: () -> Unit,
    onEditExerciseTagsClick: (ExerciseDefinition) -> Unit,
    onDismissTagEditor: () -> Unit,
    onToggleTagSelection: (Long) -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onCreateTagClick: () -> Unit,
    onSaveExerciseTagsClick: () -> Unit,
    onEditExerciseAlternativesClick: (ExerciseDefinition) -> Unit,
    onDismissAlternativeEditor: () -> Unit,
    onToggleAlternativeSelection: (Long) -> Unit,
    onSaveExerciseAlternativesClick: () -> Unit,
    onAddImagesClick: (ExerciseDefinition, Boolean) -> Unit,
    onOpenImageViewer: (Long) -> Unit,
    onDismissImageViewer: () -> Unit,
    onSetCoverImageClick: (Long, Long) -> Unit,
    onDeleteExerciseImageClick: (Long, Long) -> Unit,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.allTags.isNotEmpty()) {
                TagFilterRow(
                    tags = uiState.allTags,
                    selectedTagIds = uiState.selectedFilterTagIds,
                    onTagClick = onFilterTagClick,
                    onClearClick = onClearFilterClick,
                )
            }

            if (uiState.exercises.isEmpty()) {
                val hasFilter = uiState.selectedFilterTagIds.isNotEmpty()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (hasFilter) {
                            "Ничего не найдено"
                        } else {
                            "Пока нет упражнений"
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Text(
                        text = if (hasFilter) {
                            "Под выбранные теги нет упражнений."
                        } else {
                            "Добавь первое упражнение в справочник."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = uiState.exercises,
                        key = { it.id },
                    ) { exercise ->
                        ExerciseCatalogItem(
                            exercise = exercise,
                            onEditClick = { onEditExerciseClick(exercise) },
                            onEditTagsClick = { onEditExerciseTagsClick(exercise) },
                            onEditAlternativesClick = { onEditExerciseAlternativesClick(exercise) },
                            onArchiveClick = { onArchiveExerciseClick(exercise.id) },
                            onImageClick = {
                                if (exercise.images.isEmpty()) {
                                    onAddImagesClick(
                                        exercise,
                                        false,
                                    )
                                } else {
                                    onOpenImageViewer(exercise.id)
                                }
                            },
                        )
                    }
                }
            }

            val imageViewerExercise = uiState.imageViewer.exerciseDefinitionId
                ?.let { exerciseDefinitionId ->
                    uiState.allExercises.firstOrNull { exercise ->
                        exercise.id == exerciseDefinitionId
                    }
                }

            if (uiState.imageViewer.isVisible && imageViewerExercise != null) {
                ExerciseImageViewer(
                    exercise = imageViewerExercise,
                    onDismiss = onDismissImageViewer,
                    onAddImagesClick = {
                        onDismissImageViewer()

                        onAddImagesClick(
                            imageViewerExercise,
                            true,
                        )
                    },
                    onSetCoverClick = { imageId ->
                        onSetCoverImageClick(
                            imageViewerExercise.id,
                            imageId,
                        )
                    },
                    onDeleteClick = { imageId ->
                        if (imageViewerExercise.images.size == 1) {
                            onDismissImageViewer()
                        }

                        onDeleteExerciseImageClick(
                            imageViewerExercise.id,
                            imageId,
                        )
                    },
                )
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

    if (uiState.tagEditor.isVisible) {
        ExerciseTagsDialog(
            tagEditor = uiState.tagEditor,
            allTags = uiState.allTags,
            onToggleTag = onToggleTagSelection,
            onNewTagNameChange = onNewTagNameChange,
            onCreateTagClick = onCreateTagClick,
            onDismiss = onDismissTagEditor,
            onSave = onSaveExerciseTagsClick,
        )
    }
    if (uiState.alternativeEditor.isVisible) {
        ExerciseAlternativesDialog(
            alternativeEditor = uiState.alternativeEditor,
            allExercises = uiState.allExercises,
            onToggleAlternative = onToggleAlternativeSelection,
            onDismiss = onDismissAlternativeEditor,
            onSave = onSaveExerciseAlternativesClick,
        )
    }
}

@Composable
private fun ExerciseCatalogItem(
    exercise: ExerciseDefinition,
    onEditClick: () -> Unit,
    onEditTagsClick: () -> Unit,
    onEditAlternativesClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onImageClick: () -> Unit,
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

            exercise.description
                .takeIf { it.isNotBlank() }
                ?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

            ExerciseCoverImage(
                coverImageUri = exercise.images.firstOrNull { image -> image.isCover }?.uri
                    ?: exercise.images.firstOrNull()?.uri,
                onClick = onImageClick,
            )

            if (exercise.tags.isNotEmpty()) {
                TagChipsRow(tags = exercise.tags)
            }

            if (exercise.alternatives.isNotEmpty()) {
                Text(
                    text = "Альтернативы: ${
                        exercise.alternatives.joinToString(
                            separator = ", ",
                        ) { alternative -> alternative.name }
                    }",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onEditClick) {
                    Text("Изменить")
                }

                Spacer(modifier = Modifier.width(4.dp))

                TextButton(onClick = onEditTagsClick) {
                    Text("Теги")
                }

                Spacer(modifier = Modifier.width(4.dp))

                TextButton(onClick = onEditAlternativesClick) {
                    Text("Альт.")
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onArchiveClick) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "удалить упражненение"
                    )
                }
            }
        }
    }
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
private fun TagChipsRow(
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
private fun ExerciseTagsDialog(
    tagEditor: ExerciseTagEditorState,
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
            Text("Теги: ${tagEditor.exerciseName}")
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

@Composable
private fun ExerciseAlternativesDialog(
    alternativeEditor: ExerciseAlternativeEditorState,
    allExercises: List<ExerciseDefinition>,
    onToggleAlternative: (Long) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val availableAlternatives = allExercises.filter { exercise ->
        exercise.id != alternativeEditor.exerciseDefinitionId
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Альтернативы: ${alternativeEditor.exerciseName}")
        },
        text = {
            if (availableAlternatives.isEmpty()) {
                Text(
                    text = "Нет других упражнений. Сначала добавь ещё упражнения в справочник.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(
                        items = availableAlternatives,
                        key = { exercise -> exercise.id },
                    ) { exercise ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = exercise.id in alternativeEditor.selectedAlternativeExerciseDefinitionIds,
                                onCheckedChange = {
                                    onToggleAlternative(exercise.id)
                                },
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                )

                                exercise.description
                                    .takeIf { it.isNotBlank() }
                                    ?.let { description ->
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                            }
                        }
                    }
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

@Composable
private fun ExerciseImagesRow(
    images: List<ExerciseImage>,
    onSetCoverClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Картинки",
            style = MaterialTheme.typography.labelLarge,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = images,
                key = { image -> image.id },
            ) { image ->
                ExerciseImageItem(
                    image = image,
                    onSetCoverClick = { onSetCoverClick(image.id) },
                    onDeleteClick = { onDeleteClick(image.id) },
                )
            }
        }
    }
}

@Composable
private fun ExerciseImageItem(
    image: ExerciseImage,
    onSetCoverClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(12.dp)),
            ) {
                AsyncImage(
                    model = image.uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Text(
                text = if (image.isCover) {
                    "Обложка"
                } else {
                    "Фото"
                },
                style = MaterialTheme.typography.labelSmall,
            )

            TextButton(
                onClick = onSetCoverClick,
                enabled = !image.isCover,
            ) {
                Text("Обложка")
            }

            TextButton(onClick = onDeleteClick) {
                Text("Удалить")
            }
        }
    }
}

@Composable
private fun ExerciseCoverImage(
    coverImageUri: String?,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (coverImageUri != null) {
            AsyncImage(
                model = coverImageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = "Добавить изображение упражнения",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}