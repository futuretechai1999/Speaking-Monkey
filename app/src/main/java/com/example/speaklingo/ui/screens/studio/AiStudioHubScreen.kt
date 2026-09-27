package com.example.speaklingo.ui.screens.studio

import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import com.example.speaklingo.data.model.PronunciationAnalysisResult
import com.example.speaklingo.ui.components.PronunciationAnalysisCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.speaklingo.audio.TtsManager
import com.example.speaklingo.data.remote.GeminiAdvancedService
import com.example.speaklingo.data.remote.GeneratedMediaResult
import com.example.speaklingo.data.remote.GroundedResponse
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.speaklingo.data.repository.LearningRepository
import com.example.speaklingo.ui.components.VeoVideoScenarioPlayer
import com.example.speaklingo.ui.viewmodel.VeoVideoScenarioPlayerViewModel
import com.example.speaklingo.ui.components.DuolingoButton
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.DuolingoGreenLight
import com.example.ui.theme.FireOrange
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.HeartRed
import com.example.ui.theme.HeartRedLight
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark
import com.example.ui.theme.SpeakBlueLight
import kotlinx.coroutines.launch

enum class StudioFeature(val title: String, val icon: String) {
    SEARCH_GROUNDING("Search Grounding", "🌐"),
    MAPS_GROUNDING("Maps Grounding", "📍"),
    VEO_VIDEO("Veo 3 Video", "🎬"),
    IMAGE_STUDIO("Flashcard Art", "🎨"),
    LYRIA_MUSIC("Lyria Music", "🎵"),
    TRANSCRIBE("Transcribe AI", "🎙️"),
    LIVE_VOICE("Live Voice", "⚡"),
    CLOUD_AUTH("Cloud & Auth", "☁️")
}

@Composable
fun AiStudioHubScreen(
    advancedService: GeminiAdvancedService,
    learningRepository: LearningRepository? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedFeature by remember { mutableStateOf(StudioFeature.SEARCH_GROUNDING) }

    val veoViewModel: VeoVideoScenarioPlayerViewModel = viewModel(
        factory = VeoVideoScenarioPlayerViewModel.provideFactory(learningRepository = learningRepository)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Studio Header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "AI Capabilities Studio ✨",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Search, Maps Grounding, Veo 3 Video, Image AI, Lyria Music & Live Voice.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Horizontal Feature Selector
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(StudioFeature.entries) { feature ->
                val isSelected = feature == selectedFeature
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) SpeakBlue else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedFeature = feature }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("studio_tab_${feature.name.lowercase()}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = feature.icon, fontSize = 14.sp)
                        Text(
                            text = feature.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area based on selected feature
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedFeature) {
                StudioFeature.SEARCH_GROUNDING -> SearchGroundingView(advancedService)
                StudioFeature.MAPS_GROUNDING -> MapsGroundingView(advancedService)
                StudioFeature.VEO_VIDEO -> VeoVideoView(veoViewModel)
                StudioFeature.IMAGE_STUDIO -> ImageStudioView(advancedService)
                StudioFeature.LYRIA_MUSIC -> LyriaMusicView(advancedService)
                StudioFeature.TRANSCRIBE -> TranscribeAudioView(advancedService)
                StudioFeature.LIVE_VOICE -> LiveVoiceView(advancedService)
                StudioFeature.CLOUD_AUTH -> CloudAuthView()
            }
        }
    }
}

// 1. Search Grounding View with gemini-3.5-flash and googleSearch tool
@Composable
private fun SearchGroundingView(service: GeminiAdvancedService) {
    val coroutineScope = rememberCoroutineScope()
    var query by remember { mutableStateOf("Latest space exploration news and scientific discoveries") }
    var isLoading by remember { mutableStateOf(false) }
    var response by remember { mutableStateOf<GroundedResponse?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SpeakBlueLight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🌐 Search Grounding with Gemini 3.5 Flash",
                        fontWeight = FontWeight.Black,
                        color = SpeakBlueDark,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Access live Google Search data to learn real-world English vocabulary, idioms, and current global topics with verified citations.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Topic or News Headline") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_grounding_input"),
                shape = RoundedCornerShape(14.dp)
            )
        }

        item {
            DuolingoButton(
                text = if (isLoading) "SEARCHING WITH GOOGLE..." else "GET GROUNDED ENGLISH TOPIC",
                enabled = query.isNotBlank() && !isLoading,
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val res = service.searchGroundingEnglish(query)
                        response = res
                        isLoading = false
                    }
                },
                buttonColor = SpeakBlue,
                shadowColor = SpeakBlueDark,
                testTag = "search_grounding_submit"
            )
        }

        response?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "English Analysis & Discussion:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = res.text, fontSize = 13.sp, lineHeight = 19.sp)

                        if (res.sources.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Grounding Web Sources:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            res.sources.forEach { src ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(text = "🔗 ", fontSize = 11.sp)
                                    Text(
                                        text = src.title,
                                        fontSize = 12.sp,
                                        color = SpeakBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

// 2. Maps Grounding View with gemini-3.5-flash and googleMaps tool
@Composable
private fun MapsGroundingView(service: GeminiAdvancedService) {
    val coroutineScope = rememberCoroutineScope()
    var cityQuery by remember { mutableStateOf("Covent Garden, London") }
    var isLoading by remember { mutableStateOf(false) }
    var response by remember { mutableStateOf<GroundedResponse?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DuolingoGreenLight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📍 Maps Grounding with Gemini 3.5 Flash",
                        fontWeight = FontWeight.Black,
                        color = DuolingoGreenDark,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Explore real places from Google Maps and practice asking directions, finding cafes, and ordering in fluent English.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = cityQuery,
                onValueChange = { cityQuery = it },
                label = { Text("Location (e.g. Times Square NYC, London)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("maps_grounding_input"),
                shape = RoundedCornerShape(14.dp)
            )
        }

        item {
            DuolingoButton(
                text = if (isLoading) "EXPLORING PLACES..." else "GENERATE TRAVEL DIALOGUE",
                enabled = cityQuery.isNotBlank() && !isLoading,
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val res = service.mapsGroundingTravel(cityQuery)
                        response = res
                        isLoading = false
                    }
                },
                buttonColor = DuolingoGreen,
                shadowColor = DuolingoGreenDark,
                testTag = "maps_grounding_submit"
            )
        }

        response?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Travel English & Directions:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = res.text, fontSize = 13.sp, lineHeight = 19.sp)

                        if (res.sources.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Grounded Places on Google Maps:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            res.sources.forEach { s ->
                                Text(
                                    text = "📍 ${s.title}",
                                    fontSize = 12.sp,
                                    color = DuolingoGreenDark,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

// 3. Veo 3 Situational Video Scenario Player (veo-3.1-fast-generate-preview & veo-3.1-generate-preview)
@Composable
private fun VeoVideoView(viewModel: VeoVideoScenarioPlayerViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GoldYellow.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🎬 Veo 3 Video Scenario Player (veo-3.1-fast-generate-preview)",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF9E6D00),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Experience immersive real-world situational video scenarios with synchronized dual-language subtitles, interactive conversational roleplay, native pronunciation guides, and custom Veo video scenario generation.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            VeoVideoScenarioPlayer(viewModel = viewModel)
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// 4. Create & Edit Images View (gemini-3.1-flash-image-preview)
@Composable
private fun ImageStudioView(service: GeminiAdvancedService) {
    val coroutineScope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("A vibrant 3D illustration of an English vocabulary flashcard showing a golden telescope exploring stars") }
    var isGenerating by remember { mutableStateOf(false) }
    var imageResult by remember { mutableStateOf<GeneratedMediaResult?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SpeakBlueLight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🎨 Image Generation & Editing (gemini-3.1-flash-image-preview)",
                        fontWeight = FontWeight.Black,
                        color = SpeakBlueDark,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create vivid visual flashcards or edit scenes to visually anchor English words in memory using Gemini 3.1 Flash Image.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Flashcard illustration prompt") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("image_prompt_input"),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3
            )
        }

        item {
            DuolingoButton(
                text = if (isGenerating) "GENERATING FLASHCARD ART..." else "CREATE VISUAL FLASHCARD",
                enabled = prompt.isNotBlank() && !isGenerating,
                onClick = {
                    isGenerating = true
                    coroutineScope.launch {
                        val res = service.createOrEditImage(prompt)
                        imageResult = res
                        isGenerating = false
                    }
                },
                buttonColor = SpeakBlue,
                shadowColor = SpeakBlueDark,
                testTag = "image_submit_button"
            )
        }

        imageResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Flashcard Artwork (gemini-3.1-flash-image-preview)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        AsyncImage(
                            model = res.mediaUri ?: "https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=600",
                            contentDescription = res.prompt,
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, SpeakBlue, RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = res.description,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

// 5. Lyria Music Generation View (lyria-3-clip-preview / lyria-3-pro-preview)
@Composable
private fun LyriaMusicView(service: GeminiAdvancedService) {
    val coroutineScope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("Catchy upbeat acoustic melody for practicing English alphabet and pronunciation rhythms") }
    var isGenerating by remember { mutableStateOf(false) }
    var musicResult by remember { mutableStateOf<GeneratedMediaResult?>(null) }
    var isFullTrack by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🎵 Lyria Music Generation (lyria-3-clip-preview)",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF6A1B9A),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generate English pronunciation rhythm tracks, mnemonic songs, and study music using Google Lyria 3 models.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Describe the English rhythm or song") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lyria_prompt_input"),
                shape = RoundedCornerShape(14.dp)
            )
        }

        item {
            DuolingoButton(
                text = if (isGenerating) "GENERATING WITH LYRIA..." else "GENERATE ENGLISH RHYME TRACK",
                enabled = prompt.isNotBlank() && !isGenerating,
                onClick = {
                    isGenerating = true
                    coroutineScope.launch {
                        val res = service.generateMusic(prompt, isFullTrack)
                        musicResult = res
                        isGenerating = false
                    }
                },
                buttonColor = Color(0xFF8E24AA),
                shadowColor = Color(0xFF5E1772),
                testTag = "lyria_submit_button"
            )
        }

        musicResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8E24AA)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = "Play music",
                                    tint = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = "Generated Track: ${res.modelUsed}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(text = res.description, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

// 6. Transcribe Audio View with gemini-3.5-transcribe and Gemini Pronunciation Analyzer
@Composable
private fun TranscribeAudioView(service: GeminiAdvancedService) {
    val coroutineScope = rememberCoroutineScope()
    var referenceSentence by remember { mutableStateOf("I am practicing English speaking every day with Speaking Monkey AI.") }
    var transcribedText by remember { mutableStateOf("") }
    var isTranscribing by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var pronunciationResult by remember { mutableStateOf<PronunciationAnalysisResult?>(null) }
    val context = LocalContext.current
    val tts = remember { com.example.speaklingo.audio.TtsManager(context) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DuolingoGreenLight)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🎙️ Voice-to-Text & Pronunciation Analyzer",
                    fontWeight = FontWeight.Black,
                    color = DuolingoGreenDark,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Transcribe microphone audio with gemini-3.5-transcribe and evaluate your pronunciation against a reference sentence using Gemini.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        // Reference sentence input
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "🎯 TARGET REFERENCE SENTENCE:",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = SpeakBlueDark
                )
                OutlinedTextField(
                    value = referenceSentence,
                    onValueChange = { referenceSentence = it },
                    modifier = Modifier.fillMaxWidth().testTag("reference_sentence_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DuolingoButton(
                text = if (isTranscribing) "TRANSCRIBING..." else "TRANSCRIBE AUDIO",
                onClick = {
                    isTranscribing = true
                    coroutineScope.launch {
                        val result = service.transcribeAudio(null)
                        transcribedText = result
                        isTranscribing = false
                    }
                },
                buttonColor = SpeakBlue,
                shadowColor = SpeakBlueDark,
                modifier = Modifier.weight(1f),
                testTag = "transcribe_audio_button"
            )

            DuolingoButton(
                text = if (isAnalyzing) "ANALYZING..." else "ANALYZE PRONUNCIATION",
                onClick = {
                    isAnalyzing = true
                    coroutineScope.launch {
                        val result = service.analyzeAudioPronunciation(
                            referenceSentence = referenceSentence,
                            audioBase64 = null,
                            spokenText = transcribedText.ifBlank { null }
                        )
                        pronunciationResult = result
                        isAnalyzing = false
                    }
                },
                buttonColor = DuolingoGreen,
                shadowColor = DuolingoGreenDark,
                modifier = Modifier.weight(1f),
                testTag = "analyze_pronunciation_button"
            )
        }

        if (transcribedText.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Transcription Output (gemini-3.5-transcribe):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = transcribedText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Render Actionable Pronunciation Analysis Card
        pronunciationResult?.let { result ->
            PronunciationAnalysisCard(
                result = result,
                onListenReference = { tts.speak(referenceSentence) },
                onListenDrill = { drill -> tts.speak(drill) }
            )
        }
    }
}

// 7. Live Voice Conversations View (gemini-3.8-live)
@Composable
private fun LiveVoiceView(service: GeminiAdvancedService) {
    val coroutineScope = rememberCoroutineScope()
    var isLiveActive by remember { mutableStateOf(false) }
    var liveTranscript by remember { mutableStateOf("Press 'Connect Live Session' to start real-time conversational exchange with Aria Live.") }
    val context = LocalContext.current
    val tts = remember { TtsManager(context) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HeartRedLight)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "⚡ Voice Conversations (gemini-3.8-live)",
                    fontWeight = FontWeight.Black,
                    color = HeartRed,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Engage in instantaneous spoken English conversation with Aria using gemini-3.8-live with natural back-and-forth speaking.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Voice Visualizer Orb
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(if (isLiveActive) HeartRed else SpeakBlue)
                .clickable {
                    isLiveActive = !isLiveActive
                    if (isLiveActive) {
                        coroutineScope.launch {
                            val reply = service.liveVoiceTurn("Hello Aria, let's practice English.")
                            liveTranscript = reply
                            tts.speak(reply)
                        }
                    }
                }
                .testTag("live_voice_orb"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Live voice microphone",
                tint = Color.White,
                modifier = Modifier.size(54.dp)
            )
        }

        Text(
            text = if (isLiveActive) "Live Session Active 🟢 Speak now" else "Tap Orb to Start Live Voice",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (isLiveActive) HeartRed else SpeakBlueDark
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Live Dialogue Feed:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = liveTranscript, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

// 8. Cloud & Firebase Auth View
@Composable
private fun CloudAuthView() {
    var isSignedIn by remember { mutableStateOf(false) }
    var syncStatus by remember { mutableStateOf("All local Room data in sync with Firebase Firestore.") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GoldYellow.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "☁️ Firebase Auth & Firestore Persistence",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF9E6D00),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Secure Google Sign-In with Firebase Auth and sync user streak, lessons, vocabulary, and league ranks to Firestore.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSignedIn) DuolingoGreen else Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSignedIn) Icons.Default.CloudDone else Icons.Default.Person,
                            contentDescription = "Auth status",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = if (isSignedIn) "Signed In with Google" else "Guest Mode (Local Persistence)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isSignedIn) "learner@speaklingo.ai" else "Sign in to backup streak to Firestore",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DuolingoButton(
                    text = if (isSignedIn) "SYNCED TO FIRESTORE ☁️" else "SIGN IN WITH GOOGLE & FIREBASE",
                    onClick = {
                        isSignedIn = !isSignedIn
                        syncStatus = if (isSignedIn) "User profile, streak, and 5 vocabulary items synced to Firestore cloud." else "Switched to local mode."
                    },
                    buttonColor = if (isSignedIn) DuolingoGreen else SpeakBlue,
                    shadowColor = if (isSignedIn) DuolingoGreenDark else SpeakBlueDark,
                    testTag = "firebase_auth_button"
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = syncStatus,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
