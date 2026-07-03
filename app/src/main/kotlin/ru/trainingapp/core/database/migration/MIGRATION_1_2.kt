package ru.trainingapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS workout_tag_cross_refs")
        db.execSQL("DROP TABLE IF EXISTS tags")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tags (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                color TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_tags_name 
            ON tags(name)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workout_tag_cross_refs (
                workoutId INTEGER NOT NULL,
                tagId INTEGER NOT NULL,
                PRIMARY KEY(workoutId, tagId),
                FOREIGN KEY(workoutId) REFERENCES workouts(id) ON DELETE CASCADE,
                FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_workout_tag_cross_refs_workoutId 
            ON workout_tag_cross_refs(workoutId)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_workout_tag_cross_refs_tagId 
            ON workout_tag_cross_refs(tagId)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS exercise_definition_tag_cross_refs (
                exerciseDefinitionId INTEGER NOT NULL,
                tagId INTEGER NOT NULL,
                PRIMARY KEY(exerciseDefinitionId, tagId),
                FOREIGN KEY(exerciseDefinitionId) REFERENCES exercise_definitions(id) ON DELETE CASCADE,
                FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_exercise_definition_tag_cross_refs_exerciseDefinitionId 
            ON exercise_definition_tag_cross_refs(exerciseDefinitionId)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_exercise_definition_tag_cross_refs_tagId 
            ON exercise_definition_tag_cross_refs(tagId)
            """.trimIndent()
        )
    }
}