package com.example.speaklingo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speaklingo.data.local.UserProfileEntity
import com.example.ui.theme.FireOrange
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.HeartRed
import com.example.ui.theme.SpeakBlue

@Composable
fun TopStatusBar(
    profile: UserProfileEntity?,
    onHeartsClick: () -> Unit,
    onStreakClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val streak = profile?.streakDays ?: 0
    val gems = profile?.gems ?: 0
    val hearts = profile?.hearts ?: 5
    val xp = profile?.xp ?: 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Streak
        StatusPill(
            emoji = "🔥",
            label = "$streak",
            color = FireOrange,
            onClick = onStreakClick,
            testTag = "status_streak"
        )

        // Gems
        StatusPill(
            emoji = "💎",
            label = "$gems",
            color = SpeakBlue,
            testTag = "status_gems"
        )

        // XP
        StatusPill(
            emoji = "⚡",
            label = "$xp XP",
            color = GoldYellow,
            testTag = "status_xp"
        )

        // Hearts
        StatusPill(
            emoji = "❤️",
            label = "$hearts",
            color = HeartRed,
            onClick = onHeartsClick,
            testTag = "status_hearts"
        )
    }
}

@Composable
private fun StatusPill(
    emoji: String,
    label: String,
    color: Color,
    onClick: (() -> Unit)? = null,
    testTag: String
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = clickableModifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, fontSize = 16.sp)
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = color
            )
        }
    }
}
