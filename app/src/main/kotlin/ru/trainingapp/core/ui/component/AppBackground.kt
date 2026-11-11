package ru.trainingapp.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import java.io.File
import ru.trainingapp.core.model.AppAppearance

@Composable
fun AppBackground(
    appearance: AppAppearance,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val backgroundFile = appearance.backgroundImagePath
            ?.let(::File)
            ?.takeIf(File::exists)

        if (backgroundFile != null) {
            AsyncImage(
                model = backgroundFile,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = BiasAbsoluteAlignment(
                    horizontalBias = appearance.backgroundCropOffsetX.coerceIn(-1f, 1f),
                    verticalBias = appearance.backgroundCropOffsetY.coerceIn(-1f, 1f),
                ),
                contentScale = ZoomCropContentScale(
                    zoom = appearance.backgroundCropZoom.coerceIn(1f, 4f),
                ),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha = appearance.backgroundDimAmount.coerceIn(
                                0f,
                                AppAppearance.MAX_BACKGROUND_DIM_AMOUNT,
                            ),
                        )
                    ),
            )
        }

        content()
    }
}
