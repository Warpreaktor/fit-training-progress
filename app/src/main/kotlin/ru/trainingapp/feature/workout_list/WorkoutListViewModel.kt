package ru.trainingapp.feature.workout_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.trainingapp.core.domain.workout.ArchiveWorkoutUseCase
import ru.trainingapp.core.domain.workout.CreateWorkoutUseCase
import ru.trainingapp.core.domain.workout.ObserveWorkoutsUseCase
import ru.trainingapp.core.model.Workout
import javax.inject.Inject

@HiltViewModel
class WorkoutListViewModel @Inject constructor(
    observeWorkoutsUseCase: ObserveWorkoutsUseCase,
    private val createWorkoutUseCase: CreateWorkoutUseCase,
    private val archiveWorkoutUseCase: ArchiveWorkoutUseCase,
) : ViewModel() {

    private val editorState = MutableStateFlow(WorkoutEditorState())

    val uiState: StateFlow<WorkoutListUiState> =
        combine(
            observeWorkoutsUseCase(),
            editorState,
        ) { workouts, editor ->
            WorkoutListUiState(
                workouts = workouts,
                editor = editor,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WorkoutListUiState(),
        )

    fun onCreateWorkoutClick() {
        editorState.value = WorkoutEditorState(
            isVisible = true,
            name = "",
            description = "",
            nameError = null,
        )
    }

    fun onWorkoutNameChange(value: String) {
        editorState.value = editorState.value.copy(
            name = value,
            nameError = null,
        )
    }

    fun onWorkoutDescriptionChange(value: String) {
        editorState.value = editorState.value.copy(description = value)
    }

    fun onDismissEditor() {
        editorState.value = WorkoutEditorState()
    }

    fun onSaveWorkoutClick() {
        val editor = editorState.value

        if (editor.name.isBlank()) {
            editorState.value = editor.copy(nameError = "Название обязательно")
            return
        }

        viewModelScope.launch {
            createWorkoutUseCase(
                name = editor.name,
                description = editor.description,
            )

            editorState.value = WorkoutEditorState()
        }
    }

    fun onArchiveWorkoutClick(id: Long) {
        viewModelScope.launch {
            archiveWorkoutUseCase(id)
        }
    }
}

data class WorkoutListUiState(
    val workouts: List<Workout> = emptyList(),
    val editor: WorkoutEditorState = WorkoutEditorState(),
)

data class WorkoutEditorState(
    val isVisible: Boolean = false,
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
)