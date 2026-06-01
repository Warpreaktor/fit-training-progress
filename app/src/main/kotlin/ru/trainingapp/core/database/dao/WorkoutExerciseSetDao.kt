package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.database.entity.WorkoutExerciseSetEntity

@Dao
interface WorkoutExerciseSetDao {

    @Query(
        """
        SELECT *
        FROM workout_exercise_sets
        WHERE workoutExerciseId = :workoutExerciseId
        ORDER BY setNumber ASC
        """
    )
    fun observeSetsByWorkoutExerciseId(
        workoutExerciseId: String
    ): Flow<List<WorkoutExerciseSetEntity>>

    @Query(
        """
        SELECT *
        FROM workout_exercise_sets
        WHERE workoutExerciseId = :workoutExerciseId
        ORDER BY setNumber ASC
        """
    )
    suspend fun getSetsByWorkoutExerciseId(
        workoutExerciseId: String
    ): List<WorkoutExerciseSetEntity>

    @Query(
        """
        SELECT *
        FROM workout_exercise_sets
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getSetById(id: String): WorkoutExerciseSetEntity?

    @Insert
    suspend fun insertSet(entity: WorkoutExerciseSetEntity)

    @Update
    suspend fun updateSet(entity: WorkoutExerciseSetEntity)

    @Upsert
    suspend fun upsertSet(entity: WorkoutExerciseSetEntity)

    @Delete
    suspend fun deleteSet(entity: WorkoutExerciseSetEntity)

    @Query(
        """
        DELETE FROM workout_exercise_sets
        WHERE id = :id
        """
    )
    suspend fun deleteSetById(id: String)

    @Query(
        """
        SELECT COALESCE(MAX(setNumber), 0) + 1
        FROM workout_exercise_sets
        WHERE workoutExerciseId = :workoutExerciseId
        """
    )
    suspend fun getNextSetNumber(workoutExerciseId: String): Int
}