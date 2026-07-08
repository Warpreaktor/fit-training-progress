package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "exercise_definition_alternative_cross_refs",
    primaryKeys = ["exerciseDefinitionId", "alternativeExerciseDefinitionId"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseDefinitionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["alternativeExerciseDefinitionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["exerciseDefinitionId"]),
        Index(value = ["alternativeExerciseDefinitionId"]),
    ],
)
data class ExerciseDefinitionAlternativeCrossRefEntity(
    val exerciseDefinitionId: Long,
    val alternativeExerciseDefinitionId: Long,
)