package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.database.entity.WorkoutExerciseEntity
import ru.trainingapp.core.database.model.WorkoutExerciseListItemDbModel

@Dao
interface WorkoutExerciseDao {

    @Query(
        """
        SELECT
            we.id AS id,
            we.workoutId AS workoutId,
            we.exerciseDefinitionId AS exerciseDefinitionId,
            ed.name AS exerciseName,
            we.sortOrder AS sortOrder,
            we.comment AS comment,
            we.isChecked AS isChecked,
            we.checkedAt AS checkedAt,
            we.isArchived AS isArchived,
            we.archivedAt AS archivedAt,
            we.createdAt AS createdAt,
            we.updatedAt AS updatedAt
        FROM workout_exercises we
        INNER JOIN exercise_definitions ed
            ON ed.id = we.exerciseDefinitionId
        WHERE we.workoutId = :workoutId
          AND we.isArchived = 0
        ORDER BY we.sortOrder ASC
        """
    )
    fun observeActiveWorkoutExercises(
        workoutId: Long
    ): Flow<List<WorkoutExerciseListItemDbModel>>

    @Query(
        """
        SELECT *
        FROM workout_exercises
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getWorkoutExerciseById(id: Long): WorkoutExerciseEntity?

    @Insert
    suspend fun insertWorkoutExercise(entity: WorkoutExerciseEntity): Long

    @Update
    suspend fun updateWorkoutExercise(entity: WorkoutExerciseEntity)

    @Upsert
    suspend fun upsertWorkoutExercise(entity: WorkoutExerciseEntity)

    @Query(
        """
        UPDATE workout_exercises
        SET isChecked = :isChecked,
            checkedAt = :checkedAt,
            updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateCheckedState(
        id: Long,
        isChecked: Boolean,
        checkedAt: Long?,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE workout_exercises
        SET isChecked = 0,
            checkedAt = NULL,
            updatedAt = :updatedAt
        WHERE workoutId = :workoutId
          AND isArchived = 0
        """
    )
    suspend fun resetWorkoutCheckmarks(
        workoutId: Long,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE workout_exercises
        SET isArchived = 1,
            archivedAt = :archivedAt,
            updatedAt = :archivedAt
        WHERE id = :id
        """
    )
    suspend fun archiveWorkoutExercise(
        id: Long,
        archivedAt: Long
    )

    @Query(
        """
        SELECT COALESCE(MAX(sortOrder), -1) + 1
        FROM workout_exercises
        WHERE workoutId = :workoutId
          AND isArchived = 0
        """
    )
    suspend fun getNextSortOrder(workoutId: Long): Int

    @Query(
        """
        SELECT *
        FROM workout_exercises
        WHERE workoutId = :workoutId
            AND isArchived = 0
        ORDER BY sortOrder ASC
        """
    )
    suspend fun getActiveWorkoutExerciseEntities(
        workoutId: Long,
    ): List<WorkoutExerciseEntity>
}