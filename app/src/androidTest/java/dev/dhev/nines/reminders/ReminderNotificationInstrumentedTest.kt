package dev.dhev.nines.reminders

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.ReminderRepository
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitType
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class ReminderNotificationInstrumentedTest {
    private lateinit var context: Context
    private lateinit var habits: HabitRepository
    private lateinit var reminders: ReminderRepository
    private lateinit var coordinator: ReminderNotificationCoordinator
    private lateinit var scheduler: ReminderScheduler
    private val createdHabitIds = mutableListOf<Long>()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        uiAutomation.adoptShellPermissionIdentity("android.permission.GRANT_RUNTIME_PERMISSIONS")
        uiAutomation.executeShellCommand("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS").close()
        uiAutomation.dropShellPermissionIdentity()
        assumeTrue(NotificationManagerCompat.from(context).areNotificationsEnabled())
        habits = GlobalContext.get().get()
        reminders = GlobalContext.get().get()
        coordinator = GlobalContext.get().get()
        scheduler = GlobalContext.get().get()
    }

    @After
    fun tearDown() = runBlocking {
        GlobalContext.get().get<dev.dhev.nines.data.ReminderPreferencesStore>().setCombineSimultaneous(false)
        createdHabitIds.forEach {
            coordinator.cancel(it)
            scheduler.cancel(it)
            reminders.remove(it)
            habits.archiveHabit(it)
        }
        context.getSystemService(NotificationManager::class.java)
            .cancel(ReminderNotificationCoordinator.COMBINED_NOTIFICATION_ID)
    }

    @Test
    fun dueNotificationIsOngoingAndMarkDoneResolvesOriginalDate() = runBlocking {
        val habitId = createHabit("Reminder instrumentation boolean")
        reminders.configure(habitId, LocalTime.now().plusMinutes(1))

        coordinator.deliver(habitId)

        var notification: android.app.Notification? = null
        withTimeout(5_000) {
            while (true) {
                notification = context.getSystemService(NotificationManager::class.java)
                    .activeNotifications.firstOrNull { it.id == habitId.toInt() }
                    ?.notification
                if (notification != null) break
                delay(50)
            }
        }
        assertTrue(notification!!.flags and android.app.Notification.FLAG_ONGOING_EVENT != 0)

        ReminderActionReceiver.pendingIntent(context, habitId, ReminderActionReceiver.MARK_DONE).send()
        withTimeout(5_000) {
            while (habits.entryOn(habitId, java.time.LocalDate.now())?.state != EntryState.COMPLETED) {
                delay(50)
            }
        }
        assertEquals(0, context.getSystemService(NotificationManager::class.java)
            .activeNotifications.count { it.id == habitId.toInt() })
    }

    @Test
    fun sameMinutePreferenceUsesOneActionlessSummary() = runBlocking {
        val first = createHabit("Reminder instrumentation first")
        val second = createHabit("Reminder instrumentation second")
        val time = LocalTime.now().plusMinutes(1)
        reminders.configure(first, time)
        reminders.configure(second, time)
        val preferences = GlobalContext.get().get<dev.dhev.nines.data.ReminderPreferencesStore>()
        preferences.setCombineSimultaneous(true)

        coordinator.deliver(first)
        coordinator.deliver(second)

        val summary = context.getSystemService(NotificationManager::class.java)
            .activeNotifications.first { it.id == ReminderNotificationCoordinator.COMBINED_NOTIFICATION_ID }
        assertEquals(0, summary.notification.actions?.size ?: 0)
        assertEquals(0, context.getSystemService(NotificationManager::class.java)
            .activeNotifications.count { it.id == first.toInt() || it.id == second.toInt() })
        preferences.setCombineSimultaneous(false)
    }

    @Test
    fun deniedPermissionDoesNotDiscardConfiguredReminder() = runBlocking {
        val habitId = createHabit("Reminder instrumentation permission")
        reminders.configure(habitId, LocalTime.now().plusMinutes(1))
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        uiAutomation.adoptShellPermissionIdentity("android.permission.GRANT_RUNTIME_PERMISSIONS")
        uiAutomation.executeShellCommand("pm revoke ${context.packageName} android.permission.POST_NOTIFICATIONS").close()
        uiAutomation.dropShellPermissionIdentity()

        coordinator.deliver(habitId)

        assertTrue(reminders.get(habitId)?.activeDate != null)
        assertTrue(reminders.get(habitId)?.localTime != null)
        uiAutomation.adoptShellPermissionIdentity("android.permission.GRANT_RUNTIME_PERMISSIONS")
        uiAutomation.executeShellCommand("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS").close()
        uiAutomation.dropShellPermissionIdentity()
    }

    private suspend fun createHabit(name: String): Long {
        return habits.createHabit(
            name = name,
            type = HabitType.BOOLEAN,
            color = 0,
            iconKey = "default",
            scheduleEffectiveFrom = java.time.LocalDate.now(),
        ).also(createdHabitIds::add)
    }
}
