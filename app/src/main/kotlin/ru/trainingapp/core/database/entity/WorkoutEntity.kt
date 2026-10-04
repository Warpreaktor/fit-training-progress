package ru.trainingapp.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val sortOrder: Int = 0,
    val isLocked: Boolean = false,
    val isArchived: Boolean = false,
    val archivedAt: Long? = null,
    val targetDurationMinutes: Int? = null,
    val timerStartedAt: Long? = null,
    @ColumnInfo(defaultValue = "0")
    val timerElapsedMillis: Long = 0L,
    @ColumnInfo(defaultValue = "0")
    val timerIsFinished: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
