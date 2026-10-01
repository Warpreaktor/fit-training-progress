package ru.trainingapp.core.domain.exportimport

import ru.trainingapp.core.domain.repository.TrainingPackRepository
import javax.inject.Inject

class ExportWorkoutUseCase @Inject constructor(
    private val repository: TrainingPackRepository,
) {

    suspend operator fun invoke(
        workoutId: Long,
        destinationUri: String,
    ) {
        require(workoutId > 0L) {
            "Не выбрана тренировка для экспорта"
        }

        require(destinationUri.isNotBlank()) {
            "Не выбран файл для экспорта"
        }

        repository.exportWorkout(
            workoutId = workoutId,
            destinationUri = destinationUri,
        )
    }
}
