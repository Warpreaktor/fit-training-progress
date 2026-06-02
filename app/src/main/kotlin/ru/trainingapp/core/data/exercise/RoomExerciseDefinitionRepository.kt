package ru.trainingapp.core.data.exercise

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.trainingapp.core.database.dao.ExerciseDefinitionDao
import ru.trainingapp.core.database.entity.ExerciseDefinitionEntity
import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import ru.trainingapp.core.model.ExerciseDefinition
import javax.inject.Inject

class RoomExerciseDefinitionRepository @Inject constructor(
    private val exerciseDefinitionDao: ExerciseDefinitionDao,
) : ExerciseDefinitionRepository {

    override fun observeActiveExerciseDefinitions(): Flow<List<ExerciseDefinition>> {
        return exerciseDefinitionDao
            .observeActiveExerciseDefinitions()
            .map { entities -> entities.map { it.toDomain() } }
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
}