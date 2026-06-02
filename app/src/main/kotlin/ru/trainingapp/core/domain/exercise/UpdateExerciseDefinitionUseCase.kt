package ru.trainingapp.core.domain.exercise

import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import javax.inject.Inject

class UpdateExerciseDefinitionUseCase @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
) {

    suspend operator fun invoke(
        id: Long,
        name: String,
        description: String,
    ) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return

        repository.updateExerciseDefinition(
            id = id,
            name = normalizedName,
            description = description.trim(),
        )
    }
}