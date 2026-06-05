package ru.trainingapp.feature.workout_editor

import ru.trainingapp.core.model.ExerciseDefinition

data class WorkoutEditorUiState(
    val workoutId: Long,
    val title: String = "",
    val description: String? = null,
    val isLoading: Boolean = true,
    val exercises: List<WorkoutExerciseUi> = emptyList(),
    val availableExercises: List<ExerciseDefinition> = emptyList(),
    val isAddExerciseDialogVisible: Boolean = false,
    val errorMessage: String? = null,
)