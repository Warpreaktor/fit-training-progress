package ru.trainingapp.feature.workout_editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
internal fun rememberWorkoutElapsedMillis(
    timerStartedAt: Long?,
    timerElapsedMillis: Long,
): Long {
    var now by remember(timerStartedAt) {
        mutableStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(timerStartedAt) {
        while (timerStartedAt != null) {
            now = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    val runningSegmentMillis = timerStartedAt?.let { startedAt ->
        (now - startedAt).coerceAtLeast(0L)
    } ?: 0L

    return timerElapsedMillis + runningSegmentMillis
}

@Composable
internal fun WorkoutProgressHeader(
    completedExercises: Int,
    totalExercises: Int,
    targetDurationMinutes: Int?,
    elapsedMillis: Long,
    isTimerRunning: Boolean,
    isTimerFinished: Boolean,
    onTargetDurationClick: () -> Unit,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onFinishClick: () -> Unit,
) {
    val remainingExercises = (totalExercises - completedExercises).coerceAtLeast(0)
    val progress = if (totalExercises == 0) {
        0f
    } else {
        completedExercises.toFloat() / totalExercises.toFloat()
    }
    val hasTimerProgress = elapsedMillis > 0L
    val isPaused = !isTimerRunning && !isTimerFinished && hasTimerProgress
    val targetMillis = targetDurationMinutes?.toLong()?.times(60_000L)
    val isOverTarget = targetMillis != null && elapsedMillis > targetMillis

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$completedExercises / $totalExercises выполнено",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    text = "Осталось $remainingExercises",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "${(progress * 100f).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (isTimerRunning || isPaused || isTimerFinished) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "⏱ ${formatWorkoutDuration(elapsedMillis)}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOverTarget) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )

                    TextButton(onClick = onTargetDurationClick) {
                        Text(
                            text = targetDurationMinutes?.let { minutes ->
                                "/ $minutes min"
                            } ?: "Задать цель",
                        )
                    }
                }

                if (isTimerFinished) {
                    Text(
                        text = "Тренировка завершена",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onStartClick,
                    ) {
                        Text("▶ Начать заново")
                    }
                } else {
                    if (isPaused) {
                        Text(
                            text = "На паузе",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = if (isTimerRunning) onPauseClick else onStartClick,
                        ) {
                            Text(
                                if (isTimerRunning) {
                                    "Ⅱ Пауза"
                                } else {
                                    "▶ Продолжить"
                                }
                            )
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onFinishClick,
                        ) {
                            Text("■ Завершить")
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = targetDurationMinutes?.let { minutes ->
                            "⏱ Цель: $minutes min"
                        } ?: "⏱ Целевое время не задано",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    TextButton(onClick = onTargetDurationClick) {
                        Text(
                            if (targetDurationMinutes == null) {
                                "+ Задать"
                            } else {
                                "Изменить"
                            }
                        )
                    }
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onStartClick,
                ) {
                    Text("▶ Начать тренировку")
                }
            }
        }
    }
}

@Composable
internal fun CompactWorkoutProgressHeader(
    completedExercises: Int,
    totalExercises: Int,
    elapsedMillis: Long,
    targetDurationMinutes: Int?,
    isTimerRunning: Boolean,
    isTimerFinished: Boolean,
    onStartClick: () -> Unit,
) {
    val progress = if (totalExercises == 0) {
        0f
    } else {
        completedExercises.toFloat() / totalExercises.toFloat()
    }
    val hasTimerProgress = elapsedMillis > 0L
    val targetMillis = targetDurationMinutes?.toLong()?.times(60_000L)
    val isOverTarget = targetMillis != null && elapsedMillis > targetMillis

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "$completedExercises / $totalExercises",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.weight(1f),
            )

            when {
                !isTimerRunning && !isTimerFinished && hasTimerProgress -> {
                    TextButton(onClick = onStartClick) {
                        Text(
                            text = "▶ ${formatWorkoutDuration(elapsedMillis)}",
                            color = if (isOverTarget) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                    }
                }

                isTimerRunning || isTimerFinished -> {
                    Text(
                        text = "⏱ ${formatWorkoutDuration(elapsedMillis)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isOverTarget) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }

                else -> {
                    TextButton(onClick = onStartClick) {
                        Text("▶ Начать")
                    }
                }
            }
        }
    }
}

@Composable
internal fun TargetDurationDialog(
    currentMinutes: Int?,
    onDismiss: () -> Unit,
    onSave: (Int?) -> Unit,
) {
    var value by rememberSaveable(currentMinutes) {
        mutableStateOf(currentMinutes?.toString().orEmpty())
    }
    val parsedMinutes = value.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Целевое время")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { newValue ->
                        if (newValue.all { character -> character.isDigit() }) {
                            value = newValue
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Минуты, min") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    listOf(30, 45, 60, 90).forEach { minutes ->
                        TextButton(
                            onClick = {
                                value = minutes.toString()
                            },
                        ) {
                            Text("$minutes")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(parsedMinutes?.takeIf { minutes -> minutes > 0 })
                },
                enabled = value.isBlank() || (parsedMinutes != null && parsedMinutes > 0),
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        onSave(null)
                    },
                ) {
                    Text("Без цели")
                }

                Spacer(modifier = Modifier.width(4.dp))

                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        },
    )
}

private fun formatWorkoutDuration(elapsedMillis: Long): String {
    val totalSeconds = (elapsedMillis.coerceAtLeast(0L) / 1_000L)
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L

    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
