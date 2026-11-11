package dev.dhev.nines.debug

import dev.dhev.nines.data.DemoData
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.HabitDao
import dev.dhev.nines.data.local.ScheduleVersionDao
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.ScheduleVersionEntity
import dev.dhev.nines.data.local.entity.TargetKind
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

/**
 * Nine habits with histories of different shapes (long streaks, a broken streak, numeric, weekly
 * and at-most targets), inserted in-process for manual testing in debug builds.
 */
class DemoDataSeeder(
    private val repository: HabitRepository,
    private val habitDao: HabitDao,
    private val scheduleVersionDao: ScheduleVersionDao,
) : DemoData {
    override val isAvailable = true

    override suspend fun seed() {
        val today = LocalDate.now()
        val random = Random(42)
        fun daysAgo(n: Long) = today.minusDays(n)
        fun createdAt(daysAgoCreated: Long): Instant = daysAgo(daysAgoCreated).atStartOfDay(ZoneId.systemDefault()).toInstant()

        suspend fun addHabit(
            name: String,
            type: HabitType,
            unit: String?,
            color: Int,
            iconKey: String,
            backgroundStyleKey: String?,
            createdDaysAgo: Long,
            targetNumerator: Int,
            targetDenominator: Int,
            targetValue: Double?,
            targetKind: TargetKind?,
        ): Long {
            val habitId = habitDao.insert(
                HabitEntity(
                    name = name,
                    type = type,
                    unit = unit,
                    color = color,
                    iconKey = iconKey,
                    backgroundStyleKey = backgroundStyleKey,
                    position = habitDao.nextPosition(),
                    createdAt = createdAt(createdDaysAgo),
                ),
            )
            scheduleVersionDao.insert(
                ScheduleVersionEntity(
                    habitId = habitId,
                    effectiveFromDate = daysAgo(createdDaysAgo),
                    targetNumerator = targetNumerator,
                    targetDenominator = targetDenominator,
                    targetValue = targetValue,
                    targetKind = targetKind,
                ),
            )
            return habitId
        }

        // 1. Meditate — daily, ~9.5 months old (crosses a year boundary so the calendar's sticky
        // year indicator has something to actually demonstrate), 21-day streak up to today.
        val meditateId = addHabit("Meditate", HabitType.BOOLEAN, null, 0xFF00897B.toInt(), "meditate", "rings", 290, 1, 1, null, null)
        for (daysAgo in 0 until 290) {
            if (daysAgo < 21) {
                repository.toggleCompletion(meditateId, daysAgo(daysAgo.toLong()))
            } else if (random.nextFloat() < 0.7f) {
                repository.toggleCompletion(meditateId, daysAgo(daysAgo.toLong()))
            } else if (random.nextFloat() < 0.5f) {
                repository.setSkipped(meditateId, daysAgo(daysAgo.toLong()))
            }
        }

        // 2. Read — numerical, 20 pages/day target, inconsistent, in-progress today.
        val readId = addHabit("Read", HabitType.NUMERICAL, "pages", 0xFFFB8C00.toInt(), "book", "waves", 45, 1, 1, 20.0, TargetKind.AT_LEAST)
        for (daysAgo in 1 until 45) {
            val r = random.nextFloat()
            if (r < 0.45f) {
                repository.setNumericEntry(readId, daysAgo(daysAgo.toLong()), listOf(20, 25, 30, 22, 40).random(random).toDouble())
            } else if (r < 0.6f) {
                repository.setNumericEntry(readId, daysAgo(daysAgo.toLong()), listOf(5, 8, 12, 15).random(random).toDouble())
            }
        }
        repository.setNumericEntry(readId, today, 12.0)

        // 3. Gym — 3x/week, medium score, recent 3-day active streak.
        val gymId = addHabit("Gym", HabitType.BOOLEAN, null, 0xFF8E24AA.toInt(), "fitness", "zigzag", 50, 3, 7, null, null)
        for (daysAgo in 3 until 50) {
            if (random.nextFloat() < 0.35f) repository.toggleCompletion(gymId, daysAgo(daysAgo.toLong()))
        }
        for (daysAgo in 0 until 3) repository.toggleCompletion(gymId, daysAgo(daysAgo.toLong()))

        // 4. No Sugar — just started, short streak.
        val noSugarId = addHabit("No Sugar", HabitType.BOOLEAN, null, 0xFFD81B60.toInt(), "noSmoking", "sparkle", 4, 1, 1, null, null)
        repository.toggleCompletion(noSugarId, daysAgo(3))
        repository.setSkipped(noSugarId, daysAgo(2))
        repository.toggleCompletion(noSugarId, daysAgo(1))
        repository.toggleCompletion(noSugarId, today)

        // 5. Journal — long history, broken streak (missed last 4 days), decaying score.
        val journalId = addHabit("Journal", HabitType.BOOLEAN, null, 0xFF43A047.toInt(), "checklist", null, 70, 1, 1, null, null)
        for (daysAgo in 4 until 70) {
            if (random.nextFloat() < 0.55f) repository.toggleCompletion(journalId, daysAgo(daysAgo.toLong()))
        }

        // 6. Water — 8 glasses/day, ~4.5 months old, solid recent streak.
        val waterId = addHabit("Water", HabitType.NUMERICAL, "glasses", 0xFF039BE5.toInt(), "water", "peaks", 130, 1, 1, 8.0, TargetKind.AT_LEAST)
        for (daysAgo in 0 until 130) {
            if (daysAgo < 6) {
                repository.setNumericEntry(waterId, daysAgo(daysAgo.toLong()), listOf(8, 9, 10).random(random).toDouble())
            } else {
                val r = random.nextFloat()
                if (r < 0.5f) {
                    repository.setNumericEntry(waterId, daysAgo(daysAgo.toLong()), listOf(8, 9).random(random).toDouble())
                } else if (r < 0.7f) {
                    repository.setNumericEntry(waterId, daysAgo(daysAgo.toLong()), listOf(3, 5, 6).random(random).toDouble())
                }
            }
        }

        // 7. Sleep before 11 — ~6.5 months old, near-perfect record, second long-history habit.
        val sleepId = addHabit("Sleep before 11", HabitType.BOOLEAN, null, 0xFF3949AB.toInt(), "sleep", "dots", 195, 1, 1, null, null)
        for (daysAgo in 0 until 195) {
            if (random.nextFloat() < 0.93f) {
                repository.toggleCompletion(sleepId, daysAgo(daysAgo.toLong()))
            } else if (random.nextFloat() < 0.5f) {
                repository.setSkipped(sleepId, daysAgo(daysAgo.toLong()))
            }
        }

        // 8. Learn Spanish — every-2-days, brand new, low data.
        val spanishId = addHabit("Learn Spanish", HabitType.NUMERICAL, "min", 0xFFFFB300.toInt(), "language", "leaves", 2, 1, 2, 15.0, TargetKind.AT_LEAST)
        repository.setNumericEntry(spanishId, daysAgo(1), 15.0)

        // 9. Screen Time — AT_MOST target, the only demo habit exercising that path.
        val screenTimeId = addHabit("Screen Time", HabitType.NUMERICAL, "min", 0xFF00ACC1.toInt(), "phone", "arc", 25, 1, 1, 120.0, TargetKind.AT_MOST)
        for (daysAgo in 0 until 25) {
            repository.setNumericEntry(screenTimeId, daysAgo(daysAgo.toLong()), listOf(60, 90, 110, 130, 150, 180).random(random).toDouble())
        }
    }
}
