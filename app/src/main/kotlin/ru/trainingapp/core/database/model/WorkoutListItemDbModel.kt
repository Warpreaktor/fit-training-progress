package ru.trainingapp.core.database.model

data class WorkoutListItemDbModel(
    val id: String,
    val name: String,
    val description: String?,
    val isLocked: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val exerciseCount: Int,
    val checkedExerciseCount: Int
)