package ru.trainingapp.feature.exercise_catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.trainingapp.core.model.ExerciseImage
import ru.trainingapp.core.ui.component.CroppedExerciseImage
import ru.trainingapp.core.ui.component.EXERCISE_IMAGE_ASPECT_RATIO

@Composable
fun ExerciseImageCropEditor(
    image: ExerciseImage,
    exerciseName: String,
    onDismiss: () -> Unit,
    onSave: (
        cropZoom: Float,
        cropOffsetX: Float,
        cropOffsetY: Float,
    ) -> Unit,
) {
    var cropZoom by remember(image.id) {
        mutableFloatStateOf(image.cropZoom.coerceIn(1f, 4f))
    }
    var cropOffsetX by remember(image.id) {
        mutableFloatStateOf(image.cropOffsetX.coerceIn(-1f, 1f))
    }
    var cropOffsetY by remember(image.id) {
        mutableFloatStateOf(image.cropOffsetY.coerceIn(-1f, 1f))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Настроить изображение",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = exerciseName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    val frameShape = RoundedCornerShape(16.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(EXERCISE_IMAGE_ASPECT_RATIO)
                            .clip(frameShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .pointerInput(image.id) {
                                detectTransformGestures { _, pan, gestureZoom, _ ->
                                    val newZoom = (cropZoom * gestureZoom)
                                        .coerceIn(1f, 4f)

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
                        CroppedExerciseImage(
                            uri = image.uri,
                            cropZoom = cropZoom,
                            cropOffsetX = cropOffsetX,
                            cropOffsetY = cropOffsetY,
                            contentDescription = "Предпросмотр изображения упражнения $exerciseName",
                            modifier = Modifier.fillMaxSize(),
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
                            text = "Масштаб ${formatCropZoom(cropZoom)}×",
                            style = MaterialTheme.typography.labelLarge,
                        )

                        Slider(
                            value = cropZoom,
                            onValueChange = { value ->
                                cropZoom = value.coerceIn(1f, 4f)
                            },
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
                                    onSave(
                                        cropZoom,
                                        cropOffsetX,
                                        cropOffsetY,
                                    )
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
}

private fun formatCropZoom(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", value)
    }
}
