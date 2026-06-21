package ru.trainingapp.feature.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.trainingapp.core.domain.progress.ObserveWorkoutExerciseProgressUseCase
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExerciseProgressPoint
import ru.trainingapp.core.model.WorkoutExerciseProgressSet
import ru.trainingapp.core.model.WorkoutExerciseSetLoadType
import ru.trainingapp.navigation.AppRoute
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeWorkoutExerciseProgressUseCase: ObserveWorkoutExerciseProgressUseCase,
) : ViewModel() {

    private val workoutExerciseId: Long = savedStateHandle
        .get<Long>(AppRoute.ExerciseProgress.ARG_WORKOUT_EXERCISE_ID)
        ?: 0L

    val uiState: StateFlow<ProgressUiState> = if (workoutExerciseId <= 0L) {
        flowOf(
            ProgressUiState(
                title = "Прогресс",
                isLoading = false,
                emptyTitle = "Выбери упражнение",
                emptyMessage = "Открой прогресс из карточки упражнения в тренировке.",
            )
        )
    } else {
        observeWorkoutExerciseProgressUseCase(workoutExerciseId)
            .map { points ->
                points.toUiState()
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState(
            title = "Прогресс",
            isLoading = workoutExerciseId > 0L,
        ),
    )
}

data class ProgressUiState(
    val title: String = "Прогресс",
    val isLoading: Boolean = true,
    val summaryTitle: String = "Лучший результат",
    val summaryValue: String = "",
    val series: List<ProgressChartSeriesUi> = emptyList(),
    val points: List<ProgressChartPointUi> = emptyList(),
    val emptyTitle: String = "Графиков пока нет",
    val emptyMessage: String = "Измени значения упражнения и выйди из редактора, чтобы появилась первая точка прогресса.",
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

private fun List<WorkoutExerciseProgressPoint>.toUiState(): ProgressUiState {
    val dailyPoints = latestPointPerLocalDate()

    if (dailyPoints.isEmpty()) {
        return ProgressUiState(
            title = "Прогресс упражнения",
            isLoading = false,
            emptyTitle = "Пока нет точек прогресса",
            emptyMessage = "Измени подходы упражнения и выйди из редактора. После фиксации появится первая точка.",
        )
    }

    val bestResults = dailyPoints.mapNotNull { point -> point.findBestResult() }

    if (bestResults.isEmpty()) {
        return ProgressUiState(
            title = dailyPoints.last().exerciseNameSnapshot,
            isLoading = false,
            emptyTitle = "Пока нет данных для графика",
            emptyMessage = "В сохранённых точках прогресса нет повторов, веса или времени.",
        )
    }

    val mode = bestResults.resolveProgressMode()
    val weightUnitLabel = bestResults.resolveWeightUnitLabel()
    val chartPoints = dailyPoints.mapNotNull { point ->
        point.toChartPoint(
            mode = mode,
            weightUnitLabel = weightUnitLabel,
        )
    }
    val lastPoint = chartPoints.last()

    return ProgressUiState(
        title = dailyPoints.last().exerciseNameSnapshot,
        isLoading = false,
        summaryValue = lastPoint.resultLabel,
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
        .mapNotNull { points -> points.maxByOrNull { point -> point.createdAt } }
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
            compareBy<WorkoutExerciseProgressSet> { set -> set.weightValue ?: 0.0 }
                .thenBy { set -> set.reps }
                .thenBy { set -> set.setNumber }
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
            compareBy<WorkoutExerciseProgressSet> { set -> set.durationSeconds ?: 0 }
                .thenBy { set -> set.setNumber }
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
        compareBy<WorkoutExerciseProgressSet> { set -> set.reps }
            .thenBy { set -> set.setNumber }
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