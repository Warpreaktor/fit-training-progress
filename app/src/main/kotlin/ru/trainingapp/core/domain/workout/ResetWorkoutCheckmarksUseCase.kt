package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class ResetWorkoutCheckmarksUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutId: Long,
    ) {
        if (workoutId <= 0) return

        repository.resetWorkoutCheckmarks(workoutId)
    }
}