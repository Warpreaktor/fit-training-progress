package ru.trainingapp.feature.workout_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.trainingapp.navigation.AppRoute
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
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
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExerciseSetLoadType

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
        savedStateHandle.get<Long>(AppRoute.WorkoutEditor.ARG_WORKOUT_ID)
    ) {
        "Missing workoutId navigation argument"
    }

    private val isAddExerciseDialogVisible = MutableStateFlow(false)

    private val errorMessage = MutableStateFlow<String?>(null)

    private val setDrafts = MutableStateFlow<Map<Long, WorkoutExerciseSetDraft>>(emptyMap())

    val uiState: StateFlow<WorkoutEditorUiState> = combine(
        observeWorkoutEditorUseCase(workoutId),
        observeExerciseDefinitionsUseCase(),
        isAddExerciseDialogVisible,
        errorMessage,
        setDrafts,
    ) { editorData, availableExercises, isDialogVisible, error, drafts ->
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
            availableExercises = availableExercises,
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
                isAddExerciseDialogVisible.value = true
            }

            WorkoutEditorAction.DismissAddExerciseDialog -> {
                isAddExerciseDialogVisible.value = false
            }

            WorkoutEditorAction.ErrorMessageShown -> {
                errorMessage.value = null
            }

            is WorkoutEditorAction.ExerciseSelected -> {
                addExerciseToWorkout(action.exerciseDefinitionId)
            }

            is WorkoutEditorAction.ArchiveExerciseClick -> {
                archiveWorkoutExercise(action.workoutExerciseId)
            }

            is WorkoutEditorAction.AddSetClick -> {
                addWorkoutExerciseSet(action.workoutExerciseId)
            }

            is WorkoutEditorAction.RemoveSetClick -> {
                removeWorkoutExerciseSet(action.workoutExerciseSetId)
            }

            is WorkoutEditorAction.MoveExerciseUpClick -> {
                moveWorkoutExerciseUp(action.workoutExerciseId)
            }

            is WorkoutEditorAction.MoveExerciseDownClick -> {
                moveWorkoutExerciseDown(action.workoutExerciseId)
            }

            is WorkoutEditorAction.SetRepsChanged -> {
                updateSetReps(
                    workoutExerciseSetId = action.workoutExerciseSetId,
                    value = action.value,
                )
            }

            is WorkoutEditorAction.SetLoadTypeChanged -> {
                updateSetLoadType(
                    workoutExerciseSetId = action.workoutExerciseSetId,
                    loadType = action.loadType,
                )
            }

            is WorkoutEditorAction.SetWeightChanged -> {
                updateSetWeight(
                    workoutExerciseSetId = action.workoutExerciseSetId,
                    value = action.value,
                )
            }

            is WorkoutEditorAction.SetWeightUnitChanged -> {
                updateSetWeightUnit(
                    workoutExerciseSetId = action.workoutExerciseSetId,
                    weightUnit = action.weightUnit,
                )
            }

            is WorkoutEditorAction.SetDurationSecondsChanged -> {
                updateSetDurationSeconds(
                    workoutExerciseSetId = action.workoutExerciseSetId,
                    value = action.value,
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
            val currentDraft = currentDrafts[workoutExerciseSetId] ?: WorkoutExerciseSetDraft()

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

    private fun updateSetLoadType(
        workoutExerciseSetId: Long,
        loadType: WorkoutExerciseSetLoadType,
    ) {
        updateSetDraft(
            workoutExerciseSetId = workoutExerciseSetId,
        ) { draft ->
            when (loadType) {
                WorkoutExerciseSetLoadType.WEIGHT -> {
                    draft.copy(
                        durationSecondsText = null,
                    )
                }

                WorkoutExerciseSetLoadType.TIME -> {
                    draft.copy(
                        weightText = null,
                    )
                }
            }
        }

        launchOperation {
            updateWorkoutExerciseSetUseCase(
                UpdateWorkoutExerciseSetUseCase.Command.ChangeLoadType(
                    setId = workoutExerciseSetId,
                    loadType = loadType,
                )
            )
        }
    }

    private fun updateSetWeight(
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
                weightText = value,
            )
        }

        val parsedWeight = value.parseNullableDouble()

        if (parsedWeight is ParsedNumber.Invalid) {
            return
        }

        val weightValue = (parsedWeight as ParsedNumber.Valid).value

        launchOperation {
            updateWorkoutExerciseSetUseCase(
                UpdateWorkoutExerciseSetUseCase.Command.UpdateWeight(
                    setId = workoutExerciseSetId,
                    value = weightValue,
                )
            )
        }
    }

    private fun updateSetWeightUnit(
        workoutExerciseSetId: Long,
        weightUnit: WeightUnit,
    ) {
        launchOperation {
            updateWorkoutExerciseSetUseCase(
                UpdateWorkoutExerciseSetUseCase.Command.UpdateWeightUnit(
                    setId = workoutExerciseSetId,
                    unit = weightUnit,
                )
            )
        }
    }

    private fun updateSetDurationSeconds(
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
                durationSecondsText = value,
            )
        }

        val parsedDuration = value.parseNullableInt()

        if (parsedDuration is ParsedNumber.Invalid) {
            return
        }

        val durationSeconds = (parsedDuration as ParsedNumber.Valid).value

        launchOperation {
            updateWorkoutExerciseSetUseCase(
                UpdateWorkoutExerciseSetUseCase.Command.UpdateDurationSeconds(
                    setId = workoutExerciseSetId,
                    durationSeconds = durationSeconds,
                )
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
        return all { character -> character.isDigit() }
    }

    private fun String.isDecimalDraft(): Boolean {
        return isEmpty() || matches(Regex("""\d*([.,]\d*)?"""))
    }

    private fun String.parseNullableInt(): ParsedNumber<Int> {
        if (isBlank()) {
            return ParsedNumber.Valid(null)
        }

        return toIntOrNull()
            ?.let { value -> ParsedNumber.Valid(value) }
            ?: ParsedNumber.Invalid
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
            ?.let { value -> ParsedNumber.Valid(value) }
            ?: ParsedNumber.Invalid
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
}