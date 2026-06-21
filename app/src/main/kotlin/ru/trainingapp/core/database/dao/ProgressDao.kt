package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.database.entity.WorkoutExerciseProgressPointEntity
import ru.trainingapp.core.database.entity.WorkoutExerciseProgressSetEntity
import ru.trainingapp.core.database.model.WorkoutExerciseProgressPointWithSetsDbModel

@Dao
interface ProgressDao {

    @Query(
        """
        SELECT *
        FROM workout_exercise_progress_points
        WHERE workoutExerciseId = :workoutExerciseId
        ORDER BY createdAt ASC, revision ASC
        """
    )
    fun observeProgressPointsByWorkoutExerciseId(
        workoutExerciseId: Long,
    ): Flow<List<WorkoutExerciseProgressPointEntity>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM workout_exercise_progress_points
        WHERE workoutExerciseId = :workoutExerciseId
        ORDER BY createdAt ASC, revision ASC
        """
    )
    fun observeProgressPointsWithSetsByWorkoutExerciseId(
        workoutExerciseId: Long,
    ): Flow<List<WorkoutExerciseProgressPointWithSetsDbModel>>

    @Query(
        """
        SELECT *
        FROM workout_exercise_progress_points
        WHERE workoutId = :workoutId
        ORDER BY createdAt ASC, revision ASC
        """
    )
    fun observeProgressPointsByWorkoutId(
        workoutId: Long,
    ): Flow<List<WorkoutExerciseProgressPointEntity>>

    @Query(
        """
        SELECT *
        FROM workout_exercise_progress_points
        WHERE workoutExerciseId = :workoutExerciseId
        ORDER BY createdAt ASC, revision ASC
        """
    )
    suspend fun getProgressPointsByWorkoutExerciseId(
        workoutExerciseId: Long,
    ): List<WorkoutExerciseProgressPointEntity>

    @Query(
        """
        SELECT *
        FROM workout_exercise_progress_points
        WHERE workoutExerciseId = :workoutExerciseId
          AND createdAt >= :dayStartMillis
          AND createdAt < :dayEndMillis
        ORDER BY createdAt DESC, revision DESC
        """
    )
    suspend fun getProgressPointsByWorkoutExerciseIdAndDay(
        workoutExerciseId: Long,
        dayStartMillis: Long,
        dayEndMillis: Long,
    ): List<WorkoutExerciseProgressPointEntity>

    @Query(
        """
        SELECT COALESCE(MAX(revision), 0) + 1
        FROM workout_exercise_progress_points
        WHERE workoutExerciseId = :workoutExerciseId
        """
    )
    suspend fun getNextRevision(
        workoutExerciseId: Long,
    ): Int

    @Insert
    suspend fun insertProgressPoint(
        entity: WorkoutExerciseProgressPointEntity,
    ): Long

    @Insert
    suspend fun insertProgressSets(
        entities: List<WorkoutExerciseProgressSetEntity>,
    )

    @Query(
        """
        UPDATE workout_exercise_progress_points
        SET workoutId = :workoutId,
            exerciseDefinitionId = :exerciseDefinitionId,
            exerciseNameSnapshot = :exerciseNameSnapshot,
            createdAt = :createdAt,
            reason = :reason
        WHERE id = :progressPointId
        """
    )
    suspend fun updateProgressPointSnapshot(
        progressPointId: Long,
        workoutId: Long,
        exerciseDefinitionId: Long,
        exerciseNameSnapshot: String,
        createdAt: Long,
        reason: String,
    )

    @Query(
        """
        DELETE FROM workout_exercise_progress_sets
        WHERE progressPointId = :progressPointId
        """
    )
    suspend fun deleteProgressSetsByProgressPointId(
        progressPointId: Long,
    )

    @Query(
        """
        DELETE FROM workout_exercise_progress_points
        WHERE id = :progressPointId
        """
    )
    suspend fun deleteProgressPoint(
        progressPointId: Long,
    )

    @Transaction
    @Query(
        """
    SELECT *
    FROM workout_exercise_progress_points
    WHERE workoutId = :workoutId
    ORDER BY createdAt ASC, revision ASC
    """
    )
    fun observeProgressPointsWithSetsByWorkoutId(
        workoutId: Long,
    ): Flow<List<WorkoutExerciseProgressPointWithSetsDbModel>>
}