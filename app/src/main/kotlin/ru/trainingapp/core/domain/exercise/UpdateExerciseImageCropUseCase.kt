package ru.trainingapp.core.domain.exercise

import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import javax.inject.Inject

class UpdateExerciseImageCropUseCase @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
) {

    suspend operator fun invoke(
        exerciseDefinitionId: Long,
        imageId: Long,
        cropZoom: Float,
        cropOffsetX: Float,
        cropOffsetY: Float,
    ) {
        if (exerciseDefinitionId <= 0L || imageId <= 0L) {
            return
        }

        repository.updateExerciseImageCrop(
            exerciseDefinitionId = exerciseDefinitionId,
            imageId = imageId,
            cropZoom = cropZoom.coerceIn(1f, 4f),
            cropOffsetX = cropOffsetX.coerceIn(-1f, 1f),
            cropOffsetY = cropOffsetY.coerceIn(-1f, 1f),
        )
    }
}
