package ru.trainingapp.feature.exercise_catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.trainingapp.core.domain.exercise.ArchiveExerciseDefinitionUseCase
import ru.trainingapp.core.domain.exercise.CreateExerciseDefinitionUseCase
import ru.trainingapp.core.domain.exercise.ObserveExerciseDefinitionsUseCase
import ru.trainingapp.core.domain.exercise.UpdateExerciseDefinitionUseCase
import ru.trainingapp.core.model.ExerciseDefinition
import javax.inject.Inject

@HiltViewModel
class ExerciseCatalogViewModel @Inject constructor(
    observeExerciseDefinitionsUseCase: ObserveExerciseDefinitionsUseCase,
    private val createExerciseDefinitionUseCase: CreateExerciseDefinitionUseCase,
    private val updateExerciseDefinitionUseCase: UpdateExerciseDefinitionUseCase,
    private val archiveExerciseDefinitionUseCase: ArchiveExerciseDefinitionUseCase,
) : ViewModel() {

    private val editorState = MutableStateFlow(ExerciseEditorState())

    val uiState: StateFlow<ExerciseCatalogUiState> =
        combine(
            observeExerciseDefinitionsUseCase(),
            editorState,
        ) { exercises, editor ->
            ExerciseCatalogUiState(
                exercises = exercises,
                editor = editor,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExerciseCatalogUiState(),
        )

    fun onAddExerciseClick() {
        editorState.value = ExerciseEditorState(
            isVisible = true,
            exerciseId = null,
            name = "",
            description = "",
        )
    }

    fun onEditExerciseClick(exercise: ExerciseDefinition) {
        editorState.value = ExerciseEditorState(
            isVisible = true,
            exerciseId = exercise.id,
            name = exercise.name,
            description = exercise.description,
        )
    }

    fun onEditorNameChange(value: String) {
        editorState.value = editorState.value.copy(name = value)
    }

    fun onEditorDescriptionChange(value: String) {
        editorState.value = editorState.value.copy(description = value)
    }

    fun onDismissEditor() {
        editorState.value = ExerciseEditorState()
    }

    fun onSaveExerciseClick() {
        val editor = editorState.value
        val name = editor.name.trim()

        if (name.isBlank()) {
            editorState.value = editor.copy(nameError = "Название обязательно")
            return
        }

        viewModelScope.launch {
            val exerciseId = editor.exerciseId

            if (exerciseId == null) {
                createExerciseDefinitionUseCase(
                    name = editor.name,
                    description = editor.description,
                )
            } else {
                updateExerciseDefinitionUseCase(
                    id = exerciseId,
                    name = editor.name,
                    description = editor.description,
                )
            }

            editorState.value = ExerciseEditorState()
        }
    }

    fun onArchiveExerciseClick(exerciseId: Long) {
        viewModelScope.launch {
            archiveExerciseDefinitionUseCase(exerciseId)
        }
    }
}

data class ExerciseCatalogUiState(
    val exercises: List<ExerciseDefinition> = emptyList(),
    val editor: ExerciseEditorState = ExerciseEditorState(),
)

data class ExerciseEditorState(

    val isVisible: Boolean = false,
    val exerciseId: Long? = null,
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
) {
    val isEditing: Boolean
        get() = exerciseId != null
}