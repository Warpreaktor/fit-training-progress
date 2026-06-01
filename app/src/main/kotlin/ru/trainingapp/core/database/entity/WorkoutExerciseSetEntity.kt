package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.trainingapp.core.model.WeightUnit

@Entity(
    tableName = "workout_exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutExerciseId"]),
        Index(value = ["workoutExerciseId", "setNumber"], unique = true)
    ]
)
data class WorkoutExerciseSetEntity(

    @PrimaryKey
    val id: String,
    val workoutExerciseId: String,
    val setNumber: Int,
    val reps: Int?,
    val weightValue: Double?,
    val weightUnit: WeightUnit?,
    val durationSeconds: Int?,
    val createdAt: Long,
    val updatedAt: Long
)