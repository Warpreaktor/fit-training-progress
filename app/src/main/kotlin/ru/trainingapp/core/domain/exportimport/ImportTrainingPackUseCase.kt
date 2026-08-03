package ru.trainingapp.core.domain.exportimport

import ru.trainingapp.core.domain.repository.TrainingPackExportType
import ru.trainingapp.core.domain.repository.TrainingPackRepository
import ru.trainingapp.core.model.TrainingPackImportResult
import javax.inject.Inject

class ImportTrainingPackUseCase @Inject constructor(
    private val repository: TrainingPackRepository,
) {

    suspend operator fun invoke(
        sourceUri: String,
        expectedExportType: TrainingPackExportType,
    ): TrainingPackImportResult {
        require(sourceUri.isNotBlank()) {
            "Не выбран файл для импорта"
        }

        return repository.importTrainingPack(
            sourceUri = sourceUri,
            expectedExportType = expectedExportType,
        )
    }
}
