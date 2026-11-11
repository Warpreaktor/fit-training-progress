package ru.trainingapp.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.trainingapp.core.data.settings.AppAppearanceStore
import ru.trainingapp.core.model.AppAppearance

@HiltViewModel
class AppAppearanceViewModel @Inject constructor(
    private val appearanceStore: AppAppearanceStore,
) : ViewModel() {
    val appearance: StateFlow<AppAppearance> = appearanceStore.appearance
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppAppearance(),
        )

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun importBackground(uriString: String) {
        viewModelScope.launch {
            val imported = appearanceStore.importBackground(uriString)
            if (!imported) {
                _message.value = "Не удалось загрузить фон. GIF для фона не поддерживается."
            }
        }
    }

    fun updateCrop(
        zoom: Float,
        offsetX: Float,
        offsetY: Float,
    ) {
        viewModelScope.launch {
            appearanceStore.updateBackgroundCrop(
                zoom = zoom,
                offsetX = offsetX,
                offsetY = offsetY,
            )
        }
    }

    fun updateDimAmount(value: Float) {
        viewModelScope.launch {
            appearanceStore.updateBackgroundDimAmount(value)
        }
    }

    fun removeBackground() {
        viewModelScope.launch {
            appearanceStore.removeBackground()
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
