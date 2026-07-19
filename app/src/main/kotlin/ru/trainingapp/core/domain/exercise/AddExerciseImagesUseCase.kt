package ru.trainingapp.core.domain.exercise

import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import javax.inject.Inject

class AddExerciseImagesUseCase @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
) {

    suspend operator fun invoke(
        exerciseDefinitionId: Long,
        sourceUris: List<String>,
    ) {
        if (exerciseDefinitionId <= 0L || sourceUris.isEmpty()) {
            return
        }

        repository.addExerciseImages(
            exerciseDefinitionId = exerciseDefinitionId,
            sourceUris = sourceUris,
        )
    }
}