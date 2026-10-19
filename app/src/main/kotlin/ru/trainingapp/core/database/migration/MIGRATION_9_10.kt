package ru.trainingapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS exercise_definition_muscle_cross_refs (
                exerciseDefinitionId INTEGER NOT NULL,
                muscleCode TEXT NOT NULL,
                PRIMARY KEY(exerciseDefinitionId, muscleCode),
                FOREIGN KEY(exerciseDefinitionId)
                    REFERENCES exercise_definitions(id)
                    ON UPDATE NO ACTION
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_exercise_definition_muscle_cross_refs_exerciseDefinitionId
            ON exercise_definition_muscle_cross_refs(exerciseDefinitionId)
            """.trimIndent()
        )
    }
}
