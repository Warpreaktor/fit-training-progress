package ru.trainingapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE exercise_definition_muscle_cross_refs
            ADD COLUMN loadType TEXT NOT NULL DEFAULT 'PRIMARY'
            """.trimIndent()
        )
    }
}
