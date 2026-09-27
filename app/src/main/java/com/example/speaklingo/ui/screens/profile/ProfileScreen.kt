package com.example.speaklingo.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.clickable
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speaklingo.data.local.UserProfileEntity
import com.example.speaklingo.data.repository.AiTutorRepository
import com.example.speaklingo.ui.components.StreakTracker
import com.example.speaklingo.ui.screens.leaderboard.LeaderboardScreen
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.FireOrange
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.HeartRed
import com.example.ui.theme.SpeakBlue

data class Quest(
    val title: String,
    val current: Int,
    val target: Int,
    val rewardXp: Int,
    val iconEmoji: String
)

data class Badge(
    val title: String,
    val desc: String,
    val iconEmoji: String,
    val isUnlocked: Boolean
)

@Composable
fun ProfileScreen(
    profile: UserProfileEntity?,
    aiTutorRepository: AiTutorRepository,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Profile, 1: Leaderboard

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selectedTab == 0) SpeakBlue else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Profile & Stats 👤",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selectedTab == 1) SpeakBlue else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Silver League 🛡️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (selectedTab == 1) {
            LeaderboardScreen(profile = profile)
            return
        }

        val streak = profile?.streakDays ?: 3
        val xp = profile?.xp ?: 180
        val gems = profile?.gems ?: 320
        val league = profile?.league ?: "Silver League"

    val dailyQuests = listOf(
        Quest("Speak 3 sentences in AI Chat", 2, 3, 20, "🎙️"),
        Quest("Earn 50 XP today", (xp % 50).coerceAtLeast(20), 50, 15, "⚡"),
        Quest("Practice in Fluency Lab", 1, 1, 10, "🎯")
    )

    val badges = listOf(
        Badge("First Words", "Completed first English lesson", "🌱", true),
        Badge("Streak Starter", "Achieved a 3-day speaking streak", "🔥", true),
        Badge("AI Conversation", "Chatted with Aria for 5 minutes", "🤖", true),
        Badge("Fluency Master", "Scored 90%+ in Speech Lab", "🏆", false)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // User Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_user_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(SpeakBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🚀", fontSize = 34.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "English Learner",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Joined September 2026 • Daily Explorer",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DuolingoGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Speaking Level: Intermediate",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuolingoGreenDark
                            )
                        }
                    }
                }
            }
        }

        // StreakTracker Progress & Consistency Component
        item {
            StreakTracker(profile = profile)
        }

        // Stats Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "STATISTICS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        emoji = "🔥",
                        value = "$streak Days",
                        label = "Day Streak",
                        color = FireOrange,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji = "⚡",
                        value = "$xp",
                        label = "Total XP",
                        color = GoldYellow,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        emoji = "🛡️",
                        value = league,
                        label = "Current League",
                        color = SpeakBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji = "💎",
                        value = "$gems",
                        label = "Gems Earned",
                        color = SpeakBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Daily Quests
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_quests_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Quests 🎯",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    dailyQuests.forEach { quest ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = quest.iconEmoji, fontSize = 16.sp)
                                    Text(
                                        text = quest.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${quest.current}/${quest.target}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (quest.current.toFloat() / quest.target.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = DuolingoGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Badges & Achievements
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_badges_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Badges & Achievements 🏅",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    badges.forEach { badge ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (badge.isUnlocked) GoldYellow.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = if (badge.isUnlocked) badge.iconEmoji else "🔒", fontSize = 20.sp)
                            }

                            Column {
                                Text(
                                    text = badge.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                                )
                                Text(
                                    text = badge.desc,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Gemini AI Engine Status
        item {
            val isAiActive = aiTutorRepository.isGeminiConfigured()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAiActive) DuolingoGreen.copy(alpha = 0.1f) else SpeakBlue.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "🤖", fontSize = 24.sp)
                    Column {
                        Text(
                            text = if (isAiActive) "Gemini 3.5 Flash Active" else "Smart Offline AI Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isAiActive) DuolingoGreenDark else SpeakBlue
                        )
                        Text(
                            text = if (isAiActive)
                                "Real-time grammar analysis, speech scoring, and adaptive tutor enabled."
                            else
                                "Built-in intelligent English tutor active. Add GEMINI_API_KEY in Secrets panel for live cloud model.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}
}

@Composable
private fun StatCard(
    emoji: String,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
