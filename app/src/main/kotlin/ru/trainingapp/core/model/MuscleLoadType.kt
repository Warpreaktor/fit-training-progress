package ru.trainingapp.core.model

enum class MuscleLoadType(
    val code: String,
    val score: Float,
) {
    PRIMARY(
        code = "PRIMARY",
        score = 1f,
    ),
    SECONDARY(
        code = "SECONDARY",
        score = 0.5f,
    );

    companion object {
        fun fromCode(code: String): MuscleLoadType =
            entries.firstOrNull { type -> type.code == code } ?: PRIMARY
    }
}
