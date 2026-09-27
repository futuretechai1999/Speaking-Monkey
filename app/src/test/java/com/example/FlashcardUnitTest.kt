package com.example

import com.example.speaklingo.data.local.VocabularyEntity
import com.example.speaklingo.data.model.FlashcardTopicCatalog
import com.example.speaklingo.data.model.GeneratedFlashcard
import com.example.speaklingo.data.model.toFlashcard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlashcardUnitTest {

    @Test
    fun `test flashcard topic presets and levels`() {
        val presets = FlashcardTopicCatalog.presets
        assertTrue("Topic presets must not be empty", presets.isNotEmpty())

        val interviewPreset = presets.find { it.title.contains("Interview", ignoreCase = true) }
        assertNotNull("Job Interview preset should exist", interviewPreset)

        val levels = FlashcardTopicCatalog.proficiencyLevels
        assertEquals(3, levels.size)
        assertTrue(levels.contains("Beginner (A1-A2)"))
        assertTrue(levels.contains("Intermediate (B1-B2)"))
        assertTrue(levels.contains("Advanced (C1-C2)"))
    }

    @Test
    fun `test conversion between GeneratedFlashcard and VocabularyEntity`() {
        val card = GeneratedFlashcard(
            word = "Spearhead",
            phonetic = "/ˈspɪə.hɛd/",
            partOfSpeech = "verb",
            englishMeaning = "To lead or take initiative.",
            hindiMeaning = "नेतृत्व करना",
            exampleSentence = "She spearheaded the project.",
            mnemonicOrTip = "Think spear.",
            synonyms = listOf("Lead", "Pioneer"),
            isSavedToRoom = false
        )

        val entity = card.toVocabularyEntity()
        assertEquals("Spearhead", entity.word)
        assertEquals("/ˈspɪə.hɛd/", entity.phonetic)
        assertEquals("verb", entity.partOfSpeech)
        assertEquals("To lead or take initiative.", entity.englishMeaning)
        assertEquals("नेतृत्व करना", entity.hindiMeaning)
        assertEquals("She spearheaded the project.", entity.exampleSentence)
        assertFalse("New entity shouldn't be marked mastered by default", entity.isMastered)

        // Convert back
        val convertedCard = entity.toFlashcard()
        assertEquals("Spearhead", convertedCard.word)
        assertTrue("Should be marked as saved to room", convertedCard.isSavedToRoom)
    }

    @Test
    fun `test flashcard equality and defaults`() {
        val card = GeneratedFlashcard(
            word = "Resilient",
            phonetic = "/rɪˈzɪl.jənt/",
            partOfSpeech = "adjective",
            englishMeaning = "Able to bounce back quickly.",
            hindiMeaning = "लचीला",
            exampleSentence = "She is resilient under pressure."
        )

        assertNotNull(card.id)
        assertFalse(card.isSavedToRoom)
        assertTrue(card.synonyms.isEmpty())
    }
}
