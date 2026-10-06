package ru.trainingapp.feature.exercise_catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.trainingapp.core.domain.exercise.AddExerciseImagesUseCase
import ru.trainingapp.core.domain.exercise.ArchiveExerciseDefinitionUseCase
import ru.trainingapp.core.domain.exercise.CreateExerciseDefinitionUseCase
import ru.trainingapp.core.domain.exercise.DeleteExerciseImageUseCase
import ru.trainingapp.core.domain.exercise.ObserveExerciseDefinitionsUseCase
import ru.trainingapp.core.domain.exercise.ReplaceExerciseDefinitionAlternativesUseCase
import ru.trainingapp.core.domain.exercise.ReplaceExerciseDefinitionTagsUseCase
import ru.trainingapp.core.domain.exercise.SetExerciseCoverImageUseCase
import ru.trainingapp.core.domain.exercise.UpdateExerciseDefinitionUseCase
import ru.trainingapp.core.domain.exportimport.ExportExerciseUseCase
import ru.trainingapp.core.domain.exportimport.ImportTrainingPackUseCase
import ru.trainingapp.core.domain.repository.TrainingPackExportType
import ru.trainingapp.core.domain.workout.AddExerciseToWorkoutUseCase
import ru.trainingapp.core.domain.workout.ObserveExerciseWorkoutMembershipUseCase
import ru.trainingapp.core.domain.workout.ObserveWorkoutUseCase
import ru.trainingapp.core.domain.workout.RemoveExerciseFromWorkoutUseCase
import ru.trainingapp.core.domain.tag.CreateTagUseCase
import ru.trainingapp.core.domain.tag.ObserveTagsUseCase
import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.Tag
import ru.trainingapp.core.model.Workout
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExerciseCatalogViewModel @Inject constructor(
    observeExerciseDefinitionsUseCase: ObserveExerciseDefinitionsUseCase,
    observeTagsUseCase: ObserveTagsUseCase,
    observeWorkoutUseCase: ObserveWorkoutUseCase,
    observeExerciseWorkoutMembershipUseCase: ObserveExerciseWorkoutMembershipUseCase,
    private val createExerciseDefinitionUseCase: CreateExerciseDefinitionUseCase,
    private val updateExerciseDefinitionUseCase: UpdateExerciseDefinitionUseCase,
    private val archiveExerciseDefinitionUseCase: ArchiveExerciseDefinitionUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val replaceExerciseDefinitionTagsUseCase: ReplaceExerciseDefinitionTagsUseCase,
    private val replaceExerciseDefinitionAlternativesUseCase: ReplaceExerciseDefinitionAlternativesUseCase,
    private val addExerciseImagesUseCase: AddExerciseImagesUseCase,
    private val setExerciseCoverImageUseCase: SetExerciseCoverImageUseCase,
    private val deleteExerciseImageUseCase: DeleteExerciseImageUseCase,
    private val exportExerciseUseCase: ExportExerciseUseCase,
    private val importTrainingPackUseCase: ImportTrainingPackUseCase,
    private val addExerciseToWorkoutUseCase: AddExerciseToWorkoutUseCase,
    private val removeExerciseFromWorkoutUseCase: RemoveExerciseFromWorkoutUseCase,
) : ViewModel() {

    private val editorState = MutableStateFlow(ExerciseEditorState())

    private val selectedFilterTagIds = MutableStateFlow<Set<Long>>(emptySet())

    private val tagEditorState = MutableStateFlow(ExerciseTagEditorState())

    private val alternativeEditorState = MutableStateFlow(ExerciseAlternativeEditorState())

    private var imagePickerExerciseDefinitionId: Long? = null

    private var imageViewerExerciseDefinitionIdAfterPicker: Long? = null

    private val imageViewerState = MutableStateFlow(ExerciseImageViewerState())

    private val transferState = MutableStateFlow(ExerciseTransferState())

    private var pendingExportExerciseDefinitionId: Long? = null

    private val workoutMembershipState = MutableStateFlow(ExerciseWorkoutMembershipState())

    private val selectedWorkoutIds = workoutMembershipState
        .map { state -> state.exerciseDefinitionId }
        .distinctUntilChanged()
        .flatMapLatest { exerciseDefinitionId ->
            if (exerciseDefinitionId == null) {
                flowOf(emptySet<Long>())
            } else {
                observeExerciseWorkoutMembershipUseCase(exerciseDefinitionId)
            }
        }

    private val workoutMembershipUiState = combine(
        observeWorkoutUseCase(),
        workoutMembershipState,
        selectedWorkoutIds,
    ) { workouts, state, workoutIds ->
        state.copy(
            workouts = workouts,
            selectedWorkoutIds = workoutIds,
        )
    }

    val uiState: StateFlow<ExerciseCatalogUiState> =
        combine(
            combine(
                observeExerciseDefinitionsUseCase(),
                observeTagsUseCase(),
                editorState,
                tagEditorState,
                alternativeEditorState,
            ) { exercises, tags, editor, tagEditor, alternativeEditor ->
                ExerciseCatalogCombinedState(
                    exercises = exercises,
                    tags = tags,
                    editor = editor,
                    tagEditor = tagEditor,
                    alternativeEditor = alternativeEditor,
                )
            },
            selectedFilterTagIds,
            imageViewerState,
            transferState,
            workoutMembershipUiState,
        ) { combinedState, filterTagIds, imageViewer, transfer, workoutMembership ->

            val filteredExercises = if (filterTagIds.isEmpty()) {
                combinedState.exercises
            } else {
                combinedState.exercises.filter { exercise ->
                    exercise.tags.any { tag -> tag.id in filterTagIds }
                }
            }

            ExerciseCatalogUiState(
                exercises = filteredExercises,
                allExercises = combinedState.exercises,
                allTags = combinedState.tags,
                selectedFilterTagIds = filterTagIds,
                editor = combinedState.editor,
                tagEditor = combinedState.tagEditor,
                alternativeEditor = combinedState.alternativeEditor,
                imageViewer = imageViewer,
                transfer = transfer,
                workoutMembership = workoutMembership,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExerciseCatalogUiState(),
        )

    fun onAddExerciseClick() {
        editorState.value = ExerciseEditorState(
            isVisible = true,
            exerciseId = null,
            name = "",
            description = "",
        )
    }

    fun onEditExerciseClick(exercise: ExerciseDefinition) {
        editorState.value = ExerciseEditorState(
            isVisible = true,
            exerciseId = exercise.id,
            name = exercise.name,
            description = exercise.description,
        )
    }

    fun onEditExerciseWorkoutsClick(exerciseDefinitionId: Long) {
        if (exerciseDefinitionId <= 0L) {
            return
        }

        workoutMembershipState.value = ExerciseWorkoutMembershipState(
            isVisible = true,
            exerciseDefinitionId = exerciseDefinitionId,
        )
    }

    fun onDismissWorkoutMembership() {
        workoutMembershipState.value = ExerciseWorkoutMembershipState()
    }

    fun onWorkoutMembershipChange(
        workoutId: Long,
        isSelected: Boolean,
    ) {
        val currentState = workoutMembershipState.value
        val exerciseDefinitionId = currentState.exerciseDefinitionId ?: return

        if (workoutId in currentState.pendingWorkoutIds) {
            return
        }

        workoutMembershipState.value = currentState.copy(
            pendingWorkoutIds = currentState.pendingWorkoutIds + workoutId,
        )

        viewModelScope.launch {
            try {
                if (isSelected) {
                    addExerciseToWorkoutUseCase(
                        workoutId = workoutId,
                        exerciseDefinitionId = exerciseDefinitionId,
                    )
                } else {
                    removeExerciseFromWorkoutUseCase(
                        workoutId = workoutId,
                        exerciseDefinitionId = exerciseDefinitionId,
                    )
                }
            } finally {
                val latestState = workoutMembershipState.value
                workoutMembershipState.value = latestState.copy(
                    pendingWorkoutIds = latestState.pendingWorkoutIds - workoutId,
                )
            }
        }
    }

    fun onEditorNameChange(value: String) {
        editorState.value = editorState.value.copy(name = value)
    }

    fun onEditorDescriptionChange(value: String) {
        editorState.value = editorState.value.copy(description = value)
    }

    fun onDismissEditor() {
        editorState.value = ExerciseEditorState()
    }

    fun onSaveExerciseClick() {
        val editor = editorState.value
        val name = editor.name.trim()

        if (name.isBlank()) {
            editorState.value = editor.copy(nameError = "Название обязательно")
            return
        }

        viewModelScope.launch {
            val exerciseId = editor.exerciseId

            if (exerciseId == null) {
                createExerciseDefinitionUseCase(
                    name = editor.name,
                    description = editor.description,
                )
            } else {
                updateExerciseDefinitionUseCase(
                    id = exerciseId,
                    name = editor.name,
                    description = editor.description,
                )
            }

            editorState.value = ExerciseEditorState()
        }
    }

    fun onArchiveExerciseClick(exerciseId: Long) {
        viewModelScope.launch {
            archiveExerciseDefinitionUseCase(exerciseId)
        }
    }

    fun onFilterTagClick(tagId: Long) {
        selectedFilterTagIds.value = selectedFilterTagIds.value.toggle(tagId)
    }

    fun onClearFilterClick() {
        selectedFilterTagIds.value = emptySet()
    }

    fun onEditExerciseTagsClick(exercise: ExerciseDefinition) {
        tagEditorState.value = ExerciseTagEditorState(
            isVisible = true,
            exerciseDefinitionId = exercise.id,
            exerciseName = exercise.name,
            selectedTagIds = exercise.tags.map { tag -> tag.id }.toSet(),
        )
    }

    fun onDismissTagEditor() {
        tagEditorState.value = ExerciseTagEditorState()
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

    fun onSaveExerciseTagsClick() {
        val current = tagEditorState.value

        viewModelScope.launch {
            replaceExerciseDefinitionTagsUseCase(
                exerciseDefinitionId = current.exerciseDefinitionId,
                tagIds = current.selectedTagIds,
            )

            tagEditorState.value = ExerciseTagEditorState()
        }
    }

    fun onEditExerciseAlternativesClick(exercise: ExerciseDefinition) {
        alternativeEditorState.value = ExerciseAlternativeEditorState(
            isVisible = true,
            exerciseDefinitionId = exercise.id,
            exerciseName = exercise.name,
            selectedAlternativeExerciseDefinitionIds = exercise.alternatives
                .map { alternative -> alternative.id }
                .toSet(),
        )
    }

    fun onDismissAlternativeEditor() {
        alternativeEditorState.value = ExerciseAlternativeEditorState()
    }

    fun onToggleAlternativeSelection(alternativeExerciseDefinitionId: Long) {
        val current = alternativeEditorState.value

        alternativeEditorState.value = current.copy(
            selectedAlternativeExerciseDefinitionIds = current
                .selectedAlternativeExerciseDefinitionIds
                .toggle(alternativeExerciseDefinitionId),
        )
    }

    fun onSaveExerciseAlternativesClick() {
        val current = alternativeEditorState.value

        viewModelScope.launch {
            replaceExerciseDefinitionAlternativesUseCase(
                exerciseDefinitionId = current.exerciseDefinitionId,
                alternativeExerciseDefinitionIds = current.selectedAlternativeExerciseDefinitionIds,
            )

            alternativeEditorState.value = ExerciseAlternativeEditorState()
        }
    }

    fun onOpenImageViewer(exerciseDefinitionId: Long) {
        if (exerciseDefinitionId <= 0L) {
            return
        }

        imageViewerState.value = ExerciseImageViewerState(
            exerciseDefinitionId = exerciseDefinitionId,
        )
    }

    fun onDismissImageViewer() {
        imageViewerState.value = ExerciseImageViewerState()
    }

    fun onPrepareAddImagesClick(
        exercise: ExerciseDefinition,
        reopenViewerAfterSelection: Boolean = false,
    ) {
        imagePickerExerciseDefinitionId = exercise.id

        imageViewerExerciseDefinitionIdAfterPicker = exercise.id
            .takeIf { reopenViewerAfterSelection }
    }

    fun onImagesSelected(uriStrings: List<String>) {
        val exerciseDefinitionId =
            imagePickerExerciseDefinitionId ?: return

        val viewerExerciseDefinitionId =
            imageViewerExerciseDefinitionIdAfterPicker

        imagePickerExerciseDefinitionId = null
        imageViewerExerciseDefinitionIdAfterPicker = null

        if (uriStrings.isEmpty()) {
            viewerExerciseDefinitionId?.let { exerciseId ->
                imageViewerState.value = ExerciseImageViewerState(
                    exerciseDefinitionId = exerciseId,
                )
            }

            return
        }

        viewModelScope.launch {
            addExerciseImagesUseCase(
                exerciseDefinitionId = exerciseDefinitionId,
                sourceUris = uriStrings,
            )

            viewerExerciseDefinitionId?.let { exerciseId ->
                imageViewerState.value = ExerciseImageViewerState(
                    exerciseDefinitionId = exerciseId,
                )
            }
        }
    }

    fun onPrepareExportExercise(exerciseDefinitionId: Long) {
        pendingExportExerciseDefinitionId = exerciseDefinitionId
    }

    fun onExportDocumentCreated(destinationUri: String?) {
        val exerciseDefinitionId = pendingExportExerciseDefinitionId
        pendingExportExerciseDefinitionId = null

        if (destinationUri == null || exerciseDefinitionId == null) {
            return
        }

        viewModelScope.launch {
            transferState.value = ExerciseTransferState(isInProgress = true)

            transferState.value = runCatching {
                exportExerciseUseCase(
                    exerciseDefinitionId = exerciseDefinitionId,
                    destinationUri = destinationUri,
                )

                ExerciseTransferState(
                    message = "Упражнение экспортировано",
                )
            }.getOrElse { exception ->
                ExerciseTransferState(
                    message = exception.message
                        ?: "Не удалось экспортировать упражнение",
                )
            }
        }
    }

    fun onImportDocumentSelected(sourceUri: String?) {
        if (sourceUri == null) {
            return
        }

        viewModelScope.launch {
            transferState.value = ExerciseTransferState(isInProgress = true)

            transferState.value = runCatching {
                val result = importTrainingPackUseCase(
                    sourceUri = sourceUri,
                    expectedExportType = TrainingPackExportType.SINGLE_EXERCISE,
                )

                ExerciseTransferState(
                    message = buildString {
                        append("Импортировано упражнений: ${result.createdExercises}")
                        append(", тегов: ${result.createdTags}")
                        append(", картинок: ${result.createdImages}")

                        if (result.warnings.isNotEmpty()) {
                            append(", предупреждений: ${result.warnings.size}")
                        }
                    },
                )
            }.getOrElse { exception ->
                ExerciseTransferState(
                    message = exception.message
                        ?: "Не удалось импортировать упражнение",
                )
            }
        }
    }

    fun onTransferMessageShown() {
        transferState.value = transferState.value.copy(message = null)
    }

    fun onSetCoverImageClick(
        exerciseDefinitionId: Long,
        imageId: Long,
    ) {
        viewModelScope.launch {
            setExerciseCoverImageUseCase(
                exerciseDefinitionId = exerciseDefinitionId,
                imageId = imageId,
            )
        }
    }

    fun onDeleteExerciseImageClick(
        exerciseDefinitionId: Long,
        imageId: Long,
    ) {
        viewModelScope.launch {
            deleteExerciseImageUseCase(
                exerciseDefinitionId = exerciseDefinitionId,
                imageId = imageId,
            )
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

private data class ExerciseCatalogCombinedState(
    val exercises: List<ExerciseDefinition>,
    val tags: List<Tag>,
    val editor: ExerciseEditorState,
    val tagEditor: ExerciseTagEditorState,
    val alternativeEditor: ExerciseAlternativeEditorState,
)

data class ExerciseCatalogUiState(
    val exercises: List<ExerciseDefinition> = emptyList(),
    val allExercises: List<ExerciseDefinition> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val selectedFilterTagIds: Set<Long> = emptySet(),
    val editor: ExerciseEditorState = ExerciseEditorState(),
    val tagEditor: ExerciseTagEditorState = ExerciseTagEditorState(),
    val alternativeEditor: ExerciseAlternativeEditorState = ExerciseAlternativeEditorState(),
    val imageViewer: ExerciseImageViewerState = ExerciseImageViewerState(),
    val transfer: ExerciseTransferState = ExerciseTransferState(),
    val workoutMembership: ExerciseWorkoutMembershipState = ExerciseWorkoutMembershipState(),
)

data class ExerciseEditorState(

    val isVisible: Boolean = false,
    val exerciseId: Long? = null,
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
) {
    val isEditing: Boolean
        get() = exerciseId != null
}

data class ExerciseTagEditorState(
    val isVisible: Boolean = false,
    val exerciseDefinitionId: Long = 0L,
    val exerciseName: String = "",
    val selectedTagIds: Set<Long> = emptySet(),
    val newTagName: String = "",
    val newTagNameError: String? = null,
)

data class ExerciseAlternativeEditorState(
    val isVisible: Boolean = false,
    val exerciseDefinitionId: Long = 0L,
    val exerciseName: String = "",
    val selectedAlternativeExerciseDefinitionIds: Set<Long> = emptySet(),
)

data class ExerciseWorkoutMembershipState(
    val isVisible: Boolean = false,
    val exerciseDefinitionId: Long? = null,
    val workouts: List<Workout> = emptyList(),
    val selectedWorkoutIds: Set<Long> = emptySet(),
    val pendingWorkoutIds: Set<Long> = emptySet(),
)

data class ExerciseImageViewerState(
    val exerciseDefinitionId: Long? = null,
) {
    val isVisible: Boolean
        get() = exerciseDefinitionId != null
}

data class ExerciseTransferState(
    val isInProgress: Boolean = false,
    val message: String? = null,
)
