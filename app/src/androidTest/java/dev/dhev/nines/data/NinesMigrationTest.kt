package dev.dhev.nines.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dhev.nines.data.local.NinesDatabase
import dev.dhev.nines.data.local.ninesMigrations
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Builds a database file at an old schema version from the DDL in app/schemas, upgrades it through
 * Room with the real migrations, and lets Room validate the result against the current entities.
 */
@RunWith(AndroidJUnit4::class)
class NinesMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun deleteLeftovers() {
        context.deleteDatabase(DB)
    }

    @After
    fun cleanUp() {
        context.deleteDatabase(DB)
    }

    @Test
    fun versionOneDataSurvivesEveryUpgrade() {
        createDatabase(version = 1, habitsDdl = HABITS_V1) {
            execSQL(
                "INSERT INTO habits (id, uuid, name, type, unit, color, position, archivedAt, createdAt) " +
                    "VALUES (1, 'u-1', 'Read', 'BOOLEAN', NULL, -16777216, 0, NULL, 1000)",
            )
            execSQL(
                "INSERT INTO schedule_versions (habitId, effectiveFromDate, targetNumerator, targetDenominator, " +
                    "targetValue, targetKind) VALUES (1, 20000, 3, 7, NULL, NULL)",
            )
            execSQL(
                "INSERT INTO entries (habitId, date, state, numericValue, notes, tzOffsetMinutes, createdAt, updatedAt) " +
                    "VALUES (1, 20001, 'COMPLETED', NULL, NULL, 330, 2000, 2000)",
            )
        }

        val db = openThroughRoom()

        db.query("SELECT name, iconKey, backgroundStyleKey FROM habits").use {
            assertTrue(it.moveToFirst())
            assertEquals("Read", it.getString(0))
            assertEquals("fitness", it.getString(1))
            assertTrue(it.isNull(2))
        }
        db.query("SELECT targetNumerator, targetDenominator FROM schedule_versions").use {
            assertTrue(it.moveToFirst())
            assertEquals(3, it.getInt(0))
            assertEquals(7, it.getInt(1))
        }
        db.query("SELECT state FROM entries").use {
            assertTrue(it.moveToFirst())
            assertEquals("COMPLETED", it.getString(0))
        }
        db.query("SELECT COUNT(*) FROM reminders").use {
            assertTrue(it.moveToFirst())
            assertEquals(0, it.getInt(0))
        }
    }

    @Test
    fun remindersAreRemovedWithTheirHabitAfterUpgrading() {
        createDatabase(version = 3, habitsDdl = HABITS_V3) {
            execSQL(
                "INSERT INTO habits (id, uuid, name, type, unit, color, iconKey, backgroundStyleKey, position, archivedAt, createdAt) " +
                    "VALUES (1, 'u-1', 'Read', 'BOOLEAN', NULL, -16777216, 'book', NULL, 0, NULL, 1000)",
            )
        }

        val db = openThroughRoom()
        db.execSQL("INSERT INTO reminders (habitId, localTime) VALUES (1, 32400000000000)")
        db.execSQL("DELETE FROM habits WHERE id = 1")

        db.query("SELECT COUNT(*) FROM reminders").use {
            assertTrue(it.moveToFirst())
            assertEquals(0, it.getInt(0))
        }
        db.query("SELECT iconKey FROM habits").use { assertFalse(it.moveToFirst()) }
    }

    private fun openThroughRoom(): SupportSQLiteDatabase {
        val room = Room
            .databaseBuilder(context, NinesDatabase::class.java, DB)
            .addMigrations(*ninesMigrations)
            .build()
        // Opening runs the migrations, then Room validates the schema and throws on any mismatch.
        return room.openHelper.writableDatabase
    }

    private fun createDatabase(
        version: Int,
        habitsDdl: String,
        seed: SQLiteDatabase.() -> Unit,
    ) {
        val helper = object : SQLiteOpenHelper(context, DB, null, version) {
            override fun onConfigure(db: SQLiteDatabase) {
                db.setForeignKeyConstraintsEnabled(true)
            }

            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL(habitsDdl)
                db.execSQL(SCHEDULE_VERSIONS)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_schedule_versions_habitId` ON `schedule_versions` (`habitId`)")
                db.execSQL(ENTRIES)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_entries_habitId` ON `entries` (`habitId`)")
            }

            override fun onUpgrade(
                db: SQLiteDatabase,
                oldVersion: Int,
                newVersion: Int,
            ) = Unit
        }
        helper.writableDatabase.use { it.seed() }
        helper.close()
    }

    private companion object {
        const val DB = "migration-test"

        const val HABITS_V1 =
            "CREATE TABLE IF NOT EXISTS `habits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uuid` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, `type` TEXT NOT NULL, `unit` TEXT, `color` INTEGER NOT NULL, " +
                "`position` INTEGER NOT NULL, `archivedAt` INTEGER, `createdAt` INTEGER NOT NULL)"

        const val HABITS_V3 =
            "CREATE TABLE IF NOT EXISTS `habits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uuid` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, `type` TEXT NOT NULL, `unit` TEXT, `color` INTEGER NOT NULL, " +
                "`iconKey` TEXT NOT NULL, `backgroundStyleKey` TEXT, `position` INTEGER NOT NULL, " +
                "`archivedAt` INTEGER, `createdAt` INTEGER NOT NULL)"

        const val SCHEDULE_VERSIONS =
            "CREATE TABLE IF NOT EXISTS `schedule_versions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`habitId` INTEGER NOT NULL, `effectiveFromDate` INTEGER NOT NULL, `targetNumerator` INTEGER NOT NULL, " +
                "`targetDenominator` INTEGER NOT NULL, `targetValue` REAL, `targetKind` TEXT, " +
                "FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"

        const val ENTRIES =
            "CREATE TABLE IF NOT EXISTS `entries` (`habitId` INTEGER NOT NULL, `date` INTEGER NOT NULL, " +
                "`state` TEXT NOT NULL, `numericValue` REAL, `notes` TEXT, `tzOffsetMinutes` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`habitId`, `date`), " +
                "FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
    }
}
