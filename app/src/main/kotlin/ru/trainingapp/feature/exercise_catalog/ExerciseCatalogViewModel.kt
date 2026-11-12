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
import ru.trainingapp.core.domain.exercise.ReplaceExerciseDefinitionMusclesUseCase
import ru.trainingapp.core.domain.exercise.ReplaceExerciseDefinitionTagsUseCase
import ru.trainingapp.core.domain.exercise.ReplaceExerciseImagesUseCase
import ru.trainingapp.core.domain.exercise.SetExerciseCoverImageUseCase
import ru.trainingapp.core.domain.exercise.UpdateExerciseDefinitionUseCase
import ru.trainingapp.core.domain.exercise.UpdateExerciseImageCropUseCase
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
import ru.trainingapp.core.model.ExerciseImageEdit
import ru.trainingapp.core.model.MuscleGroup
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
    private val replaceExerciseDefinitionMusclesUseCase: ReplaceExerciseDefinitionMusclesUseCase,
    private val replaceExerciseImagesUseCase: ReplaceExerciseImagesUseCase,
    private val addExerciseImagesUseCase: AddExerciseImagesUseCase,
    private val setExerciseCoverImageUseCase: SetExerciseCoverImageUseCase,
    private val updateExerciseImageCropUseCase: UpdateExerciseImageCropUseCase,
    private val deleteExerciseImageUseCase: DeleteExerciseImageUseCase,
    private val exportExerciseUseCase: ExportExerciseUseCase,
    private val importTrainingPackUseCase: ImportTrainingPackUseCase,
    private val addExerciseToWorkoutUseCase: AddExerciseToWorkoutUseCase,
    private val removeExerciseFromWorkoutUseCase: RemoveExerciseFromWorkoutUseCase,
) : ViewModel() {

    private val editorState = MutableStateFlow(ExerciseEditorState())

    private var nextDraftImageKey = -1L

    private val selectedFilterTagIds = MutableStateFlow<Set<Long>>(emptySet())

    private val searchQuery = MutableStateFlow("")

    private val filterState = combine(
        selectedFilterTagIds,
        searchQuery,
    ) { tagIds, query ->
        ExerciseCatalogFilterState(
            tagIds = tagIds,
            query = query,
        )
    }

    private val tagEditorState = MutableStateFlow(ExerciseTagEditorState())

    private val alternativeEditorState = MutableStateFlow(ExerciseAlternativeEditorState())

    private val muscleEditorState = MutableStateFlow(ExerciseMuscleEditorState())

    private var imagePickerExerciseDefinitionId: Long? = null

    private var imageViewerExerciseDefinitionIdAfterPicker: Long? = null

    private val imageViewerState = MutableStateFlow(ExerciseImageViewerState())

    private val imageCropEditorState = MutableStateFlow(ExerciseImageCropEditorState())

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
            }.combine(muscleEditorState) { combinedState, muscleEditor ->
                combinedState.copy(muscleEditor = muscleEditor)
            }.combine(imageCropEditorState) { combinedState, imageCropEditor ->
                combinedState.copy(imageCropEditor = imageCropEditor)
            },
            filterState,
            imageViewerState,
            transferState,
            workoutMembershipUiState,
        ) { combinedState, filters, imageViewer, transfer, workoutMembership ->

            val normalizedQuery = filters.query.trim()
            val filteredExercises = combinedState.exercises.filter { exercise ->
                val matchesTags = filters.tagIds.isEmpty() ||
                    exercise.tags.any { tag -> tag.id in filters.tagIds }
                val matchesQuery = normalizedQuery.isEmpty() ||
                    exercise.name.contains(normalizedQuery, ignoreCase = true)

                matchesTags && matchesQuery
            }

            ExerciseCatalogUiState(
                exercises = filteredExercises,
                allExercises = combinedState.exercises,
                allTags = combinedState.tags,
                selectedFilterTagIds = filters.tagIds,
                searchQuery = filters.query,
                editor = combinedState.editor,
                tagEditor = combinedState.tagEditor,
                alternativeEditor = combinedState.alternativeEditor,
                muscleEditor = combinedState.muscleEditor,
                imageViewer = imageViewer,
                imageCropEditor = combinedState.imageCropEditor,
                transfer = transfer,
                workoutMembership = workoutMembership,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExerciseCatalogUiState(),
        )

    fun onAddExerciseClick() {
        nextDraftImageKey = -1L
        editorState.value = ExerciseEditorState(
            isVisible = true,
            name = "",
            description = "",
        )
    }

    fun onEditExerciseClick(exercise: ExerciseDefinition) {
        nextDraftImageKey = -1L
        editorState.value = ExerciseEditorState(
            isVisible = true,
            exerciseId = exercise.id,
            name = exercise.name,
            description = exercise.description,
            selectedTagIds = exercise.tags.map { tag -> tag.id }.toSet(),
            selectedAlternativeExerciseDefinitionIds = exercise.alternatives
                .map { alternative -> alternative.id }
                .toSet(),
            selectedPrimaryMuscleGroups = exercise.muscles.toSet(),
            selectedSecondaryMuscleGroups = exercise.secondaryMuscles.toSet(),
            images = exercise.images.map { image ->
                ExerciseEditorImageState(
                    key = image.id,
                    sourceImageId = image.id,
                    uri = image.uri,
                    isCover = image.isCover,
                    cropZoom = image.cropZoom,
                    cropOffsetX = image.cropOffsetX,
                    cropOffsetY = image.cropOffsetY,
                )
            },
        )
    }

    fun onCopyExerciseClick(exercise: ExerciseDefinition) {
        nextDraftImageKey = -1L
        editorState.value = ExerciseEditorState(
            isVisible = true,
            templateExerciseId = exercise.id,
            name = buildCopyExerciseName(exercise.name),
            description = exercise.description,
            selectedTagIds = exercise.tags.map { tag -> tag.id }.toSet(),
            selectedAlternativeExerciseDefinitionIds = exercise.alternatives
                .map { alternative -> alternative.id }
                .toSet(),
            selectedPrimaryMuscleGroups = exercise.muscles.toSet(),
            selectedSecondaryMuscleGroups = exercise.secondaryMuscles.toSet(),
            images = exercise.images.map { image ->
                ExerciseEditorImageState(
                    key = nextDraftImageKey--,
                    sourceImageId = null,
                    uri = image.uri,
                    isCover = image.isCover,
                    cropZoom = image.cropZoom,
                    cropOffsetX = image.cropOffsetX,
                    cropOffsetY = image.cropOffsetY,
                )
            },
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

    fun onEditorToggleTag(tagId: Long) {
        val current = editorState.value
        editorState.value = current.copy(
            selectedTagIds = current.selectedTagIds.toggle(tagId),
        )
    }

    fun onEditorNewTagNameChange(value: String) {
        editorState.value = editorState.value.copy(
            newTagName = value,
            newTagNameError = null,
        )
    }

    fun onEditorAddTag() {
        val current = editorState.value
        val normalizedName = current.newTagName
            .trim()
            .removePrefix("#")
            .trim()

        if (normalizedName.isBlank()) {
            editorState.value = current.copy(
                newTagNameError = "Название тега обязательно",
            )
            return
        }

        val existingTag = uiState.value.allTags.firstOrNull { tag ->
            tag.name.equals(normalizedName, ignoreCase = true)
        }
        val existingPendingTag = current.pendingTagNames.firstOrNull { tagName ->
            tagName.equals(normalizedName, ignoreCase = true)
        }

        editorState.value = when {
            existingTag != null -> current.copy(
                selectedTagIds = current.selectedTagIds + existingTag.id,
                newTagName = "",
                newTagNameError = null,
            )
            existingPendingTag != null -> current.copy(
                newTagName = "",
                newTagNameError = null,
            )
            else -> current.copy(
                pendingTagNames = current.pendingTagNames + normalizedName,
                newTagName = "",
                newTagNameError = null,
            )
        }
    }

    fun onEditorRemovePendingTag(tagName: String) {
        val current = editorState.value
        editorState.value = current.copy(
            pendingTagNames = current.pendingTagNames - tagName,
        )
    }

    fun onEditorToggleAlternative(exerciseDefinitionId: Long) {
        val current = editorState.value
        editorState.value = current.copy(
            selectedAlternativeExerciseDefinitionIds = current
                .selectedAlternativeExerciseDefinitionIds
                .toggle(exerciseDefinitionId),
        )
    }

    fun onEditorMusclesClick() {
        val editor = editorState.value
        muscleEditorState.value = ExerciseMuscleEditorState(
            isVisible = true,
            exerciseDefinitionId = editor.exerciseId ?: 0L,
            exerciseName = editor.name.ifBlank { "Новое упражнение" },
            selectedPrimaryMuscleGroups = editor.selectedPrimaryMuscleGroups,
            selectedSecondaryMuscleGroups = editor.selectedSecondaryMuscleGroups,
        )
    }

    fun onEditorImagesClick() {
        editorState.value = editorState.value.copy(isImagesEditorVisible = true)
    }

    fun onDismissEditorImages() {
        editorState.value = editorState.value.copy(
            isImagesEditorVisible = false,
            cropImageKey = null,
        )
    }

    fun onEditorImagesSelected(uriStrings: List<String>) {
        if (uriStrings.isEmpty()) return

        val current = editorState.value
        val shouldFirstBecomeCover = current.images.none { image -> image.isCover }
        val newImages = uriStrings.mapIndexed { index, uri ->
            ExerciseEditorImageState(
                key = nextDraftImageKey--,
                uri = uri,
                isCover = shouldFirstBecomeCover && index == 0,
            )
        }

        editorState.value = current.copy(images = current.images + newImages)
    }

    fun onEditorSetCoverImage(imageKey: Long) {
        val current = editorState.value
        editorState.value = current.copy(
            images = current.images.map { image ->
                image.copy(isCover = image.key == imageKey)
            },
        )
    }

    fun onEditorDeleteImage(imageKey: Long) {
        val current = editorState.value
        val remaining = current.images.filterNot { image -> image.key == imageKey }
        val normalized = if (remaining.isNotEmpty() && remaining.none { image -> image.isCover }) {
            remaining.mapIndexed { index, image -> image.copy(isCover = index == 0) }
        } else {
            remaining
        }

        editorState.value = current.copy(
            images = normalized,
            cropImageKey = current.cropImageKey.takeIf { it != imageKey },
        )
    }

    fun onEditorOpenImageCrop(imageKey: Long) {
        if (editorState.value.images.none { image -> image.key == imageKey }) return
        editorState.value = editorState.value.copy(cropImageKey = imageKey)
    }

    fun onEditorDismissImageCrop() {
        editorState.value = editorState.value.copy(cropImageKey = null)
    }

    fun onEditorSaveImageCrop(
        imageKey: Long,
        cropZoom: Float,
        cropOffsetX: Float,
        cropOffsetY: Float,
    ) {
        val current = editorState.value
        editorState.value = current.copy(
            images = current.images.map { image ->
                if (image.key == imageKey) {
                    image.copy(
                        cropZoom = cropZoom.coerceIn(1f, 4f),
                        cropOffsetX = cropOffsetX.coerceIn(-1f, 1f),
                        cropOffsetY = cropOffsetY.coerceIn(-1f, 1f),
                    )
                } else {
                    image
                }
            },
            cropImageKey = null,
        )
    }

    fun onDismissEditor() {
        editorState.value = ExerciseEditorState()
        muscleEditorState.value = ExerciseMuscleEditorState()
    }

    fun onSaveExerciseClick() {
        val editor = editorState.value
        val name = editor.name.trim()

        if (name.isBlank()) {
            editorState.value = editor.copy(nameError = "Название обязательно")
            return
        }

        if (editor.isSaving) return

        editorState.value = editor.copy(isSaving = true)

        viewModelScope.launch {
            val exerciseId = editor.exerciseId ?: createExerciseDefinitionUseCase(
                name = editor.name,
                description = editor.description,
            )

            if (exerciseId <= 0L) {
                editorState.value = editor.copy(
                    isSaving = false,
                    nameError = "Не удалось сохранить упражнение",
                )
                return@launch
            }

            if (editor.exerciseId != null) {
                updateExerciseDefinitionUseCase(
                    id = exerciseId,
                    name = editor.name,
                    description = editor.description,
                )
            }

            val createdTagIds = editor.pendingTagNames
                .map { tagName -> createTagUseCase(tagName) }
                .filter { tagId -> tagId > 0L }
                .toSet()

            replaceExerciseDefinitionTagsUseCase(
                exerciseDefinitionId = exerciseId,
                tagIds = editor.selectedTagIds + createdTagIds,
            )

            replaceExerciseDefinitionAlternativesUseCase(
                exerciseDefinitionId = exerciseId,
                alternativeExerciseDefinitionIds = editor.selectedAlternativeExerciseDefinitionIds,
            )

            replaceExerciseDefinitionMusclesUseCase(
                exerciseDefinitionId = exerciseId,
                primaryMuscleGroups = editor.selectedPrimaryMuscleGroups,
                secondaryMuscleGroups = editor.selectedSecondaryMuscleGroups,
            )

            replaceExerciseImagesUseCase(
                exerciseDefinitionId = exerciseId,
                images = editor.images.map { image ->
                    ExerciseImageEdit(
                        sourceImageId = image.sourceImageId,
                        uri = image.uri,
                        isCover = image.isCover,
                        cropZoom = image.cropZoom,
                        cropOffsetX = image.cropOffsetX,
                        cropOffsetY = image.cropOffsetY,
                    )
                },
            )

            editorState.value = ExerciseEditorState()
        }
    }

    fun onArchiveExerciseClick(exerciseId: Long) {
        viewModelScope.launch {
            archiveExerciseDefinitionUseCase(exerciseId)
        }
    }

    fun onSearchQueryChange(value: String) {
        searchQuery.value = value
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

    fun onEditExerciseMusclesClick(exercise: ExerciseDefinition) {
        muscleEditorState.value = ExerciseMuscleEditorState(
            isVisible = true,
            exerciseDefinitionId = exercise.id,
            exerciseName = exercise.name,
            selectedPrimaryMuscleGroups = exercise.muscles.toSet(),
            selectedSecondaryMuscleGroups = exercise.secondaryMuscles.toSet(),
        )
    }

    fun onDismissMuscleEditor() {
        muscleEditorState.value = ExerciseMuscleEditorState()
    }

    fun onTogglePrimaryMuscleSelection(muscleGroup: MuscleGroup) {
        val current = muscleEditorState.value
        val selected = current.selectedPrimaryMuscleGroups
        val isSelected = muscleGroup in selected

        muscleEditorState.value = current.copy(
            selectedPrimaryMuscleGroups = if (isSelected) {
                selected - muscleGroup
            } else {
                selected + muscleGroup
            },
            selectedSecondaryMuscleGroups = if (isSelected) {
                current.selectedSecondaryMuscleGroups
            } else {
                current.selectedSecondaryMuscleGroups - muscleGroup
            },
        )
    }

    fun onToggleSecondaryMuscleSelection(muscleGroup: MuscleGroup) {
        val current = muscleEditorState.value
        val selected = current.selectedSecondaryMuscleGroups
        val isSelected = muscleGroup in selected

        muscleEditorState.value = current.copy(
            selectedSecondaryMuscleGroups = if (isSelected) {
                selected - muscleGroup
            } else {
                selected + muscleGroup
            },
            selectedPrimaryMuscleGroups = if (isSelected) {
                current.selectedPrimaryMuscleGroups
            } else {
                current.selectedPrimaryMuscleGroups - muscleGroup
            },
        )
    }

    fun onSaveExerciseMusclesClick() {
        val current = muscleEditorState.value
        val editor = editorState.value

        editorState.value = editor.copy(
            selectedPrimaryMuscleGroups = current.selectedPrimaryMuscleGroups,
            selectedSecondaryMuscleGroups = current.selectedSecondaryMuscleGroups,
        )
        muscleEditorState.value = ExerciseMuscleEditorState()
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

    fun onOpenImageCropEditor(imageId: Long) {
        if (imageId <= 0L) {
            return
        }

        imageCropEditorState.value = ExerciseImageCropEditorState(
            imageId = imageId,
        )
    }

    fun onDismissImageCropEditor() {
        imageCropEditorState.value = ExerciseImageCropEditorState()
    }

    fun onSaveImageCrop(
        exerciseDefinitionId: Long,
        imageId: Long,
        cropZoom: Float,
        cropOffsetX: Float,
        cropOffsetY: Float,
    ) {
        viewModelScope.launch {
            updateExerciseImageCropUseCase(
                exerciseDefinitionId = exerciseDefinitionId,
                imageId = imageId,
                cropZoom = cropZoom,
                cropOffsetX = cropOffsetX,
                cropOffsetY = cropOffsetY,
            )

            imageCropEditorState.value = ExerciseImageCropEditorState()
        }
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

    private fun buildCopyExerciseName(sourceName: String): String {
        val normalizedName = sourceName.trimEnd()

        return if (normalizedName.endsWith("*")) {
            "$normalizedName*"
        } else {
            "$normalizedName *"
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

private data class ExerciseCatalogFilterState(
    val tagIds: Set<Long> = emptySet(),
    val query: String = "",
)

private data class ExerciseCatalogCombinedState(
    val exercises: List<ExerciseDefinition>,
    val tags: List<Tag>,
    val editor: ExerciseEditorState,
    val tagEditor: ExerciseTagEditorState,
    val alternativeEditor: ExerciseAlternativeEditorState,
    val muscleEditor: ExerciseMuscleEditorState = ExerciseMuscleEditorState(),
    val imageCropEditor: ExerciseImageCropEditorState = ExerciseImageCropEditorState(),
)

data class ExerciseCatalogUiState(
    val exercises: List<ExerciseDefinition> = emptyList(),
    val allExercises: List<ExerciseDefinition> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val selectedFilterTagIds: Set<Long> = emptySet(),
    val searchQuery: String = "",
    val editor: ExerciseEditorState = ExerciseEditorState(),
    val tagEditor: ExerciseTagEditorState = ExerciseTagEditorState(),
    val alternativeEditor: ExerciseAlternativeEditorState = ExerciseAlternativeEditorState(),
    val muscleEditor: ExerciseMuscleEditorState = ExerciseMuscleEditorState(),
    val imageViewer: ExerciseImageViewerState = ExerciseImageViewerState(),
    val imageCropEditor: ExerciseImageCropEditorState = ExerciseImageCropEditorState(),
    val transfer: ExerciseTransferState = ExerciseTransferState(),
    val workoutMembership: ExerciseWorkoutMembershipState = ExerciseWorkoutMembershipState(),
)

data class ExerciseEditorState(
    val isVisible: Boolean = false,
    val exerciseId: Long? = null,
    val templateExerciseId: Long? = null,
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
    val selectedTagIds: Set<Long> = emptySet(),
    val pendingTagNames: Set<String> = emptySet(),
    val newTagName: String = "",
    val newTagNameError: String? = null,
    val selectedAlternativeExerciseDefinitionIds: Set<Long> = emptySet(),
    val selectedPrimaryMuscleGroups: Set<MuscleGroup> = emptySet(),
    val selectedSecondaryMuscleGroups: Set<MuscleGroup> = emptySet(),
    val images: List<ExerciseEditorImageState> = emptyList(),
    val isImagesEditorVisible: Boolean = false,
    val cropImageKey: Long? = null,
    val isSaving: Boolean = false,
) {
    val isEditing: Boolean
        get() = exerciseId != null

    val isCopying: Boolean
        get() = templateExerciseId != null
}

data class ExerciseEditorImageState(
    val key: Long,
    val sourceImageId: Long? = null,
    val uri: String,
    val isCover: Boolean,
    val cropZoom: Float = 1f,
    val cropOffsetX: Float = 0f,
    val cropOffsetY: Float = 0f,
)

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

data class ExerciseMuscleEditorState(
    val isVisible: Boolean = false,
    val exerciseDefinitionId: Long = 0L,
    val exerciseName: String = "",
    val selectedPrimaryMuscleGroups: Set<MuscleGroup> = emptySet(),
    val selectedSecondaryMuscleGroups: Set<MuscleGroup> = emptySet(),
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


data class ExerciseImageCropEditorState(
    val imageId: Long? = null,
) {
    val isVisible: Boolean
        get() = imageId != null
}

data class ExerciseTransferState(
    val isInProgress: Boolean = false,
    val message: String? = null,
)
