package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.database.entity.PendingWorkoutChangeEntity

@Dao
interface PendingWorkoutChangeDao {

    @Query(
        """
        SELECT *
        FROM pending_workout_changes
        WHERE workoutId = :workoutId
        ORDER BY firstChangedAt ASC
        """
    )
    fun observePendingChangesByWorkoutId(
        workoutId: String
    ): Flow<List<PendingWorkoutChangeEntity>>

    @Query(
        """
        SELECT *
        FROM pending_workout_changes
        WHERE workoutId = :workoutId
        ORDER BY firstChangedAt ASC
        """
    )
    suspend fun getPendingChangesByWorkoutId(
        workoutId: String
    ): List<PendingWorkoutChangeEntity>

    @Query(
        """
        SELECT *
        FROM pending_workout_changes
        WHERE workoutExerciseId = :workoutExerciseId
          AND workoutExerciseSetId = :workoutExerciseSetId
          AND fieldName = :fieldName
        LIMIT 1
        """
    )
    suspend fun findPendingChange(
        workoutExerciseId: String,
        workoutExerciseSetId: String,
        fieldName: String
    ): PendingWorkoutChangeEntity?

    @Insert
    suspend fun insertPendingChange(entity: PendingWorkoutChangeEntity)

    @Update
    suspend fun updatePendingChange(entity: PendingWorkoutChangeEntity)

    @Upsert
    suspend fun upsertPendingChange(entity: PendingWorkoutChangeEntity)

    @Query(
        """
        UPDATE pending_workout_changes
        SET newValue = :newValue,
            lastChangedAt = :lastChangedAt
        WHERE id = :id
        """
    )
    suspend fun updatePendingChangeNewValue(
        id: String,
        newValue: String?,
        lastChangedAt: Long
    )

    @Query(
        """
        DELETE FROM pending_workout_changes
        WHERE workoutId = :workoutId
        """
    )
    suspend fun deletePendingChangesByWorkoutId(workoutId: String)

    @Query(
        """
        DELETE FROM pending_workout_changes
        WHERE workoutExerciseId = :workoutExerciseId
        """
    )
    suspend fun deletePendingChangesByWorkoutExerciseId(workoutExerciseId: String)
}