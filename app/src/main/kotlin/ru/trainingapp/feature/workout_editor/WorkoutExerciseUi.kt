package ru.trainingapp.feature.workout_editor

import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExercise
import ru.trainingapp.core.model.WorkoutExerciseSet
import ru.trainingapp.core.model.WorkoutExerciseSetLoad
import ru.trainingapp.core.model.WorkoutExerciseSetLoadType

data class WorkoutExerciseUi(
    val id: Long,
    val workoutId: Long,
    val exerciseDefinitionId: Long,
    val exerciseName: String,
    val sortOrder: Int,
    val comment: String?,
    val isChecked: Boolean,
    val sets: List<WorkoutExerciseSetUi>,
)

data class WorkoutExerciseSetUi(
    val id: Long,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val repsText: String,
    val loadType: WorkoutExerciseSetLoadType,
    val weightText: String,
    val weightUnit: WeightUnit,
    val durationSecondsText: String,
)

data class WorkoutExerciseSetDraft(
    val repsText: String? = null,
    val weightText: String? = null,
    val durationSecondsText: String? = null,
)

fun WorkoutExercise.toUi(
    setDrafts: Map<Long, WorkoutExerciseSetDraft>,
): WorkoutExerciseUi {
    return WorkoutExerciseUi(
        id = id,
        workoutId = workoutId,
        exerciseDefinitionId = exerciseDefinitionId,
        exerciseName = exerciseName,
        sortOrder = sortOrder,
        comment = comment,
        isChecked = isChecked,
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
        loadType = load.toLoadType(),
        weightText = draft?.weightText ?: load.toWeightText(),
        weightUnit = load.toWeightUnit(),
        durationSecondsText = draft?.durationSecondsText ?: load.toDurationSecondsText(),
    )
}

private fun WorkoutExerciseSetLoad.toLoadType(): WorkoutExerciseSetLoadType {
    return when (this) {
        is WorkoutExerciseSetLoad.Weight -> WorkoutExerciseSetLoadType.WEIGHT
        is WorkoutExerciseSetLoad.Time -> WorkoutExerciseSetLoadType.TIME
    }
}

private fun WorkoutExerciseSetLoad.toWeightText(): String {
    return when (this) {
        is WorkoutExerciseSetLoad.Weight -> value?.formatWeightValue().orEmpty()
        is WorkoutExerciseSetLoad.Time -> ""
    }
}

private fun WorkoutExerciseSetLoad.toWeightUnit(): WeightUnit {
    return when (this) {
        is WorkoutExerciseSetLoad.Weight -> unit
        is WorkoutExerciseSetLoad.Time -> WeightUnit.KG
    }
}

private fun WorkoutExerciseSetLoad.toDurationSecondsText(): String {
    return when (this) {
        is WorkoutExerciseSetLoad.Weight -> ""
        is WorkoutExerciseSetLoad.Time -> durationSeconds?.toString().orEmpty()
    }
}

private fun Double.formatWeightValue(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}