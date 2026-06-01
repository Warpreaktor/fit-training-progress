package ru.trainingapp.feature.workout_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WorkoutEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val workoutId: Long = savedStateHandle["workoutId"] ?: 0L

    private val _uiState = MutableStateFlow(
        WorkoutEditorUiState(
            workoutId = workoutId,
            title = "Живой редактор тренировки",
        ),
    )
    val uiState: StateFlow<WorkoutEditorUiState> = _uiState
}

data class WorkoutEditorUiState(
    val workoutId: Long,
    val title: String,
)
