package ru.trainingapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS exercise_definition_alternative_cross_refs (
                exerciseDefinitionId INTEGER NOT NULL,
                alternativeExerciseDefinitionId INTEGER NOT NULL,
                PRIMARY KEY(exerciseDefinitionId, alternativeExerciseDefinitionId),
                FOREIGN KEY(exerciseDefinitionId) 
                    REFERENCES exercise_definitions(id) 
                    ON DELETE CASCADE,
                FOREIGN KEY(alternativeExerciseDefinitionId) 
                    REFERENCES exercise_definitions(id) 
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_exercise_definition_alternative_cross_refs_exerciseDefinitionId
            ON exercise_definition_alternative_cross_refs(exerciseDefinitionId)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_exercise_definition_alternative_cross_refs_alternativeExerciseDefinitionId
            ON exercise_definition_alternative_cross_refs(alternativeExerciseDefinitionId)
            """.trimIndent()
        )
    }
}