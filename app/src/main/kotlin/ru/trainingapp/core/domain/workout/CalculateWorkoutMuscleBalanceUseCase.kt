package ru.trainingapp.core.domain.workout

import javax.inject.Inject
import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.MuscleGroup
import ru.trainingapp.core.model.MuscleLoadType
import ru.trainingapp.core.model.WorkoutExercise
import ru.trainingapp.core.model.WorkoutMuscleBalance
import ru.trainingapp.core.model.WorkoutMuscleBalanceItem

class CalculateWorkoutMuscleBalanceUseCase @Inject constructor() {

    operator fun invoke(
        exercises: List<WorkoutExercise>,
        exerciseDefinitions: List<ExerciseDefinition>,
    ): WorkoutMuscleBalance {
        val definitionsById = exerciseDefinitions.associateBy { definition -> definition.id }
        val muscleStats = linkedMapOf<MuscleGroup, MutableMuscleBalance>()
        var exercisesWithMuscles = 0

        exercises.forEach { exercise ->
            val definition = definitionsById[exercise.selectedExerciseDefinitionId]
            val primaryMuscles = definition?.muscles.orEmpty().distinct()
            val secondaryMuscles = definition?.secondaryMuscles
                .orEmpty()
                .distinct()
                .filterNot { muscleGroup -> muscleGroup in primaryMuscles }

            if (primaryMuscles.isNotEmpty() || secondaryMuscles.isNotEmpty()) {
                exercisesWithMuscles += 1
            }

            primaryMuscles.forEach { muscleGroup ->
                muscleStats.getOrPut(muscleGroup) { MutableMuscleBalance() }
                    .add(MuscleLoadType.PRIMARY.score)
            }

            secondaryMuscles.forEach { muscleGroup ->
                muscleStats.getOrPut(muscleGroup) { MutableMuscleBalance() }
                    .add(MuscleLoadType.SECONDARY.score)
            }
        }

        val totalExercises = exercises.size
        val items = muscleStats
            .map { (muscleGroup, stats) ->
                WorkoutMuscleBalanceItem(
                    muscleGroup = muscleGroup,
                    exerciseCount = stats.exerciseCount,
                    score = stats.score,
                    share = if (totalExercises == 0) {
                        0f
                    } else {
                        stats.score / totalExercises.toFloat()
                    },
                )
            }
            .sortedWith(
                compareByDescending<WorkoutMuscleBalanceItem> { item -> item.score }
                    .thenByDescending { item -> item.exerciseCount }
                    .thenBy { item -> item.muscleGroup.ordinal }
            )

        return WorkoutMuscleBalance(
            totalExercises = totalExercises,
            exercisesWithMuscles = exercisesWithMuscles,
            items = items,
        )
    }

    private data class MutableMuscleBalance(
        var exerciseCount: Int = 0,
        var score: Float = 0f,
    ) {
        fun add(score: Float) {
            exerciseCount += 1
            this.score += score
        }
    }
}
