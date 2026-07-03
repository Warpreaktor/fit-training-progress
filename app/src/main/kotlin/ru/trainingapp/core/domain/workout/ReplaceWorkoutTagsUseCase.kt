package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class ReplaceWorkoutTagsUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        workoutId: Long,
        tagIds: Set<Long>,
    ) {
        if (workoutId <= 0) {
            return
        }

        repository.replaceWorkoutTags(
            workoutId = workoutId,
            tagIds = tagIds,
        )
    }
}