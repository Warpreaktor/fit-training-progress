package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class MoveWorkoutExerciseUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend fun up(
        workoutId: Long,
        workoutExerciseId: Long,
    ) {
        repository.moveWorkoutExerciseUp(
            workoutId = workoutId,
            workoutExerciseId = workoutExerciseId,
        )
    }

    suspend fun down(
        workoutId: Long,
        workoutExerciseId: Long,
    ) {
        repository.moveWorkoutExerciseDown(
            workoutId = workoutId,
            workoutExerciseId = workoutExerciseId,
        )
    }
}