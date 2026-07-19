package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.database.entity.ExerciseImageEntity

@Dao
interface ExerciseImageDao {

    @Query(
        """
        SELECT *
        FROM exercise_images
        ORDER BY exerciseDefinitionId ASC, sortOrder ASC
        """
    )
    fun observeExerciseImages(): Flow<List<ExerciseImageEntity>>

    @Query(
        """
        SELECT *
        FROM exercise_images
        WHERE exerciseDefinitionId = :exerciseDefinitionId
        ORDER BY sortOrder ASC
        """
    )
    suspend fun getImagesByExerciseDefinitionId(
        exerciseDefinitionId: Long,
    ): List<ExerciseImageEntity>

    @Query(
        """
        SELECT *
        FROM exercise_images
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getImageById(
        id: Long,
    ): ExerciseImageEntity?

    @Query(
        """
        SELECT COALESCE(MAX(sortOrder), -1) + 1
        FROM exercise_images
        WHERE exerciseDefinitionId = :exerciseDefinitionId
        """
    )
    suspend fun getNextSortOrder(
        exerciseDefinitionId: Long,
    ): Int

    @Insert
    suspend fun insertImage(
        image: ExerciseImageEntity,
    ): Long

    @Query(
        """
        UPDATE exercise_images
        SET isCover = CASE WHEN id = :imageId THEN 1 ELSE 0 END,
            updatedAt = :updatedAt
        WHERE exerciseDefinitionId = :exerciseDefinitionId
        """
    )
    suspend fun markAsCover(
        exerciseDefinitionId: Long,
        imageId: Long,
        updatedAt: Long,
    )

    @Query(
        """
        DELETE FROM exercise_images
        WHERE id = :id
        """
    )
    suspend fun deleteImage(
        id: Long,
    )
}