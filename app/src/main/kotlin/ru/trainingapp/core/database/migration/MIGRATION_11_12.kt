package ru.trainingapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE exercise_images ADD COLUMN cropZoom REAL NOT NULL DEFAULT 1.0"
        )
        database.execSQL(
            "ALTER TABLE exercise_images ADD COLUMN cropOffsetX REAL NOT NULL DEFAULT 0.0"
        )
        database.execSQL(
            "ALTER TABLE exercise_images ADD COLUMN cropOffsetY REAL NOT NULL DEFAULT 0.0"
        )
    }
}
