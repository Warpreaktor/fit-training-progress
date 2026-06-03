package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class RemoveWorkoutExerciseSetUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutExerciseSetId: Long,
    ) {
        repository.removeWorkoutExerciseSet(workoutExerciseSetId)
    }
}