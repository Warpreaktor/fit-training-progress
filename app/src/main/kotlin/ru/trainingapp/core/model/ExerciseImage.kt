package ru.trainingapp.core.model

data class ExerciseImage(
    val id: Long,
    val exerciseDefinitionId: Long,
    val uri: String,
    val sortOrder: Int,
    val isCover: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)