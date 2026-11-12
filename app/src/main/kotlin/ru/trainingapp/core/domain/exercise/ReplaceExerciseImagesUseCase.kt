package ru.trainingapp.core.domain.exercise

import javax.inject.Inject
import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import ru.trainingapp.core.model.ExerciseImageEdit

class ReplaceExerciseImagesUseCase @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
) {
    suspend operator fun invoke(
        exerciseDefinitionId: Long,
        images: List<ExerciseImageEdit>,
    ) {
        if (exerciseDefinitionId <= 0L) return

        repository.replaceExerciseImages(
            exerciseDefinitionId = exerciseDefinitionId,
            images = images,
        )
    }
}
