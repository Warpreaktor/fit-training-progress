package ru.trainingapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE workouts
            ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        db.query(
            """
            SELECT id
            FROM workouts
            ORDER BY isArchived ASC, updatedAt DESC, id ASC
            """.trimIndent()
        ).use { cursor ->
            var sortOrder = 0

            while (cursor.moveToNext()) {
                val workoutId = cursor.getLong(0)

                db.execSQL(
                    """
                    UPDATE workouts
                    SET sortOrder = ?
                    WHERE id = ?
                    """.trimIndent(),
                    arrayOf<Any>(sortOrder, workoutId),
                )

                sortOrder++
            }
        }
    }
}