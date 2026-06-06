package ru.trainingapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.model.Workout
import ru.trainingapp.core.model.WorkoutEditorData
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

    suspend fun addExerciseToWorkout(
        workoutId: Long,
        exerciseDefinitionId: Long,
    ): Long

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
}