package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExerciseSetLoadType

@Entity(
    tableName = "workout_exercise_progress_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseProgressPointEntity::class,
            parentColumns = ["id"],
            childColumns = ["progressPointId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["progressPointId"]),
        Index(value = ["progressPointId", "setNumber"], unique = true),
    ],
)
data class WorkoutExerciseProgressSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val progressPointId: Long,
    val setNumber: Int,
    val reps: Int,
    val loadType: WorkoutExerciseSetLoadType,
    val weightValue: Double?,
    val weightUnit: WeightUnit?,
    val durationSeconds: Int?,
)