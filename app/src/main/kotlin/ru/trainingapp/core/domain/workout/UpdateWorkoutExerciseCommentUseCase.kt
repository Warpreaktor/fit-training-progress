package ru.trainingapp.core.domain.workout

import javax.inject.Inject
import ru.trainingapp.core.domain.repository.WorkoutRepository

class UpdateWorkoutExerciseCommentUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutExerciseId: Long,
        comment: String?,
    ) {
        if (workoutExerciseId <= 0L) return

        repository.updateWorkoutExerciseComment(
            workoutExerciseId = workoutExerciseId,
            comment = comment
                ?.trim()
                ?.takeIf { value -> value.isNotBlank() },
        )
    }
}