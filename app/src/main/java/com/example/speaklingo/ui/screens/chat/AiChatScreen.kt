package com.example.speaklingo.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.speaklingo.audio.SpeechInputManager
import com.example.speaklingo.audio.TtsManager
import com.example.speaklingo.data.local.ChatMessageEntity
import com.example.speaklingo.data.local.VocabularyEntity
import com.example.speaklingo.data.model.PracticeScenario
import com.example.speaklingo.data.model.ScenarioCatalog
import com.example.speaklingo.data.remote.ChatBotModel
import com.example.speaklingo.data.repository.AiTutorRepository
import com.example.speaklingo.data.repository.LearningRepository
import com.example.speaklingo.ui.components.DuolingoButton
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.DuolingoGreenLight
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GoldYellowDark
import com.example.ui.theme.HeartRed
import com.example.ui.theme.HeartRedLight
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark
import com.example.ui.theme.SpeakBlueLight
import kotlinx.coroutines.launch

/**
 * Chat Interface with Aria AI Tutor.
 *
 * Specifically designed for learners to interact with Aria to ask questions
 * about English grammar, vocabulary, idioms, sentence corrections, and dialogue practice.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiChatScreen(
    aiTutorRepository: AiTutorRepository,
    learningRepository: LearningRepository? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedScenario by remember { mutableStateOf(ScenarioCatalog.scenarios.first()) }
    var selectedModel by remember { mutableStateOf(ChatBotModel.FLASH) }

    val messagesFlow = remember(selectedScenario.id) { aiTutorRepository.getMessages(selectedScenario.id) }
    val messages by messagesFlow.collectAsState(initial = emptyList())

    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }

    // Quick word save dialog state
    var wordToSaveModal by remember { mutableStateOf<String?>(null) }

    // TTS Manager for voice output
    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    // Speech Input for real-time voice queries
    val speechManager = remember {
        SpeechInputManager(
            context = context,
            onListeningChanged = { isListening = it },
            onResultReceived = { spoken ->
                inputText = spoken
            },
            onPartialResult = { partial ->
                inputText = partial
            },
            onErrorReceived = { /* handled */ }
        )
    }

    DisposableEffect(Unit) {
        onDispose { speechManager.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechManager.startListening()
        }
    }

    fun handleMicClick() {
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

    // Seed initial greeting message from Aria if conversation is empty
    LaunchedEffect(selectedScenario.id, messages.isEmpty()) {
        if (messages.isEmpty()) {
            coroutineScope.launch {
                aiTutorRepository.sendMessage(
                    scenarioId = selectedScenario.id,
                    scenarioContext = "${selectedScenario.title}: ${selectedScenario.roleTitle}",
                    userText = "Hi Aria! I'm ready to learn English grammar and vocabulary with you.",
                    modelId = selectedModel.modelId
                )
            }
        }
    }

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendUserMessage(text: String) {
        val textToSend = text.trim()
        if (textToSend.isBlank() || isSending) return
        inputText = ""
        isSending = true

        coroutineScope.launch {
            val roleInstruction = when (selectedModel) {
                ChatBotModel.PRO ->
                    "You are Professor Pro, an advanced IELTS/TOEFL master grammarian and linguist. When explaining grammar rules or vocabulary, provide in-depth rule breakdowns, etymology, and formal nuances."
                ChatBotModel.LITE ->
                    "You are Sprint Lite, a rapid English conversational and vocabulary coach. Provide fast, concise, bulleted explanations."
                ChatBotModel.FLASH ->
                    "You are Aria, a friendly, encouraging AI English tutor like on SpeakX and Duolingo. Explain grammar rules simply, provide practical examples, and teach vocabulary with synonyms and Hindi context."
            }

            val response = aiTutorRepository.sendMessage(
                scenarioId = selectedScenario.id,
                scenarioContext = "${selectedScenario.title}: ${selectedScenario.roleTitle}",
                userText = textToSend,
                modelId = selectedModel.modelId,
                customRoleInstruction = roleInstruction
            )
            isSending = false
            // Auto-speak tutor's reply
            ttsManager.speak(response.replyText)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
        ) {
            // Tutor Header Bar with Aria Status & Persona
            AriaTutorHeader(
                scenario = selectedScenario,
                selectedModel = selectedModel,
                onSelectModel = { selectedModel = it },
                onClearChat = {
                    coroutineScope.launch {
                        aiTutorRepository.clearHistory(selectedScenario.id)
                    }
                }
            )

            // Focus Mode Tabs (Grammar Doctor, Vocabulary Lab, Speaking Partner...)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ScenarioCatalog.scenarios) { scenario ->
                    val isSelected = scenario.id == selectedScenario.id
                    val tabColor = when (scenario.id) {
                        "grammar_tutor" -> DuolingoGreen
                        "vocab_tutor" -> SpeakBlue
                        "free_tutor" -> GoldYellow
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) tabColor else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedScenario = scenario }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("scenario_tab_${scenario.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = scenario.iconEmoji, fontSize = 15.sp)
                            Text(
                                text = scenario.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Quick Ask Grammar & Vocabulary Topic Chips
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                selectedScenario.suggestedPhrases.forEach { promptPhrase ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                SpeakBlue.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable {
                                inputText = promptPhrase
                                sendUserMessage(promptPhrase)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = GoldYellowDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = promptPhrase,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SpeakBlueDark
                            )
                        }
                    }
                }
            }

            // Messages Conversation Thread
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(6.dp)) }

                items(messages) { msg ->
                    AriaMessageBubble(
                        message = msg,
                        onPlayAudio = { ttsManager.speak(msg.text) },
                        onSaveWord = { word ->
                            wordToSaveModal = word
                        }
                    )
                }

                if (isSending) {
                    item {
                        AriaThinkingBubble(selectedModel = selectedModel)
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // Bottom Input Row (Voice Mic, OutlinedTextField, Send Button)
            ChatInputBar(
                inputText = inputText,
                onInputChange = { inputText = it },
                isListening = isListening,
                isSending = isSending,
                onMicClick = { handleMicClick() },
                onSendClick = { sendUserMessage(inputText) }
            )
        }

        // Quick Save Vocabulary Word Modal
        wordToSaveModal?.let { word ->
            SaveWordDialog(
                initialWord = word,
                onDismiss = { wordToSaveModal = null },
                onSave = { entity ->
                    wordToSaveModal = null
                    coroutineScope.launch {
                        learningRepository?.addVocabulary(entity)
                        snackbarHostState.showSnackbar("Saved '${entity.word}' to your Vocabulary Vault! ⭐")
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        )
    }
}

/**
 * Header section with Aria AI profile, online indicator, model picker, and clear history button.
 */
@Composable
private fun AriaTutorHeader(
    scenario: PracticeScenario,
    selectedModel: ChatBotModel,
    onSelectModel: (ChatBotModel) -> Unit,
    onClearChat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("aria_tutor_header"),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Aria Avatar with pulse ring
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(DuolingoGreen, SpeakBlue)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👩‍🏫", fontSize = 22.sp)
                        }

                        // Online green dot
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(DuolingoGreen)
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Aria AI English Tutor",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "✨", fontSize = 13.sp)
                        }
                        Text(
                            text = "Grammar Rules, Vocabulary & Pronunciation Coach",
                            fontSize = 11.sp,
                            color = DuolingoGreenDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Clear chat button
                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear chat history",
                        tint = Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // AI Model Personality Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChatBotModel.entries.forEach { model ->
                    val isChosen = model == selectedModel
                    val modelName = when (model) {
                        ChatBotModel.FLASH -> "Aria (Flash)"
                        ChatBotModel.PRO -> "Professor (Pro)"
                        ChatBotModel.LITE -> "Sprint (Lite)"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isChosen) SpeakBlue else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onSelectModel(model) }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = modelName,
                            fontSize = 11.sp,
                            fontWeight = if (isChosen) FontWeight.Black else FontWeight.Bold,
                            color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Chat bubble displaying tutor answers (with grammar rules, Hindi translation, TTS pronunciation)
 * or user messages (with AI Grammar Doctor mistake detection card).
 */
@Composable
private fun AriaMessageBubble(
    message: ChatMessageEntity,
    onPlayAudio: () -> Unit,
    onSaveWord: (String) -> Unit
) {
    var showHindi by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (message.isUser) "user_bubble" else "tutor_bubble"),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        if (!message.isUser) {
            // Tutor (Aria) Bubble
            Row(
                modifier = Modifier.fillMaxWidth(0.92f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Mini Aria Avatar
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(SpeakBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👩‍🏫", fontSize = 16.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Message content
                            Text(
                                text = message.text,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 21.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Bar: Listen Audio, Hindi Translation, Save Vocabulary
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Listen to Pronunciation Button
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SpeakBlueLight)
                                        .clickable(onClick = onPlayAudio)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Listen to pronunciation",
                                            tint = SpeakBlueDark,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Listen",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SpeakBlueDark
                                        )
                                    }
                                }

                                // Hindi Translation Toggle
                                if (message.hindiTranslation != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GoldYellow.copy(alpha = 0.2f))
                                            .clickable { showHindi = !showHindi }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Translate,
                                                contentDescription = null,
                                                tint = Color(0xFF8A6200),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = if (showHindi) "Hide हिन्दी" else "हिन्दी व्याख्या",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF8A6200)
                                            )
                                        }
                                    }
                                }

                                // Quick Bookmark / Add to Vocabulary
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DuolingoGreenLight)
                                        .clickable {
                                            // Extract first word or prompt
                                            val candidateWord = message.text
                                                .split(" ", "\n", ":")
                                                .firstOrNull { it.length > 3 && it.all { c -> c.isLetter() } }
                                                ?: "Grammar Note"
                                            onSaveWord(candidateWord)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = "Save word to vault",
                                            tint = DuolingoGreenDark,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Save Word",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DuolingoGreenDark
                                        )
                                    }
                                }
                            }

                            // Expanded Hindi Translation
                            if (showHindi && message.hindiTranslation != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = "🇮🇳", fontSize = 16.sp)
                                        Text(
                                            text = message.hindiTranslation,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 19.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // User Bubble
            Surface(
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomEnd = 18.dp, bottomStart = 18.dp),
                color = SpeakBlue
            ) {
                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    color = Color.White,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }

            // AI Grammar Doctor Feedback Card
            if (message.grammarCorrection != null || message.betterPhrasing != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .testTag("ai_grammar_doctor_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldYellow.copy(alpha = 0.15f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldYellow, DuolingoGreen)))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF9E6D00),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "AI Grammar Doctor Analysis",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF9E6D00)
                            )
                        }

                        if (message.betterPhrasing != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💡 Natural English: \"${message.betterPhrasing}\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (message.grammarCorrection != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📝 Grammar Rule: ${message.grammarCorrection}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated thinking indicator when Aria is evaluating grammar & preparing answer.
 */
@Composable
private fun AriaThinkingBubble(selectedModel: ChatBotModel) {
    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = SpeakBlue,
            strokeWidth = 2.dp
        )
        Text(
            text = when (selectedModel) {
                ChatBotModel.PRO -> "Professor Pro is analyzing grammar depth..."
                ChatBotModel.LITE -> "Sprint Lite is answering..."
                ChatBotModel.FLASH -> "Aria is analyzing grammar rules & writing reply..."
            },
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Bottom Input Bar with speech mic, text input, and send button.
 */
@Composable
private fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    isListening: Boolean,
    isSending: Boolean,
    onMicClick: () -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Voice Microphone Button
        val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = if (isListening) 1.25f else 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )

        Box(
            modifier = Modifier
                .size(46.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(if (isListening) HeartRed else SpeakBlueLight)
                .clickable(onClick = onMicClick)
                .testTag("chat_mic_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop voice input" else "Start speech recognition",
                tint = if (isListening) Color.White else SpeakBlueDark,
                modifier = Modifier.size(24.dp)
            )
        }

        // Text input field
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = { Text("Ask about grammar, tenses, or vocabulary...", fontSize = 13.sp) },
            modifier = Modifier
                .weight(1f)
                .testTag("chat_input_field"),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background
            ),
            maxLines = 3
        )

        // Send Button
        val canSend = inputText.isNotBlank() && !isSending
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (canSend) DuolingoGreen else Color.LightGray)
                .clickable(
                    enabled = canSend,
                    onClick = onSendClick
                )
                .testTag("chat_send_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Dialog enabling the user to save a word directly into their local Room vocabulary bank.
 */
@Composable
private fun SaveWordDialog(
    initialWord: String,
    onDismiss: () -> Unit,
    onSave: (VocabularyEntity) -> Unit
) {
    var word by remember { mutableStateOf(initialWord) }
    var meaning by remember { mutableStateOf("") }
    var hindiMeaning by remember { mutableStateOf("") }
    var exampleSentence by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "📚 Save to Vocabulary", fontSize = 18.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text("English Word") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = meaning,
                    onValueChange = { meaning = it },
                    label = { Text("English Definition") },
                    placeholder = { Text("e.g. Extremely happy and joyful") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = hindiMeaning,
                    onValueChange = { hindiMeaning = it },
                    label = { Text("Hindi Meaning (हिन्दी)") },
                    placeholder = { Text("e.g. अत्यधिक खुश") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = exampleSentence,
                    onValueChange = { exampleSentence = it },
                    label = { Text("Example Sentence") },
                    placeholder = { Text("e.g. She was elated with her score.") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                DuolingoButton(
                    text = "SAVE TO VAULT ⭐",
                    onClick = {
                        onSave(
                            VocabularyEntity(
                                word = word.trim(),
                                phonetic = "/${word.trim().lowercase()}/",
                                partOfSpeech = "vocabulary",
                                englishMeaning = meaning.ifBlank { "Important vocabulary word" },
                                hindiMeaning = hindiMeaning.ifBlank { "उपयोगी अंग्रेज़ी शब्द" },
                                exampleSentence = exampleSentence.ifBlank { "Practice using '$word' in daily English." }
                            )
                        )
                    },
                    buttonColor = DuolingoGreen,
                    shadowColor = DuolingoGreenDark
                )
            }
        }
    }
}
