package ru.trainingapp.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import java.io.File
import java.util.Locale
import ru.trainingapp.core.model.AppAppearance
import ru.trainingapp.core.ui.component.ZoomCropContentScale

@Composable
fun AppBackgroundCropEditor(
    appearance: AppAppearance,
    onDismiss: () -> Unit,
    onSave: (
        zoom: Float,
        offsetX: Float,
        offsetY: Float,
    ) -> Unit,
) {
    val backgroundPath = appearance.backgroundImagePath ?: return
    val configuration = LocalConfiguration.current
    val previewAspectRatio = (
        configuration.screenWidthDp.toFloat() /
            configuration.screenHeightDp.toFloat().coerceAtLeast(1f)
        ).coerceIn(0.4f, 1.8f)

    var cropZoom by remember(backgroundPath) {
        mutableFloatStateOf(appearance.backgroundCropZoom.coerceIn(1f, 4f))
    }
    var cropOffsetX by remember(backgroundPath) {
        mutableFloatStateOf(appearance.backgroundCropOffsetX.coerceIn(-1f, 1f))
    }
    var cropOffsetY by remember(backgroundPath) {
        mutableFloatStateOf(appearance.backgroundCropOffsetY.coerceIn(-1f, 1f))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Настроить фон",
                    style = MaterialTheme.typography.titleLarge,
                )

                val frameShape = RoundedCornerShape(16.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.64f)
                        .aspectRatio(previewAspectRatio)
                        .align(Alignment.CenterHorizontally)
                        .clip(frameShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .pointerInput(backgroundPath) {
                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                val newZoom = (cropZoom * gestureZoom).coerceIn(1f, 4f)
                                cropZoom = newZoom

                                val width = size.width.toFloat().coerceAtLeast(1f)
                                val height = size.height.toFloat().coerceAtLeast(1f)

                                cropOffsetX = (
                                    cropOffsetX - (pan.x / width) * 2f / newZoom
                                    ).coerceIn(-1f, 1f)
                                cropOffsetY = (
                                    cropOffsetY - (pan.y / height) * 2f / newZoom
                                    ).coerceIn(-1f, 1f)
                            }
                        }
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = frameShape,
                        ),
                ) {
                    AsyncImage(
                        model = File(backgroundPath),
                        contentDescription = "Предпросмотр фона приложения",
                        modifier = Modifier.fillMaxSize(),
                        alignment = BiasAbsoluteAlignment(
                            horizontalBias = cropOffsetX,
                            verticalBias = cropOffsetY,
                        ),
                        contentScale = ZoomCropContentScale(cropZoom),
                    )
                }

                Text(
                    text = "Перемещай изображение одним пальцем. Масштаб меняется двумя пальцами или ползунком.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Масштаб ${formatZoom(cropZoom)}×",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Slider(
                        value = cropZoom,
                        onValueChange = { value -> cropZoom = value.coerceIn(1f, 4f) },
                        valueRange = 1f..4f,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            cropZoom = 1f
                            cropOffsetX = 0f
                            cropOffsetY = 0f
                        },
                    ) {
                        Text("Сбросить")
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Отмена")
                        }
                        TextButton(
                            onClick = {
                                onSave(cropZoom, cropOffsetX, cropOffsetY)
                            },
                        ) {
                            Text("Готово")
                        }
                    }
                }
            }
        }
    }
}

private fun formatZoom(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}
