package ru.trainingapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE workout_exercises ADD COLUMN sectionId TEXT"
        )
        db.execSQL(
            "ALTER TABLE workout_exercises ADD COLUMN sectionName TEXT"
        )
    }
}
