package ru.trainingapp.core.model

data class ExerciseImage(
    val id: Long,
    val exerciseDefinitionId: Long,
    val uri: String,
    val sortOrder: Int,
    val isCover: Boolean,
    val cropZoom: Float = 1f,
    val cropOffsetX: Float = 0f,
    val cropOffsetY: Float = 0f,
    val createdAt: Long,
    val updatedAt: Long,
)
