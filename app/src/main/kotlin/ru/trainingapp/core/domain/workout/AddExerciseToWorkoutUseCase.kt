package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class AddExerciseToWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutId: Long,
        exerciseDefinitionId: Long,
    ): Long {
        return repository.addExerciseToWorkout(
            workoutId = workoutId,
            exerciseDefinitionId = exerciseDefinitionId,
        )
    }
}