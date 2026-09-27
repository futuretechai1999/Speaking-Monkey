package com.example.speaklingo.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.speaklingo.data.local.LessonProgressEntity
import com.example.speaklingo.data.local.UserProfileEntity
import com.example.speaklingo.data.model.Lesson
import com.example.speaklingo.data.model.LessonCatalog
import com.example.speaklingo.data.model.UnitData
import com.example.speaklingo.ui.components.DuolingoButton
import com.example.speaklingo.ui.components.LevelCelebrationData
import com.example.speaklingo.ui.components.StreakTracker
import com.example.speaklingo.ui.components.StreakTrackerDialog
import com.example.speaklingo.ui.components.TopStatusBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.speaklingo.ui.components.RoadmapFlashcardGeneratorComponent
import com.example.speaklingo.ui.components.VerticalLearningRoadmap
import com.example.speaklingo.ui.screens.roadmap.ProficiencyRoadmapScreen
import com.example.speaklingo.ui.viewmodel.FlashcardGeneratorViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.speaklingo.data.repository.AiTutorRepository
import com.example.speaklingo.data.repository.LearningRepository
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark

@Composable
fun HomeScreen(
    profile: UserProfileEntity?,
    progressList: List<LessonProgressEntity>,
    onStartLesson: (String) -> Unit,
    onNavigateToAiChat: () -> Unit,
    onNavigateToStudio: () -> Unit = {},
    onHeartsClick: () -> Unit,
    externalCelebration: LevelCelebrationData? = null,
    onDismissCelebration: () -> Unit = {},
    learningRepository: LearningRepository? = null,
    aiTutorRepository: AiTutorRepository? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentRoadmapMode by remember { mutableStateOf("PROFICIENCY") }
    var selectedLessonForModal by remember { mutableStateOf<Lesson?>(null) }
    var localCelebration by remember { mutableStateOf<LevelCelebrationData?>(null) }
    var showStreakDialog by remember { mutableStateOf(false) }
    val activeCelebration = externalCelebration ?: localCelebration

    val completedTimestamps by (learningRepository?.completedLessonTimestamps?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList<Long>()) })

    if (currentRoadmapMode == "PROFICIENCY") {
        ProficiencyRoadmapScreen(
            profile = profile,
            progressList = progressList,
            completedTimestamps = completedTimestamps,
            onNavigateToAiTutor = { onNavigateToAiChat() },
            onStartLesson = onStartLesson,
            onHeartsClick = onHeartsClick,
            onQuickPractice = {
                coroutineScope.launch {
                    learningRepository?.recordQuickPractice()
                }
            },
            onSwitchToUnitsPath = { currentRoadmapMode = "UNITS" },
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Header with Streak, Gems, Hearts, XP
        TopStatusBar(
            profile = profile,
            onHeartsClick = onHeartsClick,
            onStreakClick = { showStreakDialog = true }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Switcher Bar between CEFR Levels and Units
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
                            .background(Color.Transparent)
                            .clickable { currentRoadmapMode = "PROFICIENCY" }
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

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
                            Text(text = "🗺️", fontSize = 14.sp)
                            Text(
                                text = "Daily Units Path",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // StreakTracker Component
            item {
                StreakTracker(
                    profile = profile,
                    completedTimestamps = completedTimestamps,
                    onQuickPractice = {
                        coroutineScope.launch {
                            learningRepository?.recordQuickPractice()
                        }
                    }
                )
            }

            // Roadmap Flashcard Generator Component using Gemini
            if (learningRepository != null && aiTutorRepository != null) {
                item {
                    val roadmapFlashcardVm: FlashcardGeneratorViewModel = viewModel(
                        factory = FlashcardGeneratorViewModel.provideFactory(
                            learningRepository = learningRepository,
                            aiTutorRepository = aiTutorRepository
                        )
                    )
                    RoadmapFlashcardGeneratorComponent(viewModel = roadmapFlashcardVm)
                }
            }

            // SpeakX Quick AI Practice Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable(onClick = onNavigateToAiChat)
                        .testTag("home_ai_tutor_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SpeakBlue)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Speak with Aria AI",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "🎙️", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Practice real speaking with instant grammar corrections & Hindi explanations.",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start AI Chat",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // AI Capabilities Studio Card (Search, Maps, Veo 3, Lyria Music)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToStudio)
                        .testTag("home_studio_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldYellow.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(GoldYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✨", fontSize = 22.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Capabilities Studio",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF9E6D00)
                            )
                            Text(
                                text = "Veo 3 Video, Search & Maps Grounding, Flashcards & Lyria Music.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Roadmap Header with Demo Animation Trigger
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🗺️", fontSize = 18.sp)
                        Text(
                            text = "Learning Adventure Roadmap",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Interactive test button to preview level completion animation anytime
                    Surface(
                        modifier = Modifier
                            .clickable {
                                localCelebration = LevelCelebrationData(
                                    lessonId = "u1_l1",
                                    lessonTitle = "Greetings & Introductions",
                                    xpEarned = 25,
                                    gemsEarned = 10,
                                    stars = 3
                                )
                            }
                            .testTag("test_celebration_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = GoldYellow.copy(alpha = 0.18f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🎉", fontSize = 12.sp)
                            Text(
                                text = "Test Level Finish",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9E6D00)
                            )
                        }
                    }
                }
            }

            // Vertical Learning Roadmap Component
            item {
                VerticalLearningRoadmap(
                    units = LessonCatalog.units,
                    progressList = progressList,
                    onSelectLesson = { lesson ->
                        selectedLessonForModal = lesson
                    },
                    activeCelebration = activeCelebration,
                    onDismissCelebration = {
                        localCelebration = null
                        onDismissCelebration()
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Lesson Start Preview Modal
    selectedLessonForModal?.let { lesson ->
        LessonStartDialog(
            lesson = lesson,
            onDismiss = { selectedLessonForModal = null },
            onStart = {
                selectedLessonForModal = null
                onStartLesson(lesson.id)
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
                coroutineScope.launch {
                    learningRepository?.recordQuickPractice()
                }
            },
            onDismiss = { showStreakDialog = false }
        )
    }
}

@Composable
private fun LessonStartDialog(
    lesson: Lesson,
    onDismiss: () -> Unit,
    onStart: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("lesson_preview_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎯",
                    fontSize = 36.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = lesson.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = lesson.subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+${lesson.xpReward} XP",
                        fontWeight = FontWeight.Bold,
                        color = GoldYellow,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "+${lesson.gemReward} 💎",
                        fontWeight = FontWeight.Bold,
                        color = SpeakBlue,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${lesson.questions.size} Questions",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                DuolingoButton(
                    text = "START (+${lesson.xpReward} XP)",
                    onClick = onStart,
                    buttonColor = DuolingoGreen,
                    shadowColor = DuolingoGreenDark,
                    testTag = "dialog_start_lesson_button"
                )

                Spacer(modifier = Modifier.height(8.dp))

                DuolingoButton(
                    text = "LATER",
                    onClick = onDismiss,
                    buttonColor = Color.LightGray,
                    shadowColor = Color.Gray,
                    textColor = Color.DarkGray,
                    height = 44.dp,
                    testTag = "dialog_cancel_button"
                )
            }
        }
    }
}
