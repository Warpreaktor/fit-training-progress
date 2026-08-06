package ru.trainingapp.feature.workout_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.trainingapp.core.domain.exercise.ObserveExerciseDefinitionsUseCase
import ru.trainingapp.core.domain.workout.AddExerciseToWorkoutUseCase
import ru.trainingapp.core.domain.workout.AddWorkoutExerciseSetUseCase
import ru.trainingapp.core.domain.workout.ArchiveWorkoutExerciseUseCase
import ru.trainingapp.core.domain.workout.CommitPendingProgressUseCase
import ru.trainingapp.core.domain.workout.MoveWorkoutExerciseUseCase
import ru.trainingapp.core.domain.workout.ObserveWorkoutEditorUseCase
import ru.trainingapp.core.domain.workout.RemoveWorkoutExerciseSetUseCase
import ru.trainingapp.core.domain.workout.ResetWorkoutCheckmarksUseCase
import ru.trainingapp.core.domain.workout.ToggleWorkoutExerciseCheckedUseCase
import ru.trainingapp.core.domain.workout.UpdateWorkoutExerciseSetUseCase
import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.navigation.AppRoute

@HiltViewModel
class WorkoutEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeWorkoutEditorUseCase: ObserveWorkoutEditorUseCase,
    observeExerciseDefinitionsUseCase: ObserveExerciseDefinitionsUseCase,
    private val addExerciseToWorkoutUseCase: AddExerciseToWorkoutUseCase,
    private val archiveWorkoutExerciseUseCase: ArchiveWorkoutExerciseUseCase,
    private val addWorkoutExerciseSetUseCase: AddWorkoutExerciseSetUseCase,
    private val removeWorkoutExerciseSetUseCase: RemoveWorkoutExerciseSetUseCase,
    private val moveWorkoutExerciseUseCase: MoveWorkoutExerciseUseCase,
    private val updateWorkoutExerciseSetUseCase: UpdateWorkoutExerciseSetUseCase,
    private val toggleWorkoutExerciseCheckedUseCase: ToggleWorkoutExerciseCheckedUseCase,
    private val resetWorkoutCheckmarksUseCase: ResetWorkoutCheckmarksUseCase,
    private val commitPendingProgressUseCase: CommitPendingProgressUseCase,
) : ViewModel() {

    private val workoutId: Long = requireNotNull(
        savedStateHandle.get<Long>(
            AppRoute.WorkoutEditor.ARG_WORKOUT_ID,
        )
    ) {
        "Missing workoutId navigation argument"
    }

    private val isAddExerciseDialogVisible = MutableStateFlow(false)

    private val errorMessage = MutableStateFlow<String?>(null)

    private val setDrafts =
        MutableStateFlow<Map<Long, WorkoutExerciseSetDraft>>(emptyMap())

    private val addExerciseSearchQuery = MutableStateFlow("")

    private val addExercisePickerState = combine(
        observeExerciseDefinitionsUseCase(),
        addExerciseSearchQuery,
    ) { exercises, query ->
        AddExercisePickerState(
            query = query,
            exercises = filterAvailableExercises(
                exercises = exercises,
                query = query,
            ),
        )
    }

    val uiState: StateFlow<WorkoutEditorUiState> = combine(
        observeWorkoutEditorUseCase(workoutId),
        addExercisePickerState,
        isAddExerciseDialogVisible,
        errorMessage,
        setDrafts,
    ) { editorData, exercisePickerState, isDialogVisible, error, drafts ->
        WorkoutEditorUiState(
            workoutId = workoutId,
            title = editorData?.workout?.name.orEmpty(),
            description = editorData?.workout?.description,
            isLoading = editorData == null,
            exercises = editorData
                ?.exercises
                .orEmpty()
                .map { exercise ->
                    exercise.toUi(
                        setDrafts = drafts,
                    )
                },
            availableExercises = exercisePickerState.exercises,
            addExerciseSearchQuery = exercisePickerState.query,
            isAddExerciseDialogVisible = isDialogVisible,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutEditorUiState(
            workoutId = workoutId,
            isLoading = true,
        ),
    )

    fun onAction(
        action: WorkoutEditorAction,
    ) {
        when (action) {
            WorkoutEditorAction.AddExerciseClick -> {
                addExerciseSearchQuery.value = ""
                isAddExerciseDialogVisible.value = true
            }

            WorkoutEditorAction.DismissAddExerciseDialog -> {
                isAddExerciseDialogVisible.value = false
                addExerciseSearchQuery.value = ""
            }

            is WorkoutEditorAction.AddExerciseSearchQueryChanged -> {
                addExerciseSearchQuery.value = action.query
            }

            WorkoutEditorAction.ErrorMessageShown -> {
                errorMessage.value = null
            }

            is WorkoutEditorAction.ExerciseSelected -> {
                addExerciseToWorkout(
                    exerciseDefinitionId = action.exerciseDefinitionId,
                )
            }

            is WorkoutEditorAction.ArchiveExerciseClick -> {
                archiveWorkoutExercise(
                    workoutExerciseId = action.workoutExerciseId,
                )
            }

            is WorkoutEditorAction.AddSetClick -> {
                addWorkoutExerciseSet(
                    workoutExerciseId = action.workoutExerciseId,
                )
            }

            is WorkoutEditorAction.RemoveSetClick -> {
                removeWorkoutExerciseSet(
                    workoutExerciseSetId = action.workoutExerciseSetId,
                )
            }

            is WorkoutEditorAction.MoveExerciseUpClick -> {
                moveWorkoutExerciseUp(
                    workoutExerciseId = action.workoutExerciseId,
                )
            }

            is WorkoutEditorAction.MoveExerciseDownClick -> {
                moveWorkoutExerciseDown(
                    workoutExerciseId = action.workoutExerciseId,
                )
            }

            is WorkoutEditorAction.SetRepsChanged -> {
                updateSetReps(
                    workoutExerciseSetId = action.workoutExerciseSetId,
                    value = action.value,
                )
            }

            is WorkoutEditorAction.SetQuantityChanged -> {
                updateSetQuantity(
                    workoutExerciseSetId = action.workoutExerciseSetId,
                    value = action.value,
                )
            }

            is WorkoutEditorAction.SetMeasurementUnitChanged -> {
                updateSetMeasurementUnit(
                    workoutExerciseSetId = action.workoutExerciseSetId,
                    unit = action.unit,
                )
            }

            is WorkoutEditorAction.ExerciseCheckedChanged -> {
                updateExerciseChecked(
                    workoutExerciseId = action.workoutExerciseId,
                    isChecked = action.isChecked,
                )
            }

            WorkoutEditorAction.ResetCheckmarksClick -> {
                resetWorkoutCheckmarks()
            }
        }
    }

    fun commitPendingProgress() {
        launchOperation {
            commitPendingProgressUseCase(workoutId)
        }
    }

    fun commitPendingProgressAndThen(
        onComplete: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                commitPendingProgressUseCase(workoutId)
                onComplete()
            } catch (exception: Exception) {
                errorMessage.value = exception.message
                    ?: "Не удалось зафиксировать прогресс"
            }
        }
    }

    private fun resetWorkoutCheckmarks() {
        launchOperation {
            resetWorkoutCheckmarksUseCase(workoutId)
        }
    }

    private fun addExerciseToWorkout(
        exerciseDefinitionId: Long,
    ) {
        isAddExerciseDialogVisible.value = false
        addExerciseSearchQuery.value = ""

        launchOperation {
            addExerciseToWorkoutUseCase(
                workoutId = workoutId,
                exerciseDefinitionId = exerciseDefinitionId,
            )
        }
    }

    private fun archiveWorkoutExercise(
        workoutExerciseId: Long,
    ) {
        launchOperation {
            archiveWorkoutExerciseUseCase(workoutExerciseId)
        }
    }

    private fun addWorkoutExerciseSet(
        workoutExerciseId: Long,
    ) {
        launchOperation {
            addWorkoutExerciseSetUseCase(workoutExerciseId)
        }
    }

    private fun removeWorkoutExerciseSet(
        workoutExerciseSetId: Long,
    ) {
        launchOperation {
            removeWorkoutExerciseSetUseCase(workoutExerciseSetId)
        }
    }

    private fun moveWorkoutExerciseUp(
        workoutExerciseId: Long,
    ) {
        launchOperation {
            moveWorkoutExerciseUseCase.up(
                workoutId = workoutId,
                workoutExerciseId = workoutExerciseId,
            )
        }
    }

    private fun moveWorkoutExerciseDown(
        workoutExerciseId: Long,
    ) {
        launchOperation {
            moveWorkoutExerciseUseCase.down(
                workoutId = workoutId,
                workoutExerciseId = workoutExerciseId,
            )
        }
    }

    private fun filterAvailableExercises(
        exercises: List<ExerciseDefinition>,
        query: String,
    ): List<ExerciseDefinition> {
        val normalizedQuery = query.normalizeForSearch()

        if (normalizedQuery.isBlank()) {
            return exercises
        }

        val queryParts = normalizedQuery
            .split(" ")
            .filter { part ->
                part.isNotBlank()
            }

        val firstQueryPart = queryParts.first()

        return exercises
            .filter { exercise ->
                val normalizedName = exercise.name.normalizeForSearch()

                queryParts.all { part ->
                    normalizedName.contains(part)
                }
            }
            .sortedWith(
                compareBy<ExerciseDefinition> { exercise ->
                    val normalizedName =
                        exercise.name.normalizeForSearch()

                    when {
                        normalizedName.startsWith(firstQueryPart) -> {
                            0
                        }

                        normalizedName
                            .split(" ")
                            .any { word ->
                                word.startsWith(firstQueryPart)
                            } -> {
                            1
                        }

                        else -> {
                            2
                        }
                    }
                }.thenBy { exercise ->
                    exercise.name.normalizeForSearch()
                }
            )
    }

    private fun String.normalizeForSearch(): String {
        return trim()
            .lowercase()
            .replace('ё', 'е')
            .replace(
                regex = Regex("""\s+"""),
                replacement = " ",
            )
    }

    private fun launchOperation(
        block: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (exception: Exception) {
                errorMessage.value = exception.message
                    ?: "Не удалось выполнить действие"
            }
        }
    }

    private fun updateSetDraft(
        workoutExerciseSetId: Long,
        transform: (WorkoutExerciseSetDraft) -> WorkoutExerciseSetDraft,
    ) {
        setDrafts.update { currentDrafts ->
            val currentDraft =
                currentDrafts[workoutExerciseSetId]
                    ?: WorkoutExerciseSetDraft()

            currentDrafts + (
                    workoutExerciseSetId to transform(currentDraft)
                    )
        }
    }

    private fun updateSetReps(
        workoutExerciseSetId: Long,
        value: String,
    ) {
        if (!value.isDigitsOnlyOrBlank()) {
            return
        }

        updateSetDraft(
            workoutExerciseSetId = workoutExerciseSetId,
        ) { draft ->
            draft.copy(
                repsText = value,
            )
        }

        val reps = value.toIntOrNull() ?: return

        launchOperation {
            updateWorkoutExerciseSetUseCase(
                UpdateWorkoutExerciseSetUseCase.Command.UpdateReps(
                    setId = workoutExerciseSetId,
                    reps = reps,
                )
            )
        }
    }

    private fun updateSetQuantity(
        workoutExerciseSetId: Long,
        value: String,
    ) {
        if (!value.isDecimalDraft()) {
            return
        }

        updateSetDraft(
            workoutExerciseSetId = workoutExerciseSetId,
        ) { draft ->
            draft.copy(
                quantityText = value,
            )
        }

        val parsedQuantity = value.parseNullableDouble()

        if (parsedQuantity is ParsedNumber.Invalid) {
            return
        }

        val quantity = (parsedQuantity as ParsedNumber.Valid).value

        launchOperation {
            updateWorkoutExerciseSetUseCase(
                UpdateWorkoutExerciseSetUseCase.Command.UpdateQuantity(
                    setId = workoutExerciseSetId,
                    value = quantity,
                )
            )
        }
    }

    private fun updateSetMeasurementUnit(
        workoutExerciseSetId: Long,
        unit: WeightUnit,
    ) {
        launchOperation {
            updateWorkoutExerciseSetUseCase(
                UpdateWorkoutExerciseSetUseCase.Command.UpdateMeasurementUnit(
                    setId = workoutExerciseSetId,
                    unit = unit,
                )
            )
        }
    }

    private fun updateExerciseChecked(
        workoutExerciseId: Long,
        isChecked: Boolean,
    ) {
        launchOperation {
            toggleWorkoutExerciseCheckedUseCase(
                workoutExerciseId = workoutExerciseId,
                isChecked = isChecked,
            )
        }
    }

    private sealed interface ParsedNumber<out T> {

        data object Invalid : ParsedNumber<Nothing>

        data class Valid<T>(
            val value: T?,
        ) : ParsedNumber<T>
    }

    private fun String.isDigitsOnlyOrBlank(): Boolean {
        return all { character ->
            character.isDigit()
        }
    }

    private fun String.isDecimalDraft(): Boolean {
        return isEmpty() || matches(
            Regex("""\d*([.,]\d*)?""")
        )
    }

    private fun String.parseNullableDouble(): ParsedNumber<Double> {
        if (isBlank()) {
            return ParsedNumber.Valid(null)
        }

        if (endsWith('.') || endsWith(',')) {
            return ParsedNumber.Invalid
        }

        return replace(',', '.')
            .toDoubleOrNull()
            ?.let { value ->
                ParsedNumber.Valid(value)
            }
            ?: ParsedNumber.Invalid
    }
}

private data class AddExercisePickerState(
    val query: String,
    val exercises: List<ExerciseDefinition>,
)