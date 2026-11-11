package ru.trainingapp.core.domain.exercise

import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import javax.inject.Inject

class CreateExerciseDefinitionFromTemplateUseCase @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
) {

    suspend operator fun invoke(
        templateExerciseDefinitionId: Long,
        name: String,
        description: String,
    ) {
        val normalizedName = name.trim()
        if (templateExerciseDefinitionId <= 0L || normalizedName.isBlank()) return

        repository.createExerciseDefinitionFromTemplate(
            templateExerciseDefinitionId = templateExerciseDefinitionId,
            name = normalizedName,
            description = description.trim(),
        )
    }
}
