package ru.trainingapp.core.model

data class WorkoutExerciseProgressPoint(
    val id: Long,
    val workoutId: Long,
    val workoutExerciseId: Long,
    val exerciseDefinitionId: Long,
    val exerciseNameSnapshot: String,
    val createdAt: Long,
    val revision: Int,
    val reason: String,
    val sets: List<WorkoutExerciseProgressSet>,
)

data class WorkoutExerciseProgressSet(
    val id: Long,
    val progressPointId: Long,
    val setNumber: Int,
    val reps: Int,
    val loadType: WorkoutExerciseSetLoadType,
    val weightValue: Double?,
    val weightUnit: WeightUnit?,
    val durationSeconds: Int?,
)