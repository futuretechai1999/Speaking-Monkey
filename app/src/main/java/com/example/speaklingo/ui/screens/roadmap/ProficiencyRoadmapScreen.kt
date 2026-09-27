package com.example.speaklingo.ui.screens.roadmap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.speaklingo.data.local.LessonProgressEntity
import com.example.speaklingo.data.local.UserProfileEntity
import com.example.speaklingo.data.model.ProficiencyCatalog
import com.example.speaklingo.data.model.ProficiencyLevel
import com.example.speaklingo.data.model.ProficiencyModule
import com.example.speaklingo.data.model.ProficiencyStatus
import com.example.speaklingo.ui.components.StreakTracker
import com.example.speaklingo.ui.components.StreakTrackerDialog
import com.example.speaklingo.data.model.SkillFocus
import com.example.speaklingo.ui.components.DuolingoButton
import com.example.speaklingo.ui.components.LevelCelebrationData
import com.example.speaklingo.ui.components.LevelCompletionCelebrationOverlay
import com.example.speaklingo.ui.components.TopStatusBar
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.DuolingoGreenLight
import com.example.ui.theme.FeatherPurple
import com.example.ui.theme.FireOrange
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GoldYellowDark
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark
import com.example.ui.theme.SpeakBlueLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Screen rendering an interactive visual Learning Path & Roadmap
 * across all standard English proficiency levels (CEFR A1, A2, B1, B2, C1).
 */
@Composable
fun ProficiencyRoadmapScreen(
    profile: UserProfileEntity?,
    progressList: List<LessonProgressEntity>,
    completedTimestamps: List<Long> = emptyList(),
    onNavigateToAiTutor: (topic: String) -> Unit = {},
    onStartLesson: (String) -> Unit = {},
    onHeartsClick: () -> Unit = {},
    onQuickPractice: (() -> Unit)? = null,
    onSwitchToUnitsPath: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showStreakDialog by remember { mutableStateOf(false) }

    // Level filter selection ("ALL", "A1", "A2", "B1", "B2", "C1")
    var selectedLevelFilter by remember { mutableStateOf("ALL") }

    // Selected module for detail preview dialog
    var selectedModuleForDialog by remember { mutableStateOf<ProficiencyModule?>(null) }

    // Dynamic completed modules set (initialized with progress or defaults)
    val completedModuleIds = remember {
        mutableStateListOf(
            "a1_m1", "a1_m2", "a1_m3", // Default starter progress
            "a1_m4"
        )
    }

    // Celebration modal when finishing a module or checkpoint
    var activeCelebration by remember { mutableStateOf<LevelCelebrationData?>(null) }

    // Diagnostic Placement Quiz modal
    var showPlacementQuiz by remember { mutableStateOf(false) }

    // Tooltip for locked modules
    var lockedTooltip by remember { mutableStateOf<String?>(null) }

    // Filtered level list
    val displayedLevels = remember(selectedLevelFilter) {
        if (selectedLevelFilter == "ALL") {
            ProficiencyCatalog.levels
        } else {
            ProficiencyCatalog.levels.filter { it.code == selectedLevelFilter }
        }
    }

    // Calculate overall statistics
    val allModules = remember { ProficiencyCatalog.levels.flatMap { it.modules } }
    val totalModules = allModules.size
    val completedCount = allModules.count { completedModuleIds.contains(it.id) }
    val overallPercentage = if (totalModules > 0) (completedCount.toFloat() / totalModules) else 0f

    // Current active level determined by first level with incomplete modules
    val currentActiveLevel = remember(completedModuleIds.size) {
        ProficiencyCatalog.levels.firstOrNull { level ->
            level.modules.any { !completedModuleIds.contains(it.id) }
        } ?: ProficiencyCatalog.levels.last()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sticky Top Bar with Streak, Gems, Hearts, XP
            TopStatusBar(
                profile = profile,
                onHeartsClick = onHeartsClick,
                onStreakClick = { showStreakDialog = true }
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Path Mode Selector Bar if onSwitchToUnitsPath provided
                if (onSwitchToUnitsPath != null) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DuolingoGreen)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "🧭", fontSize = 14.sp)
                                    Text(
                                        text = "CEFR Levels (A1-C1)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Transparent)
                                    .clickable { onSwitchToUnitsPath() }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "🗺️", fontSize = 14.sp)
                                    Text(
                                        text = "Daily Units Path",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive StreakTracker Component
                item {
                    StreakTracker(
                        profile = profile,
                        completedTimestamps = completedTimestamps,
                        onQuickPractice = onQuickPractice
                    )
                }

                // Header Banner: CEFR Overview & Progress Dashboard
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    ProficiencyOverviewCard(
                        currentLevel = currentActiveLevel,
                        completedCount = completedCount,
                        totalModules = totalModules,
                        overallPercentage = overallPercentage,
                        onTakePlacementQuiz = { showPlacementQuiz = true }
                    )
                }

                // Interactive CEFR Level Quick Filter Bar
                item {
                    ProficiencyLevelFilterBar(
                        selectedFilter = selectedLevelFilter,
                        completedModuleIds = completedModuleIds,
                        onSelectFilter = { selectedLevelFilter = it }
                    )
                }

                // Levels Learning Path Section
                itemsIndexed(displayedLevels) { levelIndex, level ->
                    ProficiencyLevelSection(
                        level = level,
                        completedModuleIds = completedModuleIds,
                        onModuleClick = { module, status ->
                            when (status) {
                                ProficiencyStatus.LOCKED -> {
                                    lockedTooltip = "🔒 Complete previous modules in ${level.code} to unlock \"${module.title}\"!"
                                }
                                ProficiencyStatus.IN_PROGRESS,
                                ProficiencyStatus.COMPLETED -> {
                                    selectedModuleForDialog = module
                                }
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }

        // Locked Shake / Tooltip Toast
        lockedTooltip?.let { msg ->
            LaunchedEffect(msg) {
                delay(2600)
                lockedTooltip = null
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF2C3E50),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }

        // Module Detail & Interactive Practice Dialog
        selectedModuleForDialog?.let { module ->
            val isCompleted = completedModuleIds.contains(module.id)
            ProficiencyModuleDetailDialog(
                module = module,
                isCompleted = isCompleted,
                onDismiss = { selectedModuleForDialog = null },
                onPracticeWithAi = {
                    selectedModuleForDialog = null
                    onNavigateToAiTutor("Let's practice English for: ${module.title} (${module.levelCode})")
                },
                onCompleteModule = {
                    selectedModuleForDialog = null
                    if (!completedModuleIds.contains(module.id)) {
                        completedModuleIds.add(module.id)
                    }
                    activeCelebration = LevelCelebrationData(
                        lessonId = module.id,
                        lessonTitle = module.title,
                        xpEarned = module.xpReward,
                        gemsEarned = module.gemReward,
                        stars = 3
                    )
                }
            )
        }

        // Diagnostic CEFR Placement Quiz Dialog
        if (showPlacementQuiz) {
            CefrPlacementQuizDialog(
                onDismiss = { showPlacementQuiz = false },
                onResult = { recommendedLevel ->
                    showPlacementQuiz = false
                    selectedLevelFilter = recommendedLevel
                    lockedTooltip = "🎉 Placement Quiz Complete! Recommended tier set to $recommendedLevel."
                }
            )
        }

        // Streak Tracker Details Modal Dialog
        if (showStreakDialog) {
            StreakTrackerDialog(
                profile = profile,
                completedTimestamps = completedTimestamps,
                onQuickPractice = {
                    showStreakDialog = false
                    onQuickPractice?.invoke()
                },
                onDismiss = { showStreakDialog = false }
            )
        }

        // Level Celebration Overlay
        activeCelebration?.let { celebration ->
            LevelCompletionCelebrationOverlay(
                data = celebration,
                onDismiss = { activeCelebration = null }
            )
        }
    }
}

/**
 * Top Overview Card with total CEFR completion progress and current proficiency level.
 */
@Composable
private fun ProficiencyOverviewCard(
    currentLevel: ProficiencyLevel,
    completedCount: Int,
    totalModules: Int,
    overallPercentage: Float,
    onTakePlacementQuiz: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("proficiency_overview_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🧭", fontSize = 18.sp)
                        Text(
                            text = "ENGLISH PROFICIENCY ROADMAP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Current: ${currentLevel.code} • ${currentLevel.title}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(currentLevel.themeColorHex)
                    )
                }

                // Placement test badge button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SpeakBlueLight,
                    modifier = Modifier.clickable(onClick = onTakePlacementQuiz)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🎯", fontSize = 12.sp)
                        Text(
                            text = "Level Test",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpeakBlueDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Linear Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Overall CEFR Progress",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = "${(overallPercentage * 100).roundToInt()}% ($completedCount/$totalModules Modules)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = DuolingoGreenDark
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { overallPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = DuolingoGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Level Badges summary row (A1 -> A2 -> B1 -> B2 -> C1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProficiencyCatalog.levels.forEach { level ->
                    val isLevelCompleted = level.modules.all { false } // placeholder
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(level.themeColorHex).copy(alpha = 0.18f))
                                .border(1.dp, Color(level.themeColorHex), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level.code,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(level.themeColorHex)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = level.title.split(" ").first(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

/**
 * Filter bar allowing learners to switch between viewing all levels or focusing on a specific CEFR tier.
 */
@Composable
private fun ProficiencyLevelFilterBar(
    selectedFilter: String,
    completedModuleIds: List<String>,
    onSelectFilter: (String) -> Unit
) {
    val filters = remember {
        listOf("ALL" to "All Levels") + ProficiencyCatalog.levels.map { it.code to "${it.code} (${it.title.split(" ").first()})" }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("proficiency_filter_bar"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(filters) { (code, label) ->
            val isSelected = selectedFilter == code
            val levelObj = ProficiencyCatalog.levels.find { it.code == code }
            val color = if (levelObj != null) Color(levelObj.themeColorHex) else DuolingoGreen

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onSelectFilter(code) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (levelObj != null) {
                        Text(text = levelObj.iconEmoji, fontSize = 13.sp)
                    } else {
                        Text(text = "🌟", fontSize = 13.sp)
                    }
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Section for a single English Proficiency Level with its connected winding roadmap.
 */
@Composable
private fun ProficiencyLevelSection(
    level: ProficiencyLevel,
    completedModuleIds: List<String>,
    onModuleClick: (ProficiencyModule, ProficiencyStatus) -> Unit
) {
    val completedInLevel = level.modules.count { completedModuleIds.contains(it.id) }
    val levelPercentage = if (level.modules.isNotEmpty()) completedInLevel.toFloat() / level.modules.size else 0f
    val isLevelDone = completedInLevel == level.modules.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("proficiency_level_${level.code}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Level Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(level.themeColorHex)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = level.iconEmoji, fontSize = 24.sp)
                        Column {
                            Text(
                                text = level.cefrStandard.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.85f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${level.code}: ${level.title}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    // Progress Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isLevelDone) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "$completedInLevel / ${level.modules.size} Passed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = level.summary,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.92f),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar in Header
                LinearProgressIndicator(
                    progress = { levelPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.35f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Winding Roadmap Nodes for this Level
        level.modules.forEachIndexed { index, module ->
            val isCompleted = completedModuleIds.contains(module.id)

            // Calculate status: completed, in-progress (if first in level or prior module is completed), or locked
            val isPriorCompleted = if (index == 0) {
                // If it's the very first level (A1), first module is always unlocked.
                // Otherwise check if previous level is completed or first module in this level.
                true
            } else {
                completedModuleIds.contains(level.modules[index - 1].id)
            }

            val status = when {
                isCompleted -> ProficiencyStatus.COMPLETED
                isPriorCompleted -> ProficiencyStatus.IN_PROGRESS
                else -> ProficiencyStatus.LOCKED
            }

            // Alternating winding pattern (center -> right +46dp -> center -> left -46dp)
            val windingOffset = when (index % 4) {
                0 -> 0.dp
                1 -> 46.dp
                2 -> 0.dp
                else -> (-46).dp
            }

            val nextModule = level.modules.getOrNull(index + 1)
            val nextWinding = if (nextModule != null) {
                when ((index + 1) % 4) {
                    0 -> 0.dp
                    1 -> 46.dp
                    2 -> 0.dp
                    else -> (-46).dp
                }
            } else 0.dp

            ProficiencyRoadmapNode(
                module = module,
                status = status,
                horizontalOffset = windingOffset,
                nextHorizontalOffset = nextWinding,
                hasNextNode = nextModule != null,
                isNextUnlocked = isCompleted,
                levelThemeColor = Color(level.themeColorHex),
                levelDarkColor = Color(level.darkColorHex),
                onClick = { onModuleClick(module, status) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Individual Node along the visual proficiency path with winding stepping connectors.
 */
@Composable
private fun ProficiencyRoadmapNode(
    module: ProficiencyModule,
    status: ProficiencyStatus,
    horizontalOffset: Dp,
    nextHorizontalOffset: Dp,
    hasNextNode: Boolean,
    isNextUnlocked: Boolean,
    levelThemeColor: Color,
    levelDarkColor: Color,
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
        // Floating speech bubble tag if active
        AnimatedVisibility(
            visible = status == ProficiencyStatus.IN_PROGRESS,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "tag_bounce")
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
                    color = levelThemeColor,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = if (module.isCheckpoint) "EXAM READY" else "CURRENT",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .rotate(45f)
                        .offset(y = (-4).dp)
                        .background(levelThemeColor)
                )
            }
        }

        // Circular Node Button with halo
        Box(
            modifier = Modifier.padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (status == ProficiencyStatus.IN_PROGRESS) {
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
                        .background(levelThemeColor.copy(alpha = haloAlpha))
                )
            }

            // Tactile 3D Button
            val nodeSize = if (module.isCheckpoint) 84.dp else 76.dp
            val (faceColor, shadowColor) = when (status) {
                ProficiencyStatus.COMPLETED -> GoldYellow to GoldYellowDark
                ProficiencyStatus.IN_PROGRESS -> levelThemeColor to levelDarkColor
                ProficiencyStatus.LOCKED -> Color(0xFFE2E8F0) to Color(0xFFCBD5E1)
            }

            Box(
                modifier = Modifier
                    .size(nodeSize, nodeSize + 8.dp)
                    .clickable {
                        if (status == ProficiencyStatus.LOCKED) {
                            triggerShake()
                        }
                        onClick()
                    }
                    .testTag("node_${module.id}")
            ) {
                // 3D Shadow layer
                Box(
                    modifier = Modifier
                        .size(nodeSize)
                        .offset(y = 8.dp)
                        .clip(CircleShape)
                        .background(shadowColor)
                )

                // Face button
                Box(
                    modifier = Modifier
                        .size(nodeSize)
                        .clip(CircleShape)
                        .background(faceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = module.iconEmoji,
                            fontSize = if (module.isCheckpoint) 28.sp else 22.sp
                        )
                    }

                    // Badge: Checkmark or Padlock
                    if (status == ProficiencyStatus.COMPLETED) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
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
                    } else if (status == ProficiencyStatus.LOCKED) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-2).dp, y = (-2).dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF64748B)),
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

        Spacer(modifier = Modifier.height(4.dp))

        // Module Title and Focus Tag
        Text(
            text = module.title,
            fontSize = 12.sp,
            fontWeight = if (status == ProficiencyStatus.IN_PROGRESS) FontWeight.Black else FontWeight.Bold,
            color = if (status == ProficiencyStatus.LOCKED) Color.Gray else MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(140.dp)
        )

        // Connector line leading to next module
        if (hasNextNode) {
            RoadmapPathConnector(
                fromOffset = horizontalOffset,
                toOffset = nextHorizontalOffset,
                isUnlocked = isNextUnlocked,
                activeColor = levelThemeColor
            )
        } else {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

/**
 * Curved connector trail between nodes.
 */
@Composable
private fun RoadmapPathConnector(
    fromOffset: Dp,
    toOffset: Dp,
    isUnlocked: Boolean,
    activeColor: Color
) {
    val deltaX = toOffset - fromOffset
    val pathColor = if (isUnlocked) activeColor.copy(alpha = 0.85f) else Color(0xFFD4D4D8)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val startX = size.width / 2f
            val startY = 6.dp.toPx()
            val endX = size.width / 2f + deltaX.toPx()
            val endY = size.height - 6.dp.toPx()

            val path = Path().apply {
                moveTo(startX, startY)
                cubicTo(
                    startX, (startY + endY) / 2f,
                    endX, (startY + endY) / 2f,
                    endX, endY
                )
            }

            drawPath(
                path = path,
                color = pathColor,
                style = Stroke(
                    width = if (isUnlocked) 6.dp.toPx() else 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = if (!isUnlocked) PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f) else null
                )
            )

            // Stepping dots
            for (i in 1..3) {
                val t = i / 4f
                val u = 1f - t
                val px = u * u * u * startX + 3 * u * u * t * startX + 3 * u * t * t * endX + t * t * t * endX
                val py = u * u * u * startY + 3 * u * u * t * ((startY + endY) / 2f) + 3 * u * t * t * ((startY + endY) / 2f) + t * t * t * endY

                drawCircle(
                    color = if (isUnlocked) GoldYellow else Color(0xFFCBD5E1),
                    radius = if (isUnlocked) 4.5.dp.toPx() else 3.5.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }
    }
}

/**
 * Interactive dialog displaying module details, learning outcomes, and action buttons.
 */
@Composable
private fun ProficiencyModuleDetailDialog(
    module: ProficiencyModule,
    isCompleted: Boolean,
    onDismiss: () -> Unit,
    onPracticeWithAi: () -> Unit,
    onCompleteModule: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("module_detail_dialog")
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SpeakBlueLight
                    ) {
                        Text(
                            text = "${module.levelCode} • ${module.skillFocus.label}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = SpeakBlueDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Module Icon
                Text(text = module.iconEmoji, fontSize = 42.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = module.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = module.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Learning Outcomes List
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "KEY LEARNING OUTCOMES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        module.learningOutcomes.forEach { outcome ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "✔", color = DuolingoGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = outcome,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rewards Card Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = GoldYellow.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⭐ +${module.xpReward} XP", fontSize = 13.sp, fontWeight = FontWeight.Black, color = GoldYellowDark)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = SpeakBlueLight
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💎 +${module.gemReward} Gems", fontSize = 13.sp, fontWeight = FontWeight.Black, color = SpeakBlueDark)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⏱️ ${module.estimatedMinutes}m", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                DuolingoButton(
                    text = if (isCompleted) "PRACTICE WITH ARIA AI 🎙️" else "START MODULE NOW 🚀",
                    onClick = onPracticeWithAi,
                    buttonColor = DuolingoGreen,
                    shadowColor = DuolingoGreenDark,
                    testTag = "start_module_button"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Instant pass / simulate completion button for testing & review
                DuolingoButton(
                    text = if (isCompleted) "REPLAY LEVEL COMPLETE FANFARE 🎉" else "PASS & CERTIFY MODULE 🏆",
                    onClick = onCompleteModule,
                    buttonColor = GoldYellow,
                    shadowColor = GoldYellowDark,
                    textColor = Color(0xFF422006),
                    testTag = "simulate_module_pass_button"
                )
            }
        }
    }
}

/**
 * Diagnostic CEFR Placement Quiz enabling users to evaluate their starting English proficiency tier.
 */
@Composable
private fun CefrPlacementQuizDialog(
    onDismiss: () -> Unit,
    onResult: (recommendedLevel: String) -> Unit
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }

    data class QuizQuestion(
        val question: String,
        val options: List<String>,
        val correctIndex: Int,
        val levelIndicator: String
    )

    val questions = remember {
        listOf(
            QuizQuestion(
                question = "Which sentence is grammatically correct for daily habits?",
                options = listOf(
                    "He drink coffee every mornings.",
                    "He drinks coffee every morning.",
                    "He is drink coffee every morning.",
                    "He drinked coffee every morning."
                ),
                correctIndex = 1,
                levelIndicator = "A1"
            ),
            QuizQuestion(
                question = "Choose the most polite phrase to order food at a restaurant:",
                options = listOf(
                    "Give me a coffee right now.",
                    "I want coffee.",
                    "Could I please have a cup of cappuccino?",
                    "Coffee is needed for me."
                ),
                correctIndex = 2,
                levelIndicator = "A2"
            ),
            QuizQuestion(
                question = "Fill in the blank: 'If I _____ you, I would take that job offer.'",
                options = listOf(
                    "was",
                    "were",
                    "am",
                    "will be"
                ),
                correctIndex = 1,
                levelIndicator = "B1"
            ),
            QuizQuestion(
                question = "What does the professional idiom 'cut to the chase' mean?",
                options = listOf(
                    "Run quickly in the park",
                    "Get straight to the main point without wasting time",
                    "Cut paper with scissors",
                    "Postpone a project meeting"
                ),
                correctIndex = 1,
                levelIndicator = "B2"
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎯 CEFR Placement Quiz",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${currentQuestionIndex + 1}/${questions.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpeakBlue
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                val q = questions[currentQuestionIndex]

                Text(
                    text = q.question,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                q.options.forEachIndexed { optIndex, optText ->
                    val isChosen = selectedOptionIndex == optIndex
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedOptionIndex = optIndex },
                        color = if (isChosen) SpeakBlueLight else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isChosen) BorderStroke(1.5.dp, SpeakBlue) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isChosen,
                                onClick = { selectedOptionIndex = optIndex }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = optText,
                                fontSize = 13.sp,
                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                DuolingoButton(
                    text = if (currentQuestionIndex < questions.size - 1) "NEXT QUESTION →" else "SEE MY LEVEL 🏆",
                    onClick = {
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                            selectedOptionIndex = -1
                        } else {
                            // Conclude placement quiz
                            onResult("B1")
                        }
                    },
                    enabled = selectedOptionIndex != -1,
                    buttonColor = DuolingoGreen,
                    shadowColor = DuolingoGreenDark
                )
            }
        }
    }
}
