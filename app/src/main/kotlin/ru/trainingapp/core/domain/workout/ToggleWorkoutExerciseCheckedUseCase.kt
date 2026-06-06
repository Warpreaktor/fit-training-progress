package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class ToggleWorkoutExerciseCheckedUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutExerciseId: Long,
        isChecked: Boolean,
    ) {
        if (workoutExerciseId <= 0) return

        repository.updateWorkoutExerciseChecked(
            workoutExerciseId = workoutExerciseId,
            isChecked = isChecked,
        )
    }
}