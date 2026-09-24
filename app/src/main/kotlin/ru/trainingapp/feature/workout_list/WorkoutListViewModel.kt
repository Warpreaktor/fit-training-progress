package ru.trainingapp.feature.workout_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.trainingapp.core.domain.exportimport.ExportWorkoutUseCase
import ru.trainingapp.core.domain.exportimport.ImportTrainingPackUseCase
import ru.trainingapp.core.domain.repository.TrainingPackExportType
import ru.trainingapp.core.domain.tag.CreateTagUseCase
import ru.trainingapp.core.domain.tag.ObserveTagsUseCase
import ru.trainingapp.core.domain.workout.ArchiveWorkoutUseCase
import ru.trainingapp.core.domain.workout.CreateWorkoutUseCase
import ru.trainingapp.core.domain.workout.DuplicateWorkoutUseCase
import ru.trainingapp.core.domain.workout.MoveWorkoutUseCase
import ru.trainingapp.core.domain.workout.ObserveWorkoutUseCase
import ru.trainingapp.core.domain.workout.ReplaceWorkoutTagsUseCase
import ru.trainingapp.core.domain.workout.UpdateWorkoutUseCase
import ru.trainingapp.core.model.Tag
import ru.trainingapp.core.model.Workout
import javax.inject.Inject

@HiltViewModel
class WorkoutListViewModel @Inject constructor(
    observeWorkoutUseCase: ObserveWorkoutUseCase,
    observeTagsUseCase: ObserveTagsUseCase,
    private val createWorkoutUseCase: CreateWorkoutUseCase,
    private val archiveWorkoutUseCase: ArchiveWorkoutUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val replaceWorkoutTagsUseCase: ReplaceWorkoutTagsUseCase,
    private val duplicateWorkoutUseCase: DuplicateWorkoutUseCase,
    private val updateWorkoutUseCase: UpdateWorkoutUseCase,
    private val moveWorkoutUseCase: MoveWorkoutUseCase,
    private val exportWorkoutUseCase: ExportWorkoutUseCase,
    private val importTrainingPackUseCase: ImportTrainingPackUseCase,
) : ViewModel() {

    private val editorState = MutableStateFlow(WorkoutEditorState())

    private val selectedFilterTagIds = MutableStateFlow<Set<Long>>(emptySet())

    private val tagEditorState = MutableStateFlow(WorkoutTagEditorState())

    private val transferState = MutableStateFlow(WorkoutTransferState())

    private var pendingExportWorkoutId: Long? = null

    private val workoutListState = combine(
        observeWorkoutUseCase(),
        observeTagsUseCase(),
        editorState,
        tagEditorState,
        selectedFilterTagIds,
    ) { workouts, tags, editor, tagEditor, filterTagIds ->
        val filteredWorkouts = if (filterTagIds.isEmpty()) {
            workouts
        } else {
            workouts.filter { workout ->
                workout.tags.any { tag -> tag.id in filterTagIds }
            }
        }

        WorkoutListUiState(
            workouts = filteredWorkouts,
            allTags = tags,
            selectedFilterTagIds = filterTagIds,
            editor = editor,
            tagEditor = tagEditor,
        )
    }

    val uiState: StateFlow<WorkoutListUiState> = combine(
        workoutListState,
        transferState,
    ) { listState, transfer ->
        listState.copy(transfer = transfer)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutListUiState(),
    )

    fun onCreateWorkoutClick() {
        editorState.value = WorkoutEditorState(
            isVisible = true,
            mode = WorkoutEditorMode.CREATE,
            name = "",
            description = "",
            nameError = null,
        )
    }

    fun onEditWorkoutClick(workout: Workout) {
        editorState.value = WorkoutEditorState(
            isVisible = true,
            mode = WorkoutEditorMode.EDIT,
            workoutId = workout.id,
            name = workout.name,
            description = workout.description.orEmpty(),
        )
    }

    fun onDuplicateWorkoutClick(workout: Workout) {
        editorState.value = WorkoutEditorState(
            isVisible = true,
            mode = WorkoutEditorMode.DUPLICATE,
            workoutId = workout.id,
            name = workout.name,
            description = workout.description.orEmpty(),
        )
    }

    fun onWorkoutNameChange(value: String) {
        editorState.value = editorState.value.copy(
            name = value,
            nameError = null,
        )
    }

    fun onWorkoutDescriptionChange(value: String) {
        editorState.value = editorState.value.copy(description = value)
    }

    fun onDismissEditor() {
        editorState.value = WorkoutEditorState()
    }

    fun onSaveWorkoutClick() {
        val editor = editorState.value

        if (editor.name.isBlank()) {
            editorState.value = editor.copy(nameError = "Название обязательно")
            return
        }

        viewModelScope.launch {
            when (editor.mode) {
                WorkoutEditorMode.CREATE -> {
                    createWorkoutUseCase(
                        name = editor.name,
                        description = editor.description,
                    )
                }

                WorkoutEditorMode.EDIT -> {
                    updateWorkoutUseCase(
                        workoutId = editor.workoutId,
                        name = editor.name,
                        description = editor.description,
                    )
                }

                WorkoutEditorMode.DUPLICATE -> {
                    duplicateWorkoutUseCase(
                        workoutId = editor.workoutId,
                        name = editor.name,
                    )
                }
            }

            editorState.value = WorkoutEditorState()
        }
    }

    fun onArchiveWorkoutClick(id: Long) {
        viewModelScope.launch {
            archiveWorkoutUseCase(id)
        }
    }

    fun onMoveWorkoutUpClick(id: Long) {
        viewModelScope.launch {
            moveWorkoutUseCase.up(id)
        }
    }

    fun onMoveWorkoutDownClick(id: Long) {
        viewModelScope.launch {
            moveWorkoutUseCase.down(id)
        }
    }

    fun onPrepareExportWorkout(workoutId: Long) {
        pendingExportWorkoutId = workoutId
    }

    fun onExportDocumentCreated(destinationUri: String?) {
        val workoutId = pendingExportWorkoutId
        pendingExportWorkoutId = null

        if (destinationUri == null || workoutId == null) {
            return
        }

        viewModelScope.launch {
            transferState.value = WorkoutTransferState(isInProgress = true)

            transferState.value = runCatching {
                exportWorkoutUseCase(
                    workoutId = workoutId,
                    destinationUri = destinationUri,
                )

                WorkoutTransferState(
                    message = "Тренировка экспортирована",
                )
            }.getOrElse { exception ->
                WorkoutTransferState(
                    message = exception.message
                        ?: "Не удалось экспортировать тренировку",
                )
            }
        }
    }

    fun onImportDocumentSelected(sourceUri: String?) {
        if (sourceUri == null) {
            return
        }

        viewModelScope.launch {
            transferState.value = WorkoutTransferState(isInProgress = true)

            transferState.value = runCatching {
                val result = importTrainingPackUseCase(
                    sourceUri = sourceUri,
                    expectedExportType = TrainingPackExportType.WORKOUT,
                )

                WorkoutTransferState(
                    message = buildString {
                        append("Импортировано тренировок: ${result.createdWorkouts}")
                        append(", упражнений: ${result.createdExercises}")
                        append(", тегов: ${result.createdTags}")
                        append(", картинок: ${result.createdImages}")

                        if (result.warnings.isNotEmpty()) {
                            append(", предупреждений: ${result.warnings.size}")
                        }
                    },
                )
            }.getOrElse { exception ->
                WorkoutTransferState(
                    message = exception.message
                        ?: "Не удалось импортировать тренировку",
                )
            }
        }
    }

    fun onTransferMessageShown() {
        transferState.value = transferState.value.copy(message = null)
    }

    fun onFilterTagClick(tagId: Long) {
        selectedFilterTagIds.value = selectedFilterTagIds.value.toggle(tagId)
    }

    fun onClearFilterClick() {
        selectedFilterTagIds.value = emptySet()
    }

    fun onEditWorkoutTagsClick(workout: Workout) {
        tagEditorState.value = WorkoutTagEditorState(
            isVisible = true,
            workoutId = workout.id,
            workoutName = workout.name,
            selectedTagIds = workout.tags.map { tag -> tag.id }.toSet(),
        )
    }

    fun onDismissTagEditor() {
        tagEditorState.value = WorkoutTagEditorState()
    }

    fun onToggleTagSelection(tagId: Long) {
        val current = tagEditorState.value

        tagEditorState.value = current.copy(
            selectedTagIds = current.selectedTagIds.toggle(tagId),
        )
    }

    fun onNewTagNameChange(value: String) {
        tagEditorState.value = tagEditorState.value.copy(
            newTagName = value,
            newTagNameError = null,
        )
    }

    fun onCreateTagClick() {
        val current = tagEditorState.value
        val tagName = current.newTagName.trim()

        if (tagName.isBlank()) {
            tagEditorState.value = current.copy(
                newTagNameError = "Название тега обязательно",
            )
            return
        }

        viewModelScope.launch {
            val tagId = createTagUseCase(tagName)

            if (tagId <= 0L) {
                tagEditorState.value = tagEditorState.value.copy(
                    newTagNameError = "Не удалось создать тег",
                )
                return@launch
            }

            tagEditorState.value = tagEditorState.value.copy(
                selectedTagIds = tagEditorState.value.selectedTagIds + tagId,
                newTagName = "",
                newTagNameError = null,
            )
        }
    }

    fun onSaveWorkoutTagsClick() {
        val current = tagEditorState.value

        viewModelScope.launch {
            replaceWorkoutTagsUseCase(
                workoutId = current.workoutId,
                tagIds = current.selectedTagIds,
            )

            tagEditorState.value = WorkoutTagEditorState()
        }
    }

    private fun Set<Long>.toggle(value: Long): Set<Long> {
        return if (value in this) {
            this - value
        } else {
            this + value
        }
    }
}

data class WorkoutListUiState(
    val workouts: List<Workout> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val selectedFilterTagIds: Set<Long> = emptySet(),
    val editor: WorkoutEditorState = WorkoutEditorState(),
    val tagEditor: WorkoutTagEditorState = WorkoutTagEditorState(),
    val transfer: WorkoutTransferState = WorkoutTransferState(),
)

data class WorkoutEditorState(
    val isVisible: Boolean = false,
    val mode: WorkoutEditorMode = WorkoutEditorMode.CREATE,
    val workoutId: Long = 0L,
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
)

enum class WorkoutEditorMode {
    CREATE,
    EDIT,
    DUPLICATE,
}

data class WorkoutTagEditorState(
    val isVisible: Boolean = false,
    val workoutId: Long = 0L,
    val workoutName: String = "",
    val selectedTagIds: Set<Long> = emptySet(),
    val newTagName: String = "",
    val newTagNameError: String? = null,
)

data class WorkoutTransferState(
    val isInProgress: Boolean = false,
    val message: String? = null,
)
