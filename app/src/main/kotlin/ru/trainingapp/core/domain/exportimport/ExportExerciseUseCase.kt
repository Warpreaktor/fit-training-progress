package ru.trainingapp.core.domain.exportimport

import ru.trainingapp.core.domain.repository.TrainingPackRepository
import javax.inject.Inject

class ExportExerciseUseCase @Inject constructor(
    private val repository: TrainingPackRepository,
) {

    suspend operator fun invoke(
        exerciseDefinitionId: Long,
        destinationUri: String,
    ) {
        require(exerciseDefinitionId > 0L) {
            "Не выбрано упражнение для экспорта"
        }

        require(destinationUri.isNotBlank()) {
            "Не выбран файл для экспорта"
        }

        repository.exportExercise(
            exerciseDefinitionId = exerciseDefinitionId,
            destinationUri = destinationUri,
        )
    }
}
