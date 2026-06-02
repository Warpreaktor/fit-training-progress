package ru.trainingapp.core.domain.exercise

import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import javax.inject.Inject

class ArchiveExerciseDefinitionUseCase @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
) {

    suspend operator fun invoke(id: Long) {
        if (id <= 0) {
            return
        }
        repository.archiveExerciseDefinition(id)
    }
}