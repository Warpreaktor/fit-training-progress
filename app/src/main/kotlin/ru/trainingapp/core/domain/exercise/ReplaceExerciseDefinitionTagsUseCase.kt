package ru.trainingapp.core.domain.exercise

import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import javax.inject.Inject

class ReplaceExerciseDefinitionTagsUseCase @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
) {

    suspend operator fun invoke(
        exerciseDefinitionId: Long,
        tagIds: Set<Long>,
    ) {
        if (exerciseDefinitionId <= 0) {
            return
        }

        repository.replaceExerciseDefinitionTags(
            exerciseDefinitionId = exerciseDefinitionId,
            tagIds = tagIds,
        )
    }
}