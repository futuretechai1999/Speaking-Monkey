package com.example

import com.example.speaklingo.data.model.ScenarioCatalog
import com.example.speaklingo.data.remote.ChatBotModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatUnitTest {

    @Test
    fun `test grammar and vocabulary scenarios exist in catalog`() {
        val scenarios = ScenarioCatalog.scenarios
        assertTrue("Scenarios catalog should not be empty", scenarios.isNotEmpty())

        val grammarScenario = scenarios.find { it.id == "grammar_tutor" }
        assertNotNull("Grammar Doctor scenario must exist", grammarScenario)
        assertEquals("Grammar Doctor", grammarScenario?.title)
        assertTrue(grammarScenario?.suggestedPhrases?.isNotEmpty() == true)

        val vocabScenario = scenarios.find { it.id == "vocab_tutor" }
        assertNotNull("Vocabulary Lab scenario must exist", vocabScenario)
        assertEquals("Vocabulary Lab", vocabScenario?.title)
        assertTrue(vocabScenario?.suggestedPhrases?.isNotEmpty() == true)
    }

    @Test
    fun `test chatbot models are available`() {
        val models = ChatBotModel.entries
        assertTrue(models.contains(ChatBotModel.FLASH))
        assertTrue(models.contains(ChatBotModel.PRO))
        assertTrue(models.contains(ChatBotModel.LITE))
    }
}
