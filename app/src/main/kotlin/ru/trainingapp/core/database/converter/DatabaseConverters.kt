package ru.trainingapp.core.database.converter

import androidx.room.TypeConverter
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExerciseSetLoadType

class DatabaseConverters {

    @TypeConverter
    fun weightUnitToString(value: WeightUnit?): String? {
        return value?.name
    }

    @TypeConverter
    fun stringToWeightUnit(value: String?): WeightUnit? {
        return value?.let(WeightUnit::valueOf)
    }

    @TypeConverter
    fun workoutExerciseSetLoadTypeToString(value: WorkoutExerciseSetLoadType): String {
        return value.name
    }

    @TypeConverter
    fun stringToWorkoutExerciseSetLoadType(value: String): WorkoutExerciseSetLoadType {
        return WorkoutExerciseSetLoadType.valueOf(value)
    }
}