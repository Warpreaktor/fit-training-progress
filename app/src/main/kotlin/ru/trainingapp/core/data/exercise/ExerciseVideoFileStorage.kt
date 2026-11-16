package ru.trainingapp.core.data.exercise

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseVideoFileStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun copyFileToPrivateStorage(
        exerciseDefinitionId: Long,
        sourceFile: File,
        extension: String,
    ): String = withContext(Dispatchers.IO) {
        require(sourceFile.isFile) {
            "Файл видео не найден"
        }

        val safeExtension = extension
            .lowercase()
            .takeIf { value -> value.matches(Regex("[a-z0-9]{1,8}")) }
            ?: "mp4"

        val directory = File(
            context.filesDir,
            "exercise_videos/$exerciseDefinitionId",
        )

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val targetFile = File(
            directory,
            "${System.currentTimeMillis()}_${UUID.randomUUID()}.$safeExtension",
        )

        sourceFile.inputStream().use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Uri.fromFile(targetFile).toString()
    }

    suspend fun copyStoredVideoToExercise(
        exerciseDefinitionId: Long,
        sourceUriString: String,
    ): String? = withContext(Dispatchers.IO) {
        val sourceUri = Uri.parse(sourceUriString)

        if (sourceUri.scheme != "file") {
            return@withContext null
        }

        val sourcePath = sourceUri.path ?: return@withContext null
        val sourceFile = File(sourcePath)

        if (!sourceFile.isFile) {
            return@withContext null
        }

        copyFileToPrivateStorage(
            exerciseDefinitionId = exerciseDefinitionId,
            sourceFile = sourceFile,
            extension = sourceFile.extension,
        )
    }
}
