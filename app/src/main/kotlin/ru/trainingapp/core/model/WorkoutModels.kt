package ru.trainingapp.core.model

data class Workout(
    val id: Long,
    val name: String,
    val description: String?,
    val isLocked: Boolean,
    val checkedExercisesCount: Int,
    val exercisesCount: Int,
)

data class ExerciseDefinition(
    val id: Long,
    val name: String,
    val description: String?,
)

data class WorkoutExercise(
    val id: Long,
    val workoutId: Long,
    val exerciseDefinitionId: Long,
    val exerciseName: String,
    val sortOrder: Int,
    val comment: String?,
    val isChecked: Boolean,
    val sets: List<WorkoutExerciseSet>,
)

data class WorkoutExerciseSet(
    val id: Long,
    val setNumber: Int,
    val reps: Int?,
    val weightValue: Double?,
    val weightUnit: WeightUnit?,
    val durationSeconds: Int?,
)
