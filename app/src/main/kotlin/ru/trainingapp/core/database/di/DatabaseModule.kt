package ru.trainingapp.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.trainingapp.core.database.DatabaseConfig
import ru.trainingapp.core.database.TrainingDatabase
import ru.trainingapp.core.database.migration.MIGRATION_1_2
import ru.trainingapp.core.database.migration.MIGRATION_2_3
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTrainingDatabase(
        @ApplicationContext context: Context,
    ): TrainingDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = TrainingDatabase::class.java,
            name = DatabaseConfig.DATABASE_NAME,
        )
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideExerciseDefinitionDao(database: TrainingDatabase) =
        database.exerciseDefinitionDao()

    @Provides
    fun provideWorkoutDao(database: TrainingDatabase) =
        database.workoutDao()

    @Provides
    fun provideWorkoutExerciseDao(database: TrainingDatabase) =
        database.workoutExerciseDao()

    @Provides
    fun provideWorkoutExerciseSetDao(database: TrainingDatabase) =
        database.workoutExerciseSetDao()

    @Provides
    fun providePendingWorkoutChangeDao(database: TrainingDatabase) =
        database.pendingWorkoutChangeDao()

    @Provides
    fun provideProgressDao(database: TrainingDatabase) =
        database.progressDao()

    @Provides
    fun provideTagDao(database: TrainingDatabase) =
        database.tagDao()

    @Provides
    fun provideExerciseAlternativeDao(database: TrainingDatabase) =
        database.exerciseAlternativeDao()
}