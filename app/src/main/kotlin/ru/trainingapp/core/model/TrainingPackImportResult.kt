package ru.trainingapp.core.model

data class TrainingPackImportResult(
    val createdWorkouts: Int = 0,
    val createdExercises: Int = 0,
    val createdTags: Int = 0,
    val createdImages: Int = 0,
    val warnings: List<String> = emptyList(),
)
