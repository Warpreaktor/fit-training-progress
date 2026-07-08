package ru.trainingapp.core.model

data class Workout(
    val id: Long,
    val name: String,
    val description: String?,
    val isLocked: Boolean,
    val checkedExercisesCount: Int,
    val exercisesCount: Int,
    val tags: List<Tag> = emptyList(),
)

data class ExerciseDefinition(
    val id: Long,
    val name: String,
    val description: String,
    val tags: List<Tag> = emptyList(),
    val alternatives: List<ExerciseDefinitionAlternative> = emptyList(),
)

data class ExerciseDefinitionAlternative(
    val id: Long,
    val name: String,
)

data class WorkoutExercise(
    val id: Long,
    val workoutId: Long,
    val exerciseDefinitionId: Long,
    val exerciseName: String,
    val sortOrder: Int,
    val comment: String?,
    val isChecked: Boolean,
    val sets: List<WorkoutExerciseSet>,
)

data class WorkoutExerciseSet(
    val id: Long,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val reps: Int,
    val load: WorkoutExerciseSetLoad,
    val createdAt: Long,
    val updatedAt: Long,
)