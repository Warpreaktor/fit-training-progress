package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExerciseSetLoadType

@Entity(
    tableName = "workout_exercise_variant_sets",
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
        Index(value = ["workoutExerciseId"]),
        Index(value = ["exerciseDefinitionId"]),
        Index(
            value = ["workoutExerciseId", "exerciseDefinitionId", "setNumber"],
            unique = true,
        ),
    ],
)
data class WorkoutExerciseVariantSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val workoutExerciseId: Long,
    val exerciseDefinitionId: Long,
    val setNumber: Int,
    val reps: Int,
    val loadType: WorkoutExerciseSetLoadType,
    val weightValue: Double?,
    val weightUnit: WeightUnit?,
    val durationSeconds: Int?,
    val createdAt: Long,
    val updatedAt: Long,
)
