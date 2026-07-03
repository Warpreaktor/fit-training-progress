package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "exercise_definition_tag_cross_refs",
    primaryKeys = ["exerciseDefinitionId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseDefinitionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["exerciseDefinitionId"]),
        Index(value = ["tagId"]),
    ],
)
data class ExerciseDefinitionTagCrossRefEntity(
    val exerciseDefinitionId: Long,
    val tagId: Long,
)