package ru.trainingapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.MuscleGroup

interface ExerciseDefinitionRepository {

    fun observeActiveExerciseDefinitions(): Flow<List<ExerciseDefinition>>

    suspend fun createExerciseDefinition(
        name: String,
        description: String,
    )

    suspend fun createExerciseDefinitionFromTemplate(
        templateExerciseDefinitionId: Long,
        name: String,
        description: String,
    )

    suspend fun updateExerciseDefinition(
        id: Long,
        name: String,
        description: String,
    )

    suspend fun archiveExerciseDefinition(id: Long)

    suspend fun replaceExerciseDefinitionTags(
        exerciseDefinitionId: Long,
        tagIds: Set<Long>,
    )

    suspend fun replaceExerciseDefinitionAlternatives(
        exerciseDefinitionId: Long,
        alternativeExerciseDefinitionIds: Set<Long>,
    )

    suspend fun replaceExerciseDefinitionMuscles(
        exerciseDefinitionId: Long,
        primaryMuscleGroups: Set<MuscleGroup>,
        secondaryMuscleGroups: Set<MuscleGroup>,
    )

    suspend fun addExerciseImages(
        exerciseDefinitionId: Long,
        sourceUris: List<String>,
    )

    suspend fun setExerciseCoverImage(
        exerciseDefinitionId: Long,
        imageId: Long,
    )

    suspend fun updateExerciseImageCrop(
        exerciseDefinitionId: Long,
        imageId: Long,
        cropZoom: Float,
        cropOffsetX: Float,
        cropOffsetY: Float,
    )

    suspend fun deleteExerciseImage(
        exerciseDefinitionId: Long,
        imageId: Long,
    )
}
