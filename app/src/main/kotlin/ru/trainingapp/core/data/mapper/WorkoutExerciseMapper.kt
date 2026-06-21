package ru.trainingapp.core.data.mapper

import ru.trainingapp.core.database.entity.WorkoutExerciseSetEntity
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExerciseSet
import ru.trainingapp.core.model.WorkoutExerciseSetLoad
import ru.trainingapp.core.model.WorkoutExerciseSetLoadType

fun WorkoutExerciseSetEntity.toDomain(): WorkoutExerciseSet {
    return WorkoutExerciseSet(
        id = id,
        workoutExerciseId = workoutExerciseId,
        setNumber = setNumber,
        reps = reps,
        load = when (loadType) {
            WorkoutExerciseSetLoadType.WEIGHT -> WorkoutExerciseSetLoad.Weight(
                value = weightValue,
                unit = weightUnit ?: WeightUnit.KG,
            )

            WorkoutExerciseSetLoadType.TIME -> WorkoutExerciseSetLoad.Time(
                durationSeconds = durationSeconds,
            )
        },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun WorkoutExerciseSet.toEntity(): WorkoutExerciseSetEntity {
    return when (val currentLoad = load) {
        is WorkoutExerciseSetLoad.Weight -> WorkoutExerciseSetEntity(
            id = id,
            workoutExerciseId = workoutExerciseId,
            setNumber = setNumber,
            reps = reps,
            loadType = WorkoutExerciseSetLoadType.WEIGHT,
            weightValue = currentLoad.value,
            weightUnit = currentLoad.unit,
            durationSeconds = null,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        is WorkoutExerciseSetLoad.Time -> WorkoutExerciseSetEntity(
            id = id,
            workoutExerciseId = workoutExerciseId,
            setNumber = setNumber,
            reps = reps,
            loadType = WorkoutExerciseSetLoadType.TIME,
            weightValue = null,
            weightUnit = null,
            durationSeconds = currentLoad.durationSeconds,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}