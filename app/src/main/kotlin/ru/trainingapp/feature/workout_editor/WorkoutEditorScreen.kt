package ru.trainingapp.feature.workout_editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import ru.trainingapp.core.ui.component.EmptyState

@Composable
fun WorkoutEditorRoute(
    workoutId: String,
    onBack: () -> Unit,
    viewModel: WorkoutEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutEditorScreen(
        uiState = uiState.copy(workoutId = workoutId),
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditorScreen(
    uiState: WorkoutEditorUiState,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("WorkoutId: ${uiState.workoutId}")
            EmptyState(
                title = "Экран готов под редактор",
                message = "Здесь будут упражнения, подходы, inline-числа, галки и автосохранение. Пока это аккуратная пустота, то есть почти архитектура.",
            )
        }
    }
}
