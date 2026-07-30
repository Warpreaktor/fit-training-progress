package ru.trainingapp.core.data.exportimport

import org.json.JSONArray
import org.json.JSONObject
import ru.trainingapp.core.data.exportimport.dto.ExportExerciseDto
import ru.trainingapp.core.data.exportimport.dto.ExportExerciseImageDto
import ru.trainingapp.core.data.exportimport.dto.ExportTagDto
import ru.trainingapp.core.data.exportimport.dto.ExportWorkoutDto
import ru.trainingapp.core.data.exportimport.dto.ExportWorkoutExerciseDto
import ru.trainingapp.core.data.exportimport.dto.ExportWorkoutExerciseSetDto
import ru.trainingapp.core.data.exportimport.dto.TrainingPackDto
import javax.inject.Inject

class TrainingPackJsonCodec @Inject constructor() {

    fun encode(trainingPack: TrainingPackDto): String {
        val root = JSONObject()
            .put("schemaVersion", trainingPack.schemaVersion)
            .put("exportType", trainingPack.exportType)
            .putNullable("createdAt", trainingPack.createdAt)
            .putNullable("sourceApp", trainingPack.sourceApp)
            .put(
                "exercises",
                JSONArray().apply {
                    trainingPack.exercises.forEach { exercise ->
                        put(exercise.toJson())
                    }
                },
            )

        if (trainingPack.tags.isNotEmpty()) {
            root.put(
                "tags",
                JSONArray().apply {
                    trainingPack.tags.forEach { tag ->
                        put(tag.toJson())
                    }
                },
            )
        }

        if (trainingPack.workouts.isNotEmpty()) {
            root.put(
                "workouts",
                JSONArray().apply {
                    trainingPack.workouts.forEach { workout ->
                        put(workout.toJson())
                    }
                },
            )
        }

        return root.toString(2)
    }

    fun decode(json: String): TrainingPackDto {
        val root = JSONObject(json)

        return TrainingPackDto(
            schemaVersion = root.optInt("schemaVersion", 1),
            exportType = root.optNullableString("exportType").orEmpty(),
            createdAt = root.optNullableLong("createdAt"),
            sourceApp = root.optNullableString("sourceApp"),
            tags = root.optArray("tags").mapObjects { item, _ ->
                ExportTagDto(
                    ref = item.optNullableString("ref"),
                    name = item.optNullableString("name"),
                    color = item.optNullableString("color"),
                )
            },
            exercises = root.optArray("exercises").mapObjects { item, _ ->
                ExportExerciseDto(
                    ref = item.optNullableString("ref"),
                    name = item.optNullableString("name"),
                    description = item.optNullableString("description"),
                    tagRefs = item.optArray("tagRefs").mapStrings(),
                    images = item.optArray("images").mapObjects { image, imageIndex ->
                        ExportExerciseImageDto(
                            ref = image.optNullableString("ref"),
                            path = image.optNullableString("path"),
                            sortOrder = image.optNullableInt("sortOrder") ?: imageIndex,
                            isCover = image.optNullableBoolean("isCover"),
                        )
                    },
                )
            },
            workouts = root.optArray("workouts").mapObjects { item, _ ->
                ExportWorkoutDto(
                    ref = item.optNullableString("ref"),
                    name = item.optNullableString("name"),
                    description = item.optNullableString("description"),
                    tagRefs = item.optArray("tagRefs").mapStrings(),
                    exercises = item.optArray("exercises").mapObjects { exercise, exerciseIndex ->
                        ExportWorkoutExerciseDto(
                            ref = exercise.optNullableString("ref"),
                            exerciseRef = exercise.optNullableString("exerciseRef"),
                            sortOrder = exercise.optNullableInt("sortOrder") ?: exerciseIndex,
                            comment = exercise.optNullableString("comment"),
                            sets = exercise.optArray("sets").mapObjects { set, setIndex ->
                                ExportWorkoutExerciseSetDto(
                                    setNumber = set.optNullableInt("setNumber") ?: setIndex + 1,
                                    reps = set.optNullableInt("reps"),
                                    weightValue = set.optNullableDouble("weightValue"),
                                    weightUnit = set.optNullableString("weightUnit"),
                                    durationSeconds = set.optNullableInt("durationSeconds"),
                                )
                            },
                        )
                    },
                )
            },
        )
    }

    private fun ExportTagDto.toJson(): JSONObject {
        return JSONObject()
            .putNullable("ref", ref)
            .putNullable("name", name)
            .putNullable("color", color)
    }

    private fun ExportExerciseDto.toJson(): JSONObject {
        return JSONObject()
            .putNullable("ref", ref)
            .putNullable("name", name)
            .putNullable("description", description)
            .put("tagRefs", tagRefs.toJsonArray())
            .put(
                "images",
                JSONArray().apply {
                    images.forEach { image ->
                        put(image.toJson())
                    }
                },
            )
    }

    private fun ExportExerciseImageDto.toJson(): JSONObject {
        return JSONObject()
            .putNullable("ref", ref)
            .putNullable("path", path)
            .putNullable("sortOrder", sortOrder)
            .putNullable("isCover", isCover)
    }

    private fun ExportWorkoutDto.toJson(): JSONObject {
        return JSONObject()
            .putNullable("ref", ref)
            .putNullable("name", name)
            .putNullable("description", description)
            .put("tagRefs", tagRefs.toJsonArray())
            .put(
                "exercises",
                JSONArray().apply {
                    exercises.forEach { exercise ->
                        put(exercise.toJson())
                    }
                },
            )
    }

    private fun ExportWorkoutExerciseDto.toJson(): JSONObject {
        return JSONObject()
            .putNullable("ref", ref)
            .putNullable("exerciseRef", exerciseRef)
            .putNullable("sortOrder", sortOrder)
            .putNullable("comment", comment)
            .put(
                "sets",
                JSONArray().apply {
                    sets.forEach { set ->
                        put(set.toJson())
                    }
                },
            )
    }

    private fun ExportWorkoutExerciseSetDto.toJson(): JSONObject {
        return JSONObject()
            .putNullable("setNumber", setNumber)
            .putNullable("reps", reps)
            .putNullable("weightValue", weightValue)
            .putNullable("weightUnit", weightUnit)
            .putNullable("durationSeconds", durationSeconds)
    }

    private fun JSONObject.putNullable(
        name: String,
        value: Any?,
    ): JSONObject {
        if (value != null) {
            put(name, value)
        }

        return this
    }

    private fun JSONObject.optArray(name: String): JSONArray {
        return optJSONArray(name) ?: JSONArray()
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (!has(name) || isNull(name)) {
            return null
        }

        return optString(name).takeIf { value -> value.isNotBlank() }
    }

    private fun JSONObject.optNullableInt(name: String): Int? {
        if (!has(name) || isNull(name)) {
            return null
        }

        return runCatching { getInt(name) }.getOrNull()
    }

    private fun JSONObject.optNullableLong(name: String): Long? {
        if (!has(name) || isNull(name)) {
            return null
        }

        return runCatching { getLong(name) }.getOrNull()
    }

    private fun JSONObject.optNullableDouble(name: String): Double? {
        if (!has(name) || isNull(name)) {
            return null
        }

        return runCatching { getDouble(name) }.getOrNull()
    }

    private fun JSONObject.optNullableBoolean(name: String): Boolean? {
        if (!has(name) || isNull(name)) {
            return null
        }

        return runCatching { getBoolean(name) }.getOrNull()
    }

    private fun JSONArray.mapStrings(): List<String> {
        return buildList {
            for (index in 0 until length()) {
                if (!isNull(index)) {
                    optString(index)
                        .takeIf { value -> value.isNotBlank() }
                        ?.let(::add)
                }
            }
        }
    }

    private inline fun <T> JSONArray.mapObjects(
        transform: (JSONObject, Int) -> T,
    ): List<T> {
        return buildList {
            for (index in 0 until length()) {
                optJSONObject(index)?.let { item ->
                    add(transform(item, index))
                }
            }
        }
    }

    private fun List<String>.toJsonArray(): JSONArray {
        return JSONArray().apply {
            this@toJsonArray.forEach { value ->
                put(value)
            }
        }
    }
}
