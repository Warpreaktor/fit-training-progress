package ru.trainingapp.core.data.exportimport

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class TrainingPackImageExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun canOpen(uriString: String): Boolean {
        return runCatching {
            openInputStream(uriString).use { }
        }.isSuccess
    }

    fun resolveExtension(uriString: String): String {
        val uri = Uri.parse(uriString)
        val extensionFromPath = uri.lastPathSegment
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?.takeIf(::isSafeExtension)

        if (extensionFromPath != null) {
            return extensionFromPath
        }

        val extensionFromMime = context.contentResolver
            .getType(uri)
            ?.let { mimeType ->
                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            }
            ?.lowercase()
            ?.takeIf(::isSafeExtension)

        return extensionFromMime ?: "jpg"
    }

    fun openInputStream(uriString: String): InputStream {
        val uri = Uri.parse(uriString)

        if (uri.scheme == "file") {
            val path = requireNotNull(uri.path) {
                "У локального изображения отсутствует путь"
            }

            return File(path).inputStream()
        }

        return context.contentResolver.openInputStream(uri)
            ?: error("Не удалось открыть изображение: $uriString")
    }

    private fun isSafeExtension(extension: String): Boolean {
        return extension.matches(Regex("[a-z0-9]{1,8}"))
    }
}
