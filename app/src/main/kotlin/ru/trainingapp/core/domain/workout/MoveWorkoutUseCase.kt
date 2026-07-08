package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class MoveWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend fun up(workoutId: Long) {
        if (workoutId <= 0L) return

        repository.moveWorkoutUp(workoutId)
    }

    suspend fun down(workoutId: Long) {
        if (workoutId <= 0L) return

        repository.moveWorkoutDown(workoutId)
    }
}