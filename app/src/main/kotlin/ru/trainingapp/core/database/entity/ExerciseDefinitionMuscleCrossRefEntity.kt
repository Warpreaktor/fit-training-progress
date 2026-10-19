package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "exercise_definition_muscle_cross_refs",
    primaryKeys = ["exerciseDefinitionId", "muscleCode"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseDefinitionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["exerciseDefinitionId"]),
    ],
)
data class ExerciseDefinitionMuscleCrossRefEntity(
    val exerciseDefinitionId: Long,
    val muscleCode: String,
)
