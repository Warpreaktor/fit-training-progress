package ru.trainingapp.feature.progress

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExerciseProgressPoint
import ru.trainingapp.core.model.WorkoutExerciseProgressSet
import ru.trainingapp.core.model.WorkoutExerciseSetLoadType

data class ProgressChartData(
    val workoutExerciseId: Long,
    val exerciseName: String,
    val latestResultLabel: String,
    val series: List<ProgressChartSeriesUi>,
    val points: List<ProgressChartPointUi>,
)

data class ProgressChartSeriesUi(
    val type: ProgressChartSeriesType,
    val title: String,
    val latestValueLabel: String,
)

enum class ProgressChartSeriesType {
    REPS,
    WEIGHT,
    DURATION,
}

data class ProgressChartPointUi(
    val dateKey: String,
    val dateLabel: String,
    val repsValue: Double?,
    val weightValue: Double?,
    val durationValue: Double?,
    val resultLabel: String,
)

private data class BestProgressResult(
    val reps: Int?,
    val weightValue: Double?,
    val weightUnit: WeightUnit?,
    val durationSeconds: Int?,
)

private enum class ProgressMode {
    REPS_AND_WEIGHT,
    REPS,
    DURATION,
}

fun List<WorkoutExerciseProgressPoint>.toProgressChartData(): ProgressChartData? {
    val dailyPoints = latestPointPerLocalDate()

    if (dailyPoints.isEmpty()) {
        return null
    }

    val bestResults = dailyPoints.mapNotNull { point ->
        point.findBestResult()
    }

    if (bestResults.isEmpty()) {
        return null
    }

    val mode = bestResults.resolveProgressMode()
    val weightUnitLabel = bestResults.resolveWeightUnitLabel()

    val chartPoints = dailyPoints.mapNotNull { point ->
        point.toChartPoint(
            mode = mode,
            weightUnitLabel = weightUnitLabel,
        )
    }

    if (chartPoints.isEmpty()) {
        return null
    }

    return ProgressChartData(
        workoutExerciseId = dailyPoints.last().workoutExerciseId,
        exerciseName = dailyPoints.last().exerciseNameSnapshot,
        latestResultLabel = chartPoints.last().resultLabel,
        series = chartPoints.toSeries(
            mode = mode,
            weightUnitLabel = weightUnitLabel,
        ),
        points = chartPoints,
    )
}

private fun List<WorkoutExerciseProgressPoint>.latestPointPerLocalDate(): List<WorkoutExerciseProgressPoint> {
    return groupBy { point -> point.createdAt.toDateKey() }
        .values
        .mapNotNull { points ->
            points.maxByOrNull { point -> point.createdAt }
        }
        .sortedBy { point -> point.createdAt }
}

private fun List<BestProgressResult>.resolveProgressMode(): ProgressMode {
    if (any { result -> result.weightValue != null }) {
        return ProgressMode.REPS_AND_WEIGHT
    }

    if (any { result -> result.durationSeconds != null }) {
        return ProgressMode.DURATION
    }

    return ProgressMode.REPS
}

private fun List<BestProgressResult>.resolveWeightUnitLabel(): String {
    val weightUnits = mapNotNull { result -> result.weightUnit }.distinct()

    return when (weightUnits.singleOrNull()) {
        WeightUnit.KG -> "кг"
        WeightUnit.LB -> "lb"
        null -> "вес"
    }
}

private fun WorkoutExerciseProgressPoint.toChartPoint(
    mode: ProgressMode,
    weightUnitLabel: String,
): ProgressChartPointUi? {
    val result = findBestResult() ?: return null

    return when (mode) {
        ProgressMode.REPS_AND_WEIGHT -> {
            val reps = result.reps ?: return null
            val weight = result.weightValue

            ProgressChartPointUi(
                dateKey = createdAt.toDateKey(),
                dateLabel = createdAt.toDateLabel(),
                repsValue = reps.toDouble(),
                weightValue = weight,
                durationValue = null,
                resultLabel = if (weight == null) {
                    "$reps повт."
                } else {
                    "$reps повт. • ${weight.formatSmart()} $weightUnitLabel"
                },
            )
        }

        ProgressMode.REPS -> {
            val reps = result.reps ?: return null

            ProgressChartPointUi(
                dateKey = createdAt.toDateKey(),
                dateLabel = createdAt.toDateLabel(),
                repsValue = reps.toDouble(),
                weightValue = null,
                durationValue = null,
                resultLabel = "$reps повт.",
            )
        }

        ProgressMode.DURATION -> {
            val durationSeconds = result.durationSeconds ?: return null

            ProgressChartPointUi(
                dateKey = createdAt.toDateKey(),
                dateLabel = createdAt.toDateLabel(),
                repsValue = null,
                weightValue = null,
                durationValue = durationSeconds.toDouble(),
                resultLabel = "$durationSeconds сек.",
            )
        }
    }
}

private fun WorkoutExerciseProgressPoint.findBestResult(): BestProgressResult? {
    val bestWeightSet = sets
        .filter { set ->
            set.loadType == WorkoutExerciseSetLoadType.WEIGHT && set.weightValue != null
        }
        .maxWithOrNull(
            compareBy<WorkoutExerciseProgressSet> { set ->
                set.weightValue ?: 0.0
            }.thenBy { set ->
                set.reps
            }.thenBy { set ->
                set.setNumber
            }
        )

    if (bestWeightSet != null) {
        return BestProgressResult(
            reps = bestWeightSet.reps,
            weightValue = bestWeightSet.weightValue,
            weightUnit = bestWeightSet.weightUnit,
            durationSeconds = null,
        )
    }

    val bestDurationSet = sets
        .filter { set ->
            set.loadType == WorkoutExerciseSetLoadType.TIME && set.durationSeconds != null
        }
        .maxWithOrNull(
            compareBy<WorkoutExerciseProgressSet> { set ->
                set.durationSeconds ?: 0
            }.thenBy { set ->
                set.setNumber
            }
        )

    if (bestDurationSet != null) {
        return BestProgressResult(
            reps = null,
            weightValue = null,
            weightUnit = null,
            durationSeconds = bestDurationSet.durationSeconds,
        )
    }

    val bestRepsSet = sets.maxWithOrNull(
        compareBy<WorkoutExerciseProgressSet> { set ->
            set.reps
        }.thenBy { set ->
            set.setNumber
        }
    ) ?: return null

    return BestProgressResult(
        reps = bestRepsSet.reps,
        weightValue = null,
        weightUnit = null,
        durationSeconds = null,
    )
}

private fun List<ProgressChartPointUi>.toSeries(
    mode: ProgressMode,
    weightUnitLabel: String,
): List<ProgressChartSeriesUi> {
    return when (mode) {
        ProgressMode.REPS_AND_WEIGHT -> listOf(
            ProgressChartSeriesUi(
                type = ProgressChartSeriesType.REPS,
                title = "Повторы",
                latestValueLabel = "${last().repsValue.formatSmartOrEmpty()} повт.",
            ),
            ProgressChartSeriesUi(
                type = ProgressChartSeriesType.WEIGHT,
                title = "Вес",
                latestValueLabel = "${last().weightValue.formatSmartOrEmpty()} $weightUnitLabel",
            ),
        )

        ProgressMode.REPS -> listOf(
            ProgressChartSeriesUi(
                type = ProgressChartSeriesType.REPS,
                title = "Повторы",
                latestValueLabel = "${last().repsValue.formatSmartOrEmpty()} повт.",
            )
        )

        ProgressMode.DURATION -> listOf(
            ProgressChartSeriesUi(
                type = ProgressChartSeriesType.DURATION,
                title = "Время",
                latestValueLabel = "${last().durationValue.formatSmartOrEmpty()} сек.",
            )
        )
    }
}

private fun Long.toDateKey(): String {
    return SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault(),
    ).format(Date(this))
}

private fun Long.toDateLabel(): String {
    return SimpleDateFormat(
        "dd.MM.yyyy",
        Locale.getDefault(),
    ).format(Date(this))
}

private fun Double?.formatSmartOrEmpty(): String {
    return this?.formatSmart().orEmpty()
}

private fun Double.formatSmart(): String {
    val rounded = roundToLong()

    if (this == rounded.toDouble()) {
        return rounded.toString()
    }

    return String.format(
        Locale.getDefault(),
        "%.1f",
        this,
    )
}