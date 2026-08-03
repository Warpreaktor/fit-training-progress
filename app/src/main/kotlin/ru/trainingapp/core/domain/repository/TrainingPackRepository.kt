package ru.trainingapp.core.domain.repository

import ru.trainingapp.core.model.TrainingPackImportResult

enum class TrainingPackExportType {
    SINGLE_EXERCISE,
    WORKOUT,
}

interface TrainingPackRepository {

    suspend fun exportExercise(
        exerciseDefinitionId: Long,
        destinationUri: String,
    )

    suspend fun exportWorkout(
        workoutId: Long,
        destinationUri: String,
    )

    suspend fun importTrainingPack(
        sourceUri: String,
        expectedExportType: TrainingPackExportType,
    ): TrainingPackImportResult
}
