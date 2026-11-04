package ru.trainingapp.feature.workout_editor

import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.WorkoutMuscleBalance

data class WorkoutEditorUiState(
    val workoutId: Long,
    val title: String = "",
    val description: String? = null,
    val isLoading: Boolean = true,
    val exercises: List<WorkoutExerciseUi> = emptyList(),
    val availableExercises: List<ExerciseDefinition> = emptyList(),
    val addExerciseSearchQuery: String = "",
    val isAddExerciseDialogVisible: Boolean = false,
    val targetDurationMinutes: Int? = null,
    val timerStartedAt: Long? = null,
    val timerElapsedMillis: Long = 0L,
    val timerIsFinished: Boolean = false,
    val muscleBalance: WorkoutMuscleBalance = WorkoutMuscleBalance(
        totalExercises = 0,
        exercisesWithMuscles = 0,
        items = emptyList(),
    ),
    val errorMessage: String? = null,
)
