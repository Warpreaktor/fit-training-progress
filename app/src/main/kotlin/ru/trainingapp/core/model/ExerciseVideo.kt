package ru.trainingapp.core.model

data class ExerciseVideo(
    val id: Long,
    val exerciseDefinitionId: Long,
    val uri: String,
    val sortOrder: Int,
    val createdAt: Long,
    val updatedAt: Long,
)
