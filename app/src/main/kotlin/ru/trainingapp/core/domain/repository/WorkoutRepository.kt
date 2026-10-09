package ru.trainingapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.model.Workout
import ru.trainingapp.core.model.WorkoutEditorData
import ru.trainingapp.core.model.WorkoutExerciseProgressPoint
import ru.trainingapp.core.model.WorkoutExerciseSet

interface WorkoutRepository {

    fun observeWorkouts(): Flow<List<Workout>>

    fun observeWorkoutEditorData(
        workoutId: Long,
    ): Flow<WorkoutEditorData?>

    suspend fun createWorkout(
        name: String,
        description: String,
    ): Long

    suspend fun archiveWorkout(
        id: Long,
    )

    suspend fun duplicateWorkout(
        workoutId: Long,
        name: String,
    ): Long

    suspend fun updateWorkout(
        workoutId: Long,
        name: String,
        description: String,
    )

    suspend fun setWorkoutTargetDuration(
        workoutId: Long,
        targetDurationMinutes: Int?,
    )

    suspend fun startWorkoutTimer(
        workoutId: Long,
    )

    suspend fun pauseWorkoutTimer(
        workoutId: Long,
    )

    suspend fun finishWorkoutTimer(
        workoutId: Long,
    )

    suspend fun moveWorkoutUp(
        workoutId: Long,
    )

    suspend fun moveWorkoutDown(
        workoutId: Long,
    )

    suspend fun replaceWorkoutTags(
        workoutId: Long,
        tagIds: Set<Long>,
    )

    suspend fun addExerciseToWorkout(
        workoutId: Long,
        exerciseDefinitionId: Long,
    ): Long

    fun observeWorkoutIdsContainingExercise(
        exerciseDefinitionId: Long,
    ): Flow<Set<Long>>

    suspend fun removeExerciseFromWorkout(
        workoutId: Long,
        exerciseDefinitionId: Long,
    )

    suspend fun selectWorkoutExerciseVariant(
        workoutExerciseId: Long,
        exerciseDefinitionId: Long,
    )

    suspend fun createWorkoutExerciseSection(
        workoutId: Long,
        workoutExerciseIds: Set<Long>,
        name: String,
    )

    suspend fun renameWorkoutExerciseSection(
        workoutId: Long,
        sectionId: String,
        name: String,
    )

    suspend fun removeWorkoutExerciseSection(
        workoutId: Long,
        sectionId: String,
    )

    suspend fun moveWorkoutExerciseToSection(
        workoutId: Long,
        workoutExerciseId: Long,
        sectionId: String?,
        sectionName: String?,
    )

    suspend fun archiveWorkoutExercise(
        workoutExerciseId: Long,
    )

    suspend fun addWorkoutExerciseSet(
        workoutExerciseId: Long,
    ): Long

    suspend fun getWorkoutExerciseSet(
        workoutExerciseSetId: Long,
    ): WorkoutExerciseSet?

    suspend fun updateWorkoutExerciseSet(
        workoutExerciseSet: WorkoutExerciseSet,
    )

    suspend fun removeWorkoutExerciseSet(
        workoutExerciseSetId: Long,
    )

    suspend fun moveWorkoutExerciseUp(
        workoutId: Long,
        workoutExerciseId: Long,
    )

    suspend fun moveWorkoutExerciseDown(
        workoutId: Long,
        workoutExerciseId: Long,
    )

    suspend fun updateWorkoutExerciseChecked(
        workoutExerciseId: Long,
        isChecked: Boolean,
    )

    suspend fun resetWorkoutCheckmarks(
        workoutId: Long,
    )

    suspend fun commitPendingProgress(
        workoutId: Long,
    )

    fun observeWorkoutExerciseProgress(
        workoutExerciseId: Long,
    ): Flow<List<WorkoutExerciseProgressPoint>>

    fun observeWorkoutProgress(
        workoutId: Long,
    ): Flow<List<WorkoutExerciseProgressPoint>>
}