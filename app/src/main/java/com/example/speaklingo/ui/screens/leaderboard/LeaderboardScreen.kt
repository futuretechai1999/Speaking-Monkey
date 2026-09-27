package com.example.speaklingo.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueLight

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val avatarEmoji: String,
    val xp: Int,
    val isCurrentUser: Boolean = false
)

@Composable
fun LeaderboardScreen(
    profile: UserProfileEntity?,
    modifier: Modifier = Modifier
) {
    val userXp = profile?.xp ?: 180

    val learners = listOf(
        LeaderboardUser(1, "Aarav Sharma", "🦁", 420),
        LeaderboardUser(2, "Priya Patel", "🦊", 350),
        LeaderboardUser(3, "Vikram Singh", "🐯", 290),
        LeaderboardUser(4, "You (Learner)", "🚀", userXp, isCurrentUser = true),
        LeaderboardUser(5, "Ananya Verma", "🐼", 175),
        LeaderboardUser(6, "Karan Mehta", "🐨", 160),
        LeaderboardUser(7, "Sneha Rao", "🦄", 140),
        LeaderboardUser(8, "Rohan Das", "🐶", 120),
        LeaderboardUser(9, "Meera Nair", "🐱", 95),
        LeaderboardUser(10, "Dev Malhotra", "🐻", 70)
    ).sortedByDescending { it.xp }
        .mapIndexed { index, user -> user.copy(rank = index + 1) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // League Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("league_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SpeakBlue)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🛡️", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Silver League",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Top 5 learners advance to the Gold League! 3 days left.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RANKING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "WEEKLY XP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
            }
        }

        itemsIndexed(learners) { _, learner ->
            val isPromotionZone = learner.rank <= 5
            val isUser = learner.isCurrentUser

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leaderboard_row_${learner.rank}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isUser -> DuolingoGreen.copy(alpha = 0.15f)
                        isPromotionZone -> MaterialTheme.colorScheme.surface
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                ),
                border = if (isUser) androidx.compose.foundation.BorderStroke(2.dp, DuolingoGreen) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Rank Number / Badge
                        Box(
                            modifier = Modifier.size(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (learner.rank) {
                                1 -> Text(text = "🥇", fontSize = 20.sp)
                                2 -> Text(text = "🥈", fontSize = 20.sp)
                                3 -> Text(text = "🥉", fontSize = 20.sp)
                                else -> Text(
                                    text = "${learner.rank}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (isPromotionZone) SpeakBlue else Color.Gray
                                )
                            }
                        }

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = learner.avatarEmoji, fontSize = 22.sp)
                        }

                        // Name
                        Column {
                            Text(
                                text = learner.name,
                                fontSize = 15.sp,
                                fontWeight = if (isUser) FontWeight.Black else FontWeight.Bold,
                                color = if (isUser) DuolingoGreenDark else MaterialTheme.colorScheme.onSurface
                            )
                            if (isPromotionZone) {
                                Text(
                                    text = "Promotion Zone 🟢",
                                    fontSize = 11.sp,
                                    color = DuolingoGreenDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // XP
                    Text(
                        text = "${learner.xp} XP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldYellow
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}
