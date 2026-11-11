package ru.trainingapp.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage

const val EXERCISE_IMAGE_ASPECT_RATIO = 1f

@Composable
fun CroppedExerciseImage(
    uri: String,
    cropZoom: Float,
    cropOffsetX: Float,
    cropOffsetY: Float,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val safeZoom = cropZoom.coerceIn(1f, 4f)

    AsyncImage(
        model = uri,
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = BiasAbsoluteAlignment(
            horizontalBias = cropOffsetX.coerceIn(-1f, 1f),
            verticalBias = cropOffsetY.coerceIn(-1f, 1f),
        ),
        contentScale = ZoomCropContentScale(safeZoom),
    )
}

