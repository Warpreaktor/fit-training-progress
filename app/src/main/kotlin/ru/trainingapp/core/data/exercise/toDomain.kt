package ru.trainingapp.core.data.exercise

import ru.trainingapp.core.database.entity.ExerciseDefinitionEntity
import ru.trainingapp.core.model.ExerciseDefinition

fun ExerciseDefinitionEntity.toDomain(): ExerciseDefinition {
    return ExerciseDefinition(
        id = id,
        name = name,
        description = description,
    )
}