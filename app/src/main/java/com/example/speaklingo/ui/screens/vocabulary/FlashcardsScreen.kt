package com.example.speaklingo.ui.screens.vocabulary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speaklingo.audio.TtsManager
import com.example.speaklingo.data.local.VocabularyEntity
import com.example.speaklingo.data.model.FlashcardTopicCatalog
import com.example.speaklingo.data.model.GeneratedFlashcard
import com.example.speaklingo.data.model.toFlashcard
import com.example.speaklingo.data.repository.AiTutorRepository
import com.example.speaklingo.data.repository.LearningRepository
import com.example.speaklingo.ui.components.DuolingoButton
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.DuolingoGreenLight
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GoldYellowDark
import com.example.ui.theme.HeartRed
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark
import com.example.ui.theme.SpeakBlueLight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.speaklingo.ui.components.RoadmapFlashcardGeneratorComponent
import com.example.speaklingo.ui.viewmodel.FlashcardGeneratorViewModel
import kotlinx.coroutines.launch

/**
 * Screen modes for Flashcards.
 */
enum class FlashcardScreenMode(val title: String, val iconEmoji: String) {
    GENERATOR("AI Generator", "✨"),
    STUDY_DECK("Flashcard Deck", "🃏"),
    SAVED_VAULT("Saved Vault", "📚")
}

/**
 * Interactive Jetpack Compose screen that allows users to generate vocabulary flashcards
 * using the Gemini API and save them directly to a local Room database.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlashcardsScreen(
    learningRepository: LearningRepository,
    aiTutorRepository: AiTutorRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // TTS Audio Manager
    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    // Active Screen Mode
    var currentMode by remember { mutableStateOf(FlashcardScreenMode.GENERATOR) }

    // Flashcard Generator ViewModel tailored to Roadmap Progress
    val generatorViewModel: FlashcardGeneratorViewModel = viewModel(
        factory = FlashcardGeneratorViewModel.provideFactory(learningRepository, aiTutorRepository)
    )

    // Generator inputs
    var selectedTopic by remember { mutableStateOf(FlashcardTopicCatalog.presets.first().title) }
    var customTopicInput by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(FlashcardTopicCatalog.proficiencyLevels[1]) }
    var cardCount by remember { mutableIntStateOf(5) }
    var isGenerating by remember { mutableStateOf(false) }

    // Active Flashcards Deck
    val generatedDeck = remember { mutableStateListOf<GeneratedFlashcard>() }
    var currentCardIndex by remember { mutableIntStateOf(0) }
    var isCardFlipped by remember { mutableStateOf(false) }

    // Room Database Saved Vocabulary
    val savedVocabularyList by learningRepository.allVocabulary.collectAsState(initial = emptyList())
    var vaultSearchQuery by remember { mutableStateOf("") }
    var vaultFilterMasteredOnly by remember { mutableStateOf(false) }

    // Helper: Generate cards with Gemini
    fun generateCards() {
        val topicToQuery = customTopicInput.trim().ifBlank { selectedTopic }
        isGenerating = true
        isCardFlipped = false

        coroutineScope.launch {
            val result = aiTutorRepository.generateFlashcards(
                topic = topicToQuery,
                level = selectedLevel,
                count = cardCount
            )

            // Check if words are already in Room database
            val updated = result.map { card ->
                val alreadySaved = learningRepository.isWordSaved(card.word)
                card.copy(isSavedToRoom = alreadySaved)
            }

            generatedDeck.clear()
            generatedDeck.addAll(updated)
            currentCardIndex = 0
            isGenerating = false
            currentMode = FlashcardScreenMode.STUDY_DECK

            snackbarHostState.showSnackbar("Generated ${updated.size} flashcards on '$topicToQuery' with Gemini!")
        }
    }

    // Helper: Save a single card to Room
    fun saveCardToRoom(card: GeneratedFlashcard, index: Int) {
        coroutineScope.launch {
            learningRepository.addVocabulary(card.toVocabularyEntity())
            if (index in generatedDeck.indices) {
                generatedDeck[index] = generatedDeck[index].copy(isSavedToRoom = true)
            }
            snackbarHostState.showSnackbar("Saved '${card.word}' to your Vocabulary Vault! ⭐")
        }
    }

    // Helper: Save all cards to Room in bulk
    fun saveAllCardsToRoom() {
        coroutineScope.launch {
            val entities = generatedDeck.map { it.toVocabularyEntity() }
            learningRepository.addVocabularyList(entities)
            for (i in generatedDeck.indices) {
                generatedDeck[i] = generatedDeck[i].copy(isSavedToRoom = true)
            }
            snackbarHostState.showSnackbar("Saved all ${entities.size} flashcards to Room Database! 📥⭐")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Screen Header
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
                        Text(text = "🎴", fontSize = 22.sp)
                        Text(
                            text = "AI Vocabulary Flashcards",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "Powered by Gemini 3.5 Flash & Room Database",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuolingoGreenDark
                    )
                }

                // Saved count badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SpeakBlueLight,
                    modifier = Modifier.clickable { currentMode = FlashcardScreenMode.SAVED_VAULT }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = SpeakBlueDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${savedVocabularyList.size} Saved",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = SpeakBlueDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Mode Tabs (AI Generator, Flashcard Deck, Saved Vault)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FlashcardScreenMode.entries.forEach { mode ->
                    val isSelected = currentMode == mode
                    val tabColor = when (mode) {
                        FlashcardScreenMode.GENERATOR -> DuolingoGreen
                        FlashcardScreenMode.STUDY_DECK -> SpeakBlue
                        FlashcardScreenMode.SAVED_VAULT -> GoldYellow
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) tabColor else Color.Transparent)
                            .clickable {
                                currentMode = mode
                                isCardFlipped = false
                            }
                            .padding(vertical = 8.dp)
                            .testTag("tab_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = mode.iconEmoji, fontSize = 13.sp)
                            Text(
                                text = mode.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Screen Content Body
            when (currentMode) {
                FlashcardScreenMode.GENERATOR -> {
                    FlashcardGeneratorView(
                        generatorViewModel = generatorViewModel,
                        selectedTopic = selectedTopic,
                        customTopic = customTopicInput,
                        selectedLevel = selectedLevel,
                        cardCount = cardCount,
                        isGenerating = isGenerating,
                        onSelectPresetTopic = { selectedTopic = it },
                        onCustomTopicChange = { customTopicInput = it },
                        onSelectLevel = { selectedLevel = it },
                        onSelectCount = { cardCount = it },
                        onGenerate = { generateCards() }
                    )
                }

                FlashcardScreenMode.STUDY_DECK -> {
                    if (generatedDeck.isEmpty()) {
                        // Empty Deck State
                        EmptyDeckPrompt(
                            onGoToGenerator = { currentMode = FlashcardScreenMode.GENERATOR },
                            onLoadFromVault = {
                                if (savedVocabularyList.isNotEmpty()) {
                                    generatedDeck.clear()
                                    generatedDeck.addAll(savedVocabularyList.map { it.toFlashcard() })
                                    currentCardIndex = 0
                                    isCardFlipped = false
                                }
                            },
                            hasSavedWords = savedVocabularyList.isNotEmpty()
                        )
                    } else {
                        FlashcardDeckStudyView(
                            deck = generatedDeck,
                            currentIndex = currentCardIndex,
                            isFlipped = isCardFlipped,
                            onFlip = { isCardFlipped = !isCardFlipped },
                            onNext = {
                                if (currentCardIndex < generatedDeck.size - 1) {
                                    currentCardIndex++
                                    isCardFlipped = false
                                }
                            },
                            onPrevious = {
                                if (currentCardIndex > 0) {
                                    currentCardIndex--
                                    isCardFlipped = false
                                }
                            },
                            onPlayTts = { word -> ttsManager.speak(word) },
                            onSaveCard = { card -> saveCardToRoom(card, currentCardIndex) },
                            onSaveAll = { saveAllCardsToRoom() }
                        )
                    }
                }

                FlashcardScreenMode.SAVED_VAULT -> {
                    SavedVocabularyVaultView(
                        savedList = savedVocabularyList,
                        searchQuery = vaultSearchQuery,
                        filterMasteredOnly = vaultFilterMasteredOnly,
                        onSearchChange = { vaultSearchQuery = it },
                        onToggleMasteredFilter = { vaultFilterMasteredOnly = !vaultFilterMasteredOnly },
                        onPlayTts = { word -> ttsManager.speak(word) },
                        onToggleMastered = { id, state ->
                            coroutineScope.launch {
                                learningRepository.toggleMastered(id, state)
                            }
                        },
                        onDelete = { id ->
                            coroutineScope.launch {
                                learningRepository.deleteVocabulary(id)
                                snackbarHostState.showSnackbar("Word removed from vault")
                            }
                        },
                        onStudyInDeck = { cardsToStudy ->
                            generatedDeck.clear()
                            generatedDeck.addAll(cardsToStudy.map { it.toFlashcard() })
                            currentCardIndex = 0
                            isCardFlipped = false
                            currentMode = FlashcardScreenMode.STUDY_DECK
                        }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

/**
 * 1. GENERATOR VIEW: Topic selector, proficiency level, card count, and trigger button.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlashcardGeneratorView(
    generatorViewModel: FlashcardGeneratorViewModel,
    selectedTopic: String,
    customTopic: String,
    selectedLevel: String,
    cardCount: Int,
    isGenerating: Boolean,
    onSelectPresetTopic: (String) -> Unit,
    onCustomTopicChange: (String) -> Unit,
    onSelectLevel: (String) -> Unit,
    onSelectCount: (Int) -> Unit,
    onGenerate: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("flashcard_generator_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Smart Roadmap Flashcard Generator Component
        item {
            RoadmapFlashcardGeneratorComponent(viewModel = generatorViewModel)
        }

        // Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SpeakBlueLight)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SpeakBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🤖", fontSize = 22.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Flashcard Engine",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = SpeakBlueDark
                        )
                        Text(
                            text = "Gemini creates rich flashcards with IPA phonetics, Hindi translations, mnemonics, and example sentences.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Section: Select Topic Preset
        item {
            Text(
                text = "1. CHOOSE A TOPIC PRESET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlashcardTopicCatalog.presets.forEach { preset ->
                    val isSelected = selectedTopic == preset.title && customTopic.isBlank()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) DuolingoGreen else MaterialTheme.colorScheme.surface)
                            .border(
                                1.5.dp,
                                if (isSelected) DuolingoGreenDark else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onSelectPresetTopic(preset.title)
                                onCustomTopicChange("")
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = preset.iconEmoji, fontSize = 14.sp)
                            Text(
                                text = preset.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Section: Custom Topic Input
        item {
            Text(
                text = "OR ENTER A CUSTOM THEME / TOPIC",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = customTopic,
                onValueChange = onCustomTopicChange,
                placeholder = { Text("e.g. Legal contracts, Medical terms, Coffee flavors...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_topic_input"),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )
        }

        // Section: CEFR Proficiency Level
        item {
            Text(
                text = "2. PROFICIENCY DIFFICULTY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlashcardTopicCatalog.proficiencyLevels.forEach { level ->
                    val isSelected = selectedLevel == level
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SpeakBlue else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onSelectLevel(level) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Section: Number of Cards (3, 5, 8)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "3. NUMBER OF FLASHCARDS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.Gray
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(3, 5, 8).forEach { count ->
                        val isSelected = cardCount == count
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) GoldYellow else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onSelectCount(count) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color(0xFF422006) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Section: Generate Button
        item {
            Spacer(modifier = Modifier.height(6.dp))
            if (isGenerating) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = DuolingoGreen)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Gemini AI is crafting your vocabulary deck...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            } else {
                DuolingoButton(
                    text = "GENERATE FLASHCARDS ✨",
                    onClick = onGenerate,
                    buttonColor = DuolingoGreen,
                    shadowColor = DuolingoGreenDark,
                    testTag = "generate_flashcards_button"
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 2. STUDY DECK VIEW: 3D interactive flipping flashcard with controls.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlashcardDeckStudyView(
    deck: List<GeneratedFlashcard>,
    currentIndex: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlayTts: (String) -> Unit,
    onSaveCard: (GeneratedFlashcard) -> Unit,
    onSaveAll: () -> Unit
) {
    val currentCard = deck.getOrNull(currentIndex) ?: return
    val progress = (currentIndex + 1).toFloat() / deck.size

    // Smooth 3D Flip Rotation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "card_flip_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("flashcard_study_deck_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Deck Progress Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CARD ${currentIndex + 1} OF ${deck.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = SpeakBlueDark
            )

            // Bulk Save All Button
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GoldYellow.copy(alpha = 0.2f),
                modifier = Modifier.clickable(onClick = onSaveAll)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdded,
                        contentDescription = null,
                        tint = GoldYellowDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Save All to Vault 📥",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldYellowDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = SpeakBlue,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 3D Flippable Card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onFlip
                )
                .testTag("flippable_card"),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // FRONT SIDE of Flashcard
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top badges
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
                                    text = currentCard.partOfSpeech.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SpeakBlueDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Save Bookmark Button
                            IconButton(
                                onClick = { onSaveCard(currentCard) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentCard.isSavedToRoom) Icons.Default.BookmarkAdded else Icons.Default.Bookmark,
                                    contentDescription = "Save to Room",
                                    tint = if (currentCard.isSavedToRoom) DuolingoGreen else Color.Gray
                                )
                            }
                        }

                        // Center: Main Word & Pronunciation
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentCard.word,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = currentCard.phonetic,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Audio Speaker Button
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SpeakBlueLight)
                                    .clickable { onPlayTts(currentCard.word) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Listen pronunciation",
                                        tint = SpeakBlueDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Pronounce",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SpeakBlueDark
                                    )
                                }
                            }
                        }

                        // Bottom Flip Prompt
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flip,
                                contentDescription = null,
                                tint = DuolingoGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Tap card to flip for Hindi meaning & examples ↻",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuolingoGreenDark
                            )
                        }
                    }
                }
            } else {
                // BACK SIDE of Flashcard (Mirrored horizontally for natural flip)
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Word badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentCard.word,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Save status
                            if (currentCard.isSavedToRoom) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DuolingoGreenLight
                                ) {
                                    Text(
                                        text = "Saved in Vault ⭐",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DuolingoGreenDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GoldYellow.copy(alpha = 0.2f))
                                        .clickable { onSaveCard(currentCard) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Save Word ⭐",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldYellowDark
                                    )
                                }
                            }
                        }

                        // Middle Information Stack
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Hindi Meaning
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GoldYellow.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "🇮🇳", fontSize = 18.sp)
                                    Text(
                                        text = currentCard.hindiMeaning,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF6B4500)
                                    )
                                }
                            }

                            // English Definition
                            Column {
                                Text(
                                    text = "DEFINITION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Gray,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentCard.englishMeaning,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            }

                            // Example Sentence
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "EXAMPLE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Gray,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "“${currentCard.exampleSentence}”",
                                        fontSize = 12.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            // Mnemonic / Memory Hook
                            if (currentCard.mnemonicOrTip != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SpeakBlueLight.copy(alpha = 0.6f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = GoldYellowDark,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = currentCard.mnemonicOrTip,
                                            fontSize = 11.sp,
                                            color = SpeakBlueDark,
                                            lineHeight = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Synonyms Chips
                            if (currentCard.synonyms.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    currentCard.synonyms.forEach { syn ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = syn,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Flip back prompt
                        Text(
                            text = "Tap to flip back to word ↺",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Navigation Action Controls (Previous, Flip, Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous Card Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (currentIndex > 0) SpeakBlue else Color.LightGray)
                    .clickable(
                        enabled = currentIndex > 0,
                        onClick = onPrevious
                    )
                    .testTag("prev_card_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous card",
                    tint = Color.White
                )
            }

            // Flip Card Button
            DuolingoButton(
                text = if (isFlipped) "SHOW WORD ↺" else "FLIP CARD ↻",
                onClick = onFlip,
                buttonColor = GoldYellow,
                shadowColor = GoldYellowDark,
                textColor = Color(0xFF422006),
                modifier = Modifier.width(170.dp),
                testTag = "flip_card_action_button"
            )

            // Next Card Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (currentIndex < deck.size - 1) DuolingoGreen else Color.LightGray)
                    .clickable(
                        enabled = currentIndex < deck.size - 1,
                        onClick = onNext
                    )
                    .testTag("next_card_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next card",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Empty prompt when the study deck has not been generated yet.
 */
@Composable
private fun EmptyDeckPrompt(
    onGoToGenerator: () -> Unit,
    onLoadFromVault: () -> Unit,
    hasSavedWords: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🃏", fontSize = 54.sp)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "No Flashcards Active",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Generate a custom deck using Gemini AI or study words already saved in your local Room database vault.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        DuolingoButton(
            text = "GENERATE WITH GEMINI ✨",
            onClick = onGoToGenerator,
            buttonColor = DuolingoGreen,
            shadowColor = DuolingoGreenDark
        )

        if (hasSavedWords) {
            Spacer(modifier = Modifier.height(10.dp))
            DuolingoButton(
                text = "STUDY SAVED VAULT WORDS 📚",
                onClick = onLoadFromVault,
                buttonColor = SpeakBlue,
                shadowColor = SpeakBlueDark
            )
        }
    }
}

/**
 * 3. SAVED VAULT VIEW: Displays cards saved to local Room database with search & study options.
 */
@Composable
private fun SavedVocabularyVaultView(
    savedList: List<VocabularyEntity>,
    searchQuery: String,
    filterMasteredOnly: Boolean,
    onSearchChange: (String) -> Unit,
    onToggleMasteredFilter: () -> Unit,
    onPlayTts: (String) -> Unit,
    onToggleMastered: (Int, Boolean) -> Unit,
    onDelete: (Int) -> Unit,
    onStudyInDeck: (List<VocabularyEntity>) -> Unit
) {
    val filtered = savedList.filter {
        val matchesSearch = it.word.contains(searchQuery, ignoreCase = true) ||
                it.hindiMeaning.contains(searchQuery, ignoreCase = true) ||
                it.englishMeaning.contains(searchQuery, ignoreCase = true)
        val matchesMastered = !filterMasteredOnly || it.isMastered
        matchesSearch && matchesMastered
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saved_vocabulary_vault_view")
    ) {
        // Search & Filter Row
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search saved English words or Hindi meaning...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mastered toggle pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (filterMasteredOnly) DuolingoGreen else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable(onClick = onToggleMasteredFilter)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (filterMasteredOnly) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (filterMasteredOnly) Color.White else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Mastered Only",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (filterMasteredOnly) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Study in Deck Button
            if (filtered.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SpeakBlueLight,
                    modifier = Modifier.clickable { onStudyInDeck(filtered) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🃏", fontSize = 12.sp)
                        Text(
                            text = "Study ${filtered.size} in Deck",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = SpeakBlueDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Saved Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (savedList.isEmpty()) "No flashcards saved in Room yet. Generate some!" else "No matching words found.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            items(filtered) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = item.word,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = item.phonetic,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onPlayTts(item.word) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Pronounce",
                                        tint = SpeakBlueDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onToggleMastered(item.id, !item.isMastered) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isMastered) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Toggle Mastered",
                                        tint = if (item.isMastered) DuolingoGreen else Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDelete(item.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "🇮🇳 ${item.hindiMeaning}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpeakBlueDark
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = item.englishMeaning,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "“${item.exampleSentence}”",
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
