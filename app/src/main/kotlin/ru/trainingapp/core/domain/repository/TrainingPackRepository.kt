package ru.trainingapp.core.domain.repository

import ru.trainingapp.core.model.TrainingPackImportResult

interface TrainingPackRepository {

    suspend fun exportExercise(
        exerciseDefinitionId: Long,
        destinationUri: String,
    )

    suspend fun importTrainingPack(
        sourceUri: String,
    ): TrainingPackImportResult
}
