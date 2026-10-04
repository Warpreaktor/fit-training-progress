package ru.trainingapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE workouts ADD COLUMN targetDurationMinutes INTEGER"
        )
        db.execSQL(
            "ALTER TABLE workouts ADD COLUMN timerStartedAt INTEGER"
        )
        db.execSQL(
            "ALTER TABLE workouts ADD COLUMN timerElapsedMillis INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "ALTER TABLE workouts ADD COLUMN timerIsFinished INTEGER NOT NULL DEFAULT 0"
        )
    }
}
