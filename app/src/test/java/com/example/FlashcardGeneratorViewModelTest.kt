package com.example

import com.example.speaklingo.data.model.GeneratedFlashcard
import com.example.speaklingo.data.model.LessonCatalog
import com.example.speaklingo.data.model.ProficiencyCatalog
import com.example.speaklingo.ui.viewmodel.FlashcardGeneratorUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlashcardGeneratorViewModelTest {

    @Test
    fun testDefaultUiState() {
        val state = FlashcardGeneratorUiState()
        assertEquals("Beginner (A1)", state.currentRoadmapLevel)
        assertEquals("A1", state.currentRoadmapLevelCode)
        assertEquals("Unit 1: Introductions & Daily Basics", state.currentUnitTitle)
        assertEquals("Greetings & Introductions", state.currentMilestoneTopic)
        assertEquals(0, state.completedLessonsCount)
        assertFalse(state.isGenerating)
        assertTrue(state.generatedCards.isEmpty())
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
        assertFalse(state.isAllSaved)
    }

    @Test
    fun testUiStateWithGeneratedCards() {
        val testCards = listOf(
            GeneratedFlashcard(
                word = "Welcome",
                phonetic = "/ˈwɛl.kəm/",
                partOfSpeech = "noun / verb",
                englishMeaning = "A friendly greeting to someone arriving.",
                hindiMeaning = "स्वागत",
                exampleSentence = "Welcome to our English class!",
                isSavedToRoom = false
            ),
            GeneratedFlashcard(
                word = "Introduce",
                phonetic = "/ˌɪn.trəˈdjuːs/",
                partOfSpeech = "verb",
                englishMeaning = "To make someone known by name to another in person.",
                hindiMeaning = "परिचय कराना",
                exampleSentence = "Let me introduce my friend Priya.",
                isSavedToRoom = true
            )
        )

        val state = FlashcardGeneratorUiState(
            currentRoadmapLevel = "Beginner (A1)",
            currentRoadmapLevelCode = "A1",
            currentUnitTitle = "Unit 1: Introductions & Daily Basics",
            currentMilestoneTopic = "Greetings & Introductions",
            selectedTopic = "Greetings & Introductions",
            generatedCards = testCards,
            isGenerating = false,
            successMessage = "Generated 2 flashcards!"
        )

        assertEquals(2, state.generatedCards.size)
        assertEquals("Welcome", state.generatedCards[0].word)
        assertFalse(state.generatedCards[0].isSavedToRoom)
        assertTrue(state.generatedCards[1].isSavedToRoom)
        assertEquals("Generated 2 flashcards!", state.successMessage)
    }

    @Test
    fun testRoadmapCatalogsIntegrityForFlashcards() {
        // Ensure LessonCatalog units are present for roadmap-based topics
        assertTrue(LessonCatalog.units.isNotEmpty())
        val unit1 = LessonCatalog.units.first()
        assertEquals(1, unit1.id)
        assertTrue(unit1.lessons.isNotEmpty())

        // Ensure CEFR levels are present for roadmap-based levels
        assertTrue(ProficiencyCatalog.levels.isNotEmpty())
        val a1 = ProficiencyCatalog.levels.first()
        assertEquals("A1", a1.code)
        assertTrue(a1.modules.isNotEmpty())
    }
}
