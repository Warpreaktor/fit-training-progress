package ru.trainingapp.core.data.exercise

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.trainingapp.core.data.mapper.toDomain
import ru.trainingapp.core.database.TrainingDatabase
import ru.trainingapp.core.database.dao.ExerciseAlternativeDao
import ru.trainingapp.core.database.dao.ExerciseDefinitionDao
import ru.trainingapp.core.database.dao.TagDao
import ru.trainingapp.core.database.entity.ExerciseDefinitionEntity
import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.ExerciseDefinitionAlternative
import javax.inject.Inject

class RoomExerciseDefinitionRepository @Inject constructor(
    private val database: TrainingDatabase,
    private val exerciseDefinitionDao: ExerciseDefinitionDao,
    private val tagDao: TagDao,
    private val exerciseAlternativeDao: ExerciseAlternativeDao,
) : ExerciseDefinitionRepository {

    override fun observeActiveExerciseDefinitions(): Flow<List<ExerciseDefinition>> {
        return combine(
            exerciseDefinitionDao.observeActiveExerciseDefinitions(),
            tagDao.observeTags(),
            tagDao.observeExerciseDefinitionTagCrossRefs(),
            exerciseAlternativeDao.observeAlternativeCrossRefs(),
        ) { exerciseEntities, tagEntities, tagCrossRefs, alternativeCrossRefs ->
            val tagsById = tagEntities.associateBy { tag -> tag.id }

            val exerciseNamesById = exerciseEntities.associateBy(
                keySelector = { exercise -> exercise.id },
                valueTransform = { exercise -> exercise.name },
            )

            val tagIdsByExerciseDefinitionId = tagCrossRefs.groupBy(
                keySelector = { crossRef -> crossRef.exerciseDefinitionId },
                valueTransform = { crossRef -> crossRef.tagId },
            )

            val alternativeIdsByExerciseDefinitionId = alternativeCrossRefs.groupBy(
                keySelector = { crossRef -> crossRef.exerciseDefinitionId },
                valueTransform = { crossRef -> crossRef.alternativeExerciseDefinitionId },
            )

            exerciseEntities.map { exerciseEntity ->
                val exerciseTags = tagIdsByExerciseDefinitionId[exerciseEntity.id]
                    .orEmpty()
                    .mapNotNull { tagId -> tagsById[tagId]?.toDomain() }
                    .sortedBy { tag -> tag.name.lowercase() }

                val alternatives = alternativeIdsByExerciseDefinitionId[exerciseEntity.id]
                    .orEmpty()
                    .mapNotNull { alternativeId ->
                        val alternativeName = exerciseNamesById[alternativeId]
                            ?: return@mapNotNull null

                        ExerciseDefinitionAlternative(
                            id = alternativeId,
                            name = alternativeName,
                        )
                    }
                    .sortedBy { alternative -> alternative.name.lowercase() }

                exerciseEntity.toDomain(
                    tags = exerciseTags,
                    alternatives = alternatives,
                )
            }
        }
    }

    override suspend fun createExerciseDefinition(
        name: String,
        description: String,
    ) {
        val now = System.currentTimeMillis()

        exerciseDefinitionDao.insertExerciseDefinition(
            ExerciseDefinitionEntity(
                name = name.trim(),
                description = description.trim(),
                isArchived = false,
                archivedAt = null,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    override suspend fun updateExerciseDefinition(
        id: Long,
        name: String,
        description: String,
    ) {
        val current = exerciseDefinitionDao.getExerciseDefinitionById(id) ?: return
        val now = System.currentTimeMillis()

        exerciseDefinitionDao.updateExerciseDefinition(
            current.copy(
                name = name.trim(),
                description = description.trim(),
                updatedAt = now,
            )
        )
    }

    override suspend fun archiveExerciseDefinition(id: Long) {
        exerciseDefinitionDao.archiveExerciseDefinition(
            id = id,
            archivedAt = System.currentTimeMillis(),
        )
    }

    override suspend fun replaceExerciseDefinitionTags(
        exerciseDefinitionId: Long,
        tagIds: Set<Long>,
    ) {
        val now = System.currentTimeMillis()

        database.withTransaction {
            val currentExercise = exerciseDefinitionDao
                .getExerciseDefinitionById(exerciseDefinitionId)
                ?: return@withTransaction

            tagDao.replaceExerciseDefinitionTags(
                exerciseDefinitionId = exerciseDefinitionId,
                tagIds = tagIds,
            )

            exerciseDefinitionDao.updateExerciseDefinition(
                currentExercise.copy(
                    updatedAt = now,
                )
            )
        }
    }

    override suspend fun replaceExerciseDefinitionAlternatives(
        exerciseDefinitionId: Long,
        alternativeExerciseDefinitionIds: Set<Long>,
    ) {
        val now = System.currentTimeMillis()

        database.withTransaction {
            val currentExercise = exerciseDefinitionDao
                .getExerciseDefinitionById(exerciseDefinitionId)
                ?: return@withTransaction

            exerciseAlternativeDao.replaceAlternatives(
                exerciseDefinitionId = exerciseDefinitionId,
                alternativeExerciseDefinitionIds = alternativeExerciseDefinitionIds,
            )

            exerciseDefinitionDao.updateExerciseDefinition(
                currentExercise.copy(
                    updatedAt = now,
                )
            )
        }
    }
}