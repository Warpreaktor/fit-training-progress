package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class RemoveExerciseFromWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutId: Long,
        exerciseDefinitionId: Long,
    ) {
        repository.removeExerciseFromWorkout(
            workoutId = workoutId,
            exerciseDefinitionId = exerciseDefinitionId,
        )
    }
}
