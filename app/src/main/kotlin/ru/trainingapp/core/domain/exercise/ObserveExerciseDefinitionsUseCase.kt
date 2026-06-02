package ru.trainingapp.core.domain.exercise

import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import javax.inject.Inject

class ObserveExerciseDefinitionsUseCase @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
) {

    operator fun invoke() = repository.observeActiveExerciseDefinitions()
}