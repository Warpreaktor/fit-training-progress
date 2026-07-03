package ru.trainingapp.core.data.exercise

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.trainingapp.core.data.mapper.toDomain
import ru.trainingapp.core.database.TrainingDatabase
import ru.trainingapp.core.database.dao.ExerciseDefinitionDao
import ru.trainingapp.core.database.dao.TagDao
import ru.trainingapp.core.database.entity.ExerciseDefinitionEntity
import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import ru.trainingapp.core.model.ExerciseDefinition
import javax.inject.Inject

class RoomExerciseDefinitionRepository @Inject constructor(
    private val database: TrainingDatabase,
    private val exerciseDefinitionDao: ExerciseDefinitionDao,
    private val tagDao: TagDao,
) : ExerciseDefinitionRepository {

    override fun observeActiveExerciseDefinitions(): Flow<List<ExerciseDefinition>> {
        return combine(
            exerciseDefinitionDao.observeActiveExerciseDefinitions(),
            tagDao.observeTags(),
            tagDao.observeExerciseDefinitionTagCrossRefs(),
        ) { exerciseEntities, tagEntities, crossRefs ->
            val tagsById = tagEntities.associateBy { tag -> tag.id }

            val tagIdsByExerciseDefinitionId = crossRefs.groupBy(
                keySelector = { crossRef -> crossRef.exerciseDefinitionId },
                valueTransform = { crossRef -> crossRef.tagId },
            )

            exerciseEntities.map { exerciseEntity ->
                val exerciseTags = tagIdsByExerciseDefinitionId[exerciseEntity.id]
                    .orEmpty()
                    .mapNotNull { tagId -> tagsById[tagId]?.toDomain() }
                    .sortedBy { tag -> tag.name.lowercase() }

                exerciseEntity.toDomain(tags = exerciseTags)
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
}