package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class UpdateWorkoutExerciseExecutionMethodUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutExerciseId: Long,
        executionMethod: String?,
    ) {
        if (workoutExerciseId <= 0L) return

        repository.updateWorkoutExerciseExecutionMethod(
            workoutExerciseId = workoutExerciseId,
            executionMethod = executionMethod
                ?.trim()
                ?.takeIf { method -> method.isNotBlank() },
        )
    }
}
