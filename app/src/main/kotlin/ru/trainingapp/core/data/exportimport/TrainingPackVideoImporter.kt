package ru.trainingapp.core.data.exportimport

import java.io.File
import javax.inject.Inject
import ru.trainingapp.core.data.exercise.ExerciseVideoFileStorage

class TrainingPackVideoImporter @Inject constructor(
    private val exerciseVideoFileStorage: ExerciseVideoFileStorage,
) {

    suspend fun copyToPrivateStorage(
        exerciseDefinitionId: Long,
        sourceFile: File,
        archivePath: String,
    ): String {
        val extension = archivePath
            .substringAfterLast('.', missingDelimiterValue = "")
            .lowercase()
            .takeIf { value -> value.matches(Regex("[a-z0-9]{1,8}")) }
            ?: "mp4"

        return exerciseVideoFileStorage.copyFileToPrivateStorage(
            exerciseDefinitionId = exerciseDefinitionId,
            sourceFile = sourceFile,
            extension = extension,
        )
    }
}
