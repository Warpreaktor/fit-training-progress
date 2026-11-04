package ru.trainingapp.core.domain.exercise

import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import ru.trainingapp.core.model.MuscleGroup
import javax.inject.Inject

class ReplaceExerciseDefinitionMusclesUseCase @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
) {

    suspend operator fun invoke(
        exerciseDefinitionId: Long,
        primaryMuscleGroups: Set<MuscleGroup>,
        secondaryMuscleGroups: Set<MuscleGroup>,
    ) {
        if (exerciseDefinitionId <= 0L) {
            return
        }

        repository.replaceExerciseDefinitionMuscles(
            exerciseDefinitionId = exerciseDefinitionId,
            primaryMuscleGroups = primaryMuscleGroups,
            secondaryMuscleGroups = secondaryMuscleGroups - primaryMuscleGroups,
        )
    }
}
