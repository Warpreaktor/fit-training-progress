package ru.trainingapp.core.data.exercise

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseImageFileStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun copyToPrivateStorage(
        exerciseDefinitionId: Long,
        sourceUriString: String,
    ): String = withContext(Dispatchers.IO) {
        val sourceUri = Uri.parse(sourceUriString)
        val extension = resolveExtension(sourceUri)
        val directory = File(
            context.filesDir,
            "exercise_images/$exerciseDefinitionId",
        )

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val targetFile = File(
            directory,
            "${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension",
        )

        val inputStream = context.contentResolver.openInputStream(sourceUri)
            ?: error("Не удалось открыть изображение")

        inputStream.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Uri.fromFile(targetFile).toString()
    }

    suspend fun deleteByUri(
        uriString: String,
    ) = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)

        if (uri.scheme != "file") {
            return@withContext
        }

        val file = File(requireNotNull(uri.path))

        if (file.exists()) {
            file.delete()
        }
    }

    private fun resolveExtension(
        uri: Uri,
    ): String {
        val mimeType = context.contentResolver.getType(uri)

        return mimeType
            ?.let { type ->
                MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
            }
            ?: "jpg"
    }
}