package com.example.speaklingo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.speaklingo.data.local.UserProfileEntity
import com.example.speaklingo.data.model.StreakCalculator
import com.example.speaklingo.data.model.StreakDayStatus
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.DuolingoGreenLight
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FireOrangeDark
import com.example.ui.theme.FireOrangeLight
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GoldYellowDark
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark

/**
 * StreakTracker: Displays user practice progress, consecutive days streak,
 * 7-day weekly calendar breakdown, and last lesson completion date stored in local Room database.
 */
@Composable
fun StreakTracker(
    profile: UserProfileEntity?,
    completedTimestamps: List<Long> = emptyList(),
    onQuickPractice: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val streakDays = profile?.streakDays ?: 0
    val lastPracticeDate = profile?.lastPracticeDate ?: 0L
    val completedCount = profile?.completedLessonsCount ?: 0
    val xp = profile?.xp ?: 0

    val isPracticedToday = remember(lastPracticeDate) {
        StreakCalculator.isPracticedToday(lastPracticeDate)
    }

    val isStreakBroken = remember(lastPracticeDate) {
        StreakCalculator.isStreakBroken(lastPracticeDate)
    }

    val weeklyStatus = remember(completedTimestamps, lastPracticeDate) {
        // Merge completedTimestamps with lastPracticeDate if present
        val allTimestamps = if (lastPracticeDate > 0L && !completedTimestamps.contains(lastPracticeDate)) {
            completedTimestamps + lastPracticeDate
        } else {
            completedTimestamps
        }
        StreakCalculator.getWeeklyStatus(allTimestamps)
    }

    val nextMilestone = remember(streakDays) {
        StreakCalculator.getNextMilestone(streakDays)
    }

    val milestoneProgress = remember(streakDays) {
        StreakCalculator.getMilestoneProgress(streakDays)
    }

    val formattedLastPractice = remember(lastPracticeDate) {
        StreakCalculator.formatLastCompletionDate(lastPracticeDate)
    }

    // Flame pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "streak_flame_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("streak_tracker_component"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Streak Header with Animated Flame
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Flame Icon Container with gradient background
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        GoldYellow,
                                        FireOrange,
                                        FireOrangeDark
                                    )
                                )
                            )
                            .scale(if (isPracticedToday) flameScale else 1.0f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔥",
                            fontSize = 32.sp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$streakDays",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = FireOrange
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (streakDays == 1) "Day Streak" else "Days Streak",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Practice status chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPracticedToday) DuolingoGreenLight.copy(alpha = 0.25f) else FireOrangeLight.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = if (isPracticedToday) "✓ Practiced Today" else "⏳ Practice pending today",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isPracticedToday) DuolingoGreenDark else FireOrangeDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Streak Shield Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Streak Shield Active",
                        tint = DuolingoGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 2. 7-Day Weekly Calendar Bar (Mon-Sun)
            WeeklyCalendarRow(weeklyStatus = weeklyStatus)

            // 3. Milestone Progression Bar
            MilestoneProgressCard(
                currentStreak = streakDays,
                milestone = nextMilestone,
                progress = milestoneProgress
            )

            // 4. Detailed Room Database Stats Grid
            StreakStatsGrid(
                formattedLastPractice = formattedLastPractice,
                completedLessonsCount = completedCount,
                xp = xp
            )

            // 5. Quick Practice Button to Extend Streak
            if (onQuickPractice != null) {
                DuolingoButton(
                    text = if (isPracticedToday) "PRACTICE MORE (+10 XP) 🌟" else "PRACTICE NOW TO EXTEND STREAK 🔥",
                    onClick = onQuickPractice,
                    buttonColor = if (isPracticedToday) SpeakBlue else FireOrange,
                    shadowColor = if (isPracticedToday) SpeakBlueDark else FireOrangeDark,
                    testTag = "streak_quick_practice_button"
                )
            }
        }
    }
}

/**
 * 7-Day Weekly row visualizing practice continuity (Mon - Sun).
 */
@Composable
private fun WeeklyCalendarRow(weeklyStatus: List<StreakDayStatus>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Practice Path",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Mon — Sun",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weeklyStatus.forEach { day ->
                    WeeklyDayCircle(day = day)
                }
            }
        }
    }
}

@Composable
private fun WeeklyDayCircle(day: StreakDayStatus) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = day.dayLetter,
            fontSize = 11.sp,
            fontWeight = if (day.isToday) FontWeight.Black else FontWeight.Bold,
            color = if (day.isToday) FireOrange else MaterialTheme.colorScheme.onSurfaceVariant
        )

        val circleBg = when {
            day.isCompleted -> DuolingoGreen
            day.isToday -> FireOrange.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surface
        }

        val borderModifier = when {
            day.isToday && !day.isCompleted -> Modifier.border(2.dp, FireOrange, CircleShape)
            day.isCompleted -> Modifier.border(1.dp, DuolingoGreenDark, CircleShape)
            else -> Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(circleBg)
                .then(borderModifier),
            contentAlignment = Alignment.Center
        ) {
            when {
                day.isCompleted -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                day.isToday -> {
                    Text(
                        text = "🔥",
                        fontSize = 16.sp
                    )
                }
                day.isFuture -> {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

/**
 * Milestone progression card with dynamic goal target and rewards.
 */
@Composable
private fun MilestoneProgressCard(
    currentStreak: Int,
    milestone: com.example.speaklingo.data.model.StreakMilestone,
    progress: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GoldYellow.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldYellow.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = milestone.badgeEmoji, fontSize = 16.sp)
                    Text(
                        text = "Next Goal: ${milestone.title}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF9E6D00)
                    )
                }
                Text(
                    text = "$currentStreak / ${milestone.targetDays} Days",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF9E6D00)
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GoldYellowDark,
                trackColor = Color.White.copy(alpha = 0.6f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val remaining = (milestone.targetDays - currentStreak).coerceAtLeast(0)
                Text(
                    text = if (remaining == 0) "Milestone Unlocked! 🎉" else "$remaining days to unlock",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Reward: +${milestone.rewardGems} 💎",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = SpeakBlueDark
                )
            }
        }
    }
}

/**
 * Grid displaying metadata retrieved directly from the Room database.
 */
@Composable
private fun StreakStatsGrid(
    formattedLastPractice: String,
    completedLessonsCount: Int,
    xp: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Last completion date card
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "LAST LESSON",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = formattedLastPractice,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Total lessons completed card
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TOTAL COMPLETED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$completedLessonsCount Lessons",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuolingoGreenDark
                )
            }
        }
    }
}

/**
 * StreakTrackerDialog: Modal dialog displayable from anywhere (e.g. TopStatusBar flame pill).
 */
@Composable
fun StreakTrackerDialog(
    profile: UserProfileEntity?,
    completedTimestamps: List<Long> = emptyList(),
    onQuickPractice: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("streak_tracker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Practice Streak 🔥",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Embedded StreakTracker component
                StreakTracker(
                    profile = profile,
                    completedTimestamps = completedTimestamps,
                    onQuickPractice = {
                        onDismiss()
                        onQuickPractice?.invoke()
                    }
                )
            }
        }
    }
}
