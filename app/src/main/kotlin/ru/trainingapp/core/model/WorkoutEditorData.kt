package ru.trainingapp.core.model

data class WorkoutEditorData(
    val workout: Workout,
    val exercises: List<WorkoutExercise>,
)