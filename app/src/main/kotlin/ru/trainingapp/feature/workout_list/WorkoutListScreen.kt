package ru.trainingapp.feature.workout_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.trainingapp.core.model.Workout
import ru.trainingapp.core.ui.component.EmptyState

@Composable
fun WorkoutListRoute(
    onOpenWorkout: (Long) -> Unit,
    onOpenExerciseCatalog: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: WorkoutListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WorkoutListScreen(
        uiState = uiState,
        onOpenWorkout = onOpenWorkout,
        onOpenExerciseCatalog = onOpenExerciseCatalog,
        onOpenProgress = onOpenProgress,
        onOpenSettings = onOpenSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    uiState: WorkoutListUiState,
    onOpenWorkout: (Long) -> Unit,
    onOpenExerciseCatalog: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Тренировки") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenExerciseCatalog) { Text("Упражнения") }
                Button(onClick = onOpenProgress) { Text("Прогресс") }
                Button(onClick = onOpenSettings) { Text("Настройки") }
            }

            if (uiState.workouts.isEmpty()) {
                EmptyState(
                    title = "Пока пусто",
                    message = "На следующем этапе тут появится создание тренировки. Да, человечество выжило и без этой кнопки, но ненадолго.",
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.workouts, key = { it.id }) { workout ->
                        WorkoutCard(
                            workout = workout,
                            onClick = { onOpenWorkout(workout.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = workout.name, style = MaterialTheme.typography.titleMedium)
            workout.description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("${workout.exercisesCount} упражнений") })
                AssistChip(onClick = {}, label = { Text("${workout.checkedExercisesCount} отмечено") })
            }
        }
    }
}
