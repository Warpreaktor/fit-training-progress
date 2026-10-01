package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class UpdateWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutId: Long,
        name: String,
        description: String,
    ) {
        if (workoutId <= 0L) return

        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return

        repository.updateWorkout(
            workoutId = workoutId,
            name = normalizedName,
            description = description,
        )
    }
}
