package ru.trainingapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.model.Workout

interface WorkoutRepository {

    fun observeWorkouts(): Flow<List<Workout>>

    suspend fun createWorkout(
        name: String,
        description: String,
    ): Long

    suspend fun archiveWorkout(id: Long)
}