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
        workoutExerciseId: String
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
        workoutExerciseId: String
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
        workoutId: String
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
        workoutExerciseId: String
    ): List<WorkoutExerciseProgressPointEntity>

    @Query(
        """
        SELECT COALESCE(MAX(revision), 0) + 1
        FROM workout_exercise_progress_points
        WHERE workoutExerciseId = :workoutExerciseId
        """
    )
    suspend fun getNextRevision(workoutExerciseId: String): Int

    @Insert
    suspend fun insertProgressPoint(entity: WorkoutExerciseProgressPointEntity)

    @Insert
    suspend fun insertProgressSets(entities: List<WorkoutExerciseProgressSetEntity>)

    @Query(
        """
        DELETE FROM workout_exercise_progress_points
        WHERE id = :progressPointId
        """
    )
    suspend fun deleteProgressPoint(progressPointId: String)
}