package ru.trainingapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.trainingapp.core.database.converter.DatabaseConverters
import ru.trainingapp.core.database.dao.ExerciseDefinitionDao
import ru.trainingapp.core.database.dao.PendingWorkoutChangeDao
import ru.trainingapp.core.database.dao.ProgressDao
import ru.trainingapp.core.database.dao.TagDao
import ru.trainingapp.core.database.dao.WorkoutDao
import ru.trainingapp.core.database.dao.WorkoutExerciseDao
import ru.trainingapp.core.database.dao.WorkoutExerciseSetDao
import ru.trainingapp.core.database.entity.ExerciseDefinitionEntity
import ru.trainingapp.core.database.entity.PendingWorkoutChangeEntity
import ru.trainingapp.core.database.entity.TagEntity
import ru.trainingapp.core.database.entity.WorkoutEntity
import ru.trainingapp.core.database.entity.WorkoutExerciseEntity
import ru.trainingapp.core.database.entity.WorkoutExerciseProgressPointEntity
import ru.trainingapp.core.database.entity.WorkoutExerciseProgressSetEntity
import ru.trainingapp.core.database.entity.WorkoutExerciseSetEntity
import ru.trainingapp.core.database.entity.WorkoutTagCrossRefEntity

@Database(
    entities = [
        ExerciseDefinitionEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutExerciseSetEntity::class,
        PendingWorkoutChangeEntity::class,
        WorkoutExerciseProgressPointEntity::class,
        WorkoutExerciseProgressSetEntity::class,
        TagEntity::class,
        WorkoutTagCrossRefEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class TrainingDatabase : RoomDatabase() {

    abstract fun exerciseDefinitionDao(): ExerciseDefinitionDao

    abstract fun workoutDao(): WorkoutDao

    abstract fun workoutExerciseDao(): WorkoutExerciseDao

    abstract fun workoutExerciseSetDao(): WorkoutExerciseSetDao

    abstract fun pendingWorkoutChangeDao(): PendingWorkoutChangeDao

    abstract fun progressDao(): ProgressDao

    abstract fun tagDao(): TagDao
}