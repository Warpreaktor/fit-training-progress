package ru.trainingapp.core.database.model

data class WorkoutExerciseListItemDbModel(
    val id: Long,
    val workoutId: Long,
    val exerciseDefinitionId: Long,
    val exerciseName: String,
    val sortOrder: Int,
    val comment: String?,
    val isChecked: Boolean,
    val checkedAt: Long?,
    val isArchived: Boolean,
    val archivedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)