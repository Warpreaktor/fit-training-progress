package ru.trainingapp.core.domain.progress

import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.domain.repository.WorkoutRepository
import ru.trainingapp.core.model.WorkoutExerciseProgressPoint
import javax.inject.Inject

class ObserveWorkoutExerciseProgressUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    operator fun invoke(
        workoutExerciseId: Long,
    ): Flow<List<WorkoutExerciseProgressPoint>> {
        return repository.observeWorkoutExerciseProgress(workoutExerciseId)
    }
}