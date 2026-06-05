package ru.trainingapp.feature.workout_editor

import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExerciseSetLoadType

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

    data class SetRepsChanged(
        val workoutExerciseSetId: Long,
        val value: String,
    ) : WorkoutEditorAction

    data class SetLoadTypeChanged(
        val workoutExerciseSetId: Long,
        val loadType: WorkoutExerciseSetLoadType,
    ) : WorkoutEditorAction

    data class SetWeightChanged(
        val workoutExerciseSetId: Long,
        val value: String,
    ) : WorkoutEditorAction

    data class SetWeightUnitChanged(
        val workoutExerciseSetId: Long,
        val weightUnit: WeightUnit,
    ) : WorkoutEditorAction

    data class SetDurationSecondsChanged(
        val workoutExerciseSetId: Long,
        val value: String,
    ) : WorkoutEditorAction
}