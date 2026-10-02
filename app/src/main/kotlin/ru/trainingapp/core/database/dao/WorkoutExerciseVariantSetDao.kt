package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import ru.trainingapp.core.database.entity.WorkoutExerciseVariantSetEntity

@Dao
interface WorkoutExerciseVariantSetDao {

    @Query(
        """
        SELECT *
        FROM workout_exercise_variant_sets
        WHERE workoutExerciseId = :workoutExerciseId
          AND exerciseDefinitionId = :exerciseDefinitionId
        ORDER BY setNumber ASC
        """
    )
    suspend fun getVariantSets(
        workoutExerciseId: Long,
        exerciseDefinitionId: Long,
    ): List<WorkoutExerciseVariantSetEntity>

    @Query(
        """
        SELECT *
        FROM workout_exercise_variant_sets
        WHERE workoutExerciseId = :workoutExerciseId
        ORDER BY exerciseDefinitionId ASC, setNumber ASC
        """
    )
    suspend fun getVariantSetsForWorkoutExercise(
        workoutExerciseId: Long,
    ): List<WorkoutExerciseVariantSetEntity>

    @Query(
        """
        DELETE FROM workout_exercise_variant_sets
        WHERE workoutExerciseId = :workoutExerciseId
          AND exerciseDefinitionId = :exerciseDefinitionId
        """
    )
    suspend fun deleteVariantSets(
        workoutExerciseId: Long,
        exerciseDefinitionId: Long,
    )

    @Insert
    suspend fun insertVariantSets(
        entities: List<WorkoutExerciseVariantSetEntity>,
    )

    @Transaction
    suspend fun replaceVariantSets(
        workoutExerciseId: Long,
        exerciseDefinitionId: Long,
        entities: List<WorkoutExerciseVariantSetEntity>,
    ) {
        deleteVariantSets(
            workoutExerciseId = workoutExerciseId,
            exerciseDefinitionId = exerciseDefinitionId,
        )

        if (entities.isNotEmpty()) {
            insertVariantSets(entities)
        }
    }
}
