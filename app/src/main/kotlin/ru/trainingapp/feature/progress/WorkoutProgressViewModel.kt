package ru.trainingapp.feature.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import ru.trainingapp.core.domain.progress.ObserveWorkoutProgressUseCase
import ru.trainingapp.core.domain.workout.ObserveWorkoutEditorUseCase
import ru.trainingapp.core.model.WorkoutExerciseProgressPoint
import ru.trainingapp.navigation.AppRoute
import javax.inject.Inject

@HiltViewModel
class WorkoutProgressViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeWorkoutProgressUseCase: ObserveWorkoutProgressUseCase,
    observeWorkoutEditorUseCase: ObserveWorkoutEditorUseCase,
) : ViewModel() {

    private val workoutId: Long = savedStateHandle
        .get<Long>(AppRoute.WorkoutProgress.ARG_WORKOUT_ID)
        ?: 0L

    val uiState: StateFlow<WorkoutProgressUiState> = if (workoutId <= 0L) {
        flowOf(
            WorkoutProgressUiState(
                isLoading = false,
                emptyTitle = "Тренировка не выбрана",
                emptyMessage = "Открой прогресс из карточки тренировки.",
            )
        )
    } else {
        combine(
            observeWorkoutProgressUseCase(workoutId),
            observeWorkoutEditorUseCase(workoutId),
        ) { points, editorData ->
            points.toWorkoutProgressUiState(
                activeWorkoutExerciseIds = editorData
                    ?.exercises
                    ?.mapTo(mutableSetOf()) { exercise -> exercise.id }
                    .orEmpty(),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutProgressUiState(
            isLoading = workoutId > 0L,
        ),
    )
}

data class WorkoutProgressUiState(
    val title: String = "Прогресс тренировки",
    val isLoading: Boolean = true,
    val exercises: List<WorkoutProgressExerciseUi> = emptyList(),
    val archivedExercises: List<WorkoutProgressExerciseUi> = emptyList(),
    val emptyTitle: String = "Пока нет прогресса",
    val emptyMessage: String = "Измени значения упражнений и выйди из редактора. После фиксации появятся графики.",
)

data class WorkoutProgressExerciseUi(
    val workoutExerciseId: Long,
    val exerciseName: String,
    val latestResultLabel: String,
    val series: List<ProgressChartSeriesUi>,
    val points: List<ProgressChartPointUi>,
)

private fun List<WorkoutExerciseProgressPoint>.toWorkoutProgressUiState(
    activeWorkoutExerciseIds: Set<Long>,
): WorkoutProgressUiState {
    val allExercises = groupBy { point -> point.workoutExerciseId }
        .values
        .mapNotNull { points -> points.toWorkoutProgressExerciseUi() }
        .sortedBy { exercise -> exercise.exerciseName.lowercase(Locale.getDefault()) }

    if (allExercises.isEmpty()) {
        return WorkoutProgressUiState(
            isLoading = false,
            emptyTitle = "Пока нет прогресса",
            emptyMessage = "У этой тренировки ещё нет сохранённых точек прогресса.",
        )
    }

    val (activeExercises, archivedExercises) = allExercises.partition { exercise ->
        exercise.workoutExerciseId in activeWorkoutExerciseIds
    }

    return WorkoutProgressUiState(
        isLoading = false,
        exercises = activeExercises,
        archivedExercises = archivedExercises,
    )
}

private fun List<WorkoutExerciseProgressPoint>.toWorkoutProgressExerciseUi(): WorkoutProgressExerciseUi? {
    val chartData = toProgressChartData() ?: return null

    return WorkoutProgressExerciseUi(
        workoutExerciseId = chartData.workoutExerciseId,
        exerciseName = chartData.exerciseName,
        latestResultLabel = chartData.latestResultLabel,
        series = chartData.series,
        points = chartData.points,
    )
}
