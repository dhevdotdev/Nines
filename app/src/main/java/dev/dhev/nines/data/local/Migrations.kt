package dev.dhev.nines.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Habits created before icons existed get the same icon the editor offers first. */
private const val DEFAULT_ICON_KEY = "fitness"

private val migration1To2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE habits ADD COLUMN iconKey TEXT NOT NULL DEFAULT '$DEFAULT_ICON_KEY'")
    }
}

private val migration2To3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE habits ADD COLUMN backgroundStyleKey TEXT")
    }
}

private val migration3To4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `reminders` (`habitId` INTEGER NOT NULL, `localTime` INTEGER NOT NULL, " +
                "`activeDate` INTEGER, `snoozedUntil` INTEGER, `lastAlertedAt` INTEGER, PRIMARY KEY(`habitId`), " +
                "FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
    }
}

/** Every schema upgrade since version 1, in order. The exported schemas in app/schemas are the source of truth. */
val ninesMigrations: Array<Migration> = arrayOf(migration1To2, migration2To3, migration3To4)
