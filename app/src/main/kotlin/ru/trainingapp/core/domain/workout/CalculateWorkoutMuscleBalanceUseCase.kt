package ru.trainingapp.core.domain.workout

import javax.inject.Inject
import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.MuscleGroup
import ru.trainingapp.core.model.WorkoutExercise
import ru.trainingapp.core.model.WorkoutMuscleBalance
import ru.trainingapp.core.model.WorkoutMuscleBalanceItem

class CalculateWorkoutMuscleBalanceUseCase @Inject constructor() {

    operator fun invoke(
        exercises: List<WorkoutExercise>,
        exerciseDefinitions: List<ExerciseDefinition>,
    ): WorkoutMuscleBalance {
        val definitionsById = exerciseDefinitions.associateBy { definition -> definition.id }
        val muscleCounts = linkedMapOf<MuscleGroup, Int>()
        var exercisesWithMuscles = 0

        exercises.forEach { exercise ->
            val muscles = definitionsById[exercise.selectedExerciseDefinitionId]
                ?.muscles
                .orEmpty()
                .distinct()

            if (muscles.isNotEmpty()) {
                exercisesWithMuscles += 1
            }

            muscles.forEach { muscleGroup ->
                muscleCounts[muscleGroup] = (muscleCounts[muscleGroup] ?: 0) + 1
            }
        }

        val totalExercises = exercises.size
        val items = muscleCounts
            .map { (muscleGroup, exerciseCount) ->
                WorkoutMuscleBalanceItem(
                    muscleGroup = muscleGroup,
                    exerciseCount = exerciseCount,
                    share = if (totalExercises == 0) {
                        0f
                    } else {
                        exerciseCount.toFloat() / totalExercises.toFloat()
                    },
                )
            }
            .sortedWith(
                compareByDescending<WorkoutMuscleBalanceItem> { item -> item.exerciseCount }
                    .thenBy { item -> item.muscleGroup.ordinal }
            )

        return WorkoutMuscleBalance(
            totalExercises = totalExercises,
            exercisesWithMuscles = exercisesWithMuscles,
            items = items,
        )
    }
}
