package com.example.speaklingo.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.FrameLayout
import android.widget.VideoView
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionOff
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.speaklingo.audio.SpeechInputManager
import com.example.speaklingo.audio.TtsManager
import com.example.speaklingo.data.model.VeoLearnerResponse
import com.example.speaklingo.data.model.VeoScenario
import com.example.speaklingo.data.model.VeoVocabularyItem
import com.example.speaklingo.ui.viewmodel.VeoVideoScenarioPlayerUiState
import com.example.speaklingo.ui.viewmodel.VeoVideoScenarioPlayerViewModel
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.DuolingoGreenLight
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GoldYellowDark
import com.example.ui.theme.HeartRed
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark
import com.example.ui.theme.SpeakBlueLight
import kotlinx.coroutines.delay

/**
 * VeoVideoScenarioPlayer: An immersive, situational video player and roleplay component
 * utilizing Google's Veo API (veo-3.1-fast-generate-preview and veo-3.1-generate-preview)
 * for interactive English language practice.
 */
@Composable
fun VeoVideoScenarioPlayer(
    viewModel: VeoVideoScenarioPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }

    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("veo_video_scenario_player"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Scenario Selector Bar
        ScenarioSelectorCarousel(
            scenarios = uiState.availableScenarios,
            selectedScenario = uiState.currentScenario,
            onSelectScenario = { viewModel.selectScenario(it) }
        )

        // 2. Video Player Frame with Controls, Subtitles & Aspect Ratio Switcher
        VideoPlayerView(
            uiState = uiState,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onToggleAspect = {
                viewModel.setAspectRatio(if (uiState.aspectRatio == "16:9") "9:16" else "16:9")
            },
            onToggleSubtitles = { viewModel.toggleSubtitles() },
            onToggleHindi = { viewModel.toggleHindi() },
            onPositionUpdate = { currentSec, durationSec ->
                viewModel.onPlaybackPositionChanged(currentSec, durationSec)
            },
            onPlayStateChanged = { viewModel.setPlaying(it) }
        )

        // 3. Situational Roleplay Console: Dialogue, Character Context, and Speaking Challenge
        RoleplaySpeakingConsole(
            uiState = uiState,
            onSpeakTargetResponse = { text -> ttsManager.speak(text) },
            onEvaluateSpokenReply = { reply, target ->
                viewModel.evaluateSpokenReply(reply, target)
            },
            onDismissFeedback = { viewModel.dismissFeedback() }
        )

        // 4. Target Vocabulary & Situational Expressions
        VocabularyPillsSection(
            vocabulary = uiState.currentScenario.keyVocabulary,
            onPlayAudio = { ttsManager.speak(it) }
        )

        // 5. Cultural Etiquette Insight
        CulturalEtiquetteCard(tip = uiState.currentScenario.cultureTip)

        // 6. Generate Custom Scenario with Veo API Section
        GenerateVeoScenarioCard(
            uiState = uiState,
            onGenerate = { prompt, category, level, aspect, model ->
                viewModel.generateVeoScenario(prompt, category, level, aspect, model)
            }
        )
    }
}

/**
 * Horizontal Carousel allowing learners to switch between situational scenarios
 * (Airport, Cafe, Job Interview, Hotel, Doctor, or user-generated Veo scenes).
 */
@Composable
private fun ScenarioSelectorCarousel(
    scenarios: List<VeoScenario>,
    selectedScenario: VeoScenario,
    onSelectScenario: (VeoScenario) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scenario_selector_carousel"),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(scenarios) { scenario ->
            val isSelected = scenario.id == selectedScenario.id
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) SpeakBlue else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .clickable { onSelectScenario(scenario) }
                    .testTag("scenario_tab_${scenario.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = scenario.characterAvatarEmoji, fontSize = 20.sp)
                    Column {
                        Text(
                            text = scenario.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "${scenario.category} • ${scenario.cefrLevel}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (scenario.isCustomGenerated) {
                                Text(
                                    text = "✨ Veo AI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) GoldYellow else DuolingoGreenDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Embedded Video Player utilizing Android's native VideoView, with full video playback,
 * dynamic synchronized subtitles (English & Hindi), play/pause controls, and aspect ratio adaptation.
 */
@Composable
private fun VideoPlayerView(
    uiState: VeoVideoScenarioPlayerUiState,
    onTogglePlayPause: () -> Unit,
    onToggleAspect: () -> Unit,
    onToggleSubtitles: () -> Unit,
    onToggleHindi: () -> Unit,
    onPositionUpdate: (Int, Int) -> Unit,
    onPlayStateChanged: (Boolean) -> Unit
) {
    val scenario = uiState.currentScenario
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isVideoReady by remember { mutableStateOf(false) }
    var durationSec by remember { mutableStateOf(12) }
    var currentSec by remember { mutableStateOf(0) }

    // Coroutine tracking video playback time
    LaunchedEffect(uiState.isPlaying, isVideoReady) {
        while (uiState.isPlaying && isVideoReady) {
            videoViewRef?.let { vv ->
                if (vv.isPlaying) {
                    val pos = vv.currentPosition / 1000
                    val dur = (vv.duration / 1000).coerceAtLeast(1)
                    currentSec = pos
                    durationSec = dur
                    onPositionUpdate(pos, dur)
                }
            }
            delay(500)
        }
    }

    // Effect for play/pause toggle from UI state
    LaunchedEffect(uiState.isPlaying) {
        videoViewRef?.let { vv ->
            if (uiState.isPlaying) {
                vv.start()
            } else {
                vv.pause()
            }
        }
    }

    // Effect for scenario change
    LaunchedEffect(scenario.videoUrl) {
        isVideoReady = false
        currentSec = 0
        videoViewRef?.let { vv ->
            vv.setVideoURI(Uri.parse(scenario.videoUrl))
            vv.setOnPreparedListener { mp ->
                mp.isLooping = true
                val dur = (mp.duration / 1000).coerceAtLeast(1)
                durationSec = dur
                isVideoReady = true
                if (uiState.isPlaying) {
                    vv.start()
                }
                onPositionUpdate(0, dur)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("veo_video_player_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Video Frame Box with Aspect Ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (uiState.aspectRatio == "9:16") 380.dp else 220.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Native Android VideoView
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(Uri.parse(scenario.videoUrl))
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                val dur = (mp.duration / 1000).coerceAtLeast(1)
                                durationSec = dur
                                isVideoReady = true
                                onPositionUpdate(0, dur)
                            }
                            setOnCompletionListener {
                                onPlayStateChanged(false)
                            }
                            videoViewRef = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Top Floating Badges (Character Role & Aspect Ratio Button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Character Role Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = scenario.characterAvatarEmoji, fontSize = 14.sp)
                            Text(
                                text = "${scenario.characterName} (${scenario.characterRole})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Aspect Ratio & Subtitle Control Toggles
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Subtitle Toggle
                        IconButton(
                            onClick = onToggleSubtitles,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                        ) {
                            Icon(
                                imageVector = if (uiState.showSubtitles) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionOff,
                                contentDescription = "Toggle Subtitles",
                                tint = if (uiState.showSubtitles) DuolingoGreen else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Hindi Translation Toggle
                        IconButton(
                            onClick = onToggleHindi,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Toggle Hindi Translation",
                                tint = if (uiState.showHindiTranslation) GoldYellow else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Aspect Ratio Switcher
                        IconButton(
                            onClick = onToggleAspect,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "Switch Aspect Ratio",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Play / Pause Overlay Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            if (uiState.isPlaying) Color.Black.copy(alpha = 0.35f) else SpeakBlue
                        )
                        .clickable { onTogglePlayPause() }
                        .align(Alignment.Center)
                        .testTag("veo_play_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Real-Time Subtitle Banner
                if (uiState.showSubtitles && uiState.activeDialogueLine != null) {
                    val line = uiState.activeDialogueLine
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.85f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = line.speaker,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DuolingoGreen
                                )
                                Text(
                                    text = "${line.timestampSec}s",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                text = "“${line.textEnglish}”",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (uiState.showHindiTranslation && line.textHindi.isNotBlank()) {
                                Text(
                                    text = line.textHindi,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GoldYellow
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Scrub / Duration Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val progressFraction = if (durationSec > 0) {
                    (currentSec.toFloat() / durationSec.toFloat()).coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = DuolingoGreen,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "0:${currentSec.toString().padStart(2, '0')} / 0:${durationSec.toString().padStart(2, '0')}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Google Veo • ${uiState.aspectRatio} • ${scenario.cefrLevel}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DuolingoGreenLight
                    )
                }
            }
        }
    }
}

/**
 * Interactive Roleplay Console where the learner practices replying to the video scenario.
 * Provides multiple native response options, audio listening (TTS), and speech evaluation.
 */
@Composable
private fun RoleplaySpeakingConsole(
    uiState: VeoVideoScenarioPlayerUiState,
    onSpeakTargetResponse: (String) -> Unit,
    onEvaluateSpokenReply: (String, VeoLearnerResponse) -> Unit,
    onDismissFeedback: () -> Unit
) {
    val scenario = uiState.currentScenario
    var customReplyInput by remember { mutableStateOf("") }
    var selectedTarget by remember { mutableStateOf(scenario.suggestedLearnerResponses.firstOrNull()) }

    LaunchedEffect(scenario.id) {
        selectedTarget = scenario.suggestedLearnerResponses.firstOrNull()
        customReplyInput = ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("roleplay_speaking_console"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Situation orientation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🎙️", fontSize = 20.sp)
                Column {
                    Text(
                        text = "Your Turn: Respond to ${scenario.characterName}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = scenario.situationContext,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Speech Evaluation Result Banner if evaluated
            uiState.speechScore?.let { score ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (score >= 70) DuolingoGreenLight.copy(alpha = 0.25f) else GoldYellow.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (score >= 70) DuolingoGreen else GoldYellowDark
                    )
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
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (score >= 70) DuolingoGreenDark else GoldYellowDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Fluency Score: $score%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (score >= 70) DuolingoGreenDark else GoldYellowDark
                                )
                            }
                            Text(
                                text = "+15 XP 💎",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = SpeakBlueDark
                            )
                        }
                        Text(
                            text = uiState.speechFeedback ?: "Great practice!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        uiState.userSpokenText?.let { spoken ->
                            Text(
                                text = "You said: “$spoken”",
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Text(
                text = "Choose a suggested English reply to practice:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // List of suggested responses with audio playback and practice trigger
            scenario.suggestedLearnerResponses.forEach { response ->
                val isSelected = selectedTarget == response
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            selectedTarget = response
                            customReplyInput = response.textEnglish
                        }
                        .testTag("learner_response_card_${response.difficulty}"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) SpeakBlueLight.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) SpeakBlue else MaterialTheme.colorScheme.outlineVariant
                    )
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
                            // Difficulty Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (response.difficulty) {
                                            "Easy" -> DuolingoGreen.copy(alpha = 0.2f)
                                            "Medium" -> SpeakBlue.copy(alpha = 0.2f)
                                            else -> GoldYellow.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = response.difficulty,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when (response.difficulty) {
                                        "Easy" -> DuolingoGreenDark
                                        "Medium" -> SpeakBlueDark
                                        else -> GoldYellowDark
                                    }
                                )
                            }

                            // Audio Listen Button
                            IconButton(
                                onClick = { onSpeakTargetResponse(response.textEnglish) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Listen to pronunciation",
                                    tint = SpeakBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = "“${response.textEnglish}”",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = response.phonetic,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = response.textHindi,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = GoldYellowDark
                        )
                    }
                }
            }

            // Interactive Speaking & Verification Action
            selectedTarget?.let { target ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customReplyInput,
                        onValueChange = { customReplyInput = it },
                        label = { Text("Speak or edit your reply to ${scenario.characterName}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("learner_reply_input"),
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    // Set to target English for quick verification
                                    customReplyInput = target.textEnglish
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Quick Speak Input",
                                    tint = DuolingoGreen
                                )
                            }
                        }
                    )

                    DuolingoButton(
                        text = "SUBMIT & GET AI FEEDBACK 🌟",
                        onClick = {
                            val replyToEvaluate = customReplyInput.ifBlank { target.textEnglish }
                            onEvaluateSpokenReply(replyToEvaluate, target)
                        },
                        buttonColor = DuolingoGreen,
                        shadowColor = DuolingoGreenDark,
                        modifier = Modifier.testTag("submit_spoken_reply_btn")
                    )
                }
            }
        }
    }
}

/**
 * Key vocabulary pills taught in this situational video scenario.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VocabularyPillsSection(
    vocabulary: List<VeoVocabularyItem>,
    onPlayAudio: (String) -> Unit
) {
    if (vocabulary.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("veo_vocabulary_section"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "💡", fontSize = 18.sp)
                Text(
                    text = "Key Scenario Vocabulary",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                vocabulary.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = item.word,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.phonetic,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${item.meaning} (${item.hindiMeaning})",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onPlayAudio(item.word) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Listen",
                                    tint = DuolingoGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Cultural & Conversational Etiquette insight card.
 */
@Composable
private fun CulturalEtiquetteCard(tip: String) {
    if (tip.isBlank()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cultural_etiquette_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GoldYellow.copy(alpha = 0.15f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldYellow.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = GoldYellowDark,
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Native Culture & Etiquette Tip",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF9E6D00)
                )
                Text(
                    text = tip,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Generator Card empowering learners to request any custom situational video scenario
 * synthesized via the Google Veo API (veo-3.1-fast-generate-preview or veo-3.1-generate-preview).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenerateVeoScenarioCard(
    uiState: VeoVideoScenarioPlayerUiState,
    onGenerate: (prompt: String, category: String, level: String, aspect: String, model: String) -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Travel") }
    var selectedLevel by remember { mutableStateOf("B1") }
    var selectedAspect by remember { mutableStateOf("16:9") }
    var selectedModel by remember { mutableStateOf("veo-3.1-fast-generate-preview") }

    val presetIdeas = listOf(
        "Ordering street food in New York Chinatown",
        "Renting a convertible car in Los Angeles",
        "Buying a train ticket at Edinburgh Waverley station",
        "Asking for directions to Oxford University Library"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("generate_veo_scenario_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GoldYellow, DuolingoGreen))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎬", fontSize = 20.sp)
                }
                Column {
                    Text(
                        text = "Generate Custom Veo Scenario",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Powered by Google Veo 3 Video AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuolingoGreenDark
                    )
                }
            }

            // Quick Preset Prompt Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Quick Situational Prompts:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetIdeas.forEach { idea ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable { promptInput = idea }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("veo_preset_chip_${idea.take(10)}")
                        ) {
                            Text(
                                text = idea,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Custom Text Input
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                label = { Text("Describe the situational scene to generate") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("veo_scenario_prompt_input"),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3
            )

            // Aspect Ratio & Model Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 16:9 Landscape Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedAspect == "16:9") SpeakBlue else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedAspect = "16:9" }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "16:9 Landscape",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedAspect == "16:9") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                // 9:16 Portrait Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedAspect == "9:16") SpeakBlue else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedAspect = "9:16" }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "9:16 Portrait Reel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedAspect == "9:16") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Veo Model Selector (veo-3.1-fast-generate-preview vs veo-3.1-generate-preview)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selectedModel == "veo-3.1-fast-generate-preview") DuolingoGreen else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { selectedModel = "veo-3.1-fast-generate-preview" }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ Veo Fast",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (selectedModel == "veo-3.1-fast-generate-preview") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selectedModel == "veo-3.1-generate-preview") DuolingoGreen else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { selectedModel = "veo-3.1-generate-preview" }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💎 Veo Ultra HQ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (selectedModel == "veo-3.1-generate-preview") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Status notification
            uiState.generationStatusText?.let { status ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = DuolingoGreen,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            uiState.errorMessage?.let { err ->
                Text(
                    text = err,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeartRed
                )
            }

            // Generate Button
            DuolingoButton(
                text = if (uiState.isGenerating) "GENERATING VEO SCENARIO..." else "GENERATE SCENARIO WITH VEO 3 🎬",
                enabled = promptInput.isNotBlank() && !uiState.isGenerating,
                onClick = {
                    onGenerate(promptInput, selectedCategory, selectedLevel, selectedAspect, selectedModel)
                },
                buttonColor = GoldYellow,
                shadowColor = GoldYellowDark,
                textColor = Color.Black,
                modifier = Modifier.testTag("generate_veo_scenario_btn")
            )
        }
    }
}
