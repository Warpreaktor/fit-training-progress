package ru.trainingapp.core.data.workout

import androidx.room.withTransaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.trainingapp.core.data.mapper.toDomain
import ru.trainingapp.core.data.mapper.toEntity
import ru.trainingapp.core.database.TrainingDatabase
import ru.trainingapp.core.database.dao.WorkoutDao
import ru.trainingapp.core.database.dao.WorkoutExerciseDao
import ru.trainingapp.core.database.dao.WorkoutExerciseSetDao
import ru.trainingapp.core.database.entity.WorkoutEntity
import ru.trainingapp.core.database.entity.WorkoutExerciseEntity
import ru.trainingapp.core.database.entity.WorkoutExerciseSetEntity
import ru.trainingapp.core.database.model.WorkoutExerciseListItemDbModel
import ru.trainingapp.core.domain.repository.WorkoutRepository
import ru.trainingapp.core.model.WeightUnit
import ru.trainingapp.core.model.Workout
import ru.trainingapp.core.model.WorkoutEditorData
import ru.trainingapp.core.model.WorkoutExercise
import ru.trainingapp.core.model.WorkoutExerciseSet
import ru.trainingapp.core.model.WorkoutExerciseSetLoadType
import javax.inject.Inject

class RoomWorkoutRepository @Inject constructor(
    private val database: TrainingDatabase,
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val workoutExerciseSetDao: WorkoutExerciseSetDao,
) : WorkoutRepository {

    override fun observeWorkouts(): Flow<List<Workout>> {
        return workoutDao
            .observeWorkoutListItems()
            .map { items -> items.map { it.toDomain() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeWorkoutEditorData(
        workoutId: Long,
    ): Flow<WorkoutEditorData?> {
        val workoutFlow = workoutDao.observeWorkoutById(workoutId)

        val exercisesFlow = workoutExerciseDao
            .observeActiveWorkoutExercises(workoutId)
            .flatMapLatest { exerciseItems ->
                if (exerciseItems.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val exerciseFlows = exerciseItems.map { exerciseItem ->
                        workoutExerciseSetDao
                            .observeSetsByWorkoutExerciseId(exerciseItem.id)
                            .map { setEntities ->
                                exerciseItem.toDomain(
                                    sets = setEntities.map { it.toDomain() },
                                )
                            }
                    }

                    combine(exerciseFlows) { exercises ->
                        exercises.toList()
                    }
                }
            }

        return combine(
            workoutFlow,
            exercisesFlow,
        ) { workoutEntity, exercises ->
            workoutEntity?.let {
                WorkoutEditorData(
                    workout = it.toDomain(exercises),
                    exercises = exercises,
                )
            }
        }
    }

    override suspend fun createWorkout(
        name: String,
        description: String,
    ): Long {
        val now = System.currentTimeMillis()

        return workoutDao.insertWorkout(
            WorkoutEntity(
                name = name.trim(),
                description = description.trim(),
                isLocked = false,
                isArchived = false,
                archivedAt = null,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    override suspend fun archiveWorkout(
        id: Long,
    ) {
        workoutDao.archiveWorkout(
            id = id,
            archivedAt = System.currentTimeMillis(),
        )
    }

    override suspend fun addExerciseToWorkout(
        workoutId: Long,
        exerciseDefinitionId: Long,
    ): Long {
        val now = System.currentTimeMillis()

        return database.withTransaction {
            val sortOrder = workoutExerciseDao.getNextSortOrder(workoutId)

            val workoutExerciseId: Long = workoutExerciseDao.insertWorkoutExercise(
                WorkoutExerciseEntity(
                    workoutId = workoutId,
                    exerciseDefinitionId = exerciseDefinitionId,
                    sortOrder = sortOrder,
                    comment = null,
                    isChecked = false,
                    checkedAt = null,
                    isArchived = false,
                    archivedAt = null,
                    createdAt = now,
                    updatedAt = now,
                )
            )

            workoutDao.touchWorkout(
                id = workoutId,
                updatedAt = now,
            )

            workoutExerciseId
        }
    }

    override suspend fun archiveWorkoutExercise(
        workoutExerciseId: Long,
    ) {
        val now = System.currentTimeMillis()

        database.withTransaction {
            val workoutExercise = workoutExerciseDao.getWorkoutExerciseById(workoutExerciseId)
                ?: return@withTransaction

            workoutExerciseDao.archiveWorkoutExercise(
                id = workoutExerciseId,
                archivedAt = now,
            )

            workoutDao.touchWorkout(
                id = workoutExercise.workoutId,
                updatedAt = now,
            )
        }
    }

    override suspend fun addWorkoutExerciseSet(
        workoutExerciseId: Long,
    ): Long {
        val now = System.currentTimeMillis()

        return database.withTransaction {
            val workoutExercise = workoutExerciseDao.getWorkoutExerciseById(workoutExerciseId)
                ?: return@withTransaction 0L

            val nextSetNumber = workoutExerciseSetDao.getNextSetNumber(workoutExerciseId)

            val setId = workoutExerciseSetDao.insertSet(
                WorkoutExerciseSetEntity(
                    workoutExerciseId = workoutExerciseId,
                    setNumber = nextSetNumber,
                    reps = 1,
                    loadType = WorkoutExerciseSetLoadType.WEIGHT,
                    weightValue = null,
                    weightUnit = WeightUnit.KG,
                    durationSeconds = null,
                    createdAt = now,
                    updatedAt = now,
                )
            )

            workoutDao.touchWorkout(
                id = workoutExercise.workoutId,
                updatedAt = now,
            )

            setId
        }
    }

    override suspend fun removeWorkoutExerciseSet(
        workoutExerciseSetId: Long,
    ) {
        val now = System.currentTimeMillis()

        database.withTransaction {
            val set = workoutExerciseSetDao.getSetById(workoutExerciseSetId)
                ?: return@withTransaction

            val workoutExercise = workoutExerciseDao.getWorkoutExerciseById(set.workoutExerciseId)
                ?: return@withTransaction

            workoutExerciseSetDao.deleteSetById(workoutExerciseSetId)

            renumberSets(
                workoutExerciseId = set.workoutExerciseId,
                updatedAt = now,
            )

            workoutDao.touchWorkout(
                id = workoutExercise.workoutId,
                updatedAt = now,
            )
        }
    }

    override suspend fun moveWorkoutExerciseUp(
        workoutId: Long,
        workoutExerciseId: Long,
    ) {
        moveWorkoutExercise(
            workoutId = workoutId,
            workoutExerciseId = workoutExerciseId,
            direction = MoveDirection.UP,
        )
    }

    override suspend fun moveWorkoutExerciseDown(
        workoutId: Long,
        workoutExerciseId: Long,
    ) {
        moveWorkoutExercise(
            workoutId = workoutId,
            workoutExerciseId = workoutExerciseId,
            direction = MoveDirection.DOWN,
        )
    }

    private suspend fun moveWorkoutExercise(
        workoutId: Long,
        workoutExerciseId: Long,
        direction: MoveDirection,
    ) {
        val now = System.currentTimeMillis()

        database.withTransaction {
            val exercises = workoutExerciseDao
                .getActiveWorkoutExerciseEntities(workoutId)
                .sortedBy {
                    it.sortOrder
                }

            val currentIndex = exercises.indexOfFirst { it.id == workoutExerciseId }

            if (currentIndex == -1) return@withTransaction

            val targetIndex = when (direction) {
                MoveDirection.UP -> currentIndex - 1
                MoveDirection.DOWN -> currentIndex + 1
            }

            if (targetIndex !in exercises.indices) return@withTransaction

            val current = exercises[currentIndex]
            val target = exercises[targetIndex]

            workoutExerciseDao.updateWorkoutExercise(
                current.copy(
                    sortOrder = target.sortOrder,
                    updatedAt = now,
                )
            )

            workoutExerciseDao.updateWorkoutExercise(
                target.copy(
                    sortOrder = current.sortOrder,
                    updatedAt = now,
                )
            )

            workoutDao.touchWorkout(
                id = workoutId,
                updatedAt = now,
            )
        }
    }

    private suspend fun renumberSets(
        workoutExerciseId: Long,
        updatedAt: Long,
    ) {
        workoutExerciseSetDao
            .getSetsByWorkoutExerciseId(workoutExerciseId)
            .sortedBy { it.setNumber }
            .forEachIndexed { index, set ->
                val expectedSetNumber = index + 1

                if (set.setNumber != expectedSetNumber) {
                    workoutExerciseSetDao.updateSet(
                        set.copy(
                            setNumber = expectedSetNumber,
                            updatedAt = updatedAt,
                        )
                    )
                }
            }
    }

    private fun WorkoutEntity.toDomain(
        exercises: List<WorkoutExercise>,
    ): Workout {
        return Workout(
            id = id,
            name = name,
            description = description,
            isLocked = isLocked,
            checkedExercisesCount = exercises.count { it.isChecked },
            exercisesCount = exercises.size,
        )
    }

    private fun WorkoutExerciseListItemDbModel.toDomain(
        sets: List<WorkoutExerciseSet>,
    ): WorkoutExercise {
        return WorkoutExercise(
            id = id,
            workoutId = workoutId,
            exerciseDefinitionId = exerciseDefinitionId,
            exerciseName = exerciseName,
            sortOrder = sortOrder,
            comment = comment,
            isChecked = isChecked,
            sets = sets,
        )
    }

    private enum class MoveDirection {
        UP,
        DOWN,
    }

    override suspend fun getWorkoutExerciseSet(
        workoutExerciseSetId: Long,
    ): WorkoutExerciseSet? {
        return workoutExerciseSetDao
            .getSetById(workoutExerciseSetId)
            ?.toDomain()
    }

    override suspend fun updateWorkoutExerciseSet(
        workoutExerciseSet: WorkoutExerciseSet,
    ) {
        val now = System.currentTimeMillis()

        database.withTransaction {
            val currentSetEntity = workoutExerciseSetDao.getSetById(workoutExerciseSet.id)
                ?: return@withTransaction

            val workoutExercise = workoutExerciseDao.getWorkoutExerciseById(
                currentSetEntity.workoutExerciseId,
            ) ?: return@withTransaction

            val updatedSet = workoutExerciseSet.copy(
                workoutExerciseId = currentSetEntity.workoutExerciseId,
                setNumber = currentSetEntity.setNumber,
                createdAt = currentSetEntity.createdAt,
                updatedAt = now,
            )

            workoutExerciseSetDao.updateSet(
                updatedSet.toEntity(),
            )

            workoutExerciseDao.updateWorkoutExercise(
                workoutExercise.copy(
                    updatedAt = now,
                ),
            )

            workoutDao.touchWorkout(
                id = workoutExercise.workoutId,
                updatedAt = now,
            )
        }
    }

    override suspend fun updateWorkoutExerciseChecked(
        workoutExerciseId: Long,
        isChecked: Boolean,
    ) {
        val now = System.currentTimeMillis()

        database.withTransaction {
            val workoutExercise = workoutExerciseDao.getWorkoutExerciseById(workoutExerciseId)
                ?: return@withTransaction

            workoutExerciseDao.updateCheckedState(
                id = workoutExerciseId,
                isChecked = isChecked,
                checkedAt = if (isChecked) now else null,
                updatedAt = now,
            )

            workoutDao.touchWorkout(
                id = workoutExercise.workoutId,
                updatedAt = now,
            )
        }
    }

    override suspend fun resetWorkoutCheckmarks(
        workoutId: Long,
    ) {
        val now = System.currentTimeMillis()

        database.withTransaction {
            workoutExerciseDao.resetWorkoutCheckmarks(
                workoutId = workoutId,
                updatedAt = now,
            )

            workoutDao.touchWorkout(
                id = workoutId,
                updatedAt = now,
            )
        }
    }
}