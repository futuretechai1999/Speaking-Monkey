package com.example

import com.example.speaklingo.data.model.VeoLearnerResponse
import com.example.speaklingo.data.model.VeoScenario
import com.example.speaklingo.data.model.VeoScenarioCatalog
import com.example.speaklingo.ui.viewmodel.VeoVideoScenarioPlayerUiState
import com.example.speaklingo.ui.viewmodel.VeoVideoScenarioPlayerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VeoVideoScenarioPlayerTest {

    @Test
    fun testScenarioCatalogNotEmpty() {
        val scenarios = VeoScenarioCatalog.scenarios
        assertTrue("Scenario catalog should have preset situations", scenarios.isNotEmpty())
        assertTrue("Should have at least 5 scenarios", scenarios.size >= 5)

        val first = scenarios.first()
        assertEquals("veo_airport_immigration", first.id)
        assertEquals("Airport Passport Control", first.title)
        assertEquals("Travel", first.category)
        assertEquals("A2", first.cefrLevel)
        assertEquals("Officer Miller", first.characterName)
        assertTrue(first.videoUrl.startsWith("http"))
        assertTrue(first.dialogueLines.isNotEmpty())
        assertTrue(first.keyVocabulary.isNotEmpty())
        assertTrue(first.suggestedLearnerResponses.isNotEmpty())
        assertTrue(first.cultureTip.isNotBlank())
    }

    @Test
    fun testDefaultUiState() {
        val state = VeoVideoScenarioPlayerUiState()
        assertEquals("Airport Passport Control", state.currentScenario.title)
        assertFalse(state.isPlaying)
        assertEquals(0, state.currentPositionSec)
        assertTrue(state.showSubtitles)
        assertTrue(state.showHindiTranslation)
        assertEquals("16:9", state.aspectRatio)
        assertEquals("veo-3.1-fast-generate-preview", state.selectedModel)
        assertFalse(state.isGenerating)
    }

    @Test
    fun testSelectScenario() {
        val viewModel = VeoVideoScenarioPlayerViewModel()
        val secondScenario = VeoScenarioCatalog.scenarios[1] // London Cafe
        viewModel.selectScenario(secondScenario)

        val state = viewModel.uiState.value
        assertEquals("Ordering at a London Cafe", state.currentScenario.title)
        assertEquals("Dining", state.currentScenario.category)
        assertEquals("A1", state.currentScenario.cefrLevel)
        assertEquals(0, state.currentPositionSec)
        assertFalse(state.isPlaying)
    }

    @Test
    fun testToggleControls() {
        val viewModel = VeoVideoScenarioPlayerViewModel()
        assertTrue(viewModel.uiState.value.showSubtitles)
        viewModel.toggleSubtitles()
        assertFalse(viewModel.uiState.value.showSubtitles)

        assertTrue(viewModel.uiState.value.showHindiTranslation)
        viewModel.toggleHindi()
        assertFalse(viewModel.uiState.value.showHindiTranslation)

        viewModel.setAspectRatio("9:16")
        assertEquals("9:16", viewModel.uiState.value.aspectRatio)

        viewModel.setModel("veo-3.1-generate-preview")
        assertEquals("veo-3.1-generate-preview", viewModel.uiState.value.selectedModel)
    }

    @Test
    fun testEvaluateSpokenReplyAccuracy() {
        val viewModel = VeoVideoScenarioPlayerViewModel()
        val target = VeoLearnerResponse(
            textEnglish = "Can I please get a flat white with oat milk?",
            textHindi = "क्या मुझे ओट मिल्क के साथ एक फ्लैट व्हाइट कॉफ़ी मिल सकती है?",
            phonetic = "/kæn aɪ pliːz ɡɛt/",
            difficulty = "Easy"
        )

        // Perfect match
        viewModel.evaluateSpokenReply("Can I please get a flat white with oat milk?", target)
        val state = viewModel.uiState.value
        assertNotNull(state.speechScore)
        assertTrue(state.speechScore!! >= 90)
        assertTrue(state.speechFeedback!!.contains("Outstanding") || state.speechFeedback!!.contains("Great"))

        // Partial match
        viewModel.evaluateSpokenReply("Can I get a flat white?", target)
        val partialState = viewModel.uiState.value
        assertNotNull(partialState.speechScore)
        assertTrue(partialState.speechScore!! >= 50)
    }
}
