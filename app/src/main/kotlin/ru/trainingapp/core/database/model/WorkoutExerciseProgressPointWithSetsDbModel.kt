package ru.trainingapp.core.database.model

import androidx.room.Embedded
import androidx.room.Relation
import ru.trainingapp.core.database.entity.WorkoutExerciseProgressPointEntity
import ru.trainingapp.core.database.entity.WorkoutExerciseProgressSetEntity

data class WorkoutExerciseProgressPointWithSetsDbModel(
    @Embedded
    val point: WorkoutExerciseProgressPointEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "progressPointId"
    )
    val sets: List<WorkoutExerciseProgressSetEntity>
)