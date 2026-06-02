package ru.trainingapp.core.domain.exercise

import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import javax.inject.Inject

class CreateExerciseDefinitionUseCase @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
) {

    suspend operator fun invoke(
        name: String,
        description: String,
    ) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return

        repository.createExerciseDefinition(
            name = normalizedName,
            description = description.trim(),
        )
    }
}