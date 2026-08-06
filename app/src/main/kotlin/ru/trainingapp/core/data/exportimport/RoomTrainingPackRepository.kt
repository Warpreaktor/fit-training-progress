package ru.trainingapp.core.data.exportimport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.trainingapp.core.data.exportimport.dto.CURRENT_TRAINING_PACK_SCHEMA_VERSION
import ru.trainingapp.core.data.exportimport.dto.ExportExerciseDto
import ru.trainingapp.core.data.exportimport.dto.ExportExerciseImageDto
import ru.trainingapp.core.data.exportimport.dto.ExportTagDto
import ru.trainingapp.core.data.exportimport.dto.ExportWorkoutDto
import ru.trainingapp.core.data.exportimport.dto.ExportWorkoutExerciseDto
import ru.trainingapp.core.data.exportimport.dto.ExportWorkoutExerciseSetDto
import ru.trainingapp.core.data.exportimport.dto.TRAINING_PACK_EXPORT_TYPE_SINGLE_EXERCISE
import ru.trainingapp.core.data.exportimport.dto.TRAINING_PACK_EXPORT_TYPE_WORKOUT
import ru.trainingapp.core.data.exportimport.dto.TrainingPackDto
import ru.trainingapp.core.database.dao.ExerciseDefinitionDao
import ru.trainingapp.core.database.dao.ExerciseImageDao
import ru.trainingapp.core.database.dao.TagDao
import ru.trainingapp.core.database.dao.WorkoutDao
import ru.trainingapp.core.database.dao.WorkoutExerciseDao
import ru.trainingapp.core.database.dao.WorkoutExerciseSetDao
import ru.trainingapp.core.database.entity.ExerciseDefinitionEntity
import ru.trainingapp.core.database.entity.ExerciseImageEntity
import ru.trainingapp.core.database.entity.TagEntity
import ru.trainingapp.core.database.entity.WorkoutEntity
import ru.trainingapp.core.database.entity.WorkoutExerciseEntity
import ru.trainingapp.core.database.entity.WorkoutExerciseSetEntity
import ru.trainingapp.core.domain.repository.TrainingPackExportType
import ru.trainingapp.core.domain.repository.TrainingPackRepository
import ru.trainingapp.core.model.TrainingPackImportResult
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.WorkoutExerciseSetLoadType
import javax.inject.Inject

class RoomTrainingPackRepository @Inject constructor(
    private val exerciseDefinitionDao: ExerciseDefinitionDao,
    private val tagDao: TagDao,
    private val exerciseImageDao: ExerciseImageDao,
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val workoutExerciseSetDao: WorkoutExerciseSetDao,
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
        val exportExercise = buildExportExercise(
            exercise = exercise,
            exerciseRef = exerciseRef,
            tags = tags,
            tagRefsById = tagRefsById,
            imageSources = imageSources,
        )

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
            exercises = listOf(exportExercise),
        )

        zipWriter.write(
            destinationUri = destinationUri,
            manifestJson = jsonCodec.encode(trainingPack),
            imageSources = imageSources,
        )
    }

    override suspend fun exportWorkout(
        workoutId: Long,
        destinationUri: String,
    ) = withContext(Dispatchers.IO) {
        val workout = workoutDao
            .getWorkoutById(workoutId)
            ?.takeUnless { entity -> entity.isArchived }
            ?: error("Тренировка не найдена")

        val workoutExercises = workoutExerciseDao
            .getActiveWorkoutExerciseEntities(workout.id)
            .sortedBy { workoutExercise -> workoutExercise.sortOrder }

        val exercisesById = linkedMapOf<Long, ExerciseDefinitionEntity>()

        workoutExercises.forEach { workoutExercise ->
            if (workoutExercise.exerciseDefinitionId !in exercisesById) {
                val exercise = exerciseDefinitionDao
                    .getExerciseDefinitionById(workoutExercise.exerciseDefinitionId)
                    ?: error(
                        "Не найдено упражнение справочника с id=${workoutExercise.exerciseDefinitionId}"
                    )

                exercisesById[exercise.id] = exercise
            }
        }

        val exercises = exercisesById.values.toList()
        val exerciseRefsById = exercises
            .mapIndexed { index, exercise ->
                exercise.id to "exercise_${index + 1}"
            }
            .toMap()

        val workoutTags = tagDao
            .getTagsByWorkoutId(workout.id)
            .distinctBy { tag -> tag.id }

        val exerciseTagsById = exercises.associate { exercise ->
            exercise.id to tagDao
                .getTagsByExerciseDefinitionId(exercise.id)
                .distinctBy { tag -> tag.id }
        }

        val tags = buildList {
            addAll(workoutTags)
            exerciseTagsById.values.forEach { exerciseTags -> addAll(exerciseTags) }
        }
            .distinctBy { tag -> tag.id }
            .sortedBy { tag -> tag.name.lowercase() }

        val tagRefsById = tags
            .mapIndexed { index, tag ->
                tag.id to "tag_${index + 1}"
            }
            .toMap()

        val imageSources = mutableListOf<TrainingPackImageSource>()
        val exportExercises = exercises.map { exercise ->
            buildExportExercise(
                exercise = exercise,
                exerciseRef = requireNotNull(exerciseRefsById[exercise.id]),
                tags = exerciseTagsById[exercise.id].orEmpty(),
                tagRefsById = tagRefsById,
                imageSources = imageSources,
            )
        }

        val exportWorkoutExercises = workoutExercises.mapIndexed { index, workoutExercise ->
            val exerciseRef = exerciseRefsById[workoutExercise.exerciseDefinitionId]
                ?: error("Не удалось построить ссылку на упражнение тренировки")

            ExportWorkoutExerciseDto(
                ref = "workout_exercise_${index + 1}",
                exerciseRef = exerciseRef,
                sortOrder = workoutExercise.sortOrder,
                comment = workoutExercise.comment,
                sets = workoutExerciseSetDao
                    .getSetsByWorkoutExerciseId(workoutExercise.id)
                    .sortedBy { set -> set.setNumber }
                    .map { set -> set.toExportDto() },
            )
        }

        val trainingPack = TrainingPackDto(
            schemaVersion = CURRENT_TRAINING_PACK_SCHEMA_VERSION,
            exportType = TRAINING_PACK_EXPORT_TYPE_WORKOUT,
            createdAt = System.currentTimeMillis(),
            sourceApp = SOURCE_APP,
            tags = tags.map { tag ->
                ExportTagDto(
                    ref = tagRefsById[tag.id],
                    name = tag.name,
                    color = tag.color,
                )
            },
            exercises = exportExercises,
            workouts = listOf(
                ExportWorkoutDto(
                    ref = "workout_1",
                    name = workout.name,
                    description = workout.description,
                    tagRefs = workoutTags.mapNotNull { tag -> tagRefsById[tag.id] },
                    exercises = exportWorkoutExercises,
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
        expectedExportType: TrainingPackExportType,
    ): TrainingPackImportResult = withContext(Dispatchers.IO) {
        val extractedPack = zipReader.read(sourceUri)

        try {
            val trainingPack = jsonCodec.decode(extractedPack.manifestJson)
            val warnings = mutableListOf<String>()

            validateTrainingPack(
                trainingPack = trainingPack,
                expectedExportType = expectedExportType,
                warnings = warnings,
            )

            when (expectedExportType) {
                TrainingPackExportType.SINGLE_EXERCISE -> importSingleExercisePack(
                    trainingPack = trainingPack,
                    extractedPack = extractedPack,
                    warnings = warnings,
                )

                TrainingPackExportType.WORKOUT -> importWorkoutPack(
                    trainingPack = trainingPack,
                    extractedPack = extractedPack,
                    warnings = warnings,
                )
            }
        } finally {
            extractedPack.rootDirectory.deleteRecursively()
        }
    }

    private suspend fun buildExportExercise(
        exercise: ExerciseDefinitionEntity,
        exerciseRef: String,
        tags: List<TagEntity>,
        tagRefsById: Map<Long, String>,
        imageSources: MutableList<TrainingPackImageSource>,
    ): ExportExerciseDto {
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

        return ExportExerciseDto(
            ref = exerciseRef,
            name = exercise.name,
            description = exercise.description,
            tagRefs = tags.mapNotNull { tag -> tagRefsById[tag.id] },
            images = images,
        )
    }

    private fun WorkoutExerciseSetEntity.toExportDto(): ExportWorkoutExerciseSetDto {
        return ExportWorkoutExerciseSetDto(
            setNumber = setNumber,
            reps = reps,
            weightValue = weightValue.takeIf {
                loadType == WorkoutExerciseSetLoadType.WEIGHT
            },
            weightUnit = weightUnit
                ?.name
                ?.takeIf { loadType == WorkoutExerciseSetLoadType.WEIGHT },
            durationSeconds = durationSeconds.takeIf {
                loadType == WorkoutExerciseSetLoadType.TIME
            },
            durationUnit = weightUnit
                ?.takeIf { unit ->
                    loadType == WorkoutExerciseSetLoadType.TIME &&
                            unit.isTimeUnit
                }
                ?.name,
        )
    }

    private fun validateTrainingPack(
        trainingPack: TrainingPackDto,
        expectedExportType: TrainingPackExportType,
        warnings: MutableList<String>,
    ) {
        require(trainingPack.schemaVersion > 0) {
            "Некорректная версия формата"
        }

        if (trainingPack.schemaVersion > CURRENT_TRAINING_PACK_SCHEMA_VERSION) {
            warnings += "Пакет создан в более новой версии формата. Импортированы только известные поля."
        }

        require(trainingPack.exportType == expectedExportType.name) {
            when (expectedExportType) {
                TrainingPackExportType.SINGLE_EXERCISE ->
                    "Выбранный файл не является пакетом одного упражнения"

                TrainingPackExportType.WORKOUT ->
                    "Выбранный файл не является пакетом тренировки"
            }
        }
    }

    private suspend fun importSingleExercisePack(
        trainingPack: TrainingPackDto,
        extractedPack: ExtractedTrainingPack,
        warnings: MutableList<String>,
    ): TrainingPackImportResult {
        require(trainingPack.exercises.size == 1) {
            "Пакет одного упражнения должен содержать ровно одно упражнение"
        }

        val exercise = trainingPack.exercises.single()

        require(
            !exercise.ref.isNullOrBlank() &&
                !exercise.name.isNullOrBlank()
        ) {
            "У упражнения отсутствует ref или name"
        }

        val importedTags = importTags(
            tags = trainingPack.tags,
            warnings = warnings,
        )

        val importedExercises = importExercises(
            exercises = trainingPack.exercises,
            importedTagIdsByRef = importedTags.idsByRef,
            extractedPack = extractedPack,
            warnings = warnings,
        )

        require(importedExercises.createdCount == 1) {
            "Упражнение не удалось импортировать"
        }

        return TrainingPackImportResult(
            createdExercises = importedExercises.createdCount,
            createdTags = importedTags.createdCount,
            createdImages = importedExercises.createdImages,
            warnings = warnings,
        )
    }

    private suspend fun importWorkoutPack(
        trainingPack: TrainingPackDto,
        extractedPack: ExtractedTrainingPack,
        warnings: MutableList<String>,
    ): TrainingPackImportResult {
        require(trainingPack.workouts.size == 1) {
            "Пакет тренировки должен содержать ровно одну тренировку"
        }

        val workoutDto = trainingPack.workouts.single()
        val workoutRef = workoutDto.ref?.trim().orEmpty()
        val workoutName = workoutDto.name?.trim().orEmpty()

        require(workoutRef.isNotBlank() && workoutName.isNotBlank()) {
            "У тренировки отсутствует ref или name"
        }

        val importedTags = importTags(
            tags = trainingPack.tags,
            warnings = warnings,
        )

        val importedExercises = importExercises(
            exercises = trainingPack.exercises,
            importedTagIdsByRef = importedTags.idsByRef,
            extractedPack = extractedPack,
            warnings = warnings,
        )

        val now = System.currentTimeMillis()
        val importedWorkoutName = resolveImportedWorkoutName(workoutName)
        val workoutId = workoutDao.insertWorkout(
            WorkoutEntity(
                name = importedWorkoutName,
                description = workoutDto.description
                    ?.trim()
                    ?.takeIf { description -> description.isNotBlank() },
                sortOrder = workoutDao.getNextSortOrder(),
                isLocked = false,
                isArchived = false,
                archivedAt = null,
                createdAt = now,
                updatedAt = now,
            )
        )

        val workoutTagIds = workoutDto.tagRefs.mapNotNull { tagRef ->
            val tagId = importedTags.idsByRef[tagRef]

            if (tagId == null) {
                warnings += "Для тренировки «$importedWorkoutName» не найден тег с ref=$tagRef."
            }

            tagId
        }.toSet()

        tagDao.replaceWorkoutTags(
            workoutId = workoutId,
            tagIds = workoutTagIds,
        )

        val usedWorkoutExerciseRefs = mutableSetOf<String>()
        val sortedWorkoutExercises = workoutDto.exercises
            .withIndex()
            .sortedWith(
                compareBy<IndexedValue<ExportWorkoutExerciseDto>> { item ->
                    item.value.sortOrder ?: item.index
                }.thenBy { item -> item.index }
            )

        sortedWorkoutExercises.forEachIndexed { importedSortOrder, indexedExercise ->
            val exerciseDto = indexedExercise.value
            val workoutExerciseRef = exerciseDto.ref?.trim().orEmpty()
            val exerciseRef = exerciseDto.exerciseRef?.trim().orEmpty()

            if (workoutExerciseRef.isBlank() || exerciseRef.isBlank()) {
                warnings += "Пропущено упражнение тренировки №${indexedExercise.index + 1}: отсутствует ref или exerciseRef."
                return@forEachIndexed
            }

            if (!usedWorkoutExerciseRefs.add(workoutExerciseRef)) {
                warnings += "Пропущено повторяющееся упражнение тренировки с ref=$workoutExerciseRef."
                return@forEachIndexed
            }

            val exerciseDefinitionId = importedExercises.idsByRef[exerciseRef]

            if (exerciseDefinitionId == null) {
                warnings += "Пропущено упражнение тренировки с ref=$workoutExerciseRef: не найдено упражнение $exerciseRef."
                return@forEachIndexed
            }

            val workoutExerciseId = workoutExerciseDao.insertWorkoutExercise(
                WorkoutExerciseEntity(
                    workoutId = workoutId,
                    exerciseDefinitionId = exerciseDefinitionId,
                    sortOrder = importedSortOrder,
                    comment = exerciseDto.comment
                        ?.trim()
                        ?.takeIf { comment -> comment.isNotBlank() },
                    isChecked = false,
                    checkedAt = null,
                    isArchived = false,
                    archivedAt = null,
                    createdAt = now,
                    updatedAt = now,
                )
            )

            importWorkoutExerciseSets(
                workoutExerciseRef = workoutExerciseRef,
                workoutExerciseId = workoutExerciseId,
                sets = exerciseDto.sets,
                now = now,
                warnings = warnings,
            )
        }

        return TrainingPackImportResult(
            createdWorkouts = 1,
            createdExercises = importedExercises.createdCount,
            createdTags = importedTags.createdCount,
            createdImages = importedExercises.createdImages,
            warnings = warnings,
        )
    }

    private suspend fun importTags(
        tags: List<ExportTagDto>,
        warnings: MutableList<String>,
    ): ImportedTags {
        val importedTagIdsByRef = linkedMapOf<String, Long>()
        var createdTags = 0

        tags.forEachIndexed { index, tagDto ->
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

        return ImportedTags(
            idsByRef = importedTagIdsByRef,
            createdCount = createdTags,
        )
    }

    private suspend fun importExercises(
        exercises: List<ExportExerciseDto>,
        importedTagIdsByRef: Map<String, Long>,
        extractedPack: ExtractedTrainingPack,
        warnings: MutableList<String>,
    ): ImportedExercises {
        val importedExerciseIdsByRef = linkedMapOf<String, Long>()
        var createdExercises = 0
        var createdImages = 0

        exercises.forEachIndexed { index, exerciseDto ->
            val exerciseRef = exerciseDto.ref?.trim().orEmpty()
            val exerciseName = exerciseDto.name?.trim().orEmpty()

            if (exerciseRef.isBlank() || exerciseName.isBlank()) {
                warnings += "Пропущено упражнение №${index + 1}: отсутствует ref или name."
                return@forEachIndexed
            }

            if (exerciseRef in importedExerciseIdsByRef) {
                warnings += "Пропущено повторяющееся упражнение с ref=$exerciseRef."
                return@forEachIndexed
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

            createdImages += importExerciseImages(
                exerciseDefinitionId = exerciseId,
                exerciseName = importedName,
                images = exerciseDto.images,
                extractedPack = extractedPack,
                now = now,
                warnings = warnings,
            )

            importedExerciseIdsByRef[exerciseRef] = exerciseId
            createdExercises++
        }

        return ImportedExercises(
            idsByRef = importedExerciseIdsByRef,
            createdCount = createdExercises,
            createdImages = createdImages,
        )
    }

    private suspend fun importExerciseImages(
        exerciseDefinitionId: Long,
        exerciseName: String,
        images: List<ExportExerciseImageDto>,
        extractedPack: ExtractedTrainingPack,
        now: Long,
        warnings: MutableList<String>,
    ): Int {
        val imageCandidates = images
            .mapIndexedNotNull { imageIndex, imageDto ->
                val path = imageDto.path?.trim().orEmpty()

                if (!isExpectedImagePath(path)) {
                    warnings += "У упражнения «$exerciseName» пропущена картинка с некорректным path."
                    return@mapIndexedNotNull null
                }

                val sourceFile = extractedPack.filesByPath[path]

                if (sourceFile == null || !sourceFile.isFile) {
                    warnings += "У упражнения «$exerciseName» не найдена картинка $path."
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
                        exerciseDefinitionId = exerciseDefinitionId,
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
                    exerciseDefinitionId = exerciseDefinitionId,
                    uri = importedImage.localUri,
                    sortOrder = importedImage.candidate.sortOrder,
                    isCover = imageIndex == coverIndex,
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }

        return copiedImages.size
    }

    private suspend fun importWorkoutExerciseSets(
        workoutExerciseRef: String,
        workoutExerciseId: Long,
        sets: List<ExportWorkoutExerciseSetDto>,
        now: Long,
        warnings: MutableList<String>,
    ) {
        val usedSetNumbers = mutableSetOf<Int>()
        val sortedSets = sets
            .withIndex()
            .sortedWith(
                compareBy<IndexedValue<ExportWorkoutExerciseSetDto>> { item ->
                    item.value.setNumber ?: item.index + 1
                }.thenBy { item -> item.index }
            )

        sortedSets.forEachIndexed { importedIndex, indexedSet ->
            val setDto = indexedSet.value
            val requestedSetNumber = setDto.setNumber ?: importedIndex + 1
            val setNumber = resolveSetNumber(
                requestedSetNumber = requestedSetNumber,
                usedSetNumbers = usedSetNumbers,
            )

            if (setNumber != requestedSetNumber) {
                warnings += "Для $workoutExerciseRef номер подхода $requestedSetNumber заменён на $setNumber."
            }

            val durationSeconds = setDto.durationSeconds
                ?.coerceAtLeast(0)

            val loadType = if (durationSeconds != null) {
                WorkoutExerciseSetLoadType.TIME
            } else {
                WorkoutExerciseSetLoadType.WEIGHT
            }

            val parsedWeightUnit = setDto.weightUnit
                ?.trim()
                ?.takeIf { value -> value.isNotBlank() }
                ?.let { value ->
                    runCatching {
                        WeightUnit.valueOf(value.uppercase())
                    }.getOrNull()
                }

            val parsedDurationUnit = setDto.durationUnit
                ?.trim()
                ?.takeIf { value -> value.isNotBlank() }
                ?.let { value ->
                    runCatching {
                        WeightUnit.valueOf(value.uppercase())
                    }.getOrNull()
                }
                ?.takeIf { unit -> unit.isTimeUnit }

            val weightUnit = when {
                loadType == WorkoutExerciseSetLoadType.TIME -> {
                    parsedDurationUnit ?: WeightUnit.SEC
                }

                parsedWeightUnit != null -> parsedWeightUnit

                setDto.weightValue != null -> {
                    if (!setDto.weightUnit.isNullOrBlank()) {
                        warnings += "Для $workoutExerciseRef указана неизвестная единица веса ${setDto.weightUnit}; использован KG."
                    }

                    WeightUnit.KG
                }

                else -> null
            }

            workoutExerciseSetDao.insertSet(
                WorkoutExerciseSetEntity(
                    workoutExerciseId = workoutExerciseId,
                    setNumber = setNumber,
                    reps = setDto.reps?.coerceAtLeast(0) ?: 1,
                    loadType = loadType,
                    weightValue = setDto.weightValue.takeIf {
                        loadType == WorkoutExerciseSetLoadType.WEIGHT
                    },
                    weightUnit = weightUnit,
                    durationSeconds = durationSeconds.takeIf {
                        loadType == WorkoutExerciseSetLoadType.TIME
                    },
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }

    private fun resolveSetNumber(
        requestedSetNumber: Int,
        usedSetNumbers: MutableSet<Int>,
    ): Int {
        if (requestedSetNumber > 0 && usedSetNumbers.add(requestedSetNumber)) {
            return requestedSetNumber
        }

        var candidate = 1

        while (!usedSetNumbers.add(candidate)) {
            candidate++
        }

        return candidate
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

    private suspend fun resolveImportedWorkoutName(
        originalName: String,
    ): String {
        if (workoutDao.getWorkoutByName(originalName) == null) {
            return originalName
        }

        var suffixIndex = 1

        while (true) {
            val candidate = if (suffixIndex == 1) {
                "$originalName (импорт)"
            } else {
                "$originalName (импорт $suffixIndex)"
            }

            if (workoutDao.getWorkoutByName(candidate) == null) {
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

private data class ImportedTags(
    val idsByRef: Map<String, Long>,
    val createdCount: Int,
)

private data class ImportedExercises(
    val idsByRef: Map<String, Long>,
    val createdCount: Int,
    val createdImages: Int,
)

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
