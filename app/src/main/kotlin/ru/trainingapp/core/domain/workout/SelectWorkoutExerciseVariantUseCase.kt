package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class SelectWorkoutExerciseVariantUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutExerciseId: Long,
        exerciseDefinitionId: Long,
    ) {
        repository.selectWorkoutExerciseVariant(
            workoutExerciseId = workoutExerciseId,
            exerciseDefinitionId = exerciseDefinitionId,
        )
    }
}
