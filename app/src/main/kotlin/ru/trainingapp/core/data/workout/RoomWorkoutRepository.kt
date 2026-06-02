package ru.trainingapp.core.data.workout

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.trainingapp.core.database.dao.WorkoutDao
import ru.trainingapp.core.database.entity.WorkoutEntity
import ru.trainingapp.core.domain.repository.WorkoutRepository
import ru.trainingapp.core.model.Workout
import javax.inject.Inject

class RoomWorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
) : WorkoutRepository {

    override fun observeWorkouts(): Flow<List<Workout>> {
        return workoutDao
            .observeWorkoutListItems()
            .map { items -> items.map { it.toDomain() } }
    }

    override suspend fun createWorkout(
        name: String,
        description: String,
    ): Long {
        val now = System.currentTimeMillis()
        return workoutDao.insertWorkout(
            WorkoutEntity(
                name = name.trim(),
                description = description,
                isLocked = false,
                isArchived = false,
                archivedAt = null,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    override suspend fun archiveWorkout(id: Long) {
        workoutDao.archiveWorkout(
            id = id,
            archivedAt = System.currentTimeMillis(),
        )
    }
}