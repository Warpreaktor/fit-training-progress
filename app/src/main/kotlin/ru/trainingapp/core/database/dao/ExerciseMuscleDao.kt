package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.database.entity.ExerciseDefinitionMuscleCrossRefEntity

@Dao
interface ExerciseMuscleDao {

    @Query(
        """
        SELECT *
        FROM exercise_definition_muscle_cross_refs
        """
    )
    fun observeMuscleCrossRefs(): Flow<List<ExerciseDefinitionMuscleCrossRefEntity>>

    @Query(
        """
        SELECT muscleCode
        FROM exercise_definition_muscle_cross_refs
        WHERE exerciseDefinitionId = :exerciseDefinitionId
        ORDER BY muscleCode ASC
        """
    )
    suspend fun getMuscleCodesByExerciseDefinitionId(
        exerciseDefinitionId: Long,
    ): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMuscleCrossRef(
        entity: ExerciseDefinitionMuscleCrossRefEntity,
    )

    @Query(
        """
        DELETE FROM exercise_definition_muscle_cross_refs
        WHERE exerciseDefinitionId = :exerciseDefinitionId
        """
    )
    suspend fun deleteMusclesByExerciseDefinitionId(
        exerciseDefinitionId: Long,
    )

    @Transaction
    suspend fun replaceMuscleCodes(
        exerciseDefinitionId: Long,
        muscleCodes: Set<String>,
    ) {
        deleteMusclesByExerciseDefinitionId(exerciseDefinitionId)

        muscleCodes
            .map { muscleCode -> muscleCode.trim() }
            .filter { muscleCode -> muscleCode.isNotBlank() }
            .distinct()
            .forEach { muscleCode ->
                insertMuscleCrossRef(
                    ExerciseDefinitionMuscleCrossRefEntity(
                        exerciseDefinitionId = exerciseDefinitionId,
                        muscleCode = muscleCode,
                    )
                )
            }
    }
}
