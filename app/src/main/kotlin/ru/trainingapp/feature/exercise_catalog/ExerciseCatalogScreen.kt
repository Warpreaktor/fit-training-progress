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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.ExerciseImage
import ru.trainingapp.core.model.MuscleGroup
import ru.trainingapp.core.model.Tag
import ru.trainingapp.core.ui.component.CroppedExerciseImage

@Composable
fun ExerciseCatalogRoute(
    onBack: () -> Unit,
    viewModel: ExerciseCatalogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val exportExerciseLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        viewModel.onExportDocumentCreated(uri?.toString())
    }

    val importExerciseLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        viewModel.onImportDocumentSelected(uri?.toString())
    }

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
        onCopyExerciseClick = viewModel::onCopyExerciseClick,
        onEditExerciseWorkoutsClick = { exerciseDefinitionId ->
            viewModel.onEditExerciseWorkoutsClick(exerciseDefinitionId)
        },
        onArchiveExerciseClick = viewModel::onArchiveExerciseClick,
        onEditorNameChange = viewModel::onEditorNameChange,
        onEditorDescriptionChange = viewModel::onEditorDescriptionChange,
        onDismissEditor = viewModel::onDismissEditor,
        onSaveExerciseClick = viewModel::onSaveExerciseClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterTagClick = viewModel::onFilterTagClick,
        onClearFilterClick = viewModel::onClearFilterClick,
        onEditExerciseTagsClick = viewModel::onEditExerciseTagsClick,
        onDismissTagEditor = viewModel::onDismissTagEditor,
        onToggleTagSelection = viewModel::onToggleTagSelection,
        onNewTagNameChange = viewModel::onNewTagNameChange,
        onCreateTagClick = viewModel::onCreateTagClick,
        onSaveExerciseTagsClick = viewModel::onSaveExerciseTagsClick,
        onEditExerciseAlternativesClick = viewModel::onEditExerciseAlternativesClick,
        onEditExerciseMusclesClick = viewModel::onEditExerciseMusclesClick,
        onDismissAlternativeEditor = viewModel::onDismissAlternativeEditor,
        onToggleAlternativeSelection = viewModel::onToggleAlternativeSelection,
        onSaveExerciseAlternativesClick = viewModel::onSaveExerciseAlternativesClick,
        onDismissMuscleEditor = viewModel::onDismissMuscleEditor,
        onTogglePrimaryMuscleSelection = viewModel::onTogglePrimaryMuscleSelection,
        onToggleSecondaryMuscleSelection = viewModel::onToggleSecondaryMuscleSelection,
        onSaveExerciseMusclesClick = viewModel::onSaveExerciseMusclesClick,
        onAddImagesClick = { exercise, reopenViewerAfterSelection ->
            viewModel.onPrepareAddImagesClick(
                exercise = exercise,
                reopenViewerAfterSelection = reopenViewerAfterSelection,
            )

            imagePickerLauncher.launch("image/*")
        },
        onOpenImageViewer = viewModel::onOpenImageViewer,
        onDismissImageViewer = viewModel::onDismissImageViewer,
        onOpenImageCropEditor = viewModel::onOpenImageCropEditor,
        onDismissImageCropEditor = viewModel::onDismissImageCropEditor,
        onSaveImageCrop = viewModel::onSaveImageCrop,
        onSetCoverImageClick = viewModel::onSetCoverImageClick,
        onDeleteExerciseImageClick = viewModel::onDeleteExerciseImageClick,
        onExportExerciseClick = { exercise ->
            viewModel.onPrepareExportExercise(exercise.id)
            exportExerciseLauncher.launch(buildExercisePackFileName(exercise.name))
        },
        onImportExerciseClick = {
            importExerciseLauncher.launch(
                arrayOf(
                    "application/zip",
                    "application/octet-stream",
                    "*/*",
                )
            )
        },
        onTransferMessageShown = viewModel::onTransferMessageShown,
        onDismissWorkoutMembership = viewModel::onDismissWorkoutMembership,
        onWorkoutMembershipChange = viewModel::onWorkoutMembershipChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCatalogScreen(
    uiState: ExerciseCatalogUiState,
    onBack: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onEditExerciseClick: (ExerciseDefinition) -> Unit,
    onCopyExerciseClick: (ExerciseDefinition) -> Unit,
    onEditExerciseWorkoutsClick: (Long) -> Unit,
    onArchiveExerciseClick: (Long) -> Unit,
    onEditorNameChange: (String) -> Unit,
    onEditorDescriptionChange: (String) -> Unit,
    onDismissEditor: () -> Unit,
    onSaveExerciseClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterTagClick: (Long) -> Unit,
    onClearFilterClick: () -> Unit,
    onEditExerciseTagsClick: (ExerciseDefinition) -> Unit,
    onDismissTagEditor: () -> Unit,
    onToggleTagSelection: (Long) -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onCreateTagClick: () -> Unit,
    onSaveExerciseTagsClick: () -> Unit,
    onEditExerciseAlternativesClick: (ExerciseDefinition) -> Unit,
    onEditExerciseMusclesClick: (ExerciseDefinition) -> Unit,
    onDismissAlternativeEditor: () -> Unit,
    onToggleAlternativeSelection: (Long) -> Unit,
    onSaveExerciseAlternativesClick: () -> Unit,
    onDismissMuscleEditor: () -> Unit,
    onTogglePrimaryMuscleSelection: (MuscleGroup) -> Unit,
    onToggleSecondaryMuscleSelection: (MuscleGroup) -> Unit,
    onSaveExerciseMusclesClick: () -> Unit,
    onAddImagesClick: (ExerciseDefinition, Boolean) -> Unit,
    onOpenImageViewer: (Long) -> Unit,
    onDismissImageViewer: () -> Unit,
    onOpenImageCropEditor: (Long) -> Unit,
    onDismissImageCropEditor: () -> Unit,
    onSaveImageCrop: (Long, Long, Float, Float, Float) -> Unit,
    onSetCoverImageClick: (Long, Long) -> Unit,
    onDeleteExerciseImageClick: (Long, Long) -> Unit,
    onExportExerciseClick: (ExerciseDefinition) -> Unit,
    onImportExerciseClick: () -> Unit,
    onTransferMessageShown: () -> Unit,
    onDismissWorkoutMembership: () -> Unit,
    onWorkoutMembershipChange: (Long, Boolean) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isScreenMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.transfer.message) {
        val message = uiState.transfer.message ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(message)
        onTransferMessageShown()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Справочник упражнений") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isScreenMenuExpanded = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Действия со списком упражнений",
                        )
                    }

                    DropdownMenu(
                        expanded = isScreenMenuExpanded,
                        onDismissRequest = {
                            isScreenMenuExpanded = false
                        },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Создать упражнение") },
                            enabled = !uiState.transfer.isInProgress,
                            onClick = {
                                isScreenMenuExpanded = false
                                onAddExerciseClick()
                            },
                        )

                        DropdownMenuItem(
                            text = { Text("Загрузить упражнение") },
                            enabled = !uiState.transfer.isInProgress,
                            onClick = {
                                isScreenMenuExpanded = false
                                onImportExerciseClick()
                            },
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.transfer.isInProgress) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Поиск по названию") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                    )
                },
                trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Очистить поиск",
                            )
                        }
                    }
                } else {
                    null
                },
            )

            if (uiState.allTags.isNotEmpty()) {
                TagFilterRow(
                    tags = uiState.allTags,
                    selectedTagIds = uiState.selectedFilterTagIds,
                    onTagClick = onFilterTagClick,
                    onClearClick = onClearFilterClick,
                )
            }

            if (uiState.exercises.isEmpty()) {
                val hasFilter = uiState.selectedFilterTagIds.isNotEmpty() ||
                    uiState.searchQuery.isNotBlank()

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
                            "По текущему поиску и фильтрам упражнений нет."
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
                            onCopyClick = { onCopyExerciseClick(exercise) },
                            onWorkoutsClick = { onEditExerciseWorkoutsClick(exercise.id) },
                            onEditTagsClick = { onEditExerciseTagsClick(exercise) },
                            onEditAlternativesClick = { onEditExerciseAlternativesClick(exercise) },
                            onEditMusclesClick = { onEditExerciseMusclesClick(exercise) },
                            onArchiveClick = { onArchiveExerciseClick(exercise.id) },
                            onExportClick = { onExportExerciseClick(exercise) },
                            onImageClick = {
                                if (exercise.images.isEmpty()) {
                                    onAddImagesClick(
                                        exercise,
                                        true,
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
                    onEditCropClick = onOpenImageCropEditor,
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
            onWorkoutsClick = {
                uiState.editor.exerciseId?.let(onEditExerciseWorkoutsClick)
            },
            onMusclesClick = {
                val exerciseId = uiState.editor.exerciseId

                if (exerciseId != null) {
                    uiState.allExercises
                        .firstOrNull { exercise -> exercise.id == exerciseId }
                        ?.let(onEditExerciseMusclesClick)
                }
            },
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

    if (uiState.muscleEditor.isVisible) {
        ExerciseMusclesDialog(
            muscleEditor = uiState.muscleEditor,
            onTogglePrimaryMuscle = onTogglePrimaryMuscleSelection,
            onToggleSecondaryMuscle = onToggleSecondaryMuscleSelection,
            onDismiss = onDismissMuscleEditor,
            onSave = onSaveExerciseMusclesClick,
        )
    }

    if (uiState.workoutMembership.isVisible) {
        ExerciseWorkoutMembershipDialog(
            state = uiState.workoutMembership,
            onToggleWorkout = onWorkoutMembershipChange,
            onDismiss = onDismissWorkoutMembership,
        )
    }

    val cropEditorTarget = uiState.imageCropEditor.imageId
        ?.let { imageId ->
            uiState.allExercises.firstNotNullOfOrNull { exercise ->
                exercise.images
                    .firstOrNull { image -> image.id == imageId }
                    ?.let { image -> exercise to image }
            }
        }

    if (uiState.imageCropEditor.isVisible && cropEditorTarget != null) {
        val (exercise, image) = cropEditorTarget

        ExerciseImageCropEditor(
            image = image,
            exerciseName = exercise.name,
            onDismiss = onDismissImageCropEditor,
            onSave = { cropZoom, cropOffsetX, cropOffsetY ->
                onSaveImageCrop(
                    exercise.id,
                    image.id,
                    cropZoom,
                    cropOffsetX,
                    cropOffsetY,
                )
            },
        )
    }
}

@Composable
private fun ExerciseCatalogItem(
    exercise: ExerciseDefinition,
    onEditClick: () -> Unit,
    onCopyClick: () -> Unit,
    onWorkoutsClick: () -> Unit,
    onEditTagsClick: () -> Unit,
    onEditAlternativesClick: () -> Unit,
    onEditMusclesClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onExportClick: () -> Unit,
    onImageClick: () -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = exercise.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )

                Box {
                    IconButton(
                        onClick = {
                            isMenuExpanded = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Действия с упражнением",
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = {
                            isMenuExpanded = false
                        },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Изменить") },
                            onClick = {
                                isMenuExpanded = false
                                onEditClick()
                            },
                        )

                        DropdownMenuItem(
                            text = { Text("Копировать") },
                            onClick = {
                                isMenuExpanded = false
                                onCopyClick()
                            },
                        )

                        DropdownMenuItem(
                            text = { Text("Теги") },
                            onClick = {
                                isMenuExpanded = false
                                onEditTagsClick()
                            },
                        )

                        DropdownMenuItem(
                            text = { Text("Мышцы") },
                            onClick = {
                                isMenuExpanded = false
                                onEditMusclesClick()
                            },
                        )

                        DropdownMenuItem(
                            text = { Text("Альтернативы") },
                            onClick = {
                                isMenuExpanded = false
                                onEditAlternativesClick()
                            },
                        )

                        DropdownMenuItem(
                            text = { Text("Выгрузить") },
                            onClick = {
                                isMenuExpanded = false
                                onExportClick()
                            },
                        )

                        DropdownMenuItem(
                            text = { Text("В архив") },
                            onClick = {
                                isMenuExpanded = false
                                onArchiveClick()
                            },
                        )
                    }
                }
            }

            exercise.description
                .takeIf { it.isNotBlank() }
                ?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

            ExerciseCoverImage(
                coverImage = exercise.images.firstOrNull { image -> image.isCover }
                    ?: exercise.images.firstOrNull(),
                onClick = onImageClick,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            if (exercise.tags.isNotEmpty()) {
                TagChipsRow(tags = exercise.tags)
            }

            if (exercise.muscles.isNotEmpty()) {
                MuscleChipsRow(
                    label = "Основная нагрузка",
                    muscles = exercise.muscles,
                )
            }

            if (exercise.secondaryMuscles.isNotEmpty()) {
                MuscleChipsRow(
                    label = "Доп. нагрузка",
                    muscles = exercise.secondaryMuscles,
                )
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

            TextButton(
                onClick = onWorkoutsClick,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Тренировки")
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
private fun MuscleChipsRow(
    label: String,
    muscles: List<MuscleGroup>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = muscles,
                key = { muscleGroup -> muscleGroup.code },
            ) { muscleGroup ->
                AssistChip(
                    onClick = {},
                    label = { Text(muscleGroup.displayName) },
                )
            }
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
    onWorkoutsClick: () -> Unit,
    onMusclesClick: () -> Unit,
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

                if (editor.isCopying) {
                    ExerciseCopyTemplateSummary(editor = editor)
                }

                if (editor.isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onMusclesClick) {
                            Text("Мышцы")
                        }

                        TextButton(onClick = onWorkoutsClick) {
                            Text("Тренировки")
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
private fun ExerciseCopyTemplateSummary(
    editor: ExerciseEditorState,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Шаблон: ${editor.templateExerciseName}",
            style = MaterialTheme.typography.labelLarge,
        )

        editor.templateTagNames
            .takeIf { names -> names.isNotEmpty() }
            ?.let { names ->
                Text(
                    text = "Теги: ${names.joinToString { name -> "#$name" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

        editor.templatePrimaryMuscles
            .takeIf { muscles -> muscles.isNotEmpty() }
            ?.let { muscles ->
                Text(
                    text = "Основные мышцы: ${muscles.joinToString { muscle -> muscle.displayName }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

        editor.templateSecondaryMuscles
            .takeIf { muscles -> muscles.isNotEmpty() }
            ?.let { muscles ->
                Text(
                    text = "Доп. мышцы: ${muscles.joinToString { muscle -> muscle.displayName }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

        editor.templateAlternativeNames
            .takeIf { names -> names.isNotEmpty() }
            ?.let { names ->
                Text(
                    text = "Альтернативы: ${names.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

        if (editor.templateImagesCount > 0) {
            Text(
                text = "Изображения: ${editor.templateImagesCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = "Эти данные будут скопированы при сохранении.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExerciseMusclesDialog(
    muscleEditor: ExerciseMuscleEditorState,
    onTogglePrimaryMuscle: (MuscleGroup) -> Unit,
    onToggleSecondaryMuscle: (MuscleGroup) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Мышцы: ${muscleEditor.exerciseName}")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Основная нагрузка учитывается за 1 балл, дополнительная — за 0,5 балла.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Основная",
                        modifier = Modifier.width(82.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = "Доп.",
                        modifier = Modifier.width(60.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(
                        items = MuscleGroup.entries,
                        key = { muscleGroup -> muscleGroup.code },
                    ) { muscleGroup ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = muscleGroup.displayName,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            Box(
                                modifier = Modifier.width(82.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Checkbox(
                                    checked = muscleGroup in muscleEditor.selectedPrimaryMuscleGroups,
                                    onCheckedChange = { onTogglePrimaryMuscle(muscleGroup) },
                                )
                            }

                            Box(
                                modifier = Modifier.width(60.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Checkbox(
                                    checked = muscleGroup in muscleEditor.selectedSecondaryMuscleGroups,
                                    onCheckedChange = { onToggleSecondaryMuscle(muscleGroup) },
                                )
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
private fun ExerciseWorkoutMembershipDialog(
    state: ExerciseWorkoutMembershipState,
    onToggleWorkout: (Long, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Тренировки")
        },
        text = {
            if (state.workouts.isEmpty()) {
                Text(
                    text = "Пока нет тренировок",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(
                        items = state.workouts,
                        key = { workout -> workout.id },
                    ) { workout ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val isSelected = workout.id in state.selectedWorkoutIds

                            Checkbox(
                                checked = isSelected,
                                enabled = workout.id !in state.pendingWorkoutIds,
                                onCheckedChange = { checked ->
                                    onToggleWorkout(workout.id, checked)
                                },
                            )

                            Text(
                                text = workout.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
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
                CroppedExerciseImage(
                    uri = image.uri,
                    cropZoom = image.cropZoom,
                    cropOffsetX = image.cropOffsetX,
                    cropOffsetY = image.cropOffsetY,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
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
    coverImage: ExerciseImage?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(112.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (coverImage != null) {
            CroppedExerciseImage(
                uri = coverImage.uri,
                cropZoom = coverImage.cropZoom,
                cropOffsetX = coverImage.cropOffsetX,
                cropOffsetY = coverImage.cropOffsetY,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
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

private fun buildExercisePackFileName(exerciseName: String): String {
    val safeName = exerciseName
        .replace(Regex("[\\/:*?\"<>|]"), "_")
        .trim()
        .ifBlank { "exercise" }

    return "$safeName.zip"
}
