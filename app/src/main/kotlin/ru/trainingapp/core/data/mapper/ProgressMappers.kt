package ru.trainingapp.core.data.mapper

import ru.trainingapp.core.database.entity.WorkoutExerciseProgressSetEntity
import ru.trainingapp.core.database.model.WorkoutExerciseProgressPointWithSetsDbModel
import ru.trainingapp.core.model.WorkoutExerciseProgressPoint
import ru.trainingapp.core.model.WorkoutExerciseProgressSet

fun WorkoutExerciseProgressPointWithSetsDbModel.toDomain(): WorkoutExerciseProgressPoint {
    return WorkoutExerciseProgressPoint(
        id = point.id,
        workoutId = point.workoutId,
        workoutExerciseId = point.workoutExerciseId,
        exerciseDefinitionId = point.exerciseDefinitionId,
        exerciseNameSnapshot = point.exerciseNameSnapshot,
        createdAt = point.createdAt,
        revision = point.revision,
        reason = point.reason,
        sets = sets
            .sortedBy { set -> set.setNumber }
            .map { set -> set.toDomain() },
    )
}

private fun WorkoutExerciseProgressSetEntity.toDomain(): WorkoutExerciseProgressSet {
    return WorkoutExerciseProgressSet(
        id = id,
        progressPointId = progressPointId,
        setNumber = setNumber,
        reps = reps,
        loadType = loadType,
        weightValue = weightValue,
        weightUnit = weightUnit,
        durationSeconds = durationSeconds,
    )
}