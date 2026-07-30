package ru.trainingapp.core.data.exportimport

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TrainingPackZipWriter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageExporter: TrainingPackImageExporter,
) {

    suspend fun write(
        destinationUri: String,
        manifestJson: String,
        imageSources: List<TrainingPackImageSource>,
    ) = withContext(Dispatchers.IO) {
        val outputStream = context.contentResolver.openOutputStream(
            Uri.parse(destinationUri),
            "w",
        ) ?: error("Не удалось открыть файл для экспорта")

        ZipOutputStream(BufferedOutputStream(outputStream)).use { zip ->
            zip.putNextEntry(ZipEntry(MANIFEST_FILE_NAME))
            zip.write(manifestJson.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            imageSources.forEach { imageSource ->
                require(isSafeArchivePath(imageSource.path)) {
                    "Некорректный путь картинки в архиве: ${imageSource.path}"
                }

                zip.putNextEntry(ZipEntry(imageSource.path))

                imageExporter.openInputStream(imageSource.sourceUri).use { input ->
                    input.copyTo(zip)
                }

                zip.closeEntry()
            }
        }
    }

    private fun isSafeArchivePath(path: String): Boolean {
        if (!path.startsWith(IMAGES_DIRECTORY_PREFIX)) {
            return false
        }

        if (path.startsWith('/') || path.startsWith('\\')) {
            return false
        }

        return path
            .split('/', '\\')
            .none { segment -> segment == ".." || segment == "." }
    }

    companion object {
        const val MANIFEST_FILE_NAME = "manifest.json"
        const val IMAGES_DIRECTORY_PREFIX = "images/"
    }
}

data class TrainingPackImageSource(
    val path: String,
    val sourceUri: String,
)
