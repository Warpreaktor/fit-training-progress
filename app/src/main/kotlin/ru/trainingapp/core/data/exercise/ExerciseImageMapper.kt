package ru.trainingapp.core.data.exercise

import ru.trainingapp.core.database.entity.ExerciseImageEntity
import ru.trainingapp.core.model.ExerciseImage

fun ExerciseImageEntity.toDomain(): ExerciseImage {
    return ExerciseImage(
        id = id,
        exerciseDefinitionId = exerciseDefinitionId,
        uri = uri,
        sortOrder = sortOrder,
        isCover = isCover,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}