package ru.trainingapp.core.data.exercise

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import org.json.JSONArray
import ru.trainingapp.core.model.ExerciseDefinition
import ru.trainingapp.core.model.ExerciseImage
import ru.trainingapp.core.model.MuscleGroup

/** Read-only catalog bundled with the APK. JSON array order is preserved. */
class OfficialExerciseCatalog @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val records: List<OfficialExercise> by lazy {
        val json = context.assets.open("official_catalog/exercises.json")
            .bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val sourceId = item.getString("id")
            require(sourceId.isNotBlank()) { "Blank official exercise id at $index" }
            val imagePaths = item.optJSONArray("images").strings()
            OfficialExercise(
                sourceId = sourceId,
                model = ExerciseDefinition(
                    id = -(index + 1L), // UI-only ID; never inserted into Room.
                    name = item.getString("name"),
                    description = item.optString("description"),
                    muscles = item.optJSONArray("muscles").strings()
                        .mapNotNull(MuscleGroup::fromCode),
                    secondaryMuscles = item.optJSONArray("secondaryMuscles").strings()
                        .mapNotNull(MuscleGroup::fromCode),
                    images = imagePaths.mapIndexed { imageIndex, path ->
                        ExerciseImage(
                            id = -(index * 1000L + imageIndex + 1L),
                            exerciseDefinitionId = -(index + 1L),
                            uri = "file:///android_asset/official_catalog/$path",
                            sortOrder = imageIndex,
                            isCover = imageIndex == 0,
                            createdAt = 0L,
                            updatedAt = 0L,
                        )
                    },
                ),
                images = imagePaths,
            )
        }.also { exercises ->
            require(exercises.map { it.sourceId }.distinct().size == exercises.size) {
                "Duplicate IDs in official catalog"
            }
        }
    }

    val exercises: List<ExerciseDefinition> get() = records.map { it.model }

    fun get(id: Long): OfficialExercise? = records.firstOrNull { it.model.id == id }

    /** Asset files are copied into cache solely to support the existing image UI. */
    private fun assetFile(path: String): File {
        require(path.startsWith("images/") && !path.contains("..") && '\\' !in path) {
            "Invalid official image path: $path"
        }
        val target = File(context.cacheDir, "official_catalog/$path")
        if (!target.isFile) {
            target.parentFile?.mkdirs()
            context.assets.open("official_catalog/$path").use { input ->
                target.outputStream().use(input::copyTo)
            }
        }
        return target
    }

    fun imageUris(exercise: OfficialExercise): List<String> = exercise.images.map { path ->
        Uri.fromFile(assetFile(path)).toString()
    }
}

data class OfficialExercise(
    val sourceId: String,
    val model: ExerciseDefinition,
    val images: List<String>,
)

private fun JSONArray?.strings(): List<String> = if (this == null) emptyList()
else (0 until length()).map { getString(it) }
