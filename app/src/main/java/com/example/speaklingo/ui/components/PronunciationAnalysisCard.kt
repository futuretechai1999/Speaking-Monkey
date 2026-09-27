package com.example.speaklingo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.speaklingo.data.model.DetailedWordPronunciation
import com.example.speaklingo.data.model.PhonemeFeedback
import com.example.speaklingo.data.model.PronunciationAnalysisResult
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark
import com.example.ui.theme.DuolingoGreenLight
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FireOrangeDark
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GoldYellowDark
import com.example.ui.theme.HeartRed
import com.example.ui.theme.HeartRedDark
import com.example.ui.theme.HeartRedLight
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark

/**
 * PronunciationAnalysisCard: Rich visual feedback component displaying Gemini's analysis
 * of user audio compared directly against a reference sentence.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PronunciationAnalysisCard(
    result: PronunciationAnalysisResult,
    onListenReference: () -> Unit,
    onListenDrill: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedWordForDetail by remember { mutableStateOf<DetailedWordPronunciation?>(null) }

    val scoreColor = when {
        result.overallScore >= 90 -> DuolingoGreen
        result.overallScore >= 75 -> SpeakBlue
        result.overallScore >= 60 -> GoldYellowDark
        else -> FireOrange
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pronunciation_analysis_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header with Overall Score Gauge & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(scoreColor.copy(alpha = 0.85f), scoreColor)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${result.overallScore}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "SCORE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (result.isNearPerfect) "Near-Native Articulation! 🎉" 
                                       else if (result.overallScore >= 75) "Great Clear Speech! 👏" 
                                       else "Actionable Phonetic Tips 💡",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Gemini Audio vs Reference Sentence",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Reference audio button
                IconButton(
                    onClick = onListenReference,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SpeakBlue.copy(alpha = 0.12f))
                        .testTag("listen_reference_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Listen to reference native audio",
                        tint = SpeakBlue
                    )
                }
            }

            // 2. Metrics Breakdown (Pronunciation, Fluency, Rhythm)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    title = "Pronunciation",
                    score = result.pronunciationScore,
                    color = DuolingoGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "Fluency",
                    score = result.fluencyScore,
                    color = SpeakBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "Rhythm",
                    score = result.rhythmIntonationScore,
                    color = FireOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. Reference vs User Audio Transcription
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Reference Sentence
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "🎯 TARGET REFERENCE:", fontSize = 10.sp, fontWeight = FontWeight.Black, color = SpeakBlueDark)
                            if (result.expectedIpa.isNotBlank()) {
                                Text(
                                    text = result.expectedIpa,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "\"${result.referenceSentence}\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Audio Transcription
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "🎙️ HEARD IN YOUR AUDIO:", fontSize = 10.sp, fontWeight = FontWeight.Black, color = DuolingoGreenDark)
                            if (result.spokenIpa.isNotBlank()) {
                                Text(
                                    text = result.spokenIpa,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "\"${result.transcribedSentence}\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // 4. Interactive Word Breakdown Pills
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Word-by-Word Articulation",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap a word for IPA & tips",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    result.wordBreakdown.forEach { wordItem ->
                        WordAssessmentPill(
                            wordItem = wordItem,
                            onClick = { selectedWordForDetail = wordItem }
                        )
                    }
                }
            }

            // 5. Actionable Mouth & Tongue Placement Coaching (Phonemes)
            if (result.phonemeFeedbacks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "👄 Actionable Mouth & Tongue Guidance",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    result.phonemeFeedbacks.forEach { feedback ->
                        PhonemeCoachingCard(feedback = feedback)
                    }
                }
            }

            // 6. Intonation, Rhythm & Minimal Pair Drills
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = GoldYellow.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldYellow.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🎵", fontSize = 16.sp)
                        Text(
                            text = "Rhythm, Cadence & Stress",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF9E6D00)
                        )
                    }

                    Text(
                        text = result.intonationCommentary,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )

                    if (result.suggestedPracticeDrill.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "DRILL TO PRACTICE:",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SpeakBlueDark
                                    )
                                    Text(
                                        text = result.suggestedPracticeDrill,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (onListenDrill != null) {
                                    IconButton(
                                        onClick = { onListenDrill(result.suggestedPracticeDrill) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Listen to drill",
                                            tint = SpeakBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Actionable Improvement Checklist
            if (result.actionableAdvice.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "✅ Actionable Next Steps",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    result.actionableAdvice.forEachIndexed { index, tip ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpeakBlue
                            )
                            Text(
                                text = tip,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 8. Hindi Bilingual Summary
            if (result.hindiSummary.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DuolingoGreenLight.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🇮🇳", fontSize = 16.sp)
                        Text(
                            text = result.hindiSummary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DuolingoGreenDark
                        )
                    }
                }
            }
        }
    }

    // Modal dialog when user taps a specific word
    selectedWordForDetail?.let { wordItem ->
        WordDetailDialog(
            wordItem = wordItem,
            onDismiss = { selectedWordForDetail = null }
        )
    }
}

@Composable
private fun MetricChip(
    title: String,
    score: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$score%",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WordAssessmentPill(
    wordItem: DetailedWordPronunciation,
    onClick: () -> Unit
) {
    val (pillBg, textColor, borderColor) = when (wordItem.status) {
        "EXCELLENT" -> Triple(DuolingoGreen.copy(alpha = 0.12f), DuolingoGreenDark, DuolingoGreen)
        "GOOD" -> Triple(SpeakBlue.copy(alpha = 0.12f), SpeakBlueDark, SpeakBlue)
        "NEEDS_WORK" -> Triple(FireOrange.copy(alpha = 0.12f), FireOrangeDark, FireOrange)
        else -> Triple(HeartRed.copy(alpha = 0.12f), HeartRedDark, HeartRed)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = pillBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("word_pill_${wordItem.word.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = wordItem.word,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "${wordItem.score}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun PhonemeCoachingCard(feedback: PhonemeFeedback) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SpeakBlue.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = feedback.sound,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = SpeakBlueDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "in \"${feedback.targetWord}\"",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = feedback.issueDescription,
                fontSize = 11.sp,
                color = HeartRedDark,
                fontWeight = FontWeight.SemiBold
            )

            // Actionable mouth position tip
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "👅", fontSize = 14.sp)
                Text(
                    text = feedback.actionableMouthPositionTip,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 15.sp
                )
            }

            if (feedback.hindiContrastTip.isNotBlank()) {
                Text(
                    text = "💡 Hindi contrast: ${feedback.hindiContrastTip}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WordDetailDialog(
    wordItem: DetailedWordPronunciation,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = wordItem.word,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Score: ${wordItem.score}% (${wordItem.status})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (wordItem.status) {
                                "EXCELLENT" -> DuolingoGreenDark
                                "GOOD" -> SpeakBlueDark
                                "NEEDS_WORK" -> FireOrangeDark
                                else -> HeartRedDark
                            }
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Expected IPA: ${wordItem.expectedIpa}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SpeakBlueDark
                        )
                        if (wordItem.spokenIpa.isNotBlank()) {
                            Text(
                                text = "Heard IPA: ${wordItem.spokenIpa}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DuolingoGreenDark
                            )
                        }
                    }
                }

                wordItem.actionableTip?.let { tip ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "💡", fontSize = 14.sp)
                        Text(
                            text = tip,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
