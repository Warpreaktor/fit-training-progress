package ru.trainingapp.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.trainingapp.core.data.exercise.RoomExerciseDefinitionRepository
import ru.trainingapp.core.data.workout.RoomWorkoutRepository
import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository
import ru.trainingapp.core.domain.repository.WorkoutRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseDefinitionRepository(
        implementation: RoomExerciseDefinitionRepository,
    ): ExerciseDefinitionRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        implementation: RoomWorkoutRepository,
    ): WorkoutRepository
}