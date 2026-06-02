package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class CreateWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        name: String,
        description: String,
    ): Long {
        val normalizedName = name.trim()

        return repository.createWorkout(
            name = normalizedName,
            description = description
        )
    }
}