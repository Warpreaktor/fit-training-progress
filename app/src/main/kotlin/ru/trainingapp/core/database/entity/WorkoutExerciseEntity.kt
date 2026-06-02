package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseDefinitionId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index("workoutId"),
        Index("exerciseDefinitionId")
    ]
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutId: Long,
    val exerciseDefinitionId: Long,
    val sortOrder: Int,
    val comment: String?,
    val isChecked: Boolean = false,
    val checkedAt: Long? = null,
    val isArchived: Boolean = false,
    val archivedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)