package com.example

import com.example.speaklingo.data.remote.GeminiApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying audio analysis against reference sentences,
 * phoneme feedback extraction, mouth position tips, and scoring logic.
 */
class PronunciationAnalysisUnitTest {

    private val geminiApiClient = GeminiApiClient()

    @Test
    fun testAccurateSpeechProducesHighOverallScoreAndNearPerfectFlag() {
        val reference = "I am excited to improve my English fluency."
        val spoken = "I am excited to improve my English fluency."

        val result = geminiApiClient.getFallbackPronunciationAnalysis(reference, spoken)

        assertNotNull(result)
        assertEquals(reference, result.referenceSentence)
        assertEquals(spoken, result.transcribedSentence)
        assertTrue("Accurate speech should score >= 85", result.overallScore >= 85)
        assertTrue("Accurate speech should be flagged near perfect", result.isNearPerfect)
        assertTrue("Actionable advice should be populated", result.actionableAdvice.isNotEmpty())
    }

    @Test
    fun testWordBreakdownMatchesReferenceTokens() {
        val reference = "The quick brown fox"
        val spoken = "The quick brown box"

        val result = geminiApiClient.getFallbackPronunciationAnalysis(reference, spoken)

        assertEquals(4, result.wordBreakdown.size)
        assertEquals("The", result.wordBreakdown[0].word)
        assertEquals("quick", result.wordBreakdown[1].word)
        assertEquals("brown", result.wordBreakdown[2].word)
        assertEquals("fox", result.wordBreakdown[3].word)

        // "fox" was replaced by "box", so it should be flagged as NEEDS_WORK or lower score
        val foxStatus = result.wordBreakdown[3].status
        assertTrue(
            "Mismatched word should not be EXCELLENT with 90+ score",
            foxStatus == "NEEDS_WORK" || foxStatus == "MISSED" || result.wordBreakdown[3].score < 80
        )
    }

    @Test
    fun testDentalFricativeThIdentifiedInPhonemeFeedback() {
        val reference = "I think that this theory is thoughtful."
        val spoken = "I tink dat dis teory is toutful."

        val result = geminiApiClient.getFallbackPronunciationAnalysis(reference, spoken)

        val hasThPhoneme = result.phonemeFeedbacks.any { it.sound.contains("θ") }
        assertTrue("Should detect /θ/ dental fricative challenge in sentence with 'think'", hasThPhoneme)

        val thFeedback = result.phonemeFeedbacks.first { it.sound.contains("θ") }
        assertTrue(
            "Phoneme feedback must contain actionable mouth position advice",
            thFeedback.actionableMouthPositionTip.contains("teeth", ignoreCase = true) ||
            thFeedback.actionableMouthPositionTip.contains("tongue", ignoreCase = true)
        )
    }

    @Test
    fun testActionableAdviceAndPracticeDrillAreGenerated() {
        val reference = "Water and vinegar are very versatile."
        val spoken = "Vater and vinegar are very versatile."

        val result = geminiApiClient.getFallbackPronunciationAnalysis(reference, spoken)

        assertTrue("Actionable advice list must not be empty", result.actionableAdvice.isNotEmpty())
        assertTrue("Must include suggested practice drill", result.suggestedPracticeDrill.isNotBlank())
        assertTrue("Must include Hindi summary explanation", result.hindiSummary.isNotBlank())
    }
}
