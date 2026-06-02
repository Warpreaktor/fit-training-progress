package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class ArchiveWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(id: Long) {
        if (id <=0 ) return

        repository.archiveWorkout(id)
    }
}