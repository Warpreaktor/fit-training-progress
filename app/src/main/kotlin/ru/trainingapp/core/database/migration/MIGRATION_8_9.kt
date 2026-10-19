package ru.trainingapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE workout_exercises ADD COLUMN executionMethod TEXT"
        )
        db.execSQL(
            "ALTER TABLE workout_exercise_progress_points ADD COLUMN executionMethodSnapshot TEXT"
        )
    }
}
