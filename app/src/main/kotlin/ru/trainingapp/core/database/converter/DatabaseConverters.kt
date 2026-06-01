package ru.trainingapp.core.database.converter

import androidx.room.TypeConverter
import ru.trainingapp.core.model.WeightUnit

class DatabaseConverters {

    @TypeConverter
    fun weightUnitToString(value: WeightUnit?): String? {
        return value?.name
    }

    @TypeConverter
    fun stringToWeightUnit(value: String?): WeightUnit? {
        return value?.let(WeightUnit::valueOf)
    }
}