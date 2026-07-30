package ru.trainingapp.core.data.exportimport.dto

const val CURRENT_TRAINING_PACK_SCHEMA_VERSION = 1
const val TRAINING_PACK_EXPORT_TYPE_SINGLE_EXERCISE = "SINGLE_EXERCISE"
const val TRAINING_PACK_EXPORT_TYPE_WORKOUT = "WORKOUT"

data class TrainingPackDto(
    val schemaVersion: Int = CURRENT_TRAINING_PACK_SCHEMA_VERSION,
    val exportType: String,
    val createdAt: Long? = null,
    val sourceApp: String? = null,
    val tags: List<ExportTagDto> = emptyList(),
    val exercises: List<ExportExerciseDto> = emptyList(),
    val workouts: List<ExportWorkoutDto> = emptyList(),
)

data class ExportTagDto(
    val ref: String?,
    val name: String?,
    val color: String? = null,
)

data class ExportExerciseDto(
    val ref: String?,
    val name: String?,
    val description: String? = null,
    val tagRefs: List<String> = emptyList(),
    val images: List<ExportExerciseImageDto> = emptyList(),
)

data class ExportExerciseImageDto(
    val ref: String? = null,
    val path: String?,
    val sortOrder: Int? = null,
    val isCover: Boolean? = null,
)

data class ExportWorkoutDto(
    val ref: String?,
    val name: String?,
    val description: String? = null,
    val tagRefs: List<String> = emptyList(),
    val exercises: List<ExportWorkoutExerciseDto> = emptyList(),
)

data class ExportWorkoutExerciseDto(
    val ref: String?,
    val exerciseRef: String?,
    val sortOrder: Int? = null,
    val comment: String? = null,
    val sets: List<ExportWorkoutExerciseSetDto> = emptyList(),
)

data class ExportWorkoutExerciseSetDto(
    val setNumber: Int? = null,
    val reps: Int? = null,
    val weightValue: Double? = null,
    val weightUnit: String? = null,
    val durationSeconds: Int? = null,
)
