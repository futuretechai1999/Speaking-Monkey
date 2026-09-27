package com.example.speaklingo.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Weekly day status representation for the StreakTracker component.
 */
data class StreakDayStatus(
    val dayLetter: String, // "M", "T", "W", "T", "F", "S", "S"
    val dayName: String,   // "Mon", "Tue", ...
    val isCompleted: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean,
    val dateEpochDays: Long
)

/**
 * Milestone status for streak progression.
 */
data class StreakMilestone(
    val targetDays: Int,
    val title: String,
    val rewardGems: Int,
    val badgeEmoji: String
)

/**
 * Utility responsible for calculating consecutive practice days,
 * calendar-day boundary comparisons, and streak milestones from Room timestamps.
 */
object StreakCalculator {

    val milestones = listOf(
        StreakMilestone(3, "Streak Starter", 20, "🌱"),
        StreakMilestone(7, "Week Warrior", 50, "🔥"),
        StreakMilestone(14, "Two-Week Titan", 100, "⚡"),
        StreakMilestone(30, "Monthly Master", 250, "🏆"),
        StreakMilestone(50, "Half-Century Hero", 500, "👑"),
        StreakMilestone(100, "Century Legend", 1000, "💎")
    )

    /**
     * Converts a millisecond timestamp to local calendar epoch day (days since Jan 1, 1970).
     * Automatically adjusts for user's timezone to ensure midnight boundaries.
     */
    fun getCalendarDayEpoch(timestampMs: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        if (timestampMs <= 0L) return -1L
        val calendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = timestampMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis / (24 * 60 * 60 * 1000L)
    }

    /**
     * Calculates the updated streak count based on the previous lesson completion date
     * and the new lesson completion date.
     *
     * Rules:
     * 1. Same calendar day (diff == 0): User already practiced today. Streak remains the same.
     * 2. Consecutive day (diff == 1): Practiced next day. Streak increments by +1.
     * 3. Gap of 2+ days (diff > 1): Missed practice day(s). Streak resets to 1 (new streak).
     * 4. Initial/unrecorded practice (lastCompletionDate <= 0): Starts at 1.
     */
    fun calculateNewStreak(
        lastCompletionDate: Long,
        newCompletionDate: Long,
        currentStreak: Int,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Int {
        if (lastCompletionDate <= 0L) return 1

        val lastDay = getCalendarDayEpoch(lastCompletionDate, timeZone)
        val currentDay = getCalendarDayEpoch(newCompletionDate, timeZone)
        val diffDays = currentDay - lastDay

        return when {
            diffDays == 0L -> currentStreak.coerceAtLeast(1)
            diffDays == 1L -> currentStreak + 1
            diffDays > 1L -> 1
            else -> currentStreak // Clock skew protection
        }
    }

    /**
     * Checks if the user has already completed a lesson on the current calendar day.
     */
    fun isPracticedToday(
        lastCompletionDate: Long,
        now: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): Boolean {
        if (lastCompletionDate <= 0L) return false
        return getCalendarDayEpoch(lastCompletionDate, timeZone) == getCalendarDayEpoch(now, timeZone)
    }

    /**
     * Checks if the streak has been broken (more than 1 full calendar day has passed since last practice).
     */
    fun isStreakBroken(
        lastCompletionDate: Long,
        now: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): Boolean {
        if (lastCompletionDate <= 0L) return true
        val lastDay = getCalendarDayEpoch(lastCompletionDate, timeZone)
        val today = getCalendarDayEpoch(now, timeZone)
        return (today - lastDay) > 1L
    }

    /**
     * Finds the next upcoming milestone for the user's current streak.
     */
    fun getNextMilestone(currentStreak: Int): StreakMilestone {
        return milestones.firstOrNull { it.targetDays > currentStreak }
            ?: milestones.last()
    }

    /**
     * Computes the progress fraction (0.0f to 1.0f) towards the next milestone.
     */
    fun getMilestoneProgress(currentStreak: Int): Float {
        val next = getNextMilestone(currentStreak)
        val prevTarget = milestones.lastOrNull { it.targetDays <= currentStreak }?.targetDays ?: 0
        val range = (next.targetDays - prevTarget).coerceAtLeast(1)
        val progress = (currentStreak - prevTarget).coerceAtLeast(0)
        return (progress.toFloat() / range.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Generates a 7-day weekly status (Monday through Sunday) for the current week.
     * Matches against all completed lesson timestamps from Room database.
     */
    fun getWeeklyStatus(
        completedTimestamps: List<Long>,
        now: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): List<StreakDayStatus> {
        val completedDayEpochs = completedTimestamps
            .map { getCalendarDayEpoch(it, timeZone) }
            .toSet()

        val calendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = now
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Move to Monday of current week
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = (currentDayOfWeek - Calendar.MONDAY + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)

        val todayEpoch = getCalendarDayEpoch(now, timeZone)
        val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        return (0..6).map { i ->
            val dayEpoch = calendar.timeInMillis / (24 * 60 * 60 * 1000L)
            val isCompleted = completedDayEpochs.contains(dayEpoch)
            val isToday = dayEpoch == todayEpoch
            val isFuture = dayEpoch > todayEpoch

            val status = StreakDayStatus(
                dayLetter = dayLetters[i],
                dayName = dayNames[i],
                isCompleted = isCompleted,
                isToday = isToday,
                isFuture = isFuture,
                dateEpochDays = dayEpoch
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            status
        }
    }

    /**
     * Formats the last lesson completion timestamp into user-friendly text.
     */
    fun formatLastCompletionDate(timestampMs: Long, now: Long = System.currentTimeMillis()): String {
        if (timestampMs <= 0L) return "No lessons completed yet"

        val lastDay = getCalendarDayEpoch(timestampMs)
        val today = getCalendarDayEpoch(now)
        val diff = today - lastDay

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val timeStr = timeFormat.format(Date(timestampMs))

        return when (diff) {
            0L -> "Today at $timeStr"
            1L -> "Yesterday at $timeStr"
            else -> {
                val dateFormat = SimpleDateFormat("MMM d 'at' h:mm a", Locale.getDefault())
                dateFormat.format(Date(timestampMs))
            }
        }
    }
}
