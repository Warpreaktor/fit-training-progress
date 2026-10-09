package ru.trainingapp.feature.workout_editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.ExerciseDefinition
import kotlin.collections.indexOfFirst

/**
 * Точка входа Compose-экрана редактора тренировки.
 *
 * Связывает UI редактора с [WorkoutEditorViewModel], наблюдает состояние экрана,
 * передаёт пользовательские действия во ViewModel и обрабатывает события
 * жизненного цикла и навигации, при которых требуется сохранить прогресс.
 *
 * Остальные функции файла формируют интерфейс редактирования тренировки:
 * список упражнений, подходы, поля ввода и диалоги выбора.
 */
@Composable
fun WorkoutEditorRoute(
    workoutId: Long,
    onBack: () -> Unit,
    onOpenExerciseProgress: (Long) -> Unit,
    viewModel: WorkoutEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.commitPendingProgress()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    WorkoutEditorScreen(
        uiState = uiState,
        onBack = {
            viewModel.commitPendingProgressAndThen(onBack)
        },
        onOpenExerciseProgress = { workoutExerciseId ->
            viewModel.commitPendingProgressAndThen {
                onOpenExerciseProgress(workoutExerciseId)
            }
        },
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutEditorScreen(
    uiState: WorkoutEditorUiState,
    onBack: () -> Unit,
    onOpenExerciseProgress: (Long) -> Unit,
    onAction: (WorkoutEditorAction) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedExerciseIds by rememberSaveable {
        mutableStateOf(emptyList<Long>())
    }
    var isCreateSectionDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }

    val selectedIds = selectedExerciseIds.toSet()
    val isSelectionMode = selectedExerciseIds.isNotEmpty()

    LaunchedEffect(uiState.exercises.map { exercise -> exercise.id }) {
        val availableIds = uiState.exercises.mapTo(mutableSetOf()) { exercise -> exercise.id }
        selectedExerciseIds = selectedExerciseIds.filter { id -> id in availableIds }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short,
        )

        onAction(WorkoutEditorAction.ErrorMessageShown)
    }

    fun toggleSelection(workoutExerciseId: Long) {
        selectedExerciseIds = if (workoutExerciseId in selectedIds) {
            selectedExerciseIds - workoutExerciseId
        } else {
            selectedExerciseIds + workoutExerciseId
        }
    }

    BackHandler {
        if (isSelectionMode) {
            selectedExerciseIds = emptyList()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isSelectionMode) {
                            "Выбрано: ${selectedExerciseIds.size}"
                        } else {
                            uiState.title.ifBlank { "Тренировка" }
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSelectionMode) {
                                selectedExerciseIds = emptyList()
                            } else {
                                onBack()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = if (isSelectionMode) {
                                Icons.Default.Close
                            } else {
                                Icons.AutoMirrored.Default.ArrowBack
                            },
                            contentDescription = if (isSelectionMode) {
                                "Отменить выбор"
                            } else {
                                "Назад"
                            },
                        )
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        TextButton(
                            onClick = {
                                isCreateSectionDialogVisible = true
                            },
                        ) {
                            Text("Создать секцию")
                        }
                    } else {
                        TextButton(
                            onClick = {
                                onAction(WorkoutEditorAction.ResetCheckmarksClick)
                            },
                            enabled = uiState.exercises.any { exercise -> exercise.isChecked },
                        ) {
                            Text("Снять галки")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
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
                        uiState = uiState,
                        selectedExerciseIds = selectedIds,
                        onToggleSelection = ::toggleSelection,
                        onOpenExerciseProgress = onOpenExerciseProgress,
                        onAction = onAction,
                    )
                }
            }
        }

        if (uiState.isAddExerciseDialogVisible) {
            AddExerciseDialog(
                exercises = uiState.availableExercises,
                searchQuery = uiState.addExerciseSearchQuery,
                onSearchQueryChange = { query ->
                    onAction(
                        WorkoutEditorAction.AddExerciseSearchQueryChanged(
                            query = query,
                        )
                    )
                },
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

        if (isCreateSectionDialogVisible) {
            SectionNameDialog(
                title = "Новая секция",
                initialName = "",
                confirmText = "Создать",
                onDismiss = {
                    isCreateSectionDialogVisible = false
                },
                onConfirm = { name ->
                    isCreateSectionDialogVisible = false
                    onAction(
                        WorkoutEditorAction.CreateSection(
                            workoutExerciseIds = selectedIds,
                            name = name,
                        )
                    )
                    selectedExerciseIds = emptyList()
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkoutExerciseList(
    uiState: WorkoutEditorUiState,
    selectedExerciseIds: Set<Long>,
    onToggleSelection: (Long) -> Unit,
    onOpenExerciseProgress: (Long) -> Unit,
    onAction: (WorkoutEditorAction) -> Unit,
) {
    val exercises = uiState.exercises
    val listState = rememberLazyListState()
    val showCompactHeader by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }
    val elapsedMillis = rememberWorkoutElapsedMillis(
        timerStartedAt = uiState.timerStartedAt,
        timerElapsedMillis = uiState.timerElapsedMillis,
    )
    val isTimerRunning = uiState.timerStartedAt != null
    val isSelectionMode = selectedExerciseIds.isNotEmpty()

    var isTargetDurationDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var collapsedSectionIds by rememberSaveable {
        mutableStateOf(emptyList<String>())
    }
    var renameSectionId by rememberSaveable {
        mutableStateOf<String?>(null)
    }
    var renameSectionName by rememberSaveable {
        mutableStateOf("")
    }
    var draggingExerciseId by remember {
        mutableStateOf<Long?>(null)
    }
    var dragOffsetY by remember {
        mutableStateOf(0f)
    }
    var dragTarget by remember {
        mutableStateOf<ExerciseSectionDropTarget?>(null)
    }

    val collapsedSections = collapsedSectionIds.toSet()
    val rows = remember(exercises, collapsedSections) {
        buildWorkoutListRows(
            exercises = exercises,
            collapsedSectionIds = collapsedSections,
        )
    }
    val sectionsById = remember(exercises) {
        exercises
            .filter { exercise -> exercise.sectionId != null }
            .groupBy { exercise -> requireNotNull(exercise.sectionId) }
            .mapValues { (sectionId, sectionExercises) ->
                WorkoutSectionUi(
                    id = sectionId,
                    name = sectionExercises.first().sectionName.orEmpty().ifBlank { "Секция" },
                    exercises = sectionExercises.sortedBy { exercise -> exercise.sortOrder },
                )
            }
    }

    fun updateDragTarget(workoutExerciseId: Long, deltaY: Float) {
        dragOffsetY += deltaY

        val draggedKey = exerciseListKey(workoutExerciseId)
        val draggedInfo = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> item.key == draggedKey }
            ?: return

        val draggedCenterY = draggedInfo.offset + draggedInfo.size / 2f + dragOffsetY
        val hoveredItem = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                item.key != draggedKey &&
                    draggedCenterY >= item.offset &&
                    draggedCenterY <= item.offset + item.size
            }

        dragTarget = when (val key = hoveredItem?.key as? String) {
            null -> null
            else -> when {
                key.startsWith(SECTION_KEY_PREFIX) -> {
                    val sectionId = key.removePrefix(SECTION_KEY_PREFIX)
                    sectionsById[sectionId]?.let { section ->
                        ExerciseSectionDropTarget(
                            sectionId = section.id,
                            sectionName = section.name,
                        )
                    }
                }

                key.startsWith(EXERCISE_KEY_PREFIX) -> {
                    val targetExerciseId = key
                        .removePrefix(EXERCISE_KEY_PREFIX)
                        .toLongOrNull()
                    val targetExercise = exercises
                        .firstOrNull { exercise -> exercise.id == targetExerciseId }

                    targetExercise?.let { exercise ->
                        ExerciseSectionDropTarget(
                            sectionId = exercise.sectionId,
                            sectionName = exercise.sectionName,
                        )
                    }
                }

                else -> null
            }
        }
    }

    fun finishDrag() {
        val workoutExerciseId = draggingExerciseId
        val target = dragTarget

        if (workoutExerciseId != null && target != null) {
            val exercise = exercises.firstOrNull { item -> item.id == workoutExerciseId }
            if (
                exercise != null &&
                (exercise.sectionId != target.sectionId || exercise.sectionName != target.sectionName)
            ) {
                onAction(
                    WorkoutEditorAction.MoveExerciseToSection(
                        workoutExerciseId = workoutExerciseId,
                        sectionId = target.sectionId,
                        sectionName = target.sectionName,
                    )
                )
            }
        }

        draggingExerciseId = null
        dragOffsetY = 0f
        dragTarget = null
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "workout-progress-header") {
            WorkoutProgressHeader(
                completedExercises = exercises.count { exercise -> exercise.isChecked },
                totalExercises = exercises.size,
                targetDurationMinutes = uiState.targetDurationMinutes,
                elapsedMillis = elapsedMillis,
                isTimerRunning = isTimerRunning,
                isTimerFinished = uiState.timerIsFinished,
                onTargetDurationClick = {
                    isTargetDurationDialogVisible = true
                },
                onStartClick = {
                    onAction(WorkoutEditorAction.StartWorkoutClick)
                },
                onPauseClick = {
                    onAction(WorkoutEditorAction.PauseWorkoutClick)
                },
                onFinishClick = {
                    onAction(WorkoutEditorAction.FinishWorkoutClick)
                },
            )
        }

        stickyHeader(key = "workout-progress-sticky-header") {
            if (showCompactHeader) {
                CompactWorkoutProgressHeader(
                    completedExercises = exercises.count { exercise -> exercise.isChecked },
                    totalExercises = exercises.size,
                    elapsedMillis = elapsedMillis,
                    targetDurationMinutes = uiState.targetDurationMinutes,
                    isTimerRunning = isTimerRunning,
                    isTimerFinished = uiState.timerIsFinished,
                    onStartClick = {
                        onAction(WorkoutEditorAction.StartWorkoutClick)
                    },
                )
            }
        }

        items(
            items = rows,
            key = { row -> row.key },
        ) { row ->
            when (row) {
                is WorkoutListRow.SectionHeader -> {
                    WorkoutSectionHeader(
                        section = row.section,
                        isCollapsed = row.section.id in collapsedSections,
                        isDropTarget = dragTarget?.sectionId == row.section.id,
                        onToggleCollapsed = {
                            collapsedSectionIds = if (row.section.id in collapsedSections) {
                                collapsedSectionIds - row.section.id
                            } else {
                                collapsedSectionIds + row.section.id
                            }
                        },
                        onRename = {
                            renameSectionId = row.section.id
                            renameSectionName = row.section.name
                        },
                        onRemove = {
                            onAction(
                                WorkoutEditorAction.RemoveSection(
                                    sectionId = row.section.id,
                                )
                            )
                        },
                    )
                }

                is WorkoutListRow.Exercise -> {
                    val exercise = row.exercise
                    val index = exercises.indexOfFirst { item -> item.id == exercise.id }
                    val previousExercise = exercises.getOrNull(index - 1)
                    val nextExercise = exercises.getOrNull(index + 1)
                    val isDragging = draggingExerciseId == exercise.id
                    val canMoveUp = previousExercise != null && previousExercise.sectionId == exercise.sectionId
                    val canMoveDown = nextExercise != null && nextExercise.sectionId == exercise.sectionId

                    var isExpanded by rememberSaveable(exercise.id) {
                        mutableStateOf(false)
                    }

                    WorkoutExerciseCard(
                        exercise = exercise,
                        isExpanded = isExpanded,
                        isSelected = exercise.id in selectedExerciseIds,
                        isSelectionMode = isSelectionMode,
                        dragTranslationY = if (isDragging) dragOffsetY else 0f,
                        onExerciseClick = {
                            if (isSelectionMode) {
                                onToggleSelection(exercise.id)
                            } else {
                                isExpanded = !isExpanded
                            }
                        },
                        onExerciseLongClick = {
                            onToggleSelection(exercise.id)
                        },
                        onDragStart = {
                            draggingExerciseId = exercise.id
                            dragOffsetY = 0f
                            dragTarget = null
                        },
                        onDragDelta = { deltaY ->
                            updateDragTarget(
                                workoutExerciseId = exercise.id,
                                deltaY = deltaY,
                            )
                        },
                        onDragEnd = ::finishDrag,
                        canMoveUp = canMoveUp,
                        canMoveDown = canMoveDown,
                        onMoveUpClick = {
                            onAction(
                                WorkoutEditorAction.MoveExerciseUpClick(
                                    workoutExerciseId = exercise.id,
                                )
                            )
                        },
                        onOpenProgressClick = {
                            onOpenExerciseProgress(exercise.id)
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
                        onAction = onAction,
                    )
                }
            }
        }
    }

    if (isTargetDurationDialogVisible) {
        TargetDurationDialog(
            currentMinutes = uiState.targetDurationMinutes,
            onDismiss = {
                isTargetDurationDialogVisible = false
            },
            onSave = { minutes ->
                isTargetDurationDialogVisible = false
                onAction(
                    WorkoutEditorAction.TargetDurationChanged(
                        minutes = minutes,
                    )
                )
            },
        )
    }

    val currentRenameSectionId = renameSectionId
    if (currentRenameSectionId != null) {
        SectionNameDialog(
            title = "Переименовать секцию",
            initialName = renameSectionName,
            confirmText = "Сохранить",
            onDismiss = {
                renameSectionId = null
            },
            onConfirm = { name ->
                renameSectionId = null
                onAction(
                    WorkoutEditorAction.RenameSection(
                        sectionId = currentRenameSectionId,
                        name = name,
                    )
                )
            },
        )
    }
}

private const val SECTION_KEY_PREFIX = "section:"
private const val EXERCISE_KEY_PREFIX = "exercise:"

private data class WorkoutSectionUi(
    val id: String,
    val name: String,
    val exercises: List<WorkoutExerciseUi>,
)

private sealed interface WorkoutListRow {
    val key: String

    data class SectionHeader(
        val section: WorkoutSectionUi,
    ) : WorkoutListRow {
        override val key: String = SECTION_KEY_PREFIX + section.id
    }

    data class Exercise(
        val exercise: WorkoutExerciseUi,
    ) : WorkoutListRow {
        override val key: String = exerciseListKey(exercise.id)
    }
}

private data class ExerciseSectionDropTarget(
    val sectionId: String?,
    val sectionName: String?,
)

private fun exerciseListKey(workoutExerciseId: Long): String {
    return EXERCISE_KEY_PREFIX + workoutExerciseId
}

private fun buildWorkoutListRows(
    exercises: List<WorkoutExerciseUi>,
    collapsedSectionIds: Set<String>,
): List<WorkoutListRow> {
    val exercisesBySectionId = exercises
        .filter { exercise -> exercise.sectionId != null }
        .groupBy { exercise -> requireNotNull(exercise.sectionId) }
        .mapValues { (_, sectionExercises) ->
            sectionExercises.sortedBy { exercise -> exercise.sortOrder }
        }

    val emittedSectionIds = mutableSetOf<String>()
    val rows = mutableListOf<WorkoutListRow>()

    exercises
        .sortedBy { exercise -> exercise.sortOrder }
        .forEach { exercise ->
            val sectionId = exercise.sectionId

            if (sectionId == null) {
                rows += WorkoutListRow.Exercise(exercise)
                return@forEach
            }

            if (!emittedSectionIds.add(sectionId)) {
                return@forEach
            }

            val sectionExercises = exercisesBySectionId[sectionId].orEmpty()
            val section = WorkoutSectionUi(
                id = sectionId,
                name = exercise.sectionName.orEmpty().ifBlank { "Секция" },
                exercises = sectionExercises,
            )

            rows += WorkoutListRow.SectionHeader(section)

            if (sectionId !in collapsedSectionIds) {
                rows += sectionExercises.map { item ->
                    WorkoutListRow.Exercise(item)
                }
            }
        }

    return rows
}

@Composable
private fun WorkoutSectionHeader(
    section: WorkoutSectionUi,
    isCollapsed: Boolean,
    isDropTarget: Boolean,
    onToggleCollapsed: () -> Unit,
    onRename: () -> Unit,
    onRemove: () -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDropTarget) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onToggleCollapsed) {
                Icon(
                    imageVector = if (isCollapsed) {
                        Icons.Default.ExpandMore
                    } else {
                        Icons.Default.ExpandLess
                    },
                    contentDescription = if (isCollapsed) {
                        "Развернуть секцию"
                    } else {
                        "Свернуть секцию"
                    },
                )
            }

            Text(
                text = "${section.name} • ${section.exercises.size}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Box {
                IconButton(
                    onClick = {
                        isMenuExpanded = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Действия с секцией",
                    )
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = {
                        isMenuExpanded = false
                    },
                ) {
                    DropdownMenuItem(
                        text = { Text("Переименовать") },
                        onClick = {
                            isMenuExpanded = false
                            onRename()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Убрать секцию") },
                        onClick = {
                            isMenuExpanded = false
                            onRemove()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionNameDialog(
    title: String,
    initialName: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable(initialName) {
        mutableStateOf(initialName)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { value ->
                    name = value
                },
                label = { Text("Название") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name.trim())
                },
                enabled = name.isNotBlank(),
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkoutExerciseCard(
    exercise: WorkoutExerciseUi,
    isExpanded: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    dragTranslationY: Float,
    onExerciseClick: () -> Unit,
    onExerciseLongClick: () -> Unit,
    onDragStart: () -> Unit,
    onDragDelta: (Float) -> Unit,
    onDragEnd: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onAddSetClick: () -> Unit,
    onOpenProgressClick: () -> Unit,
    onRemoveSetClick: (Long) -> Unit,
    onAction: (WorkoutEditorAction) -> Unit,
) {
    var isVariantDialogVisible by rememberSaveable(exercise.id, "variant-dialog") {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .padding(start = if (exercise.sectionId != null) 8.dp else 0.dp)
            .fillMaxWidth()
            .graphicsLayer {
                translationY = dragTranslationY
                alpha = if (dragTranslationY != 0f) 0.92f else 1f
            }
            .combinedClickable(
                onClick = onExerciseClick,
                onLongClick = onExerciseLongClick,
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = exercise.isChecked,
                        onCheckedChange = { isChecked ->
                            onAction(
                                WorkoutEditorAction.ExerciseCheckedChanged(
                                    workoutExerciseId = exercise.id,
                                    isChecked = isChecked,
                                )
                            )
                        },
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = exercise.exerciseName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Перетащить упражнение в секцию",
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (isSelectionMode) {
                                        Modifier
                                    } else {
                                        Modifier.pointerInput(exercise.id) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    onDragStart()
                                                },
                                                onDragEnd = onDragEnd,
                                                onDragCancel = onDragEnd,
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    onDragDelta(dragAmount.y)
                                                },
                                            )
                                        }
                                    }
                                )
                                .padding(4.dp),
                        )

                        Icon(
                            imageVector = if (isExpanded) {
                                Icons.Default.ExpandLess
                            } else {
                                Icons.Default.ExpandMore
                            },
                            contentDescription = if (isExpanded) {
                                "Свернуть упражнение"
                            } else {
                                "Развернуть упражнение"
                            },
                        )
                    }
                }

                if (exercise.alternatives.isNotEmpty() || exercise.isAlternativeSelected) {
                    Text(
                        text = if (exercise.isAlternativeSelected) {
                            "↔ Альтернатива • основное: ${exercise.originalExerciseName}"
                        } else {
                            "↔ Альтернативы: ${exercise.alternatives.size}"
                        },
                        modifier = Modifier
                            .padding(start = 48.dp)
                            .clickable {
                                isVariantDialogVisible = true
                            }
                            .padding(vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Подходов: ${exercise.sets.size}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

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
            }

            if (isExpanded) {
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
                                onAction = onAction,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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

                    TextButton(
                        onClick = onOpenProgressClick,
                    ) {
                        Text("Прогресс")
                    }
                }
            }
        }
    }

    if (isVariantDialogVisible) {
        ExerciseVariantDialog(
            exercise = exercise,
            onDismiss = {
                isVariantDialogVisible = false
            },
            onVariantSelected = { exerciseDefinitionId ->
                isVariantDialogVisible = false
                onAction(
                    WorkoutEditorAction.ExerciseVariantSelected(
                        workoutExerciseId = exercise.id,
                        exerciseDefinitionId = exerciseDefinitionId,
                    )
                )
            },
        )
    }
}

@Composable
private fun ExerciseVariantDialog(
    exercise: WorkoutExerciseUi,
    onDismiss: () -> Unit,
    onVariantSelected: (Long) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Выбрать вариант упражнения")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ExerciseVariantOption(
                    name = exercise.originalExerciseName,
                    subtitle = "Основное упражнение",
                    selected = exercise.selectedExerciseDefinitionId == exercise.exerciseDefinitionId,
                    onClick = {
                        onVariantSelected(exercise.exerciseDefinitionId)
                    },
                )

                exercise.alternatives.forEach { alternative ->
                    ExerciseVariantOption(
                        name = alternative.name,
                        subtitle = "Альтернатива",
                        selected = exercise.selectedExerciseDefinitionId == alternative.id,
                        onClick = {
                            onVariantSelected(alternative.id)
                        },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}

@Composable
private fun ExerciseVariantOption(
    name: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Строка упражнения в тренировке, редактирование подходов, количества, и ед. изм.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutExerciseSetRow(
    set: WorkoutExerciseSetUi,
    onRemoveClick: () -> Unit,
    onAction: (WorkoutEditorAction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    top = 10.dp,
                    end = 4.dp,
                    bottom = 10.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Подход ${set.setNumber}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )

                IconButton(
                    onClick = onRemoveClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить подход",
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                OutlinedTextField(
                    value = set.repsText,
                    onValueChange = { value ->
                        onAction(
                            WorkoutEditorAction.SetRepsChanged(
                                workoutExerciseSetId = set.id,
                                value = value,
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Повторы") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                )

                MeasurementField(
                    value = set.quantityText,
                    unit = set.measurementUnit,
                    onValueChange = { value ->
                        onAction(
                            WorkoutEditorAction.SetQuantityChanged(
                                workoutExerciseSetId = set.id,
                                value = value,
                            )
                        )
                    },
                    onUnitChanged = { unit ->
                        onAction(
                            WorkoutEditorAction.SetMeasurementUnitChanged(
                                workoutExerciseSetId = set.id,
                                unit = unit,
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MeasurementField(
    value: String,
    unit: WeightUnit,
    onValueChange: (String) -> Unit,
    onUnitChanged: (WeightUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
        ),
        trailingIcon = {
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isExpanded = true }
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = unit.shortLabel,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Выбрать единицу измерения",
                        modifier = Modifier.size(18.dp),
                    )
                }

                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                ) {
                    WeightUnit.entries.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.shortLabel) },
                            onClick = {
                                onUnitChanged(item)
                                isExpanded = false
                            },
                        )
                    }
                }
            }
        },
    )
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
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onExerciseClick: (Long) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Добавить упражнение")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Поиск")
                    },
                    placeholder = {
                        Text("Например: тяга или жим")
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onSearchQueryChange("")
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Очистить поиск",
                                )
                            }
                        }
                    },
                )

                when {
                    exercises.isEmpty() && searchQuery.isBlank() -> {
                        Text(
                            text = "Справочник упражнений пуст. Сначала создай упражнение в справочнике.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    exercises.isEmpty() -> {
                        Text(
                            text = "По запросу «${searchQuery.trim()}» ничего не найдено.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    else -> {
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
    val coverImageUri = exercise.images
        .firstOrNull { image -> image.isCover }
        ?.uri
        ?: exercise.images.firstOrNull()?.uri

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            ExerciseDefinitionThumbnail(
                imageUri = coverImageUri,
                exerciseName = exercise.name,
            )
        },
        headlineContent = {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
    )
}

@Composable
private fun ExerciseDefinitionThumbnail(
    imageUri: String?,
    exerciseName: String,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Изображение упражнения $exerciseName",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}