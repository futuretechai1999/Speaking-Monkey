package com.example.speaklingo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speaklingo.audio.TtsManager
import com.example.speaklingo.data.model.GeneratedFlashcard
import com.example.speaklingo.ui.viewmodel.FlashcardGeneratorUiState
import com.example.speaklingo.ui.viewmodel.FlashcardGeneratorViewModel
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.DuolingoGreenLight
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GoldYellowDark
import com.example.ui.theme.HeartRed
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark
import com.example.ui.theme.SpeakBlueLight

/**
 * A simple, polished Jetpack Compose UI component that uses Gemini to generate
 * vocabulary flashcards tailored directly to the user's current progress in
 * the English learning roadmap.
 */
@Composable
fun RoadmapFlashcardGeneratorComponent(
    viewModel: FlashcardGeneratorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }

    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    RoadmapFlashcardGeneratorCard(
        uiState = uiState,
        onGenerate = { viewModel.generateFlashcardsForCurrentProgress() },
        onSaveCard = { viewModel.saveCardToVault(it) },
        onSaveAll = { viewModel.saveAllCardsToVault() },
        onSelectTopic = { viewModel.updateSelectedTopic(it) },
        onDismissMessage = { viewModel.dismissMessages() },
        onPlayAudio = { ttsManager.speak(it) },
        modifier = modifier
    )
}

/**
 * Presentational Composable for the Roadmap Flashcard Generator Card.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoadmapFlashcardGeneratorCard(
    uiState: FlashcardGeneratorUiState,
    onGenerate: () -> Unit,
    onSaveCard: (GeneratedFlashcard) -> Unit,
    onSaveAll: () -> Unit,
    onSelectTopic: (String) -> Unit,
    onDismissMessage: () -> Unit,
    onPlayAudio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("roadmap_flashcard_generator_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Gradient Banner & Active Level Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(DuolingoGreen, SpeakBlue)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎴", fontSize = 20.sp)
                    }

                    Column {
                        Text(
                            text = "Roadmap AI Flashcards",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tailored by Gemini for your current tier",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DuolingoGreenDark
                        )
                    }
                }

                // Level Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DuolingoGreenLight.copy(alpha = 0.25f))
                        .border(1.5.dp, DuolingoGreen, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = uiState.currentRoadmapLevelCode,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = DuolingoGreenDark
                    )
                }
            }

            // Current Roadmap Progress Milestone Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = SpeakBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Current Roadmap Progress",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${(uiState.progressPercentage * 100).toInt()}% Complete",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = SpeakBlueDark
                        )
                    }

                    // Progress Bar
                    val animatedProgress by animateFloatAsState(
                        targetValue = uiState.progressPercentage,
                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                        label = "roadmap_progress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SpeakBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.currentUnitTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.completedLessonsCount}/${uiState.totalLessonsCount} Lessons",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Milestone Topic Selector Chips
            if (uiState.suggestedRoadmapTopics.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Milestone Focus:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.suggestedRoadmapTopics.forEach { topic ->
                            val isSelected = uiState.selectedTopic == topic
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) DuolingoGreen else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { onSelectTopic(topic) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("roadmap_topic_chip_$topic")
                            ) {
                                Text(
                                    text = topic,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Status & Feedback Messages
            uiState.successMessage?.let { msg ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = DuolingoGreenLight.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DuolingoGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DuolingoGreenDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = msg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuolingoGreenDark
                            )
                        }
                        IconButton(
                            onClick = onDismissMessage,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(14.dp),
                                tint = DuolingoGreenDark
                            )
                        }
                    }
                }
            }

            uiState.errorMessage?.let { err ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = HeartRed.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HeartRed)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = err,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeartRed
                        )
                        IconButton(
                            onClick = onDismissMessage,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(14.dp),
                                tint = HeartRed
                            )
                        }
                    }
                }
            }

            // Generate Button
            DuolingoButton(
                text = if (uiState.isGenerating) "GEMINI GENERATING CARDS... ✨" else "GENERATE FLASHCARDS WITH GEMINI ✨",
                enabled = !uiState.isGenerating,
                onClick = onGenerate,
                buttonColor = DuolingoGreen,
                shadowColor = DuolingoGreenDark,
                modifier = Modifier.testTag("generate_roadmap_flashcards_btn")
            )

            // Loading Spinner Indicator
            AnimatedVisibility(
                visible = uiState.isGenerating,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = DuolingoGreen,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Aria AI is tailoring vocabulary for '${uiState.selectedTopic}'...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Generated Flashcards List Display
            if (uiState.generatedCards.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generated Cards (${uiState.generatedCards.size}):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!uiState.isAllSaved) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SpeakBlue)
                                    .clickable { onSaveAll() }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("save_all_cards_to_vault_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Save All to Vault",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Render Individual Cards
                    uiState.generatedCards.forEachIndexed { index, card ->
                        FlashcardItemView(
                            card = card,
                            onPlayAudio = { onPlayAudio(card.word) },
                            onSave = { onSaveCard(card) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual generated flashcard item showing word, phonetic guide, definition,
 * Hindi meaning, example sentence, and instant Room database save action.
 */
@Composable
private fun FlashcardItemView(
    card: GeneratedFlashcard,
    onPlayAudio: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("generated_flashcard_${card.word}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (card.isSavedToRoom) DuolingoGreenLight else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Word, Part of speech, TTS Speaker & Save to Vault Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = card.word,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Part of speech pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SpeakBlueLight.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = card.partOfSpeech.lowercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpeakBlueDark
                        )
                    }

                    // Audio speaker button
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen to pronunciation",
                            tint = DuolingoGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Save to Vault Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (card.isSavedToRoom) DuolingoGreen else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable(enabled = !card.isSavedToRoom) { onSave() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("save_card_btn_${card.word}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (card.isSavedToRoom) Icons.Default.Check else Icons.Default.Star,
                            contentDescription = null,
                            tint = if (card.isSavedToRoom) Color.White else GoldYellowDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (card.isSavedToRoom) "Saved ✓" else "Save ⭐",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (card.isSavedToRoom) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // IPA Phonetic pronunciation guide
            Text(
                text = card.phonetic,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // English definition
            Text(
                text = card.englishMeaning,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Hindi Meaning Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldYellow.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "हिन्दी",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldYellowDark
                    )
                }
                Text(
                    text = card.hindiMeaning,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Example sentence
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Text(
                    text = "“${card.exampleSentence}”",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Mnemonic / Memory hook if available
            card.mnemonicOrTip?.takeIf { it.isNotBlank() }?.let { tip ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = GoldYellowDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = tip,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
