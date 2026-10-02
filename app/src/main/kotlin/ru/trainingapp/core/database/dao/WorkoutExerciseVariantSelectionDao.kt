package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ru.trainingapp.core.database.entity.WorkoutExerciseVariantSelectionEntity

@Dao
interface WorkoutExerciseVariantSelectionDao {

    @Query(
        """
        SELECT *
        FROM workout_exercise_variant_selections
        WHERE workoutExerciseId = :workoutExerciseId
        LIMIT 1
        """
    )
    suspend fun getSelection(
        workoutExerciseId: Long,
    ): WorkoutExerciseVariantSelectionEntity?

    @Upsert
    suspend fun upsertSelection(
        entity: WorkoutExerciseVariantSelectionEntity,
    )
}
