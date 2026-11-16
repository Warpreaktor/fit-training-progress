package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.database.entity.ExerciseVideoEntity

@Dao
interface ExerciseVideoDao {

    @Query(
        """
        SELECT *
        FROM exercise_videos
        ORDER BY exerciseDefinitionId ASC, sortOrder ASC
        """
    )
    fun observeExerciseVideos(): Flow<List<ExerciseVideoEntity>>

    @Query(
        """
        SELECT *
        FROM exercise_videos
        WHERE exerciseDefinitionId = :exerciseDefinitionId
        ORDER BY sortOrder ASC
        """
    )
    suspend fun getVideosByExerciseDefinitionId(
        exerciseDefinitionId: Long,
    ): List<ExerciseVideoEntity>

    @Insert
    suspend fun insertVideo(
        video: ExerciseVideoEntity,
    ): Long
}
