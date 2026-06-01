package ru.trainingapp.feature.exercise_catalog

import androidx.lifecycle.ViewModel
import ru.trainingapp.core.model.ExerciseDefinition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ExerciseCatalogViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(
        ExerciseCatalogUiState(
            exercises = listOf(
                ExerciseDefinition(1L, "Отжимания", "Заглушка справочника"),
                ExerciseDefinition(2L, "Планка", "Заглушка справочника"),
            ),
        ),
    )
    val uiState: StateFlow<ExerciseCatalogUiState> = _uiState
}

data class ExerciseCatalogUiState(
    val exercises: List<ExerciseDefinition> = emptyList(),
)
