package ru.trainingapp.core.model

data class ExerciseImageEdit(
    val sourceImageId: Long? = null,
    val uri: String,
    val isCover: Boolean,
    val cropZoom: Float = 1f,
    val cropOffsetX: Float = 0f,
    val cropOffsetY: Float = 0f,
)
