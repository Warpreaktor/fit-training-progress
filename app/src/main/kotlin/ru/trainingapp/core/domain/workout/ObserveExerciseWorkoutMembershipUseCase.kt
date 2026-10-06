package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class ObserveExerciseWorkoutMembershipUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    operator fun invoke(exerciseDefinitionId: Long) =
        repository.observeWorkoutIdsContainingExercise(exerciseDefinitionId)
}
