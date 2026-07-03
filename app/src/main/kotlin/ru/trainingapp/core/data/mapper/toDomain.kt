package ru.trainingapp.core.data.mapper

import ru.trainingapp.core.database.entity.TagEntity
import ru.trainingapp.core.model.Tag

fun TagEntity.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        color = color,
    )
}