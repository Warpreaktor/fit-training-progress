package ru.trainingapp.core.model

sealed interface WorkoutExerciseSetLoad {

    data class Weight(
        val value: Double?,
        val unit: WeightUnit,
    ) : WorkoutExerciseSetLoad

    data class Time(
        val durationSeconds: Int?,
    ) : WorkoutExerciseSetLoad
}