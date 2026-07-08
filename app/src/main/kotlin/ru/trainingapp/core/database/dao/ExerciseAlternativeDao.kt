package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.database.entity.ExerciseDefinitionAlternativeCrossRefEntity

@Dao
interface ExerciseAlternativeDao {

    @Query(
        """
        SELECT *
        FROM exercise_definition_alternative_cross_refs
        """
    )
    fun observeAlternativeCrossRefs(): Flow<List<ExerciseDefinitionAlternativeCrossRefEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlternativeCrossRef(
        entity: ExerciseDefinitionAlternativeCrossRefEntity,
    )

    @Query(
        """
        DELETE FROM exercise_definition_alternative_cross_refs
        WHERE exerciseDefinitionId = :exerciseDefinitionId
        """
    )
    suspend fun deleteAlternativesByExerciseDefinitionId(
        exerciseDefinitionId: Long,
    )

    @Transaction
    suspend fun replaceAlternatives(
        exerciseDefinitionId: Long,
        alternativeExerciseDefinitionIds: Set<Long>,
    ) {
        deleteAlternativesByExerciseDefinitionId(
            exerciseDefinitionId = exerciseDefinitionId,
        )

        alternativeExerciseDefinitionIds
            .filter { alternativeExerciseDefinitionId ->
                alternativeExerciseDefinitionId != exerciseDefinitionId
            }
            .forEach { alternativeExerciseDefinitionId ->
                insertAlternativeCrossRef(
                    ExerciseDefinitionAlternativeCrossRefEntity(
                        exerciseDefinitionId = exerciseDefinitionId,
                        alternativeExerciseDefinitionId = alternativeExerciseDefinitionId,
                    )
                )
            }
    }
}