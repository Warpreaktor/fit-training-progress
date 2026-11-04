package ru.trainingapp.feature.workout_editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import ru.trainingapp.core.model.WorkoutMuscleBalance

@Composable
fun WorkoutMuscleBalanceDialog(
    balance: WorkoutMuscleBalance,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Баланс нагрузки")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when {
                    balance.totalExercises == 0 -> {
                        Text("В тренировке пока нет упражнений.")
                    }

                    balance.items.isEmpty() -> {
                        Text(
                            text = "Для упражнений этой тренировки пока не указаны группы мышц.",
                        )
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Мышцы указаны для ${balance.exercisesWithMuscles} из ${balance.totalExercises} упражнений",
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            if (balance.exercisesWithoutMuscles > 0) {
                                Text(
                                    text = "Без указанных мышц: ${balance.exercisesWithoutMuscles}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        balance.items.forEach { item ->
                            val percentage = (item.share * 100f).roundToInt()

                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = item.muscleGroup.displayName,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                    )

                                    Text(
                                        text = "$percentage%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = item.share.coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                Text(
                                    text = "${item.exerciseCount} из ${balance.totalExercises} упражнений • Баллы: ${formatScore(item.score)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Text(
                            text = "Основная нагрузка даёт 1 балл, дополнительная — 0,5. Каждая мышечная группа учитывается отдельно, поэтому сумма процентов может быть больше 100%.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        },
    )
}

private fun formatScore(score: Float): String {
    val doubled = (score * 2f).roundToInt()
    return if (doubled % 2 == 0) {
        (doubled / 2).toString()
    } else {
        "${doubled / 2},5"
    }
}
