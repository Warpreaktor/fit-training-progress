package ru.trainingapp.core.data.workout

import ru.trainingapp.core.database.model.WorkoutListItemDbModel
import ru.trainingapp.core.model.Workout

fun WorkoutListItemDbModel.toDomain(): Workout {
    return Workout(
        id = id,
        name = name,
        description = description,
        isLocked = isLocked,
        checkedExercisesCount = checkedExerciseCount,
        exercisesCount = exerciseCount,
    )
}