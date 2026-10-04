package ru.trainingapp.core.domain.workout

import javax.inject.Inject
import ru.trainingapp.core.domain.repository.WorkoutRepository

class WorkoutTimerUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend fun setTargetDuration(
        workoutId: Long,
        targetDurationMinutes: Int?,
    ) {
        if (workoutId <= 0L) return

        repository.setWorkoutTargetDuration(
            workoutId = workoutId,
            targetDurationMinutes = targetDurationMinutes
                ?.takeIf { minutes -> minutes > 0 },
        )
    }

    suspend fun start(workoutId: Long) {
        if (workoutId <= 0L) return
        repository.startWorkoutTimer(workoutId)
    }

    suspend fun pause(workoutId: Long) {
        if (workoutId <= 0L) return
        repository.pauseWorkoutTimer(workoutId)
    }

    suspend fun finish(workoutId: Long) {
        if (workoutId <= 0L) return
        repository.finishWorkoutTimer(workoutId)
    }
}
