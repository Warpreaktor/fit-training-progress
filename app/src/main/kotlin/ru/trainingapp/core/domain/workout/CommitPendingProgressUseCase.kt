package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class CommitPendingProgressUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutId: Long,
    ) {
        if (workoutId <= 0L) return

        repository.commitPendingProgress(workoutId)
    }
}