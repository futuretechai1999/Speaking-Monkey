package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speaklingo.data.local.AppDatabase
import com.example.speaklingo.data.model.LessonCatalog
import com.example.speaklingo.data.remote.GeminiAdvancedService
import com.example.speaklingo.data.remote.GeminiApiClient
import com.example.speaklingo.data.repository.AiTutorRepository
import com.example.speaklingo.data.repository.LearningRepository
import com.example.speaklingo.ui.components.HeartRefillDialog
import com.example.speaklingo.ui.screens.chat.AiChatScreen
import com.example.speaklingo.ui.screens.home.HomeScreen
import com.example.speaklingo.ui.screens.leaderboard.LeaderboardScreen
import com.example.speaklingo.ui.screens.lesson.LessonScreen
import com.example.speaklingo.ui.screens.profile.ProfileScreen
import com.example.speaklingo.ui.screens.speaking.SpeechLabScreen
import com.example.speaklingo.ui.screens.studio.AiStudioHubScreen
import com.example.speaklingo.ui.screens.vocabulary.VocabularyScreen
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpeakBlue
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val icon: ImageVector) {
    LEARN("Learn", Icons.Default.School),
    AI_CHAT("Aria AI", Icons.Default.Chat),
    SPEECH_LAB("Speak", Icons.Default.Mic),
    STUDIO("Studio ✨", Icons.Default.AutoAwesome),
    VOCAB("Words", Icons.AutoMirrored.Filled.MenuBook),
    PROFILE("Profile", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val geminiClient = GeminiApiClient()
        val learningRepository = LearningRepository(database)
        val aiTutorRepository = AiTutorRepository(database, geminiClient)

        setContent {
            MyApplicationTheme {
                SpeakLingoApp(
                    learningRepository = learningRepository,
                    aiTutorRepository = aiTutorRepository
                )
            }
        }
    }
}

@Composable
fun SpeakLingoApp(
    learningRepository: LearningRepository,
    aiTutorRepository: AiTutorRepository
) {
    val coroutineScope = rememberCoroutineScope()
    val advancedService = remember { GeminiAdvancedService() }
    var currentTab by remember { mutableStateOf(AppTab.LEARN) }
    var activeLessonId by remember { mutableStateOf<String?>(null) }
    var showHeartsDialog by remember { mutableStateOf(false) }
    var pendingCelebration by remember { mutableStateOf<com.example.speaklingo.ui.components.LevelCelebrationData?>(null) }

    val userProfile by learningRepository.userProfile.collectAsState(initial = null)
    val progressList by learningRepository.allLessonProgress.collectAsState(initial = emptyList())

    // If activeLessonId is not null, display full screen interactive lesson runner
    if (activeLessonId != null) {
        val allLessons = LessonCatalog.units.flatMap { it.lessons }
        val currentLesson = allLessons.find { it.id == activeLessonId }

        if (currentLesson != null) {
            LessonScreen(
                lesson = currentLesson,
                initialHearts = userProfile?.hearts ?: 5,
                onLessonCompleted = { xp, gems, accuracy ->
                    coroutineScope.launch {
                        learningRepository.completeLesson(
                            lessonId = currentLesson.id,
                            unitId = currentLesson.unitId,
                            xpReward = xp,
                            gemReward = gems,
                            accuracy = accuracy
                        )
                    }
                    val stars = when {
                        accuracy >= 90 -> 3
                        accuracy >= 70 -> 2
                        else -> 1
                    }
                    pendingCelebration = com.example.speaklingo.ui.components.LevelCelebrationData(
                        lessonId = currentLesson.id,
                        lessonTitle = currentLesson.title,
                        xpEarned = xp,
                        gemsEarned = gems,
                        stars = stars
                    )
                    activeLessonId = null
                },
                onLoseHeart = {
                    coroutineScope.launch {
                        learningRepository.loseHeart()
                    }
                },
                onRefillHearts = {
                    coroutineScope.launch {
                        learningRepository.refillHearts()
                    }
                },
                onExit = {
                    activeLessonId = null
                }
            )
            return
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DuolingoGreen,
                            selectedTextColor = DuolingoGreen,
                            indicatorColor = DuolingoGreen.copy(alpha = 0.15f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.LEARN -> {
                    HomeScreen(
                        profile = userProfile,
                        progressList = progressList,
                        onStartLesson = { lessonId ->
                            activeLessonId = lessonId
                        },
                        onNavigateToAiChat = {
                            currentTab = AppTab.AI_CHAT
                        },
                        onNavigateToStudio = {
                            currentTab = AppTab.STUDIO
                        },
                        onHeartsClick = {
                            showHeartsDialog = true
                        },
                        externalCelebration = pendingCelebration,
                        onDismissCelebration = {
                            pendingCelebration = null
                        },
                        learningRepository = learningRepository,
                        aiTutorRepository = aiTutorRepository
                    )
                }

                AppTab.AI_CHAT -> {
                    AiChatScreen(
                        aiTutorRepository = aiTutorRepository,
                        learningRepository = learningRepository
                    )
                }

                AppTab.SPEECH_LAB -> {
                    SpeechLabScreen(
                        aiTutorRepository = aiTutorRepository,
                        learningRepository = learningRepository
                    )
                }

                AppTab.STUDIO -> {
                    AiStudioHubScreen(
                        advancedService = advancedService,
                        learningRepository = learningRepository
                    )
                }

                AppTab.VOCAB -> {
                    VocabularyScreen(
                        learningRepository = learningRepository,
                        aiTutorRepository = aiTutorRepository
                    )
                }

                AppTab.PROFILE -> {
                    ProfileScreen(
                        profile = userProfile,
                        aiTutorRepository = aiTutorRepository
                    )
                }
            }
        }
    }

    if (showHeartsDialog) {
        HeartRefillDialog(
            currentHearts = userProfile?.hearts ?: 5,
            onRefillWithPractice = {
                coroutineScope.launch {
                    learningRepository.refillHearts()
                }
            },
            onDismiss = { showHeartsDialog = false }
        )
    }
}
