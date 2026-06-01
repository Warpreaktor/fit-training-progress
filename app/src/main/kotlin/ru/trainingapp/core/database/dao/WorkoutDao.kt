package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.database.entity.WorkoutEntity
import ru.trainingapp.core.database.model.WorkoutListItemDbModel

@Dao
interface WorkoutDao {

    @Query(
        """
        SELECT
            w.id AS id,
            w.name AS name,
            w.description AS description,
            w.isLocked AS isLocked,
            w.createdAt AS createdAt,
            w.updatedAt AS updatedAt,
            COUNT(we.id) AS exerciseCount,
            SUM(CASE WHEN we.isChecked = 1 THEN 1 ELSE 0 END) AS checkedExerciseCount
        FROM workouts w
        LEFT JOIN workout_exercises we
            ON we.workoutId = w.id
            AND we.isArchived = 0
        WHERE w.isArchived = 0
        GROUP BY w.id
        ORDER BY w.updatedAt DESC
        """
    )
    fun observeWorkoutListItems(): Flow<List<WorkoutListItemDbModel>>

    @Query(
        """
        SELECT *
        FROM workouts
        WHERE id = :id
        LIMIT 1
        """
    )
    fun observeWorkoutById(id: String): Flow<WorkoutEntity?>

    @Query(
        """
        SELECT *
        FROM workouts
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getWorkoutById(id: String): WorkoutEntity?

    @Insert
    suspend fun insertWorkout(entity: WorkoutEntity)

    @Update
    suspend fun updateWorkout(entity: WorkoutEntity)

    @Upsert
    suspend fun upsertWorkout(entity: WorkoutEntity)

    @Query(
        """
        UPDATE workouts
        SET isArchived = 1,
            archivedAt = :archivedAt,
            updatedAt = :archivedAt
        WHERE id = :id
        """
    )
    suspend fun archiveWorkout(
        id: String,
        archivedAt: Long
    )
}