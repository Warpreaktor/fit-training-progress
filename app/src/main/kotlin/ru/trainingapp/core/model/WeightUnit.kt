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
        shortLabel = "min",
        isTimeUnit = true,
    ),
    SEC(
        shortLabel = "sec",
        isTimeUnit = true,
    ),
    DEG(
        shortLabel = "deg°",
        isTimeUnit = false,
    ),
}
