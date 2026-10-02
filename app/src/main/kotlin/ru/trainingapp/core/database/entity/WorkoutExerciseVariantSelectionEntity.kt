package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_exercise_variant_selections",
    foreignKeys = [
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
        Index(value = ["exerciseDefinitionId"]),
    ],
)
data class WorkoutExerciseVariantSelectionEntity(
    @PrimaryKey
    val workoutExerciseId: Long,
    val exerciseDefinitionId: Long,
    val updatedAt: Long,
)
