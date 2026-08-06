package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExerciseSetLoad
import javax.inject.Inject

class UpdateWorkoutExerciseSetUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {

    suspend operator fun invoke(
        command: Command,
    ) {
        val currentSet = repository.getWorkoutExerciseSet(command.setId)
            ?: return

        val updatedSet = when (command) {
            is Command.UpdateReps -> {
                if (command.reps < MIN_REPS) return

                currentSet.copy(
                    reps = command.reps,
                )
            }

            is Command.UpdateQuantity -> {
                if (command.value != null && command.value < MIN_QUANTITY_VALUE) return

                currentSet.copy(
                    load = WorkoutExerciseSetLoad.Weight(
                        value = command.value,
                        unit = currentSet.load.unit(),
                    ),
                )
            }

            is Command.UpdateMeasurementUnit -> {
                currentSet.copy(
                    load = WorkoutExerciseSetLoad.Weight(
                        value = currentSet.load.quantityValue(),
                        unit = command.unit,
                    ),
                )
            }
        }

        repository.updateWorkoutExerciseSet(updatedSet)
    }

    sealed interface Command {

        val setId: Long

        data class UpdateReps(
            override val setId: Long,
            val reps: Int,
        ) : Command

        data class UpdateQuantity(
            override val setId: Long,
            val value: Double?,
        ) : Command

        data class UpdateMeasurementUnit(
            override val setId: Long,
            val unit: WeightUnit,
        ) : Command
    }

    private fun WorkoutExerciseSetLoad.quantityValue(): Double? {
        return when (this) {
            is WorkoutExerciseSetLoad.Weight -> value
            is WorkoutExerciseSetLoad.Time -> durationSeconds?.let { seconds ->
                if (unit == WeightUnit.MIN) seconds / SECONDS_IN_MINUTE else seconds.toDouble()
            }
        }
    }

    private fun WorkoutExerciseSetLoad.unit(): WeightUnit {
        return when (this) {
            is WorkoutExerciseSetLoad.Weight -> unit
            is WorkoutExerciseSetLoad.Time -> unit
        }
    }

    private companion object {
        const val MIN_REPS = 1
        const val MIN_QUANTITY_VALUE = 0.0
        const val SECONDS_IN_MINUTE = 60.0
    }
}
