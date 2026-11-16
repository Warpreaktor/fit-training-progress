package ru.trainingapp.core.data.exercise

import ru.trainingapp.core.database.entity.ExerciseVideoEntity
import ru.trainingapp.core.model.ExerciseVideo

fun ExerciseVideoEntity.toDomain(): ExerciseVideo {
    return ExerciseVideo(
        id = id,
        exerciseDefinitionId = exerciseDefinitionId,
        uri = uri,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
