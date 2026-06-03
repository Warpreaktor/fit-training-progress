package ru.trainingapp.core.domain.workout

import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.domain.repository.WorkoutRepository
import ru.trainingapp.core.model.WorkoutEditorData
import javax.inject.Inject

class ObserveWorkoutEditorUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    operator fun invoke(
        workoutId: Long,
    ): Flow<WorkoutEditorData?> {
        return repository.observeWorkoutEditorData(workoutId)
    }
}