package ru.trainingapp.core.model

data class AppAppearance(
    val backgroundImagePath: String? = null,
    val backgroundDimAmount: Float = DEFAULT_BACKGROUND_DIM_AMOUNT,
    val backgroundCropZoom: Float = 1f,
    val backgroundCropOffsetX: Float = 0f,
    val backgroundCropOffsetY: Float = 0f,
) {
    companion object {
        const val DEFAULT_BACKGROUND_DIM_AMOUNT = 0.35f
        const val MAX_BACKGROUND_DIM_AMOUNT = 0.8f
    }
}
