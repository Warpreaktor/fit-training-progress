package ru.trainingapp.feature.workout_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.WorkoutExercise
import ru.trainingapp.navigation.AppRoute
import javax.inject.Inject

@HiltViewModel
class WorkoutEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val workoutId: Long = requireNotNull(
        savedStateHandle.get<Long>(AppRoute.WorkoutEditor.ARG_WORKOUT_ID)
    ) {
        "Missing workoutId navigation argument"
    }

    private val _uiState = MutableStateFlow(
        WorkoutEditorUiState(
            workoutId = workoutId,
            title = "Редактор тренировки",
        ),
    )

    val uiState: StateFlow<WorkoutEditorUiState> = _uiState
}

data class WorkoutEditorUiState(
    val workoutId: Long,
    val title: String,
    val isLoading: Boolean = false,
    val exercises: List<WorkoutExercise> = emptyList(),
    val availableExercises: List<ExerciseDefinition> = emptyList(),
    val isAddExerciseDialogVisible: Boolean = false,
    val errorMessage: String? = null,
)