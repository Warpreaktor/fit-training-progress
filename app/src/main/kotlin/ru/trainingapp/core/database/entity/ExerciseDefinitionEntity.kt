package ru.trainingapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_definitions")
data class ExerciseDefinitionEntity(

    @PrimaryKey
    val id: String,

    val name: String,

    val description: String = "",

    val isArchived: Boolean = false,

    val archivedAt: Long? = null,

    val createdAt: Long,

    val updatedAt: Long
)