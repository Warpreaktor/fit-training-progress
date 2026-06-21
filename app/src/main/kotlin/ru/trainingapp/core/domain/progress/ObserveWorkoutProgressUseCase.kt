package ru.trainingapp.core.domain.progress

import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.domain.repository.WorkoutRepository
import ru.trainingapp.core.model.WorkoutExerciseProgressPoint
import javax.inject.Inject

class ObserveWorkoutProgressUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    operator fun invoke(
        workoutId: Long,
    ): Flow<List<WorkoutExerciseProgressPoint>> {
        return repository.observeWorkoutProgress(workoutId)
    }
}