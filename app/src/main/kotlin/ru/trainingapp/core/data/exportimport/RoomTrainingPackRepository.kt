package ru.trainingapp.core.data.exportimport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.trainingapp.core.data.exportimport.dto.CURRENT_TRAINING_PACK_SCHEMA_VERSION
import ru.trainingapp.core.data.exportimport.dto.ExportExerciseDto
import ru.trainingapp.core.data.exportimport.dto.ExportExerciseImageDto
import ru.trainingapp.core.data.exportimport.dto.ExportTagDto
import ru.trainingapp.core.data.exportimport.dto.TRAINING_PACK_EXPORT_TYPE_SINGLE_EXERCISE
import ru.trainingapp.core.data.exportimport.dto.TrainingPackDto
import ru.trainingapp.core.database.dao.ExerciseDefinitionDao
import ru.trainingapp.core.database.dao.ExerciseImageDao
import ru.trainingapp.core.database.dao.TagDao
import ru.trainingapp.core.database.entity.ExerciseDefinitionEntity
import ru.trainingapp.core.database.entity.ExerciseImageEntity
import ru.trainingapp.core.database.entity.TagEntity
import ru.trainingapp.core.domain.repository.TrainingPackRepository
import ru.trainingapp.core.model.TrainingPackImportResult
import javax.inject.Inject

class RoomTrainingPackRepository @Inject constructor(
    private val exerciseDefinitionDao: ExerciseDefinitionDao,
    private val tagDao: TagDao,
    private val exerciseImageDao: ExerciseImageDao,
    private val jsonCodec: TrainingPackJsonCodec,
    private val zipWriter: TrainingPackZipWriter,
    private val zipReader: TrainingPackZipReader,
    private val imageExporter: TrainingPackImageExporter,
    private val imageImporter: TrainingPackImageImporter,
) : TrainingPackRepository {

    override suspend fun exportExercise(
        exerciseDefinitionId: Long,
        destinationUri: String,
    ) = withContext(Dispatchers.IO) {
        val exercise = exerciseDefinitionDao
            .getExerciseDefinitionById(exerciseDefinitionId)
            ?.takeUnless { entity -> entity.isArchived }
            ?: error("Упражнение не найдено")

        val exerciseRef = "exercise_1"
        val tags = tagDao
            .getTagsByExerciseDefinitionId(exercise.id)
            .distinctBy { tag -> tag.id }
            .sortedBy { tag -> tag.name.lowercase() }

        val tagRefsById = tags
            .mapIndexed { index, tag ->
                tag.id to "tag_${index + 1}"
            }
            .toMap()

        val imageSources = mutableListOf<TrainingPackImageSource>()
        val images = exerciseImageDao
            .getImagesByExerciseDefinitionId(exercise.id)
            .filter { image -> imageExporter.canOpen(image.uri) }
            .sortedBy { image -> image.sortOrder }
            .mapIndexed { index, image ->
                val extension = imageExporter.resolveExtension(image.uri)
                val imagePath = "images/exercises/$exerciseRef/image_${index + 1}.$extension"

                imageSources += TrainingPackImageSource(
                    path = imagePath,
                    sourceUri = image.uri,
                )

                ExportExerciseImageDto(
                    ref = "${exerciseRef}_image_${index + 1}",
                    path = imagePath,
                    sortOrder = image.sortOrder,
                    isCover = image.isCover,
                )
            }

        val trainingPack = TrainingPackDto(
            schemaVersion = CURRENT_TRAINING_PACK_SCHEMA_VERSION,
            exportType = TRAINING_PACK_EXPORT_TYPE_SINGLE_EXERCISE,
            createdAt = System.currentTimeMillis(),
            sourceApp = SOURCE_APP,
            tags = tags.map { tag ->
                ExportTagDto(
                    ref = tagRefsById[tag.id],
                    name = tag.name,
                    color = tag.color,
                )
            },
            exercises = listOf(
                ExportExerciseDto(
                    ref = exerciseRef,
                    name = exercise.name,
                    description = exercise.description,
                    tagRefs = tags.mapNotNull { tag -> tagRefsById[tag.id] },
                    images = images,
                )
            ),
        )

        zipWriter.write(
            destinationUri = destinationUri,
            manifestJson = jsonCodec.encode(trainingPack),
            imageSources = imageSources,
        )
    }

    override suspend fun importTrainingPack(
        sourceUri: String,
    ): TrainingPackImportResult = withContext(Dispatchers.IO) {
        val extractedPack = zipReader.read(sourceUri)

        try {
            importSingleExercisePack(extractedPack)
        } finally {
            extractedPack.rootDirectory.deleteRecursively()
        }
    }

    private suspend fun importSingleExercisePack(
        extractedPack: ExtractedTrainingPack,
    ): TrainingPackImportResult {
        val trainingPack = jsonCodec.decode(extractedPack.manifestJson)
        val warnings = mutableListOf<String>()

        require(trainingPack.schemaVersion > 0) {
            "Некорректная версия формата"
        }

        if (trainingPack.schemaVersion > CURRENT_TRAINING_PACK_SCHEMA_VERSION) {
            warnings += "Пакет создан в более новой версии формата. Импортированы только известные поля."
        }

        require(trainingPack.exportType == TRAINING_PACK_EXPORT_TYPE_SINGLE_EXERCISE) {
            "На этом этапе поддерживается импорт только одного упражнения"
        }

        require(trainingPack.exercises.size == 1) {
            "Пакет одного упражнения должен содержать ровно одно упражнение"
        }

        val exerciseDto = trainingPack.exercises.single()
        val exerciseRef = exerciseDto.ref?.trim().orEmpty()
        val exerciseName = exerciseDto.name?.trim().orEmpty()

        require(exerciseRef.isNotBlank() && exerciseName.isNotBlank()) {
            "У упражнения отсутствует ref или name"
        }

        val importedTagIdsByRef = linkedMapOf<String, Long>()
        var createdTags = 0

        trainingPack.tags.forEachIndexed { index, tagDto ->
            val ref = tagDto.ref?.trim().orEmpty()
            val name = tagDto.name?.trim().orEmpty()

            if (ref.isBlank() || name.isBlank()) {
                warnings += "Пропущен тег №${index + 1}: отсутствует ref или name."
                return@forEachIndexed
            }

            if (ref in importedTagIdsByRef) {
                warnings += "Пропущен повторяющийся тег с ref=$ref."
                return@forEachIndexed
            }

            val existingTag = tagDao.getTagByName(name)

            if (existingTag != null) {
                importedTagIdsByRef[ref] = existingTag.id
                return@forEachIndexed
            }

            val now = System.currentTimeMillis()
            val insertedId = tagDao.insertTag(
                TagEntity(
                    name = name,
                    color = tagDto.color,
                    createdAt = now,
                    updatedAt = now,
                )
            )

            val tagId = if (insertedId != -1L) {
                createdTags++
                insertedId
            } else {
                tagDao.getTagByName(name)?.id ?: 0L
            }

            if (tagId > 0L) {
                importedTagIdsByRef[ref] = tagId
            } else {
                warnings += "Не удалось импортировать тег «$name»."
            }
        }

        val importedName = resolveImportedExerciseName(exerciseName)
        val now = System.currentTimeMillis()
        val exerciseId = exerciseDefinitionDao.insertExerciseDefinitionAndGetId(
            ExerciseDefinitionEntity(
                name = importedName,
                description = exerciseDto.description.orEmpty().trim(),
                isArchived = false,
                archivedAt = null,
                createdAt = now,
                updatedAt = now,
            )
        )

        val tagIds = exerciseDto.tagRefs.mapNotNull { tagRef ->
            val tagId = importedTagIdsByRef[tagRef]

            if (tagId == null) {
                warnings += "Для упражнения «$importedName» не найден тег с ref=$tagRef."
            }

            tagId
        }.toSet()

        tagDao.replaceExerciseDefinitionTags(
            exerciseDefinitionId = exerciseId,
            tagIds = tagIds,
        )

        val imageCandidates = exerciseDto.images
            .mapIndexedNotNull { imageIndex, imageDto ->
                val path = imageDto.path?.trim().orEmpty()

                if (!isExpectedImagePath(path)) {
                    warnings += "У упражнения «$importedName» пропущена картинка с некорректным path."
                    return@mapIndexedNotNull null
                }

                val sourceFile = extractedPack.filesByPath[path]

                if (sourceFile == null || !sourceFile.isFile) {
                    warnings += "У упражнения «$importedName» не найдена картинка $path."
                    return@mapIndexedNotNull null
                }

                ImportImageCandidate(
                    archiveIndex = imageIndex,
                    path = path,
                    sortOrder = imageDto.sortOrder ?: imageIndex,
                    isCover = imageDto.isCover == true,
                    sourceFile = sourceFile,
                )
            }
            .sortedWith(
                compareBy<ImportImageCandidate> { candidate -> candidate.sortOrder }
                    .thenBy { candidate -> candidate.archiveIndex }
            )

        val copiedImages = imageCandidates.mapNotNull { candidate ->
            runCatching {
                ImportedImage(
                    candidate = candidate,
                    localUri = imageImporter.copyToPrivateStorage(
                        exerciseDefinitionId = exerciseId,
                        sourceFile = candidate.sourceFile,
                        archivePath = candidate.path,
                    ),
                )
            }.getOrElse { exception ->
                warnings += "Не удалось импортировать картинку ${candidate.path}: ${exception.message.orEmpty()}"
                null
            }
        }

        val coverIndex = copiedImages.indexOfFirst { importedImage ->
            importedImage.candidate.isCover
        }.takeIf { index -> index >= 0 } ?: 0

        copiedImages.forEachIndexed { imageIndex, importedImage ->
            exerciseImageDao.insertImage(
                ExerciseImageEntity(
                    exerciseDefinitionId = exerciseId,
                    uri = importedImage.localUri,
                    sortOrder = importedImage.candidate.sortOrder,
                    isCover = imageIndex == coverIndex,
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }

        return TrainingPackImportResult(
            createdExercises = 1,
            createdTags = createdTags,
            createdImages = copiedImages.size,
            warnings = warnings,
        )
    }

    private suspend fun resolveImportedExerciseName(
        originalName: String,
    ): String {
        if (exerciseDefinitionDao.getExerciseDefinitionByName(originalName) == null) {
            return originalName
        }

        var suffixIndex = 1

        while (true) {
            val candidate = if (suffixIndex == 1) {
                "$originalName (импорт)"
            } else {
                "$originalName (импорт $suffixIndex)"
            }

            if (exerciseDefinitionDao.getExerciseDefinitionByName(candidate) == null) {
                return candidate
            }

            suffixIndex++
        }
    }

    private fun isExpectedImagePath(path: String): Boolean {
        if (!path.startsWith("images/exercises/")) {
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
        private const val SOURCE_APP = "fit-training-progress"
    }
}

private data class ImportImageCandidate(
    val archiveIndex: Int,
    val path: String,
    val sortOrder: Int,
    val isCover: Boolean,
    val sourceFile: java.io.File,
)

private data class ImportedImage(
    val candidate: ImportImageCandidate,
    val localUri: String,
)
