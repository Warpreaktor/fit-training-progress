package ru.trainingapp.feature.workout_editor

sealed interface WorkoutEditorAction {

    data object AddExerciseClick : WorkoutEditorAction

    data object DismissAddExerciseDialog : WorkoutEditorAction

    data object ErrorMessageShown : WorkoutEditorAction

    data class ExerciseSelected(
        val exerciseDefinitionId: Long,
    ) : WorkoutEditorAction

    data class ArchiveExerciseClick(
        val workoutExerciseId: Long,
    ) : WorkoutEditorAction

    data class AddSetClick(
        val workoutExerciseId: Long,
    ) : WorkoutEditorAction

    data class RemoveSetClick(
        val workoutExerciseSetId: Long,
    ) : WorkoutEditorAction

    data class MoveExerciseUpClick(
        val workoutExerciseId: Long,
    ) : WorkoutEditorAction

    data class MoveExerciseDownClick(
        val workoutExerciseId: Long,
    ) : WorkoutEditorAction
}