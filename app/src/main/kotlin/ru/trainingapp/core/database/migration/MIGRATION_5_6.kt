package ru.trainingapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workout_exercise_variant_selections (
                workoutExerciseId INTEGER NOT NULL,
                exerciseDefinitionId INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                PRIMARY KEY(workoutExerciseId),
                FOREIGN KEY(workoutExerciseId)
                    REFERENCES workout_exercises(id)
                    ON DELETE CASCADE,
                FOREIGN KEY(exerciseDefinitionId)
                    REFERENCES exercise_definitions(id)
                    ON DELETE NO ACTION
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_workout_exercise_variant_selections_exerciseDefinitionId
            ON workout_exercise_variant_selections(exerciseDefinitionId)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workout_exercise_variant_sets (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                workoutExerciseId INTEGER NOT NULL,
                exerciseDefinitionId INTEGER NOT NULL,
                setNumber INTEGER NOT NULL,
                reps INTEGER NOT NULL,
                loadType TEXT NOT NULL,
                weightValue REAL,
                weightUnit TEXT,
                durationSeconds INTEGER,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                FOREIGN KEY(workoutExerciseId)
                    REFERENCES workout_exercises(id)
                    ON DELETE CASCADE,
                FOREIGN KEY(exerciseDefinitionId)
                    REFERENCES exercise_definitions(id)
                    ON DELETE NO ACTION
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_workout_exercise_variant_sets_workoutExerciseId
            ON workout_exercise_variant_sets(workoutExerciseId)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_workout_exercise_variant_sets_exerciseDefinitionId
            ON workout_exercise_variant_sets(exerciseDefinitionId)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_workout_exercise_variant_sets_workoutExerciseId_exerciseDefinitionId_setNumber
            ON workout_exercise_variant_sets(workoutExerciseId, exerciseDefinitionId, setNumber)
            """.trimIndent()
        )
    }
}
