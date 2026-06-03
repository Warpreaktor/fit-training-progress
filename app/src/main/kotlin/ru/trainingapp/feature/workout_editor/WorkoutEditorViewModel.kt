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
import kotlinx.coroutines.launch
import ru.trainingapp.core.domain.exercise.ObserveExerciseDefinitionsUseCase
import ru.trainingapp.core.domain.workout.AddExerciseToWorkoutUseCase
import ru.trainingapp.core.domain.workout.AddWorkoutExerciseSetUseCase
import ru.trainingapp.core.domain.workout.ArchiveWorkoutExerciseUseCase
import ru.trainingapp.core.domain.workout.MoveWorkoutExerciseUseCase
import ru.trainingapp.core.domain.workout.ObserveWorkoutEditorUseCase
import ru.trainingapp.core.domain.workout.RemoveWorkoutExerciseSetUseCase

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
) : ViewModel() {

    private val workoutId: Long = requireNotNull(
        savedStateHandle.get<Long>(AppRoute.WorkoutEditor.ARG_WORKOUT_ID)
    ) {
        "Missing workoutId navigation argument"
    }

    private val isAddExerciseDialogVisible = MutableStateFlow(false)

    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<WorkoutEditorUiState> = combine(
        observeWorkoutEditorUseCase(workoutId),
        observeExerciseDefinitionsUseCase(),
        isAddExerciseDialogVisible,
        errorMessage,
    ) { editorData, availableExercises, isDialogVisible, error ->
        WorkoutEditorUiState(
            workoutId = workoutId,
            title = editorData?.workout?.name.orEmpty(),
            description = editorData?.workout?.description,
            isLoading = editorData == null,
            exercises = editorData?.exercises.orEmpty(),
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
}