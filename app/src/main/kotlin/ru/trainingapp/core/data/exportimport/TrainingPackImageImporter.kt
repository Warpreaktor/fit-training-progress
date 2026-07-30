package ru.trainingapp.core.data.exportimport

import java.io.File
import ru.trainingapp.core.data.exercise.ExerciseImageFileStorage
import javax.inject.Inject

class TrainingPackImageImporter @Inject constructor(
    private val exerciseImageFileStorage: ExerciseImageFileStorage,
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
            ?: "jpg"

        return exerciseImageFileStorage.copyFileToPrivateStorage(
            exerciseDefinitionId = exerciseDefinitionId,
            sourceFile = sourceFile,
            extension = extension,
        )
    }
}
