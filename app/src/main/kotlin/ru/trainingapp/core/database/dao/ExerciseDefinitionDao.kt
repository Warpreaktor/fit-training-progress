package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.database.entity.ExerciseDefinitionEntity

@Dao
interface ExerciseDefinitionDao {

    @Query(
        """
        SELECT *
        FROM exercise_definitions
        WHERE isArchived = 0
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeActiveExerciseDefinitions(): Flow<List<ExerciseDefinitionEntity>>

    @Query(
        """
        SELECT *
        FROM exercise_definitions
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getExerciseDefinitionById(id: String): ExerciseDefinitionEntity?

    @Insert
    suspend fun insertExerciseDefinition(entity: ExerciseDefinitionEntity)

    @Update
    suspend fun updateExerciseDefinition(entity: ExerciseDefinitionEntity)

    @Upsert
    suspend fun upsertExerciseDefinition(entity: ExerciseDefinitionEntity)

    @Query(
        """
        UPDATE exercise_definitions
        SET isArchived = 1,
            archivedAt = :archivedAt,
            updatedAt = :archivedAt
        WHERE id = :id
        """
    )
    suspend fun archiveExerciseDefinition(
        id: String,
        archivedAt: Long
    )
}