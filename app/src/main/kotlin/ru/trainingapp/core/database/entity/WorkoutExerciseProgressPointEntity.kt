package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_exercise_progress_points",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseDefinitionId"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["workoutId"]),
        Index(value = ["workoutExerciseId"]),
        Index(value = ["exerciseDefinitionId"]),
        Index(value = ["workoutExerciseId", "revision"], unique = true),
        Index(value = ["workoutExerciseId", "createdAt"]),
    ],
)
data class WorkoutExerciseProgressPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val workoutId: Long,
    val workoutExerciseId: Long,
    val exerciseDefinitionId: Long,
    val exerciseNameSnapshot: String,
    val createdAt: Long,
    val revision: Int,
    val reason: String,
)