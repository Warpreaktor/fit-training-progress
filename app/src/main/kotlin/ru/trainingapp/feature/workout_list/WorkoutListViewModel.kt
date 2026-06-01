package ru.trainingapp.feature.workout_list

import androidx.lifecycle.ViewModel
import ru.trainingapp.core.model.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WorkoutListViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(
        WorkoutListUiState(
            workouts = listOf(
                Workout(
                    id = 1L,
                    name = "Тренировка №1",
                    description = "Стартовая заглушка до подключения Room",
                    isLocked = false,
                    checkedExercisesCount = 0,
                    exercisesCount = 0,
                ),
            ),
        ),
    )
    val uiState: StateFlow<WorkoutListUiState> = _uiState
}

data class WorkoutListUiState(
    val workouts: List<Workout> = emptyList(),
    val isLoading: Boolean = false,
)
