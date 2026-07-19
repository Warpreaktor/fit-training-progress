package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_images",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseDefinitionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["exerciseDefinitionId"]),
    ],
)
data class ExerciseImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseDefinitionId: Long,
    val uri: String,
    val sortOrder: Int,
    val isCover: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)