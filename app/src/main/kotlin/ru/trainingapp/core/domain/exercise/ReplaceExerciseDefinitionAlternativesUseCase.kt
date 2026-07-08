package ru.trainingapp.core.domain.exercise

import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import javax.inject.Inject

class ReplaceExerciseDefinitionAlternativesUseCase @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
) {

    suspend operator fun invoke(
        exerciseDefinitionId: Long,
        alternativeExerciseDefinitionIds: Set<Long>,
    ) {
        if (exerciseDefinitionId <= 0L) {
            return
        }

        repository.replaceExerciseDefinitionAlternatives(
            exerciseDefinitionId = exerciseDefinitionId,
            alternativeExerciseDefinitionIds = alternativeExerciseDefinitionIds
                .filter { alternativeExerciseDefinitionId ->
                    alternativeExerciseDefinitionId > 0L &&
                        alternativeExerciseDefinitionId != exerciseDefinitionId
                }
                .toSet(),
        )
    }
}