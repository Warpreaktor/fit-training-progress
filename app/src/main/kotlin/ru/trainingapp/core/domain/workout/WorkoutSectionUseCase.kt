package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Inject

class WorkoutSectionUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) {

    suspend fun create(
        workoutId: Long,
        workoutExerciseIds: Set<Long>,
        name: String,
    ) {
        workoutRepository.createWorkoutExerciseSection(
            workoutId = workoutId,
            workoutExerciseIds = workoutExerciseIds,
            name = name,
        )
    }

    suspend fun rename(
        workoutId: Long,
        sectionId: String,
        name: String,
    ) {
        workoutRepository.renameWorkoutExerciseSection(
            workoutId = workoutId,
            sectionId = sectionId,
            name = name,
        )
    }

    suspend fun remove(
        workoutId: Long,
        sectionId: String,
    ) {
        workoutRepository.removeWorkoutExerciseSection(
            workoutId = workoutId,
            sectionId = sectionId,
        )
    }

    suspend fun moveExercise(
        workoutId: Long,
        workoutExerciseId: Long,
        sectionId: String?,
        sectionName: String?,
    ) {
        workoutRepository.moveWorkoutExerciseToSection(
            workoutId = workoutId,
            workoutExerciseId = workoutExerciseId,
            sectionId = sectionId,
            sectionName = sectionName,
        )
    }
}
