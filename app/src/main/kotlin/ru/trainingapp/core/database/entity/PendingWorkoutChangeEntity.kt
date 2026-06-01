package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_workout_changes",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkoutExerciseSetEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseSetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutId"]),
        Index(value = ["workoutExerciseId"]),
        Index(value = ["workoutExerciseSetId"]),
        Index(
            value = ["workoutExerciseId", "workoutExerciseSetId", "fieldName"],
            unique = true
        )
    ]
)
data class PendingWorkoutChangeEntity(
    @PrimaryKey
    val id: String,
    val workoutId: String,
    val workoutExerciseId: String,
    val workoutExerciseSetId: String,
    val fieldName: String,
    val oldValue: String?,
    val newValue: String?,
    val firstChangedAt: Long,
    val lastChangedAt: Long
)