package ru.trainingapp.core.domain.workout
import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class AddWorkoutExerciseSetUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutExerciseId: Long,
    ): Long {
        return repository.addWorkoutExerciseSet(workoutExerciseId)
    }
}