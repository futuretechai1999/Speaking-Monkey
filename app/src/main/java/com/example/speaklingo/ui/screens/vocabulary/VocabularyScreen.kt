package com.example.speaklingo.ui.screens.vocabulary

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.speaklingo.audio.TtsManager
import com.example.speaklingo.data.local.VocabularyEntity
import com.example.speaklingo.data.repository.AiTutorRepository
import com.example.speaklingo.data.repository.LearningRepository
import com.example.speaklingo.ui.components.DuolingoButton
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.HeartRed
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark
import com.example.ui.theme.SpeakBlueLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen(
    learningRepository: LearningRepository,
    aiTutorRepository: AiTutorRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val vocabularyList by learningRepository.allVocabulary.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf("FLASHCARDS") }

    if (viewMode == "FLASHCARDS") {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // View Mode Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(DuolingoGreen)
                        .clickable { viewMode = "FLASHCARDS" }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎴 AI Flashcards & Deck",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color.Transparent)
                        .clickable { viewMode = "LIST" }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📋 All Words List (${vocabularyList.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            FlashcardsScreen(
                learningRepository = learningRepository,
                aiTutorRepository = aiTutorRepository,
                modifier = Modifier.weight(1f)
            )
        }
        return
    }

    // TTS
    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    val filteredList = vocabularyList.filter {
        it.word.contains(searchQuery, ignoreCase = true) ||
        it.hindiMeaning.contains(searchQuery, ignoreCase = true) ||
        it.englishMeaning.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // View Mode Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color.Transparent)
                        .clickable { viewMode = "FLASHCARDS" }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎴 AI Flashcards & Deck",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(SpeakBlue)
                        .clickable { viewMode = "LIST" }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📋 All Words List (${vocabularyList.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vocabulary Bank 📚",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${vocabularyList.size} Words Saved • Tap speaker to pronounce",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search English or Hindi meaning...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search words")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vocab_search_field"),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Words List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "No words saved yet. Add your first word!" else "No matching words found.",
                                color = Color.Gray,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                items(filteredList) { wordItem ->
                    VocabCard(
                        item = wordItem,
                        onPlayAudio = { ttsManager.speak(wordItem.word) },
                        onToggleMastered = {
                            coroutineScope.launch {
                                learningRepository.toggleMastered(wordItem.id, !wordItem.isMastered)
                            }
                        },
                        onDelete = {
                            coroutineScope.launch {
                                learningRepository.deleteVocabulary(wordItem.id)
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Floating Action Button to Add / Lookup Word
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_vocab_fab"),
            containerColor = DuolingoGreen,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add new word with AI")
        }
    }

    // Add Word Dialog
    if (showAddDialog) {
        AddWordWithAiDialog(
            aiTutorRepository = aiTutorRepository,
            onWordAdded = { newWord ->
                coroutineScope.launch {
                    learningRepository.addVocabulary(newWord)
                }
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun VocabCard(
    item: VocabularyEntity,
    onPlayAudio: () -> Unit,
    onToggleMastered: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vocab_card_${item.word}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        text = item.word,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.phonetic,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Audio Pronunciation
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SpeakBlueLight)
                            .clickable(onClick = onPlayAudio),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Pronounce ${item.word}",
                            tint = SpeakBlueDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Mastered Checkbox
                    IconButton(
                        onClick = onToggleMastered,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isMastered) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (item.isMastered) "Mastered" else "Mark as mastered",
                            tint = if (item.isMastered) DuolingoGreen else Color.LightGray
                        )
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete word",
                            tint = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Hindi Meaning
            Text(
                text = "🇮🇳 ${item.hindiMeaning}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SpeakBlueDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            // English Meaning
            Text(
                text = item.englishMeaning,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Example Sentence
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "“${item.exampleSentence}”",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWordWithAiDialog(
    aiTutorRepository: AiTutorRepository,
    onWordAdded: (VocabularyEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var inputWord by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_word_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🤖 Add Word with AI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Type any English word. Gemini AI will generate pronunciation, Hindi meaning, and an example sentence.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputWord,
                    onValueChange = { inputWord = it },
                    placeholder = { Text("e.g. Resilient, Articulate, Serendipity") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lookup_word_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = DuolingoGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Fetching details from Gemini AI...", fontSize = 12.sp, color = Color.Gray)
                } else {
                    DuolingoButton(
                        text = "LOOKUP & SAVE",
                        enabled = inputWord.isNotBlank(),
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                val lookup = aiTutorRepository.lookupWord(inputWord.trim())
                                val newEntity = VocabularyEntity(
                                    word = lookup.word,
                                    phonetic = lookup.phonetic,
                                    partOfSpeech = lookup.partOfSpeech,
                                    englishMeaning = lookup.englishMeaning,
                                    hindiMeaning = lookup.hindiMeaning,
                                    exampleSentence = lookup.exampleSentence,
                                    isMastered = false
                                )
                                isLoading = false
                                onWordAdded(newEntity)
                            }
                        },
                        buttonColor = DuolingoGreen,
                        shadowColor = DuolingoGreenDark,
                        testTag = "confirm_add_word_button"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DuolingoButton(
                        text = "CANCEL",
                        onClick = onDismiss,
                        buttonColor = Color.LightGray,
                        shadowColor = Color.Gray,
                        textColor = Color.DarkGray,
                        height = 44.dp
                    )
                }
            }
        }
    }
}
