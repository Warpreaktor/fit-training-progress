package ru.trainingapp.core.domain.exercise

import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import javax.inject.Inject

class DeleteExerciseImageUseCase @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
) {

    suspend operator fun invoke(
        exerciseDefinitionId: Long,
        imageId: Long,
    ) {
        if (exerciseDefinitionId <= 0L || imageId <= 0L) {
            return
        }

        repository.deleteExerciseImage(
            exerciseDefinitionId = exerciseDefinitionId,
            imageId = imageId,
        )
    }
}