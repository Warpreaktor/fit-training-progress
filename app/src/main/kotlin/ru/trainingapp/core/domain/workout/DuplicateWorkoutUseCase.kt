package ru.trainingapp.core.domain.workout

import jakarta.inject.Inject
import ru.trainingapp.core.domain.repository.WorkoutRepository

class DuplicateWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutId: Long,
        name: String,
    ): Long {
        if (workoutId <= 0L) return 0L

        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return 0L

        return repository.duplicateWorkout(
            workoutId = workoutId,
            name = normalizedName,
        )
    }
}