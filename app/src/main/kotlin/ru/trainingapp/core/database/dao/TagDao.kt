package ru.trainingapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.database.entity.ExerciseDefinitionTagCrossRefEntity
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
    SELECT *
    FROM workout_tag_cross_refs
    """
    )
    fun observeWorkoutTagCrossRefs(): Flow<List<WorkoutTagCrossRefEntity>>

    @Query(
        """
    SELECT *
    FROM exercise_definition_tag_cross_refs
    """
    )
    fun observeExerciseDefinitionTagCrossRefs(): Flow<List<ExerciseDefinitionTagCrossRefEntity>>

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
    fun observeTagsByWorkoutId(workoutId: Long): Flow<List<TagEntity>>

    @Query(
        """
    SELECT t.*
    FROM tags t
    INNER JOIN exercise_definition_tag_cross_refs ref
        ON ref.tagId = t.id
    WHERE ref.exerciseDefinitionId = :exerciseDefinitionId
    ORDER BY t.name COLLATE NOCASE ASC
    """
    )
    fun observeTagsByExerciseDefinitionId(
        exerciseDefinitionId: Long,
    ): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(entity: TagEntity): Long

    @Update
    suspend fun updateTag(entity: TagEntity)

    @Upsert
    suspend fun upsertTag(entity: TagEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkoutTagCrossRef(entity: WorkoutTagCrossRefEntity)

    @Query(
        """
        DELETE FROM workout_tag_cross_refs
        WHERE workoutId = :workoutId
          AND tagId = :tagId
        """
    )
    suspend fun deleteWorkoutTagCrossRef(
        workoutId: Long,
        tagId: Long
    )

    @Query(
        """
    DELETE FROM workout_tag_cross_refs
    WHERE workoutId = :workoutId
    """
    )
    suspend fun deleteWorkoutTagCrossRefsByWorkoutId(workoutId: Long)

    @Query(
        """
    DELETE FROM exercise_definition_tag_cross_refs
    WHERE exerciseDefinitionId = :exerciseDefinitionId
    """
    )
    suspend fun deleteExerciseDefinitionTagCrossRefsByExerciseDefinitionId(
        exerciseDefinitionId: Long,
    )

    @Query(
        """
    SELECT *
    FROM tags
    WHERE name = :name COLLATE NOCASE
    LIMIT 1
    """
    )
    suspend fun getTagByName(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExerciseDefinitionTagCrossRef(entity: ExerciseDefinitionTagCrossRefEntity)

    @Transaction
    suspend fun replaceWorkoutTags(
        workoutId: Long,
        tagIds: Set<Long>,
    ) {
        deleteWorkoutTagCrossRefsByWorkoutId(workoutId)

        tagIds.forEach { tagId ->
            insertWorkoutTagCrossRef(
                WorkoutTagCrossRefEntity(
                    workoutId = workoutId,
                    tagId = tagId,
                )
            )
        }
    }

    @Transaction
    suspend fun replaceExerciseDefinitionTags(
        exerciseDefinitionId: Long,
        tagIds: Set<Long>,
    ) {
        deleteExerciseDefinitionTagCrossRefsByExerciseDefinitionId(exerciseDefinitionId)

        tagIds.forEach { tagId ->
            insertExerciseDefinitionTagCrossRef(
                ExerciseDefinitionTagCrossRefEntity(
                    exerciseDefinitionId = exerciseDefinitionId,
                    tagId = tagId,
                )
            )
        }
    }
}