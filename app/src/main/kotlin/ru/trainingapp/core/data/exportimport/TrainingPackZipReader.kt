package ru.trainingapp.core.data.exportimport

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TrainingPackZipReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun read(sourceUri: String): ExtractedTrainingPack = withContext(Dispatchers.IO) {
        val rootDirectory = File(
            context.cacheDir,
            "training_pack_import/${UUID.randomUUID()}",
        )

        check(rootDirectory.mkdirs()) {
            "Не удалось подготовить временную папку импорта"
        }

        val filesByPath = linkedMapOf<String, File>()
        var manifestJson: String? = null
        var totalExtractedBytes = 0L

        try {
            val inputStream = context.contentResolver.openInputStream(Uri.parse(sourceUri))
                ?: error("Не удалось открыть файл импорта")

            ZipInputStream(inputStream.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    val entryName = entry.name

                    require(isSafeArchivePath(entryName)) {
                        "Архив содержит небезопасный путь: $entryName"
                    }

                    if (entry.isDirectory) {
                        zip.closeEntry()
                        continue
                    }

                    when {
                        entryName == TrainingPackZipWriter.MANIFEST_FILE_NAME -> {
                            manifestJson = zip.readTextWithLimit(MAX_MANIFEST_BYTES)
                            totalExtractedBytes += manifestJson
                                ?.toByteArray(Charsets.UTF_8)
                                ?.size
                                ?: 0
                        }

                        entryName.startsWith(TrainingPackZipWriter.IMAGES_DIRECTORY_PREFIX) -> {
                            val targetFile = safeTargetFile(
                                rootDirectory = rootDirectory,
                                entryName = entryName,
                            )

                            targetFile.parentFile?.mkdirs()

                            val copiedBytes = targetFile.outputStream().buffered().use { output ->
                                zip.copyToWithLimit(
                                    output = output,
                                    maxBytes = MAX_IMAGE_BYTES,
                                )
                            }

                            totalExtractedBytes += copiedBytes

                            require(totalExtractedBytes <= MAX_ARCHIVE_BYTES) {
                                "Архив слишком большой"
                            }

                            filesByPath[entryName] = targetFile
                        }
                    }

                    zip.closeEntry()
                }
            }

            ExtractedTrainingPack(
                manifestJson = manifestJson
                    ?: error("В архиве отсутствует manifest.json"),
                filesByPath = filesByPath,
                rootDirectory = rootDirectory,
            )
        } catch (exception: Exception) {
            rootDirectory.deleteRecursively()
            throw exception
        }
    }

    private fun safeTargetFile(
        rootDirectory: File,
        entryName: String,
    ): File {
        val rootCanonicalPath = rootDirectory.canonicalFile.path + File.separator
        val targetFile = File(rootDirectory, entryName).canonicalFile

        require(targetFile.path.startsWith(rootCanonicalPath)) {
            "Архив содержит выход за временную папку: $entryName"
        }

        return targetFile
    }

    private fun isSafeArchivePath(path: String): Boolean {
        if (path.isBlank()) {
            return false
        }

        if (path.startsWith('/') || path.startsWith('\\')) {
            return false
        }

        if (Regex("^[a-zA-Z]:").containsMatchIn(path)) {
            return false
        }

        return path
            .split('/', '\\')
            .none { segment -> segment == ".." || segment == "." }
    }

    private fun ZipInputStream.readTextWithLimit(maxBytes: Long): String {
        val output = ByteArrayOutputStream()
        copyToWithLimit(
            output = output,
            maxBytes = maxBytes,
        )
        return output.toString(Charsets.UTF_8.name())
    }

    private fun ZipInputStream.copyToWithLimit(
        output: java.io.OutputStream,
        maxBytes: Long,
    ): Long {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var totalBytes = 0L

        while (true) {
            val read = read(buffer)

            if (read < 0) {
                break
            }

            totalBytes += read

            require(totalBytes <= maxBytes) {
                "Один из файлов архива превышает допустимый размер"
            }

            output.write(buffer, 0, read)
        }

        return totalBytes
    }

    companion object {
        private const val MAX_MANIFEST_BYTES = 2L * 1024L * 1024L
        private const val MAX_IMAGE_BYTES = 40L * 1024L * 1024L
        private const val MAX_ARCHIVE_BYTES = 250L * 1024L * 1024L
    }
}

data class ExtractedTrainingPack(
    val manifestJson: String,
    val filesByPath: Map<String, File>,
    val rootDirectory: File,
)
