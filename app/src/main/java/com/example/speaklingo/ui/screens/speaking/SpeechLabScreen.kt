package com.example.speaklingo.ui.screens.speaking

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.core.content.ContextCompat
import com.example.speaklingo.audio.SpeechInputManager
import com.example.speaklingo.audio.TtsManager
import com.example.speaklingo.audio.VoiceRecorderManager
import com.example.speaklingo.data.model.PronunciationAnalysisResult
import com.example.speaklingo.data.remote.SpeechEvaluationResult
import com.example.speaklingo.data.remote.SpokenTranscriptionCoaching
import com.example.speaklingo.data.remote.WordPronunciationScore
import com.example.speaklingo.data.repository.AiTutorRepository
import com.example.speaklingo.data.repository.LearningRepository
import com.example.speaklingo.ui.components.DuolingoButton
import com.example.speaklingo.ui.components.PronunciationAnalysisCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.OutlinedTextField
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.DuolingoGreenLight
import com.example.ui.theme.FireOrange
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GoldYellowDark
import com.example.ui.theme.HeartRed
import com.example.ui.theme.HeartRedLight
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark
import com.example.ui.theme.SpeakBlueLight
import kotlinx.coroutines.launch

/**
 * Pronunciation & Phonics challenges for guided speaking practice.
 */
data class SpeechChallenge(
    val title: String,
    val targetSentence: String,
    val phoneticGuide: String,
    val hindiMeaning: String,
    val level: String,
    val keyPhonicsFocus: String
)

val challenges = listOf(
    SpeechChallenge(
        title = "Self Introduction",
        targetSentence = "Hello, I am excited to improve my English fluency.",
        phoneticGuide = "/həˈloʊ aɪ æm ɪkˈsaɪtɪd tuː ɪmˈpruːv maɪ ˈɪŋɡlɪʃ ˈfluːənsi/",
        hindiMeaning = "नमस्ते, मैं अपनी अंग्रेज़ी बोलने की क्षमता सुधारने के लिए बहुत उत्साहित हूँ।",
        level = "Beginner (A1)",
        keyPhonicsFocus = "Vowel elongation in 'excited' & 'fluency'"
    ),
    SpeechChallenge(
        title = "Cafe & Restaurant",
        targetSentence = "Could you please bring us a bottle of cold mineral water?",
        phoneticGuide = "/kʊd juː pliːz brɪŋ ʌs ə ˈbɑːtl əv koʊld ˈmɪnərəl ˈwɔːtər/",
        hindiMeaning = "क्या आप कृपया हमारे लिए ठंडे मिनरल वाटर की एक बोतल ला सकते हैं?",
        level = "Elementary (A2)",
        keyPhonicsFocus = "Polite request intonation & soft 'Could you'"
    ),
    SpeechChallenge(
        title = "Tongue Twister: /s/ vs /ʃ/",
        targetSentence = "She sells seashells by the seashore on sunny Sundays.",
        phoneticGuide = "/ʃiː sɛlz ˈsiːˌʃɛlz baɪ ðə ˈsiːˌʃɔːr ɑːn ˈsʌni ˈsʌndeɪz/",
        hindiMeaning = "उच्चारण अभ्यास: 'स' और 'श' ध्वनियों में अंतर स्पष्ट करें।",
        level = "Phonics Lab",
        keyPhonicsFocus = "Differentiating 'sh' /ʃ/ and 's' /s/ sounds cleanly"
    ),
    SpeechChallenge(
        title = "Professional Opinion",
        targetSentence = "In my opinion, teamwork leads to remarkable productivity.",
        phoneticGuide = "/ɪn maɪ əˈpɪnjən ˈtiːmˌwɜːrk liːdz tuː rɪˈmɑːrkəbl ˌproʊdʌkˈtɪvɪti/",
        hindiMeaning = "मेरी राय में, टीमवर्क से उल्लेखनीय उत्पादकता हासिल होती है।",
        level = "Intermediate (B1)",
        keyPhonicsFocus = "Stress on 'opinion', 'teamwork', 'productivity'"
    ),
    SpeechChallenge(
        title = "Workplace Pitch",
        targetSentence = "We can leverage our innovative strategy to achieve market leadership.",
        phoneticGuide = "/wiː kæn ˈlɛvərɪdʒ ˈaʊər ˌɪnəˈveɪtɪv ˈstrætədʒi tuː əˈtʃiːv ˈmɑːrkɪt ˈliːdərʃɪp/",
        hindiMeaning = "हम बाज़ार में नेतृत्व हासिल करने के लिए अपनी अभिनव रणनीति का लाभ उठा सकते हैं।",
        level = "Upper-Int (B2)",
        keyPhonicsFocus = "Fluid linkage: 'leverage our', 'innovative strategy'"
    ),
    SpeechChallenge(
        title = "Advanced Inversion",
        targetSentence = "Rarely have I witnessed such profound technological innovation.",
        phoneticGuide = "/ˈrɛərli hæv aɪ ˈwɪtnɪst sʌtʃ prəˈfaʊnd ˌtɛknəˈlɒdʒɪkəl ˌɪnəˈveɪʃən/",
        hindiMeaning = "शायद ही कभी मैंने ऐसा गहरा तकनीकी नवाचार देखा हो।",
        level = "Mastery (C1)",
        keyPhonicsFocus = "Rhetorical inversion emphasis on 'Rarely'"
    )
)

/**
 * Modes for the Speech Lab screen.
 */
enum class SpeechLabMode(val title: String, val emoji: String) {
    GUIDED_PRONUNCIATION("Pronunciation & Phonics", "🎯"),
    FREE_TRANSCRIPTION("Free Speech & Aria AI", "🗣️")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeechLabScreen(
    aiTutorRepository: AiTutorRepository,
    learningRepository: LearningRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeMode by remember { mutableStateOf(SpeechLabMode.GUIDED_PRONUNCIATION) }
    var selectedChallengeIndex by remember { mutableIntStateOf(0) }
    val currentChallenge = challenges[selectedChallengeIndex]

    // Real-Time Speech Recognition & Audio Recording States
    var spokenText by remember { mutableStateOf("") }
    var partialSpokenText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var rmsLevel by remember { mutableFloatStateOf(0f) }
    var recordedAudioBase64 by remember { mutableStateOf<String?>(null) }

    // Evaluation, Pronunciation Analysis & Coaching States
    var isEvaluating by remember { mutableStateOf(false) }
    var evaluationResult by remember { mutableStateOf<SpeechEvaluationResult?>(null) }
    var pronunciationAnalysis by remember { mutableStateOf<PronunciationAnalysisResult?>(null) }
    var freeSpeechCoaching by remember { mutableStateOf<SpokenTranscriptionCoaching?>(null) }
    var isCustomReference by remember { mutableStateOf(false) }
    var customReferenceSentence by remember { mutableStateOf("") }

    // TTS Audio Manager
    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    // Audio Recorder for Multimodal Gemini Analysis
    val voiceRecorder = remember {
        VoiceRecorderManager(
            onAmplitudeChanged = { amp ->
                if (!isListening) {
                    rmsLevel = amp
                }
            }
        )
    }

    // Android SpeechRecognizer API with real-time partial streaming & RMS amplitude
    val speechManager = remember {
        SpeechInputManager(
            context = context,
            onListeningChanged = { listening ->
                isListening = listening
                if (!listening) {
                    rmsLevel = 0f
                    if (voiceRecorder.isCurrentlyRecording) {
                        val audio = voiceRecorder.stopRecordingBase64()
                        if (audio != null) {
                            recordedAudioBase64 = audio
                        }
                    }
                }
            },
            onResultReceived = { finalResult ->
                spokenText = finalResult
                partialSpokenText = finalResult
            },
            onPartialResult = { partial ->
                partialSpokenText = partial
                if (spokenText.isBlank()) {
                    spokenText = partial
                }
            },
            onRmsChanged = { rms ->
                rmsLevel = rms
            },
            onErrorReceived = { /* handled */ }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.destroy()
            voiceRecorder.stopRecording()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            spokenText = ""
            partialSpokenText = ""
            evaluationResult = null
            pronunciationAnalysis = null
            recordedAudioBase64 = null
            speechManager.startListening()
            voiceRecorder.startRecording(coroutineScope)
        }
    }

    fun handleMic() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            if (isListening || voiceRecorder.isCurrentlyRecording) {
                speechManager.stopListening()
                val audio = voiceRecorder.stopRecordingBase64()
                if (audio != null) {
                    recordedAudioBase64 = audio
                }
            } else {
                spokenText = ""
                partialSpokenText = ""
                evaluationResult = null
                pronunciationAnalysis = null
                freeSpeechCoaching = null
                recordedAudioBase64 = null
                speechManager.startListening()
                voiceRecorder.startRecording(coroutineScope)
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🎙️", fontSize = 24.sp)
                    Text(
                        text = "Real-Time Speech & Fluency Lab",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Speaking Monkey Pronunciation Coach & Gemini Audio Analysis",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuolingoGreenDark
                )
            }
        }

        // Mode Switcher (Guided Pronunciation vs Free Speech Transcription)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SpeechLabMode.entries.forEach { mode ->
                    val isSelected = activeMode == mode
                    val tabColor = if (mode == SpeechLabMode.GUIDED_PRONUNCIATION) DuolingoGreen else SpeakBlue

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) tabColor else Color.Transparent)
                            .clickable {
                                activeMode = mode
                                spokenText = ""
                                partialSpokenText = ""
                                evaluationResult = null
                                freeSpeechCoaching = null
                            }
                            .padding(vertical = 8.dp)
                            .testTag("mode_tab_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = mode.emoji, fontSize = 13.sp)
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
        }

        // MODE 1: GUIDED PRONUNCIATION & PHONICS
        if (activeMode == SpeechLabMode.GUIDED_PRONUNCIATION) {
            // Reference Sentence Mode Switcher (Curated Challenges vs Custom Sentence)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isCustomReference) SpeakBlue else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isCustomReference = false }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "📚 Preset Challenges",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isCustomReference) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCustomReference) SpeakBlue else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isCustomReference = true }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "✍️ Custom Sentence",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCustomReference) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (!isCustomReference) {
                // Horizontal Challenge Selector
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(challenges) { idx, challenge ->
                            val isSelected = idx == selectedChallengeIndex
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) SpeakBlue else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        selectedChallengeIndex = idx
                                        spokenText = ""
                                        partialSpokenText = ""
                                        evaluationResult = null
                                        pronunciationAnalysis = null
                                        recordedAudioBase64 = null
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("speech_challenge_tab_$idx")
                            ) {
                                Text(
                                    text = challenge.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Target Sentence Card with Phonics Guide & Real-Time Word Highlight
                item {
                    TargetChallengeCard(
                        challenge = currentChallenge,
                        spokenText = partialSpokenText.ifBlank { spokenText },
                        onListenNative = { ttsManager.speak(currentChallenge.targetSentence) }
                    )
                }
            } else {
                // Custom Reference Sentence Input Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎯 YOUR REFERENCE SENTENCE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SpeakBlueDark
                                )
                                if (customReferenceSentence.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SpeakBlueLight)
                                            .clickable { ttsManager.speak(customReferenceSentence) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "🔊 Listen Native",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SpeakBlueDark
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = customReferenceSentence,
                                onValueChange = { customReferenceSentence = it },
                                placeholder = { Text("Enter any English sentence you want to practice and evaluate...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_reference_input"),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        } else {
            // MODE 2: FREE SPEECH & ARIA TRANSCRIPTION PROMPT CARD
            item {
                FreeSpeechPromptCard(
                    onSelectTopic = { topicPrompt ->
                        spokenText = ""
                        partialSpokenText = ""
                        evaluationResult = null
                        pronunciationAnalysis = null
                        freeSpeechCoaching = null
                        recordedAudioBase64 = null
                        ttsManager.speak(topicPrompt)
                    }
                )
            }
        }

        // Real-Time Microphone & Live Waveform Visualizer
        item {
            LiveAudioRecordingSection(
                isListening = isListening,
                rmsLevel = rmsLevel,
                onMicClick = { handleMic() }
            )
        }

        // Real-Time Spoken Transcription Card
        item {
            RealTimeTranscriptionCard(
                isListening = isListening,
                liveText = partialSpokenText.ifBlank { spokenText }
            )
        }

        // Action Button: Evaluate Pronunciation or Aria AI Transcription Coaching
        item {
            val textToEvaluate = partialSpokenText.ifBlank { spokenText }
            val hasSpoken = textToEvaluate.isNotBlank()

            if (activeMode == SpeechLabMode.GUIDED_PRONUNCIATION) {
                val effectiveRef = if (isCustomReference && customReferenceSentence.isNotBlank()) {
                    customReferenceSentence.trim()
                } else {
                    currentChallenge.targetSentence
                }

                DuolingoButton(
                    text = if (isEvaluating) "GEMINI ANALYZING AUDIO & PRONUNCIATION... ⚡" else "ANALYZE PRONUNCIATION (GEMINI) 🎯",
                    enabled = (hasSpoken || recordedAudioBase64 != null) && !isEvaluating,
                    onClick = {
                        isEvaluating = true
                        coroutineScope.launch {
                            val result = aiTutorRepository.analyzeAudioPronunciation(
                                referenceSentence = effectiveRef,
                                audioBase64 = recordedAudioBase64,
                                spokenText = textToEvaluate
                            )
                            pronunciationAnalysis = result
                            isEvaluating = false

                            learningRepository.completeLesson(
                                lessonId = "speech_${selectedChallengeIndex}",
                                unitId = 1,
                                xpReward = 25,
                                gemReward = 10,
                                accuracy = result.overallScore
                            )
                        }
                    },
                    buttonColor = DuolingoGreen,
                    shadowColor = DuolingoGreenDark,
                    testTag = "speech_evaluate_button"
                )
            } else {
                DuolingoButton(
                    text = if (isEvaluating) "ARIA AI COACHING SPEECH... ✨" else "ARIA AI TRANSCRIPTION & COACHING ✨",
                    enabled = hasSpoken && !isEvaluating,
                    onClick = {
                        isEvaluating = true
                        coroutineScope.launch {
                            val coaching = aiTutorRepository.transcribeAndCoachSpeech(textToEvaluate)
                            freeSpeechCoaching = coaching
                            isEvaluating = false

                            learningRepository.completeLesson(
                                lessonId = "free_speech_${System.currentTimeMillis() % 1000}",
                                unitId = 1,
                                xpReward = 25,
                                gemReward = 10,
                                accuracy = coaching.fluencyRating
                            )
                        }
                    },
                    buttonColor = SpeakBlue,
                    shadowColor = SpeakBlueDark,
                    testTag = "speech_aria_coaching_button"
                )
            }
        }

        // Gemini Pronunciation Analysis Result Card (Comparing Audio vs Reference Sentence)
        pronunciationAnalysis?.let { analysis ->
            item {
                val effectiveRef = if (isCustomReference && customReferenceSentence.isNotBlank()) {
                    customReferenceSentence.trim()
                } else {
                    currentChallenge.targetSentence
                }
                PronunciationAnalysisCard(
                    result = analysis,
                    onListenReference = { ttsManager.speak(effectiveRef) },
                    onListenDrill = { drill -> ttsManager.speak(drill) }
                )
            }
        }

        // Guided Pronunciation Result Card (Legacy fallback)
        evaluationResult?.let { result ->
            item {
                AriaPronunciationEvaluationCard(
                    result = result,
                    onPlayBetterNative = {
                        val phrasing = result.nativeBetterPhrasing ?: currentChallenge.targetSentence
                        ttsManager.speak(phrasing)
                    }
                )
            }
        }

        // Free Speech Aria AI Coaching Result Card
        freeSpeechCoaching?.let { coaching ->
            item {
                AriaFreeSpeechCoachingCard(
                    coaching = coaching,
                    onPlayNativePhrasing = {
                        ttsManager.speak(coaching.naturalNativeAlternative)
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(36.dp)) }
    }
}

/**
 * Target Challenge Card displaying target sentence, phonics, Hindi meaning,
 * and live word-by-word matching as the user speaks.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TargetChallengeCard(
    challenge: SpeechChallenge,
    spokenText: String,
    onListenNative: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("challenge_target_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldYellow.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = challenge.level.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF9E6D00),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Listen Native Audio Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpeakBlueLight)
                        .clickable(onClick = onListenNative)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen to pronunciation",
                            tint = SpeakBlueDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Listen Native",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpeakBlueDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-time Word-by-Word Highlight
            val spokenTokens = remember(spokenText) {
                spokenText.lowercase().replace(Regex("[^a-zA-Z0-9 ]"), "").split(" ").toSet()
            }
            val targetWords = remember(challenge.targetSentence) {
                challenge.targetSentence.split(" ")
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                targetWords.forEach { word ->
                    val cleanWord = word.lowercase().replace(Regex("[^a-zA-Z0-9]"), "")
                    val isSpokenAccurate = spokenTokens.contains(cleanWord)

                    val wordBg by animateColorAsState(
                        targetValue = if (isSpokenAccurate) DuolingoGreenLight else Color.Transparent,
                        animationSpec = tween(300),
                        label = "word_bg"
                    )
                    val wordColor by animateColorAsState(
                        targetValue = if (isSpokenAccurate) DuolingoGreenDark else MaterialTheme.colorScheme.onSurface,
                        animationSpec = tween(300),
                        label = "word_color"
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = wordBg,
                        border = if (isSpokenAccurate) null else null
                    ) {
                        Text(
                            text = word,
                            fontSize = 17.sp,
                            fontWeight = if (isSpokenAccurate) FontWeight.Black else FontWeight.Bold,
                            color = wordColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Phonetic Guide
            Text(
                text = challenge.phoneticGuide,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Hindi Meaning
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "🇮🇳 ${challenge.hindiMeaning}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Phonics Tip Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "🎯", fontSize = 12.sp)
                Text(
                    text = "Focus: ${challenge.keyPhonicsFocus}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpeakBlueDark
                )
            }
        }
    }
}

/**
 * Free Speech Prompt Card for spontaneous English speaking practice.
 */
@Composable
private fun FreeSpeechPromptCard(onSelectTopic: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SpeakBlueLight.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "🗣️", fontSize = 18.sp)
                Text(
                    text = "FREE SPEECH PRACTICE WITH ARIA AI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = SpeakBlueDark
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tap the mic and speak freely in English! Android's SpeechRecognizer transcribes your words in real time, and Aria AI coaches your grammar, cadence, and native expressions.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "TRY ONE OF THESE TOPICS:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            val topicPrompts = listOf(
                "Tell me about your favorite hobby or passion.",
                "Describe your ideal weekend vacation trip.",
                "What are your top career dreams for the future?"
            )

            topicPrompts.forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { onSelectTopic(prompt) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "💬", fontSize = 12.sp)
                        Text(
                            text = prompt,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SpeakBlueDark
                        )
                    }
                }
            }
        }
    }
}

/**
 * Real-Time Microphone & Live Bouncing Audio Waveform Section.
 */
@Composable
private fun LiveAudioRecordingSection(
    isListening: Boolean,
    rmsLevel: Float,
    onMicClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Live Audio Waveform Bars (7 vertical frequency bars)
        Row(
            modifier = Modifier
                .height(36.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val multipliers = listOf(0.4f, 0.7f, 1.0f, 1.3f, 1.0f, 0.7f, 0.4f)
            multipliers.forEachIndexed { i, factor ->
                val targetHeight = if (isListening) {
                    (8.dp + (26.dp * (rmsLevel * factor).coerceIn(0.1f, 1.0f)))
                } else {
                    4.dp
                }

                val animatedHeight by animateFloatAsState(
                    targetValue = targetHeight.value,
                    animationSpec = spring(stiffness = 500f),
                    label = "waveform_$i"
                )

                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(animatedHeight.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = if (isListening) listOf(HeartRed, SpeakBlue, DuolingoGreen)
                                else listOf(Color.LightGray, Color.Gray)
                            )
                        )
                )
            }
        }

        // Circular Mic Button with Pulsing Halo Ring
        Box(contentAlignment = Alignment.Center) {
            if (isListening) {
                val infiniteTransition = rememberInfiniteTransition(label = "mic_halo")
                val haloScale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.35f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "halo_scale"
                )
                val haloAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "halo_alpha"
                )

                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(haloScale)
                        .clip(CircleShape)
                        .background(HeartRed.copy(alpha = haloAlpha))
                )
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isListening) HeartRed else DuolingoGreen)
                    .clickable(onClick = onMicClick)
                    .testTag("speech_lab_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop voice recording" else "Start speech recognition",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status Label with Blinking Live Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isListening) {
                val infiniteTransition = rememberInfiniteTransition(label = "rec_blink")
                val blinkAlpha by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 0.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "rec_alpha"
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(HeartRed.copy(alpha = blinkAlpha))
                )
                Text(
                    text = "LISTENING LIVE... SPEAK CLEARLY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = HeartRed,
                    letterSpacing = 1.sp
                )
            } else {
                Text(
                    text = "Tap green microphone to start speaking",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuolingoGreenDark
                )
            }
        }
    }
}

/**
 * Real-Time Transcription Card showing words streaming live as the user speaks.
 */
@Composable
private fun RealTimeTranscriptionCard(
    isListening: Boolean,
    liveText: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("real_time_transcription_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = SpeakBlueDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "REAL-TIME TRANSCRIPTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = SpeakBlueDark,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isListening) HeartRedLight else DuolingoGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isListening) "● LIVE STREAM" else "SpeechRecognizer Ready",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isListening) HeartRed else DuolingoGreenDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (liveText.isNotBlank()) "\"$liveText\"" else "Words you speak into the microphone will appear here in real-time...",
                fontSize = 16.sp,
                fontWeight = if (liveText.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                color = if (liveText.isNotBlank()) MaterialTheme.colorScheme.onSurface else Color.Gray,
                lineHeight = 22.sp
            )
        }
    }
}

/**
 * Aria AI Guided Pronunciation Evaluation Card.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AriaPronunciationEvaluationCard(
    result: SpeechEvaluationResult,
    onPlayBetterNative: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("speech_evaluation_result_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isExcellent) DuolingoGreenLight else GoldYellow.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Score Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = if (result.isExcellent) "🌟" else "⚡", fontSize = 16.sp)
                        Text(
                            text = if (result.isExcellent) "EXCELLENT PRONUNCIATION" else "FLUENCY ASSESSMENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = if (result.isExcellent) DuolingoGreenDark else Color(0xFF9E6D00)
                        )
                    }
                    Text(
                        text = result.feedback,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${result.score}%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = if (result.isExcellent) DuolingoGreenDark else Color(0xFF9E6D00)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Metrics (Accuracy & Fluency)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Accuracy Bar
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Accuracy: ${result.accuracyScore}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuolingoGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { result.accuracyScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = DuolingoGreen
                        )
                    }
                }

                // Fluency Bar
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Fluency: ${result.fluencyScore}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpeakBlueDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { result.fluencyScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SpeakBlue
                        )
                    }
                }
            }

            // Word-level Breakdown Chips
            if (result.wordScores.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "WORD-BY-WORD ACCURACY:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    result.wordScores.forEach { ws ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (ws.isCorrect) DuolingoGreen.copy(alpha = 0.15f) else FireOrange.copy(alpha = 0.18f),
                            border = null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = if (ws.isCorrect) "✔" else "⚠",
                                    fontSize = 10.sp,
                                    color = if (ws.isCorrect) DuolingoGreenDark else FireOrange
                                )
                                Text(
                                    text = ws.word,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ws.isCorrect) DuolingoGreenDark else Color(0xFF9A3412)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Phonetic Tip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🎯 Aria AI Pronunciation Tip:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = SpeakBlueDark
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = result.pronunciationTip,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )

                    if (result.hindiCoaching != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🇮🇳 ${result.hindiCoaching}",
                            fontSize = 12.sp,
                            color = Color(0xFF6B4500)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Listen Native Phrasing Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DuolingoGreen)
                    .clickable(onClick = onPlayBetterNative)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Listen native cadence",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Listen to Native Cadence 🎧",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Aria AI Free Speech Coaching Card for spontaneous speech transcription.
 */
@Composable
private fun AriaFreeSpeechCoachingCard(
    coaching: SpokenTranscriptionCoaching,
    onPlayNativePhrasing: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("aria_free_speech_coaching_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SpeakBlueLight.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "👩‍🏫", fontSize = 20.sp)
                    Column {
                        Text(
                            text = "ARIA AI VOCAL COACH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = SpeakBlueDark
                        )
                        Text(
                            text = "Fluency Score: ${coaching.fluencyRating}%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = SpeakBlueDark
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DuolingoGreen
                ) {
                    Text(
                        text = "+25 XP ⭐",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Natural Native Alternative Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 HOW A NATIVE SPEAKER WOULD SAY IT:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“${coaching.naturalNativeAlternative}”",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = DuolingoGreenDark,
                        lineHeight = 21.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hindi Translation
                    Text(
                        text = "🇮🇳 ${coaching.hindiTranslation}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B4500)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Listen Audio Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SpeakBlue)
                            .clickable(onClick = onPlayNativePhrasing)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen to native phrasing",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Listen Pronunciation",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pronunciation & Cadence Advice
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🎯 Vocal & Cadence Advice:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = SpeakBlueDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = coaching.pronunciationAdvice,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "📝 Grammar Insight: ${coaching.grammarExplanation}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
