package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.database.entity.TagEntity
import ru.trainingapp.core.database.entity.WorkoutTagCrossRefEntity

@Dao
interface TagDao {

    @Query(
        """
        SELECT *
        FROM tags
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeTags(): Flow<List<TagEntity>>

    @Query(
        """
        SELECT t.*
        FROM tags t
        INNER JOIN workout_tag_cross_refs ref
            ON ref.tagId = t.id
        WHERE ref.workoutId = :workoutId
        ORDER BY t.name COLLATE NOCASE ASC
        """
    )
    fun observeTagsByWorkoutId(workoutId: String): Flow<List<TagEntity>>

    @Insert
    suspend fun insertTag(entity: TagEntity)

    @Update
    suspend fun updateTag(entity: TagEntity)

    @Upsert
    suspend fun upsertTag(entity: TagEntity)

    @Insert
    suspend fun insertWorkoutTagCrossRef(entity: WorkoutTagCrossRefEntity)

    @Query(
        """
        DELETE FROM workout_tag_cross_refs
        WHERE workoutId = :workoutId
          AND tagId = :tagId
        """
    )
    suspend fun deleteWorkoutTagCrossRef(
        workoutId: String,
        tagId: String
    )

    @Query(
        """
        DELETE FROM workout_tag_cross_refs
        WHERE workoutId = :workoutId
        """
    )
    suspend fun deleteWorkoutTagCrossRefsByWorkoutId(workoutId: String)
}