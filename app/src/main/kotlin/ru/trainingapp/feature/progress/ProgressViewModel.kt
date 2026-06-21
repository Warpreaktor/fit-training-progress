package ru.trainingapp.feature.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.trainingapp.core.domain.progress.ObserveWorkoutExerciseProgressUseCase
import ru.trainingapp.core.model.WorkoutExerciseProgressPoint
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

private fun List<WorkoutExerciseProgressPoint>.toUiState(): ProgressUiState {
    val chartData = toProgressChartData()

    if (chartData == null) {
        return ProgressUiState(
            title = "Прогресс упражнения",
            isLoading = false,
            emptyTitle = "Пока нет точек прогресса",
            emptyMessage = "Измени подходы упражнения и выйди из редактора. После фиксации появится первая точка.",
        )
    }

    return ProgressUiState(
        title = chartData.exerciseName,
        isLoading = false,
        summaryValue = chartData.latestResultLabel,
        series = chartData.series,
        points = chartData.points,
    )
}