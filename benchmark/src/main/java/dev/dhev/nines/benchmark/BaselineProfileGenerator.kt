package dev.dhev.nines.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Drives the exact interaction that was measurably janky on-device: open the habit list, tap
 * into a detail screen, and go back. This is what actually fixes "the first time you open a
 * detail screen it stutters," not a loading spinner over it - the stutter was ART interpreting
 * this code cold; a shipped profile means it doesn't have to.
 *
 * A fresh install has no data, and the debug-only demo-seed long-press isn't available on the
 * benchmark build variant this runs against, so the profile run creates its own habit through
 * the real add-habit flow first - same screens, same code paths, no dependency on debug tooling.
 */
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = "dev.dhev.nines") {
        pressHome()
        startActivityAndWait()

        val addHabitButton = device.wait(Until.findObject(By.desc("Add habit")), 5_000)
        if (addHabitButton != null) {
            addHabitButton.click()
            val nameField = device.wait(Until.findObject(By.text("Name")), 5_000)
            nameField?.click()
            device
                .wait(Until.findObject(By.clazz("android.widget.EditText")), 3_000)
                ?.text = "Benchmark habit"
            device.findObject(By.text("Add habit"))?.click()
            device.waitForIdle()
        }

        val habitRow = device.wait(Until.findObject(By.textContains("Benchmark habit")), 5_000)
        habitRow?.click()
        device.wait(Until.hasObject(By.textContains("consistency")), 5_000)
        device.waitForIdle()

        device.pressBack()
        device.wait(Until.hasObject(By.desc("Add habit")), 5_000)
        device.waitForIdle()
    }
}
