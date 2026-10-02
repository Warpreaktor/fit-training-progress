package ru.trainingapp.feature.workout_editor

import ru.trainingapp.core.model.ExerciseDefinitionAlternative
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExercise
import ru.trainingapp.core.model.WorkoutExerciseSet
import ru.trainingapp.core.model.WorkoutExerciseSetLoad

data class WorkoutExerciseUi(
    val id: Long,
    val workoutId: Long,
    val exerciseDefinitionId: Long,
    val selectedExerciseDefinitionId: Long,
    val originalExerciseName: String,
    val exerciseName: String,
    val sortOrder: Int,
    val comment: String?,
    val isChecked: Boolean,
    val alternatives: List<WorkoutExerciseAlternativeUi>,
    val sets: List<WorkoutExerciseSetUi>,
) {
    val isAlternativeSelected: Boolean
        get() = selectedExerciseDefinitionId != exerciseDefinitionId
}

data class WorkoutExerciseAlternativeUi(
    val id: Long,
    val name: String,
)

data class WorkoutExerciseSetUi(
    val id: Long,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val repsText: String,
    val quantityText: String,
    val measurementUnit: WeightUnit,
)

data class WorkoutExerciseSetDraft(
    val repsText: String? = null,
    val quantityText: String? = null,
)

fun WorkoutExercise.toUi(
    setDrafts: Map<Long, WorkoutExerciseSetDraft>,
    alternatives: List<ExerciseDefinitionAlternative>,
): WorkoutExerciseUi {
    return WorkoutExerciseUi(
        id = id,
        workoutId = workoutId,
        exerciseDefinitionId = exerciseDefinitionId,
        selectedExerciseDefinitionId = selectedExerciseDefinitionId,
        originalExerciseName = originalExerciseName,
        exerciseName = exerciseName,
        sortOrder = sortOrder,
        comment = comment,
        isChecked = isChecked,
        alternatives = alternatives.map { alternative ->
            WorkoutExerciseAlternativeUi(
                id = alternative.id,
                name = alternative.name,
            )
        },
        sets = sets.map { set ->
            set.toUi(
                draft = setDrafts[set.id],
            )
        },
    )
}

private fun WorkoutExerciseSet.toUi(
    draft: WorkoutExerciseSetDraft?,
): WorkoutExerciseSetUi {
    val load = load

    return WorkoutExerciseSetUi(
        id = id,
        workoutExerciseId = workoutExerciseId,
        setNumber = setNumber,
        repsText = draft?.repsText ?: reps.toString(),
        quantityText = draft?.quantityText ?: load.toQuantityText(),
        measurementUnit = load.toUnit(),
    )
}

private fun WorkoutExerciseSetLoad.toQuantityText(): String {
    return when (this) {
        is WorkoutExerciseSetLoad.Weight -> value?.formatValue().orEmpty()

        // Legacy TIME records are shown in the same generic quantity field.
        // Minutes are restored to the value the user originally saw.
        is WorkoutExerciseSetLoad.Time -> {
            val seconds = durationSeconds ?: return ""

            when (unit) {
                WeightUnit.MIN -> (seconds / 60.0).formatValue()
                else -> seconds.toDouble().formatValue()
            }
        }
    }
}

private fun WorkoutExerciseSetLoad.toUnit(): WeightUnit {
    return when (this) {
        is WorkoutExerciseSetLoad.Weight -> unit
        is WorkoutExerciseSetLoad.Time -> unit
    }
}

private fun Double.formatValue(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}
