package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val isLocked: Boolean = false,
    val isArchived: Boolean = false,
    val archivedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)