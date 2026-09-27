package com.example

import com.example.speaklingo.data.remote.SpeechEvaluationResult
import com.example.speaklingo.data.remote.SpokenTranscriptionCoaching
import com.example.speaklingo.data.remote.WordPronunciationScore
import com.example.speaklingo.ui.screens.speaking.SpeechLabMode
import com.example.speaklingo.ui.screens.speaking.challenges
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechRecognitionTest {

    @Test
    fun testSpeechChallengesCatalogNotEmpty() {
        assertTrue("Speech challenges should not be empty", challenges.isNotEmpty())
        assertEquals(6, challenges.size)

        val firstChallenge = challenges.first()
        assertEquals("Self Introduction", firstChallenge.title)
        assertTrue(firstChallenge.targetSentence.isNotBlank())
        assertTrue(firstChallenge.phoneticGuide.startsWith("/"))
        assertTrue(firstChallenge.hindiMeaning.isNotBlank())
        assertTrue(firstChallenge.level.isNotBlank())
        assertTrue(firstChallenge.keyPhonicsFocus.isNotBlank())
    }

    @Test
    fun testSpeechLabModes() {
        val modes = SpeechLabMode.entries
        assertEquals(2, modes.size)
        assertTrue(modes.contains(SpeechLabMode.GUIDED_PRONUNCIATION))
        assertTrue(modes.contains(SpeechLabMode.FREE_TRANSCRIPTION))
        assertEquals("🎯", SpeechLabMode.GUIDED_PRONUNCIATION.emoji)
        assertEquals("🗣️", SpeechLabMode.FREE_TRANSCRIPTION.emoji)
    }

    @Test
    fun testSpeechEvaluationResultDataClass() {
        val wordScores = listOf(
            WordPronunciationScore("Hello", true, "Crisp pronunciation"),
            WordPronunciationScore("world", true, null)
        )
        val result = SpeechEvaluationResult(
            score = 92,
            feedback = "Excellent pronunciation with crisp consonant sounds!",
            pronunciationTip = "Focus on elongating vowels.",
            grammarRemark = "Grammar is spot on.",
            isExcellent = true,
            accuracyScore = 94,
            fluencyScore = 90,
            wordScores = wordScores,
            hindiCoaching = "बहुत बढ़िया उच्चारण!",
            nativeBetterPhrasing = "Hello world!"
        )

        assertEquals(92, result.score)
        assertEquals(90, result.fluencyScore)
        assertEquals(94, result.accuracyScore)
        assertEquals(2, result.wordScores.size)
        assertTrue(result.wordScores[0].isCorrect)
        assertTrue(result.isExcellent)
        assertEquals("बहुत बढ़िया उच्चारण!", result.hindiCoaching)
    }

    @Test
    fun testSpokenTranscriptionCoachingDataClass() {
        val coaching = SpokenTranscriptionCoaching(
            transcribedText = "I am go to store yesterday.",
            correctedText = "I went to the store yesterday.",
            naturalNativeAlternative = "I popped over to the grocery store yesterday.",
            grammarExplanation = "Past tense verb required: 'went' instead of 'go'.",
            hindiTranslation = "मैं कल दुकान गया था।",
            pronunciationAdvice = "Link 'went' and 'to' naturally.",
            fluencyRating = 75
        )

        assertEquals("I am go to store yesterday.", coaching.transcribedText)
        assertEquals("I went to the store yesterday.", coaching.correctedText)
        assertEquals("I popped over to the grocery store yesterday.", coaching.naturalNativeAlternative)
        assertEquals(75, coaching.fluencyRating)
        assertFalse(coaching.naturalNativeAlternative.isBlank())
        assertNotNull(coaching.hindiTranslation)
    }
}
