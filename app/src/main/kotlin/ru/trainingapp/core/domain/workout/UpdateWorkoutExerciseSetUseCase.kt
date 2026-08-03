package ru.trainingapp.core.domain.workout

import ru.trainingapp.core.domain.repository.WorkoutRepository
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExerciseSet
import ru.trainingapp.core.model.WorkoutExerciseSetLoad
import ru.trainingapp.core.model.WorkoutExerciseSetLoadType
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

            is Command.ChangeLoadType -> {
                currentSet.copy(
                    load = currentSet.load.changeType(command.loadType),
                )
            }

            is Command.UpdateWeight -> {
                if (command.value != null && command.value < MIN_WEIGHT_VALUE) return

                currentSet.copy(
                    load = when (val currentLoad = currentSet.load) {
                        is WorkoutExerciseSetLoad.Weight -> currentLoad.copy(
                            value = command.value,
                        )

                        is WorkoutExerciseSetLoad.Time -> WorkoutExerciseSetLoad.Weight(
                            value = command.value,
                            unit = DEFAULT_WEIGHT_UNIT,
                        )
                    },
                )
            }

            is Command.UpdateWeightUnit -> {
                currentSet.copy(
                    load = currentSet.load.changeUnit(command.unit),
                )
            }

            is Command.UpdateDurationSeconds -> {
                if (command.durationSeconds != null && command.durationSeconds < MIN_DURATION_SECONDS) {
                    return
                }

                val currentTimeUnit = (currentSet.load as? WorkoutExerciseSetLoad.Time)
                    ?.unit
                    ?.takeIf { unit -> unit.isTimeUnit }
                    ?: DEFAULT_TIME_UNIT

                currentSet.copy(
                    load = WorkoutExerciseSetLoad.Time(
                        durationSeconds = command.durationSeconds,
                        unit = currentTimeUnit,
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

        data class ChangeLoadType(
            override val setId: Long,
            val loadType: WorkoutExerciseSetLoadType,
        ) : Command

        data class UpdateWeight(
            override val setId: Long,
            val value: Double?,
        ) : Command

        data class UpdateWeightUnit(
            override val setId: Long,
            val unit: WeightUnit,
        ) : Command

        data class UpdateDurationSeconds(
            override val setId: Long,
            val durationSeconds: Int?,
        ) : Command
    }

    private fun WorkoutExerciseSetLoad.changeType(
        loadType: WorkoutExerciseSetLoadType,
    ): WorkoutExerciseSetLoad {
        return when (loadType) {
            WorkoutExerciseSetLoadType.WEIGHT -> when (this) {
                is WorkoutExerciseSetLoad.Weight -> this
                is WorkoutExerciseSetLoad.Time -> WorkoutExerciseSetLoad.Weight(
                    value = null,
                    unit = DEFAULT_WEIGHT_UNIT,
                )
            }

            WorkoutExerciseSetLoadType.TIME -> when (this) {
                is WorkoutExerciseSetLoad.Weight -> WorkoutExerciseSetLoad.Time(
                    durationSeconds = null,
                    unit = DEFAULT_TIME_UNIT,
                )

                is WorkoutExerciseSetLoad.Time -> this
            }
        }
    }

    private fun WorkoutExerciseSetLoad.changeUnit(
        unit: WeightUnit,
    ): WorkoutExerciseSetLoad {
        return if (unit.isTimeUnit) {
            when (this) {
                is WorkoutExerciseSetLoad.Weight -> WorkoutExerciseSetLoad.Time(
                    durationSeconds = null,
                    unit = unit,
                )

                is WorkoutExerciseSetLoad.Time -> copy(
                    unit = unit,
                )
            }
        } else {
            when (this) {
                is WorkoutExerciseSetLoad.Weight -> copy(
                    unit = unit,
                )

                is WorkoutExerciseSetLoad.Time -> WorkoutExerciseSetLoad.Weight(
                    value = null,
                    unit = unit,
                )
            }
        }
    }

    private companion object {
        const val MIN_REPS = 1
        const val MIN_WEIGHT_VALUE = 0.0
        const val MIN_DURATION_SECONDS = 1
        val DEFAULT_WEIGHT_UNIT = WeightUnit.KG
        val DEFAULT_TIME_UNIT = WeightUnit.SEC
    }
}
