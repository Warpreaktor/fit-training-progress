package ru.trainingapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.model.ExerciseDefinition

interface ExerciseDefinitionRepository {

    fun observeActiveExerciseDefinitions(): Flow<List<ExerciseDefinition>>

    suspend fun createExerciseDefinition(
        name: String,
        description: String,
    )

    suspend fun updateExerciseDefinition(
        id: Long,
        name: String,
        description: String,
    )

    suspend fun archiveExerciseDefinition(id: Long)
}