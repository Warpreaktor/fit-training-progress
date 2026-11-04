package ru.trainingapp.core.model

data class WorkoutMuscleBalance(
    val totalExercises: Int,
    val exercisesWithMuscles: Int,
    val items: List<WorkoutMuscleBalanceItem>,
) {
    val exercisesWithoutMuscles: Int
        get() = (totalExercises - exercisesWithMuscles).coerceAtLeast(0)
}

data class WorkoutMuscleBalanceItem(
    val muscleGroup: MuscleGroup,
    val exerciseCount: Int,
    val score: Float,
    val share: Float,
)
