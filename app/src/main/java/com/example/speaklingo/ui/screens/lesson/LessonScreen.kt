package com.example.speaklingo.ui.screens.lesson

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.speaklingo.audio.SpeechInputManager
import com.example.speaklingo.audio.TtsManager
import com.example.speaklingo.data.model.Lesson
import com.example.speaklingo.data.model.LessonQuestion
import com.example.speaklingo.data.model.QuestionType
import com.example.speaklingo.ui.components.AudioPlayerButton
import com.example.speaklingo.ui.components.DuolingoButton
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.DuolingoGreenLight
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.HeartRed
import com.example.ui.theme.HeartRedDark
import com.example.ui.theme.HeartRedLight
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark
import com.example.ui.theme.SpeakBlueLight
import kotlinx.coroutines.launch

@Composable
fun LessonScreen(
    lesson: Lesson,
    initialHearts: Int,
    onLessonCompleted: (xp: Int, gems: Int, accuracy: Int) -> Unit,
    onLoseHeart: () -> Unit,
    onRefillHearts: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var currentHearts by remember { mutableIntStateOf(initialHearts) }
    var correctAnswersCount by remember { mutableIntStateOf(0) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showOutOfHeartsDialog by remember { mutableStateOf(false) }
    var isLessonFinished by remember { mutableStateOf(false) }

    // TTS Manager
    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    // Question State
    val questions = lesson.questions
    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    // Answer states for current question
    val selectedWords = remember { mutableStateListOf<String>() }
    val availableWords = remember { mutableStateListOf<String>() }
    var selectedChoice by remember { mutableStateOf<String?>(null) }
    var spokenAnswer by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    // Status: null = not checked, true = correct, false = incorrect
    var checkResult by remember { mutableStateOf<Boolean?>(null) }

    // Speech Recognizer helper
    val speechManager = remember {
        SpeechInputManager(
            context = context,
            onListeningChanged = { isListening = it },
            onResultReceived = { spokenAnswer = it },
            onErrorReceived = { /* handled gracefully */ }
        )
    }

    DisposableEffect(Unit) {
        onDispose { speechManager.destroy() }
    }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechManager.startListening()
        }
    }

    fun requestSpeech() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            if (isListening) speechManager.stopListening() else speechManager.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Reset question state when index changes
    LaunchedEffect(currentQuestionIndex) {
        checkResult = null
        selectedChoice = null
        spokenAnswer = ""
        selectedWords.clear()
        availableWords.clear()

        currentQuestion?.let { q ->
            if (q.type == QuestionType.WORD_SCRAMBLE) {
                availableWords.addAll(q.scrambleOptions)
            }
            // Auto play audio for listen question or speak question
            q.audioSentence?.let { sentence ->
                ttsManager.speak(sentence)
            }
        }
    }

    BackHandler {
        showExitConfirmDialog = true
    }

    val progress = if (questions.isNotEmpty()) {
        (currentQuestionIndex.toFloat() / questions.size.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "lesson_progress")

    if (isLessonFinished) {
        LessonCompleteView(
            lesson = lesson,
            correctCount = correctAnswersCount,
            totalCount = questions.size,
            onContinue = {
                val accuracy = ((correctAnswersCount.toFloat() / questions.size.toFloat()) * 100).toInt()
                onLessonCompleted(lesson.xpReward, lesson.gemReward, accuracy)
            }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp) // space for bottom check sheet
        ) {
            // Top Bar with X, Progress Bar, Hearts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = { showExitConfirmDialog = true },
                    modifier = Modifier.testTag("lesson_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit lesson",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp)),
                    color = DuolingoGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.testTag("lesson_hearts_display")
                ) {
                    Text(text = "❤️", fontSize = 18.sp)
                    Text(
                        text = "$currentHearts",
                        fontWeight = FontWeight.Black,
                        color = HeartRed,
                        fontSize = 16.sp
                    )
                }
            }

            // Question Content
            currentQuestion?.let { q ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = q.promptQuestion,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    q.promptHindi?.let { hindi ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hindi,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = SpeakBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Audio play buttons
                    if (q.audioSentence != null) {
                        AudioPlayerButton(
                            onPlayNormal = { ttsManager.speak(q.audioSentence) },
                            onPlaySlow = { ttsManager.speak(q.audioSentence, isSlow = true) }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    when (q.type) {
                        QuestionType.WORD_SCRAMBLE -> {
                            WordScrambleView(
                                selectedWords = selectedWords,
                                availableWords = availableWords,
                                onWordRemoved = { word ->
                                    selectedWords.remove(word)
                                    availableWords.add(word)
                                },
                                onWordAdded = { word ->
                                    availableWords.remove(word)
                                    selectedWords.add(word)
                                }
                            )
                        }

                        QuestionType.SPEAK_OUT_LOUD -> {
                            SpeakPracticeView(
                                targetSentence = q.targetSentence,
                                spokenText = spokenAnswer,
                                isListening = isListening,
                                onMicClick = { requestSpeech() },
                                onManualTyped = { spokenAnswer = it }
                            )
                        }

                        QuestionType.FILL_IN_BLANK, QuestionType.LISTEN_AND_CHOOSE -> {
                            MultipleChoiceView(
                                options = q.multipleChoiceOptions,
                                selectedChoice = selectedChoice,
                                onSelect = { selectedChoice = it }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Feedback / Check Banner
        currentQuestion?.let { q ->
            val hasAnswer = when (q.type) {
                QuestionType.WORD_SCRAMBLE -> selectedWords.isNotEmpty()
                QuestionType.SPEAK_OUT_LOUD -> spokenAnswer.isNotBlank()
                QuestionType.FILL_IN_BLANK, QuestionType.LISTEN_AND_CHOOSE -> selectedChoice != null
            }

            BottomCheckBanner(
                checkResult = checkResult,
                targetSentence = q.targetSentence,
                explanation = q.explanation,
                hasAnswer = hasAnswer,
                onCheck = {
                    val isCorrect = when (q.type) {
                        QuestionType.WORD_SCRAMBLE -> {
                            val userSentence = selectedWords.joinToString(" ").trim()
                            userSentence.equals(q.targetSentence.trim(), ignoreCase = true)
                        }
                        QuestionType.SPEAK_OUT_LOUD -> {
                            val cleanUser = spokenAnswer.lowercase().replace(Regex("[^a-z0-9 ]"), "")
                            val cleanTarget = q.targetSentence.lowercase().replace(Regex("[^a-z0-9 ]"), "")
                            // Tolerant speech comparison: at least 70% of target words spoken
                            val targetWords = cleanTarget.split(" ")
                            val matchCount = targetWords.count { cleanUser.contains(it) }
                            (matchCount.toFloat() / targetWords.size.toFloat()) >= 0.65f
                        }
                        QuestionType.FILL_IN_BLANK, QuestionType.LISTEN_AND_CHOOSE -> {
                            selectedChoice?.trim().equals(q.targetSentence.trim(), ignoreCase = true)
                        }
                    }

                    checkResult = isCorrect
                    if (isCorrect) {
                        correctAnswersCount++
                    } else {
                        currentHearts = (currentHearts - 1).coerceAtLeast(0)
                        onLoseHeart()
                        if (currentHearts <= 0) {
                            showOutOfHeartsDialog = true
                        }
                    }
                },
                onContinue = {
                    if (currentQuestionIndex + 1 < questions.size) {
                        currentQuestionIndex++
                    } else {
                        isLessonFinished = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Exit Confirmation Dialog
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("Leave lesson?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure? You will lose your current progress in this session.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirmDialog = false
                    onExit()
                }) {
                    Text("END SESSION", color = HeartRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("KEEP LEARNING", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Out of Hearts Dialog
    if (showOutOfHeartsDialog) {
        AlertDialog(
            onDismissRequest = { showOutOfHeartsDialog = false },
            title = { Text("You ran out of Hearts! ❤️", fontWeight = FontWeight.Bold, color = HeartRed) },
            text = { Text("Don't worry! You can refill your hearts for free to keep practicing, or take a short break.") },
            confirmButton = {
                DuolingoButton(
                    text = "REFILL HEARTS & CONTINUE",
                    onClick = {
                        currentHearts = 5
                        onRefillHearts()
                        showOutOfHeartsDialog = false
                    },
                    buttonColor = HeartRed,
                    shadowColor = HeartRedDark
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    showOutOfHeartsDialog = false
                    onExit()
                }) {
                    Text("EXIT TO HOME")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordScrambleView(
    selectedWords: List<String>,
    availableWords: List<String>,
    onWordRemoved: (String) -> Unit,
    onWordAdded: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Selected words tray
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            if (selectedWords.isEmpty()) {
                Text(
                    text = "Tap the words below in the correct order",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedWords.forEach { word ->
                        WordChip(
                            word = word,
                            isSelected = true,
                            onClick = { onWordRemoved(word) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Available words bank
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            availableWords.forEach { word ->
                WordChip(
                    word = word,
                    isSelected = false,
                    onClick = { onWordAdded(word) }
                )
            }
        }
    }
}

@Composable
private fun WordChip(
    word: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = if (isSelected) SpeakBlue else Color(0xFFDCDCDC),
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (isSelected) SpeakBlueLight else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("word_chip_$word")
    ) {
        Text(
            text = word,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) SpeakBlueDark else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SpeakPracticeView(
    targetSentence: String,
    spokenText: String,
    isListening: Boolean,
    onMicClick: () -> Unit,
    onManualTyped: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TARGET SENTENCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"$targetSentence\"",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Mic Button
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(if (isListening) HeartRed else SpeakBlue)
                .clickable(onClick = onMicClick)
                .testTag("lesson_mic_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop recording" else "Speak now",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isListening) "Listening... speak now in English" else "Tap microphone to speak",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isListening) HeartRed else SpeakBlue
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Spoken Text Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(14.dp)
        ) {
            Text(
                text = if (spokenText.isNotBlank()) "You said: \"$spokenText\"" else "Your speech will appear here...",
                fontSize = 15.sp,
                fontWeight = if (spokenText.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                color = if (spokenText.isNotBlank()) MaterialTheme.colorScheme.onSurface else Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MultipleChoiceView(
    options: List<String>,
    selectedChoice: String?,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = selectedChoice == option
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 2.dp,
                        color = if (isSelected) SpeakBlue else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(if (isSelected) SpeakBlueLight else MaterialTheme.colorScheme.surface)
                    .clickable { onSelect(option) }
                    .padding(16.dp)
                    .testTag("choice_option_$index")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (isSelected) SpeakBlue else Color.LightGray, CircleShape)
                            .background(if (isSelected) SpeakBlue else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    Text(
                        text = option,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) SpeakBlueDark else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomCheckBanner(
    checkResult: Boolean?,
    targetSentence: String,
    explanation: String,
    hasAnswer: Boolean,
    onCheck: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when (checkResult) {
        true -> DuolingoGreenLight
        false -> HeartRedLight
        null -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(bgColor)
            .padding(20.dp)
            .testTag("lesson_bottom_banner")
    ) {
        Column {
            if (checkResult == true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎉", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nice job! Excellent sentence!",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = DuolingoGreenDark
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                DuolingoButton(
                    text = "CONTINUE",
                    onClick = onContinue,
                    buttonColor = DuolingoGreen,
                    shadowColor = DuolingoGreenDark,
                    testTag = "continue_button"
                )
            } else if (checkResult == false) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "❌", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Correct solution:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = HeartRedDark
                        )
                        Text(
                            text = targetSentence,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = HeartRedDark
                        )
                    }
                }
                if (explanation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💡 $explanation",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                DuolingoButton(
                    text = "GOT IT",
                    onClick = onContinue,
                    buttonColor = HeartRed,
                    shadowColor = HeartRedDark,
                    testTag = "got_it_button"
                )
            } else {
                // Not checked yet
                DuolingoButton(
                    text = "CHECK",
                    onClick = onCheck,
                    enabled = hasAnswer,
                    buttonColor = DuolingoGreen,
                    shadowColor = DuolingoGreenDark,
                    testTag = "check_answer_button"
                )
            }
        }
    }
}

@Composable
private fun LessonCompleteView(
    lesson: Lesson,
    correctCount: Int,
    totalCount: Int,
    onContinue: () -> Unit
) {
    val accuracy = if (totalCount > 0) ((correctCount.toFloat() / totalCount.toFloat()) * 100).toInt() else 100

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🎉", fontSize = 64.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lesson Complete!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = DuolingoGreen
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You're making great progress in speaking English!",
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Rewards Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GoldYellow.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "TOTAL XP", fontSize = 11.sp, fontWeight = FontWeight.Black, color = GoldYellow)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "+${lesson.xpReward}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = GoldYellow)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SpeakBlue.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "ACCURACY", fontSize = 11.sp, fontWeight = FontWeight.Black, color = SpeakBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$accuracy%", fontSize = 24.sp, fontWeight = FontWeight.Black, color = SpeakBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        DuolingoButton(
            text = "CONTINUE",
            onClick = onContinue,
            buttonColor = DuolingoGreen,
            shadowColor = DuolingoGreenDark,
            testTag = "finish_lesson_continue_button"
        )
    }
}
