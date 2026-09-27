package com.example.speaklingo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.speaklingo.data.local.LessonProgressEntity
import com.example.speaklingo.data.model.Lesson
import com.example.speaklingo.data.model.UnitData
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.DuolingoGreenLight
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GoldYellowDark
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Status of a lesson node along the vertical roadmap path.
 */
enum class RoadmapNodeStatus {
    LOCKED,
    IN_PROGRESS,
    COMPLETED
}

/**
 * Data bundle representing level completion animation details.
 */
data class LevelCelebrationData(
    val lessonId: String,
    val lessonTitle: String,
    val xpEarned: Int = 25,
    val gemsEarned: Int = 10,
    val stars: Int = 3
)

/**
 * Confetti particle state for celebration animation.
 */
private data class ConfettiParticle(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Color,
    var rotation: Float,
    val rotationSpeed: Float,
    val shapeIsCircle: Boolean
)

/**
 * Maps a lesson to a representative Material ImageVector icon and emoji symbol.
 */
fun getLessonIconData(lesson: Lesson): Pair<ImageVector, String> {
    val titleLower = lesson.title.lowercase()
    return when {
        titleLower.contains("greet") || titleLower.contains("intro") -> Icons.Default.RecordVoiceOver to "🗣️"
        titleLower.contains("habit") || titleLower.contains("routine") -> Icons.Default.Schedule to "⏰"
        titleLower.contains("question") || titleLower.contains("clarif") -> Icons.Default.QuestionAnswer to "❓"
        titleLower.contains("cafe") || titleLower.contains("food") -> Icons.Default.LocalCafe to "☕"
        titleLower.contains("hobbi") || titleLower.contains("time") -> Icons.Default.SportsTennis to "🎧"
        titleLower.contains("interview") || titleLower.contains("career") || titleLower.contains("job") -> Icons.Default.Work to "💼"
        titleLower.contains("grammar") -> Icons.Default.School to "✏️"
        else -> Icons.Default.School to "📖"
    }
}

/**
 * Complete Vertical Learning Roadmap Component.
 *
 * Displays a gamified winding vertical adventure path of learning lessons with:
 * - Alternating curved road path with visual connectors between levels
 * - Custom lesson icons and emojis for each topic
 * - Clear status indicators: LOCKED, IN_PROGRESS (with pulsing animated halo), and COMPLETED (with 3-star rating)
 * - Full celebration animations when a user finishes a level (confetti particles, star burst, XP float-up, and unlock light streak)
 * - Bonus Unit Milestone chests
 */
@Composable
fun VerticalLearningRoadmap(
    units: List<UnitData>,
    progressList: List<LessonProgressEntity>,
    onSelectLesson: (Lesson) -> Unit,
    activeCelebration: LevelCelebrationData?,
    onDismissCelebration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var lockedTooltipMessage by remember { mutableStateOf<String?>(null) }
    var chestRewardMessage by remember { mutableStateOf<String?>(null) }

    // Flat list of all lessons with unit references to compute progression accurately
    val allLessons = remember(units) { units.flatMap { it.lessons } }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            units.forEachIndexed { unitIndex, unit ->
                // Unit Banner Header
                RoadmapUnitBanner(
                    unit = unit,
                    unitNumber = unitIndex + 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lessons in this Unit along winding path
                val lessonsInUnit = unit.lessons
                lessonsInUnit.forEachIndexed { indexInUnit, lesson ->
                    val globalIndex = allLessons.indexOfFirst { it.id == lesson.id }
                    val progress = progressList.find { it.lessonId == lesson.id }
                    val isCompleted = progress?.isCompleted == true

                    // Unlocked if completed, or first lesson overall, or previous lesson in allLessons is completed
                    val isUnlocked = isCompleted || globalIndex == 0 || (globalIndex > 0 && progressList.any {
                        it.lessonId == allLessons[globalIndex - 1].id && it.isCompleted
                    })

                    val status = when {
                        isCompleted -> RoadmapNodeStatus.COMPLETED
                        isUnlocked -> RoadmapNodeStatus.IN_PROGRESS
                        else -> RoadmapNodeStatus.LOCKED
                    }

                    val starsEarned = progress?.stars ?: if (isCompleted) 3 else 0

                    // Calculate alternating horizontal offset for winding path (-48dp, 0dp, +48dp, 0dp...)
                    val windingPattern = when (globalIndex % 4) {
                        0 -> 0.dp
                        1 -> 46.dp
                        2 -> 0.dp
                        else -> (-46).dp
                    }

                    // Connecting path segment leading to the NEXT node (if not last lesson in unit)
                    val nextLesson = lessonsInUnit.getOrNull(indexInUnit + 1)
                    val nextWinding = if (nextLesson != null) {
                        when ((globalIndex + 1) % 4) {
                            0 -> 0.dp
                            1 -> 46.dp
                            2 -> 0.dp
                            else -> (-46).dp
                        }
                    } else 0.dp

                    val (iconVector, iconEmoji) = remember(lesson.id) { getLessonIconData(lesson) }

                    // Render node with its connecting stepping path
                    RoadmapNodeContainer(
                        lesson = lesson,
                        status = status,
                        stars = starsEarned,
                        iconVector = iconVector,
                        iconEmoji = iconEmoji,
                        horizontalOffset = windingPattern,
                        nextHorizontalOffset = nextWinding,
                        hasNextNode = nextLesson != null,
                        isNextUnlocked = nextLesson != null && progressList.any { it.lessonId == lesson.id && it.isCompleted },
                        isCelebrated = activeCelebration?.lessonId == lesson.id,
                        onClick = {
                            when (status) {
                                RoadmapNodeStatus.LOCKED -> {
                                    val prevLessonTitle = if (globalIndex > 0) allLessons[globalIndex - 1].title else "previous lesson"
                                    lockedTooltipMessage = "🔒 Complete \"$prevLessonTitle\" to unlock!"
                                }
                                RoadmapNodeStatus.IN_PROGRESS,
                                RoadmapNodeStatus.COMPLETED -> {
                                    onSelectLesson(lesson)
                                }
                            }
                        }
                    )
                }

                // Unit Milestone Bonus Chest at the end of each unit
                val isUnitCompleted = unit.lessons.all { l -> progressList.any { it.lessonId == l.id && it.isCompleted } }
                UnitMilestoneChest(
                    unitTitle = unit.title,
                    isUnlocked = isUnitCompleted,
                    onClick = {
                        if (isUnitCompleted) {
                            chestRewardMessage = "🎁 Unit ${unitIndex + 1} Champion Chest Claimed!\n+50 Bonus Gems 💎 added to your vault!"
                        } else {
                            lockedTooltipMessage = "🎁 Complete all lessons in Unit ${unitIndex + 1} to open this Milestone Chest!"
                        }
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // Locked Shake / Tooltip Toast
        lockedTooltipMessage?.let { msg ->
            LaunchedEffect(msg) {
                delay(2600)
                lockedTooltipMessage = null
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF2C3E50),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Chest Reward Dialog
        chestRewardMessage?.let { msg ->
            Dialog(onDismissRequest = { chestRewardMessage = null }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🏆", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Milestone Unlocked!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldYellow
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        DuolingoButton(
                            text = "AWESOME!",
                            onClick = { chestRewardMessage = null },
                            buttonColor = GoldYellow,
                            shadowColor = GoldYellowDark,
                            textColor = Color(0xFF5D4037)
                        )
                    }
                }
            }
        }

        // Full Screen Celebration Overlay with Confetti & Star Pop Animations
        activeCelebration?.let { celebration ->
            LevelCompletionCelebrationOverlay(
                data = celebration,
                onDismiss = onDismissCelebration
            )
        }
    }
}

/**
 * Unit header banner with thematic colors and chapter title.
 */
@Composable
private fun RoadmapUnitBanner(
    unit: UnitData,
    unitNumber: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("unit_banner_${unit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(unit.bannerColorHex)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "UNIT $unitNumber",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${unit.lessons.size} Lessons",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = unit.title.substringAfter(": ").ifEmpty { unit.title },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = unit.description,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Single Lesson Node along the Roadmap with connecting curved path and indicators.
 */
@Composable
private fun RoadmapNodeContainer(
    lesson: Lesson,
    status: RoadmapNodeStatus,
    stars: Int,
    iconVector: ImageVector,
    iconEmoji: String,
    horizontalOffset: Dp,
    nextHorizontalOffset: Dp,
    hasNextNode: Boolean,
    isNextUnlocked: Boolean,
    isCelebrated: Boolean,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        coroutineScope.launch {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
            shakeOffset.snapTo(0f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = horizontalOffset + (shakeOffset.value * 12f).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating "START" speech bubble pointer above active lesson
        AnimatedVisibility(
            visible = status == RoadmapNodeStatus.IN_PROGRESS,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "speech_bubble_bounce")
            val bobbingY by infiniteTransition.animateFloat(
                initialValue = -3f,
                targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bobbing_y"
            )

            Column(
                modifier = Modifier
                    .offset { IntOffset(0, bobbingY.roundToInt()) }
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DuolingoGreen,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "START",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
                // Little downward pointing triangle tip
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .rotate(45f)
                        .offset(y = (-4).dp)
                        .background(DuolingoGreen)
                )
            }
        }

        // Lesson Circular Node Button
        Box(
            modifier = Modifier.padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing halo glow for IN_PROGRESS node
            if (status == RoadmapNodeStatus.IN_PROGRESS) {
                val infiniteTransition = rememberInfiniteTransition(label = "halo_ring")
                val haloScale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.35f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "halo_scale"
                )
                val haloAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "halo_alpha"
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(haloScale)
                        .clip(CircleShape)
                        .background(DuolingoGreen.copy(alpha = haloAlpha))
                )
            }

            // Celebratory golden aura if just finished
            if (isCelebrated) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(GoldYellow, GoldYellow.copy(alpha = 0f))
                            )
                        )
                )
            }

            LessonNodeButton(
                lesson = lesson,
                status = status,
                iconVector = iconVector,
                iconEmoji = iconEmoji,
                onClick = {
                    if (status == RoadmapNodeStatus.LOCKED) {
                        triggerShake()
                    }
                    onClick()
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Lesson Status Indicator & Stars
        when (status) {
            RoadmapNodeStatus.COMPLETED -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Star rating badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { starIndex ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (starIndex < stars) GoldYellow else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = lesson.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }

            RoadmapNodeStatus.IN_PROGRESS -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DuolingoGreenLight
                    ) {
                        Text(
                            text = "IN PROGRESS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = DuolingoGreenDark,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = lesson.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }

            RoadmapNodeStatus.LOCKED -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0F0F0)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "LOCKED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = lesson.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Stepping connector path leading to next node
        if (hasNextNode) {
            RoadmapSteppingConnector(
                fromOffset = horizontalOffset,
                toOffset = nextHorizontalOffset,
                isUnlocked = isNextUnlocked
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 3D-styled circle button representing the lesson level with custom icon.
 */
@Composable
private fun LessonNodeButton(
    lesson: Lesson,
    status: RoadmapNodeStatus,
    iconVector: ImageVector,
    iconEmoji: String,
    onClick: () -> Unit
) {
    val nodeSize = 78.dp

    val (faceColor, shadowColor, iconColor) = when (status) {
        RoadmapNodeStatus.COMPLETED -> Triple(GoldYellow, GoldYellowDark, Color.White)
        RoadmapNodeStatus.IN_PROGRESS -> Triple(DuolingoGreen, DuolingoGreenDark, Color.White)
        RoadmapNodeStatus.LOCKED -> Triple(Color(0xFFE0E0E0), Color(0xFFBDBDBD), Color(0xFF9E9E9E))
    }

    Box(
        modifier = Modifier
            .size(nodeSize, nodeSize + 8.dp)
            .clickable(onClick = onClick)
            .testTag("roadmap_node_${lesson.id}")
    ) {
        // 3D Bottom Extrusion Shadow
        Box(
            modifier = Modifier
                .size(nodeSize)
                .offset(y = 8.dp)
                .clip(CircleShape)
                .background(shadowColor)
        )

        // Main Surface Button
        Box(
            modifier = Modifier
                .size(nodeSize)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            faceColor.copy(alpha = 0.95f),
                            faceColor
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Emoji Icon Badge
                Text(
                    text = iconEmoji,
                    fontSize = 24.sp
                )
                // Vector icon
                Icon(
                    imageVector = when (status) {
                        RoadmapNodeStatus.COMPLETED -> Icons.Default.Check
                        RoadmapNodeStatus.IN_PROGRESS -> iconVector
                        RoadmapNodeStatus.LOCKED -> Icons.Default.Lock
                    },
                    contentDescription = lesson.title,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Top-right mini badge: Completed Checkmark or Locked Padlock
            if (status == RoadmapNodeStatus.COMPLETED) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(DuolingoGreen)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else if (status == RoadmapNodeStatus.LOCKED) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF757575)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Vertical stepping path connecting two consecutive lesson nodes along the roadmap.
 */
@Composable
private fun RoadmapSteppingConnector(
    fromOffset: Dp,
    toOffset: Dp,
    isUnlocked: Boolean
) {
    val deltaX = toOffset - fromOffset
    val pathColor = if (isUnlocked) DuolingoGreen.copy(alpha = 0.85f) else Color(0xFFD4D4D8)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val startX = size.width / 2f
            val startY = 8.dp.toPx()
            val endX = size.width / 2f + deltaX.toPx()
            val endY = size.height - 8.dp.toPx()

            val path = Path().apply {
                moveTo(startX, startY)
                cubicTo(
                    startX, (startY + endY) / 2f,
                    endX, (startY + endY) / 2f,
                    endX, endY
                )
            }

            // Draw winding connection trail
            drawPath(
                path = path,
                color = pathColor,
                style = Stroke(
                    width = if (isUnlocked) 6.dp.toPx() else 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = if (!isUnlocked) PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f) else null
                )
            )

            // Draw 3 playful stepping dots along the curve
            val steps = 3
            for (i in 1..steps) {
                val t = i / (steps + 1f)
                // Approximate cubic bezier point
                val u = 1f - t
                val px = u * u * u * startX + 3 * u * u * t * startX + 3 * u * t * t * endX + t * t * t * endX
                val py = u * u * u * startY + 3 * u * u * t * ((startY + endY) / 2f) + 3 * u * t * t * ((startY + endY) / 2f) + t * t * t * endY

                drawCircle(
                    color = if (isUnlocked) GoldYellow else Color(0xFFBDBDBD),
                    radius = if (isUnlocked) 4.5.dp.toPx() else 3.5.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }
    }
}

/**
 * Unit milestone treasure chest at the bottom of each unit.
 */
@Composable
private fun UnitMilestoneChest(
    unitTitle: String,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) GoldYellow.copy(alpha = 0.2f) else Color(0xFFEEEEEE))
                .border(2.dp, if (isUnlocked) GoldYellow else Color(0xFFCCCCCC), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isUnlocked) "🎁" else "🔒",
                fontSize = 32.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isUnlocked) "Unit Milestone Ready!" else "Unit Milestone Chest",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) GoldYellowDark else Color.Gray
        )
    }
}

/**
 * Full-screen celebration overlay when a user finishes a level.
 * Features:
 * - Animated Confetti Particles cascading down
 * - Staggered 3-Star Pop animation with spring scaling
 * - XP & Gem rewards cards with flying text
 * - "LEVEL COMPLETE" fanfare header
 * - Continue button
 */
@Composable
fun LevelCompletionCelebrationOverlay(
    data: LevelCelebrationData,
    onDismiss: () -> Unit
) {
    // Generate 60 confetti particles
    val particles = remember {
        val colors = listOf(
            DuolingoGreen, GoldYellow, SpeakBlue, Color(0xFFFF5722),
            Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF00E676)
        )
        (0..60).map { id ->
            ConfettiParticle(
                id = id,
                x = Random.nextFloat() * 1000f,
                y = -Random.nextFloat() * 600f,
                vx = (Random.nextFloat() - 0.5f) * 6f,
                vy = Random.nextFloat() * 8f + 5f,
                size = Random.nextFloat() * 14f + 8f,
                color = colors[Random.nextInt(colors.size)],
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 15f,
                shapeIsCircle = Random.nextBoolean()
            )
        }
    }

    var animationTick by remember { mutableIntStateOf(0) }

    // Particle loop
    LaunchedEffect(Unit) {
        while (true) {
            particles.forEach { p ->
                p.x += p.vx
                p.y += p.vy
                p.rotation += p.rotationSpeed
                if (p.y > 2200f) {
                    p.y = -50f
                    p.x = Random.nextFloat() * 1000f
                }
            }
            animationTick++
            delay(16) // ~60fps
        }
    }

    // Star pop scale animatables
    val star1Scale = remember { Animatable(0f) }
    val star2Scale = remember { Animatable(0f) }
    val star3Scale = remember { Animatable(0f) }
    val headerScale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        headerScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Staggered star pops
        delay(200)
        star1Scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
        delay(150)
        star2Scale.animateTo(
            targetValue = 1.25f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
        delay(150)
        star3Scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            // Confetti Canvas in background
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Trigger redraw on tick
                animationTick.let {}
                particles.forEach { p ->
                    if (p.shapeIsCircle) {
                        drawCircle(
                            color = p.color,
                            radius = p.size / 2f,
                            center = Offset(p.x % size.width, p.y % size.height)
                        )
                    } else {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(p.x % size.width, p.y % size.height),
                            size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.6f)
                        )
                    }
                }
            }

            // Celebration Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .scale(headerScale.value)
                    .testTag("level_celebration_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 LEVEL COMPLETE! 🎉",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = DuolingoGreen,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = data.lessonTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3 Animated Stars with Spring Scaling
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldYellow,
                            modifier = Modifier
                                .size(44.dp)
                                .scale(star1Scale.value)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldYellow,
                            modifier = Modifier
                                .size(56.dp)
                                .scale(star2Scale.value)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldYellow,
                            modifier = Modifier
                                .size(44.dp)
                                .scale(star3Scale.value)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Rewards Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GoldYellow.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "XP EARNED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldYellowDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "+${data.xpEarned} XP",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldYellowDark
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SpeakBlue.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "GEMS REWARD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SpeakBlueDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "+${data.gemsEarned} 💎",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SpeakBlueDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Unlocked Next Level Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = DuolingoGreenLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = DuolingoGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Next Level on Roadmap Unlocked! 🚀",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuolingoGreenDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    DuolingoButton(
                        text = "CONTINUE ON ROADMAP",
                        onClick = onDismiss,
                        buttonColor = DuolingoGreen,
                        shadowColor = DuolingoGreenDark,
                        testTag = "celebration_continue_button"
                    )
                }
            }
        }
    }
}
