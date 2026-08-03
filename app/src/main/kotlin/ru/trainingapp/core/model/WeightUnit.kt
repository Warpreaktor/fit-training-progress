package ru.trainingapp.core.model

enum class WeightUnit(
    val shortLabel: String,
    val isTimeUnit: Boolean,
) {
    KG(
        shortLabel = "kg",
        isTimeUnit = false,
    ),
    LB(
        shortLabel = "lb",
        isTimeUnit = false,
    ),
    MIN(
        shortLabel = "мин",
        isTimeUnit = true,
    ),
    SEC(
        shortLabel = "сек",
        isTimeUnit = true,
    ),
}
