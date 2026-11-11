package ru.trainingapp.core.data.settings

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.trainingapp.core.model.AppAppearance

private val Context.appAppearanceDataStore by preferencesDataStore(
    name = "app_appearance",
)

@Singleton
class AppAppearanceStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val appearance: Flow<AppAppearance> = context.appAppearanceDataStore.data
        .map { preferences ->
            AppAppearance(
                backgroundImagePath = preferences[BACKGROUND_IMAGE_PATH],
                backgroundDimAmount = preferences[BACKGROUND_DIM_AMOUNT]
                    ?.coerceIn(0f, AppAppearance.MAX_BACKGROUND_DIM_AMOUNT)
                    ?: AppAppearance.DEFAULT_BACKGROUND_DIM_AMOUNT,
                backgroundCropZoom = preferences[BACKGROUND_CROP_ZOOM]
                    ?.coerceIn(MIN_CROP_ZOOM, MAX_CROP_ZOOM)
                    ?: MIN_CROP_ZOOM,
                backgroundCropOffsetX = preferences[BACKGROUND_CROP_OFFSET_X]
                    ?.coerceIn(-1f, 1f)
                    ?: 0f,
                backgroundCropOffsetY = preferences[BACKGROUND_CROP_OFFSET_Y]
                    ?.coerceIn(-1f, 1f)
                    ?: 0f,
            )
        }

    suspend fun importBackground(uriString: String): Boolean {
        return withContext(Dispatchers.IO) {
            val uri = Uri.parse(uriString)
            val mimeType = context.contentResolver.getType(uri)

            if (mimeType == "image/gif") {
                return@withContext false
            }

            val directory = File(context.filesDir, BACKGROUND_DIRECTORY_NAME).apply {
                mkdirs()
            }
            val targetFile = File(
                directory,
                "background_${System.currentTimeMillis()}",
            )

            val copied = runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: return@runCatching false

                targetFile.length() > 0L
            }.getOrDefault(false)

            if (!copied || targetFile.isGifFile()) {
                targetFile.delete()
                return@withContext false
            }

            context.appAppearanceDataStore.edit { preferences ->
                preferences[BACKGROUND_IMAGE_PATH] = targetFile.absolutePath
                preferences[BACKGROUND_CROP_ZOOM] = MIN_CROP_ZOOM
                preferences[BACKGROUND_CROP_OFFSET_X] = 0f
                preferences[BACKGROUND_CROP_OFFSET_Y] = 0f
            }

            directory.listFiles()
                .orEmpty()
                .filter { file -> file != targetFile }
                .forEach { file -> file.delete() }

            true
        }
    }

    suspend fun updateBackgroundCrop(
        zoom: Float,
        offsetX: Float,
        offsetY: Float,
    ) {
        context.appAppearanceDataStore.edit { preferences ->
            preferences[BACKGROUND_CROP_ZOOM] = zoom.coerceIn(MIN_CROP_ZOOM, MAX_CROP_ZOOM)
            preferences[BACKGROUND_CROP_OFFSET_X] = offsetX.coerceIn(-1f, 1f)
            preferences[BACKGROUND_CROP_OFFSET_Y] = offsetY.coerceIn(-1f, 1f)
        }
    }

    suspend fun updateBackgroundDimAmount(value: Float) {
        context.appAppearanceDataStore.edit { preferences ->
            preferences[BACKGROUND_DIM_AMOUNT] = value.coerceIn(
                0f,
                AppAppearance.MAX_BACKGROUND_DIM_AMOUNT,
            )
        }
    }

    suspend fun removeBackground() {
        withContext(Dispatchers.IO) {
            val directory = File(context.filesDir, BACKGROUND_DIRECTORY_NAME)
            directory.listFiles().orEmpty().forEach { file -> file.delete() }

            context.appAppearanceDataStore.edit { preferences ->
                preferences.remove(BACKGROUND_IMAGE_PATH)
                preferences[BACKGROUND_CROP_ZOOM] = MIN_CROP_ZOOM
                preferences[BACKGROUND_CROP_OFFSET_X] = 0f
                preferences[BACKGROUND_CROP_OFFSET_Y] = 0f
            }
        }
    }

    private fun File.isGifFile(): Boolean {
        return runCatching {
            inputStream().use { input ->
                val signature = ByteArray(6)
                if (input.read(signature) != signature.size) {
                    return@use false
                }

                val header = signature.toString(Charsets.US_ASCII)
                header == "GIF87a" || header == "GIF89a"
            }
        }.getOrDefault(false)
    }

    private companion object {
        const val BACKGROUND_DIRECTORY_NAME = "app_background"
        const val MIN_CROP_ZOOM = 1f
        const val MAX_CROP_ZOOM = 4f

        val BACKGROUND_IMAGE_PATH = stringPreferencesKey("background_image_path")
        val BACKGROUND_DIM_AMOUNT = floatPreferencesKey("background_dim_amount")
        val BACKGROUND_CROP_ZOOM = floatPreferencesKey("background_crop_zoom")
        val BACKGROUND_CROP_OFFSET_X = floatPreferencesKey("background_crop_offset_x")
        val BACKGROUND_CROP_OFFSET_Y = floatPreferencesKey("background_crop_offset_y")
    }
}
