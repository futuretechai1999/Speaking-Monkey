package com.example

import com.example.speaklingo.data.model.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class StreakTrackerUnitTest {

    private val testTimeZone = TimeZone.getTimeZone("UTC")

    @Test
    fun testFirstPracticeSessionStartsStreakAtOne() {
        val now = 1700000000000L // arbitrary fixed time
        val newStreak = StreakCalculator.calculateNewStreak(
            lastCompletionDate = 0L,
            newCompletionDate = now,
            currentStreak = 0,
            timeZone = testTimeZone
        )
        assertEquals(1, newStreak)
    }

    @Test
    fun testSameCalendarDayDoesNotDoubleIncrementStreak() {
        val morning = 1700035200000L // 08:00 UTC
        val evening = morning + (4 * 3600 * 1000L) // 12:00 UTC same day

        val streak = StreakCalculator.calculateNewStreak(
            lastCompletionDate = morning,
            newCompletionDate = evening,
            currentStreak = 5,
            timeZone = testTimeZone
        )
        assertEquals("Practicing twice on the same day should maintain the streak", 5, streak)
    }

    @Test
    fun testConsecutiveDayIncrementsStreakByOne() {
        val day1 = 1700035200000L
        val day2 = day1 + (24 * 3600 * 1000L) // exactly 1 day later

        val streak = StreakCalculator.calculateNewStreak(
            lastCompletionDate = day1,
            newCompletionDate = day2,
            currentStreak = 3,
            timeZone = testTimeZone
        )
        assertEquals("Practicing on the consecutive day should increment streak by 1", 4, streak)
    }

    @Test
    fun testMissedDaysResetsStreakToOne() {
        val day1 = 1700035200000L
        val day4 = day1 + (3 * 24 * 3600 * 1000L) // 3 days later

        val streak = StreakCalculator.calculateNewStreak(
            lastCompletionDate = day1,
            newCompletionDate = day4,
            currentStreak = 12,
            timeZone = testTimeZone
        )
        assertEquals("Missing days should reset streak to 1", 1, streak)
    }

    @Test
    fun testIsPracticedTodayPredicate() {
        val now = 1700000000000L
        val sameDayEarlier = now - (2 * 3600 * 1000L)
        val yesterday = now - (26 * 3600 * 1000L)

        assertTrue(StreakCalculator.isPracticedToday(sameDayEarlier, now, testTimeZone))
        assertFalse(StreakCalculator.isPracticedToday(yesterday, now, testTimeZone))
        assertFalse(StreakCalculator.isPracticedToday(0L, now, testTimeZone))
    }

    @Test
    fun testIsStreakBrokenPredicate() {
        val now = 1700000000000L
        val yesterday = now - (24 * 3600 * 1000L)
        val twoDaysAgo = now - (49 * 3600 * 1000L)

        assertFalse(StreakCalculator.isStreakBroken(yesterday, now, testTimeZone))
        assertTrue(StreakCalculator.isStreakBroken(twoDaysAgo, now, testTimeZone))
        assertTrue(StreakCalculator.isStreakBroken(0L, now, testTimeZone))
    }

    @Test
    fun testMilestonesProgression() {
        val m3 = StreakCalculator.getNextMilestone(2)
        assertEquals(3, m3.targetDays)
        assertEquals("Streak Starter", m3.title)

        val m7 = StreakCalculator.getNextMilestone(3)
        assertEquals(7, m7.targetDays)
        assertEquals("Week Warrior", m7.title)

        val progress = StreakCalculator.getMilestoneProgress(5)
        assertTrue(progress in 0.0f..1.0f)
    }

    @Test
    fun testWeeklyStatusReturnsSevenDaysWithOneToday() {
        val now = 1700000000000L
        val weekly = StreakCalculator.getWeeklyStatus(
            completedTimestamps = listOf(now),
            now = now,
            timeZone = testTimeZone
        )

        assertEquals("Weekly breakdown must have exactly 7 days", 7, weekly.size)
        assertEquals("Exactly one day should be marked as today", 1, weekly.count { it.isToday })
        assertTrue("Today should be marked as completed", weekly.first { it.isToday }.isCompleted)
    }

    @Test
    fun testFormatLastCompletionDate() {
        val now = 1700000000000L
        val textToday = StreakCalculator.formatLastCompletionDate(now, now)
        assertTrue("Should mention Today", textToday.startsWith("Today"))

        val textNever = StreakCalculator.formatLastCompletionDate(0L, now)
        assertEquals("No lessons completed yet", textNever)
    }
}
