package dev.dhev.nines.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.dhev.nines.data.local.NinesDatabase
import dev.dhev.nines.data.local.ninesMigrations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NinesMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), NinesDatabase::class.java)

    @Test
    fun versionOneDataSurvivesEveryUpgrade() {
        helper.createDatabase(DB, 1).apply {
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
            close()
        }

        val db = helper.runMigrationsAndValidate(DB, 4, true, *ninesMigrations)

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
        helper.createDatabase(DB, 3).apply {
            execSQL(
                "INSERT INTO habits (id, uuid, name, type, unit, color, iconKey, backgroundStyleKey, position, archivedAt, createdAt) " +
                    "VALUES (1, 'u-1', 'Read', 'BOOLEAN', NULL, -16777216, 'book', NULL, 0, NULL, 1000)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(DB, 4, true, *ninesMigrations)
        db.execSQL("PRAGMA foreign_keys = ON")
        db.execSQL("INSERT INTO reminders (habitId, localTime) VALUES (1, 32400000000000)")
        db.execSQL("DELETE FROM habits WHERE id = 1")

        db.query("SELECT COUNT(*) FROM reminders").use {
            assertTrue(it.moveToFirst())
            assertEquals(0, it.getInt(0))
        }
        db.query("SELECT iconKey FROM habits").use { assertFalse(it.moveToFirst()) }
    }

    private companion object {
        const val DB = "migration-test"
    }
}
