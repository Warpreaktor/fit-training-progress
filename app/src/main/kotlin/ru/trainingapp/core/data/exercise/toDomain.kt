package ru.trainingapp.core.data.exercise

import ru.trainingapp.core.database.entity.ExerciseDefinitionEntity
import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.Tag

fun ExerciseDefinitionEntity.toDomain(
    tags: List<Tag> = emptyList(),
): ExerciseDefinition {
    return ExerciseDefinition(
        id = id,
        name = name,
        description = description,
        tags = tags,
    )
}