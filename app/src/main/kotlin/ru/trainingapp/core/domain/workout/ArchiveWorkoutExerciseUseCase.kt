package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class ArchiveWorkoutExerciseUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutExerciseId: Long,
    ) {
        repository.archiveWorkoutExercise(workoutExerciseId)
    }
}