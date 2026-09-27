package com.example.speaklingo.data.repository

import com.example.speaklingo.data.local.AppDatabase
import com.example.speaklingo.data.local.ChatMessageEntity
import com.example.speaklingo.data.remote.AiTutorResponse
import com.example.speaklingo.data.remote.GeminiApiClient
import com.example.speaklingo.data.remote.SpeechEvaluationResult
import com.example.speaklingo.data.remote.WordLookupResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AiTutorRepository(
    private val db: AppDatabase,
    private val geminiClient: GeminiApiClient
) {
    fun getMessages(scenarioId: String): Flow<List<ChatMessageEntity>> {
        return db.chatDao().getMessagesForScenario(scenarioId)
    }

    suspend fun sendMessage(
        scenarioId: String,
        scenarioContext: String,
        userText: String,
        modelId: String = "gemini-3.5-flash",
        customRoleInstruction: String? = null
    ): AiTutorResponse {
        // 1. Save user message to database
        db.chatDao().insertMessage(
            ChatMessageEntity(
                scenarioId = scenarioId,
                isUser = true,
                text = userText
            )
        )

        // 2. Fetch existing history for context
        val existing = db.chatDao().getMessagesForScenario(scenarioId).firstOrNull() ?: emptyList()
        val historyPairs = existing.map { Pair(it.text, it.isUser) }

        // 3. Call AI with chosen model and role instruction
        val aiResponse = geminiClient.chatWithAiTutor(
            scenarioContext = scenarioContext,
            history = historyPairs,
            userMessage = userText,
            modelId = modelId,
            customRoleInstruction = customRoleInstruction
        )

        // 4. Save AI response
        db.chatDao().insertMessage(
            ChatMessageEntity(
                scenarioId = scenarioId,
                isUser = false,
                text = aiResponse.replyText,
                grammarCorrection = aiResponse.grammarCorrection,
                betterPhrasing = aiResponse.betterPhrasing,
                hindiTranslation = aiResponse.hindiTranslation
            )
        )

        return aiResponse
    }

    suspend fun clearHistory(scenarioId: String) {
        db.chatDao().clearMessages(scenarioId)
    }

    suspend fun evaluateSpeech(
        targetSentence: String,
        spokenSentence: String
    ): SpeechEvaluationResult {
        return geminiClient.evaluateSpeech(targetSentence, spokenSentence)
    }

    suspend fun analyzeAudioPronunciation(
        referenceSentence: String,
        audioBase64: String? = null,
        audioMimeType: String = "audio/wav",
        spokenText: String? = null
    ): com.example.speaklingo.data.model.PronunciationAnalysisResult {
        return geminiClient.analyzeAudioPronunciation(
            referenceSentence = referenceSentence,
            audioBase64 = audioBase64,
            audioMimeType = audioMimeType,
            spokenText = spokenText
        )
    }

    suspend fun transcribeAndCoachSpeech(
        spokenText: String
    ): com.example.speaklingo.data.remote.SpokenTranscriptionCoaching {
        return geminiClient.transcribeAndCoachSpeech(spokenText)
    }

    suspend fun lookupWord(word: String): WordLookupResult {
        return geminiClient.lookupWord(word)
    }

    suspend fun generateFlashcards(
        topic: String,
        level: String,
        count: Int = 5
    ): List<com.example.speaklingo.data.model.GeneratedFlashcard> {
        return geminiClient.generateFlashcards(topic, level, count)
    }

    fun isGeminiConfigured(): Boolean {
        return geminiClient.isApiKeyConfigured()
    }
}
