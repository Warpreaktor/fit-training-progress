package ru.trainingapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS exercise_images (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                exerciseDefinitionId INTEGER NOT NULL,
                uri TEXT NOT NULL,
                sortOrder INTEGER NOT NULL,
                isCover INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                FOREIGN KEY(exerciseDefinitionId)
                    REFERENCES exercise_definitions(id)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_exercise_images_exerciseDefinitionId
            ON exercise_images(exerciseDefinitionId)
            """.trimIndent()
        )
    }
}