package com.example.speaklingo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.speaklingo.data.model.VeoDialogueLine
import com.example.speaklingo.data.model.VeoLearnerResponse
import com.example.speaklingo.data.model.VeoScenario
import com.example.speaklingo.data.model.VeoScenarioCatalog
import com.example.speaklingo.data.remote.VeoVideoService
import com.example.speaklingo.data.repository.LearningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Veo Video Scenario Player.
 */
data class VeoVideoScenarioPlayerUiState(
    val currentScenario: VeoScenario = VeoScenarioCatalog.scenarios.first(),
    val availableScenarios: List<VeoScenario> = VeoScenarioCatalog.scenarios,
    val isPlaying: Boolean = false,
    val currentPositionSec: Int = 0,
    val totalDurationSec: Int = 12,
    val activeDialogueLine: VeoDialogueLine? = null,
    val showSubtitles: Boolean = true,
    val showHindiTranslation: Boolean = true,
    val aspectRatio: String = "16:9", // "16:9" or "9:16"
    val isGenerating: Boolean = false,
    val generationStatusText: String? = null,
    val selectedModel: String = "veo-3.1-fast-generate-preview",
    val userSpokenText: String? = null,
    val evaluatedResponse: VeoLearnerResponse? = null,
    val speechScore: Int? = null,
    val speechFeedback: String? = null,
    val isSpeechListening: Boolean = false,
    val isVideoBuffering: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel managing situational video playback, interactive roleplay subtitles,
 * Veo API situational scenario generation, and speaking evaluation.
 */
class VeoVideoScenarioPlayerViewModel(
    private val veoVideoService: VeoVideoService = VeoVideoService(),
    private val learningRepository: LearningRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VeoVideoScenarioPlayerUiState(
            activeDialogueLine = VeoScenarioCatalog.scenarios.first().dialogueLines.firstOrNull()
        )
    )
    val uiState: StateFlow<VeoVideoScenarioPlayerUiState> = _uiState.asStateFlow()

    fun selectScenario(scenario: VeoScenario) {
        _uiState.update { current ->
            current.copy(
                currentScenario = scenario,
                aspectRatio = scenario.aspectRatio,
                currentPositionSec = 0,
                isPlaying = false,
                activeDialogueLine = scenario.dialogueLines.firstOrNull(),
                evaluatedResponse = null,
                speechScore = null,
                speechFeedback = null,
                userSpokenText = null,
                errorMessage = null
            )
        }
    }

    fun onPlaybackPositionChanged(sec: Int, duration: Int) {
        _uiState.update { current ->
            val scenario = current.currentScenario
            // Match active dialogue line to current timestamp
            val matchingLine = scenario.dialogueLines.lastOrNull { it.timestampSec <= sec }
                ?: scenario.dialogueLines.firstOrNull()

            current.copy(
                currentPositionSec = sec,
                totalDurationSec = if (duration > 0) duration else current.totalDurationSec,
                activeDialogueLine = matchingLine
            )
        }
    }

    fun setPlaying(playing: Boolean) {
        _uiState.update { it.copy(isPlaying = playing) }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun setAspectRatio(ratio: String) {
        _uiState.update { it.copy(aspectRatio = if (ratio == "9:16") "9:16" else "16:9") }
    }

    fun toggleSubtitles() {
        _uiState.update { it.copy(showSubtitles = !it.showSubtitles) }
    }

    fun toggleHindi() {
        _uiState.update { it.copy(showHindiTranslation = !it.showHindiTranslation) }
    }

    fun setModel(model: String) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun setSpeechListening(listening: Boolean) {
        _uiState.update { it.copy(isSpeechListening = listening) }
    }

    /**
     * Generates a new scenario using the Veo API and Gemini curriculum intelligence.
     */
    fun generateVeoScenario(
        prompt: String,
        category: String = "Daily Life",
        level: String = "B1",
        aspectRatio: String = "16:9",
        model: String = "veo-3.1-fast-generate-preview"
    ) {
        if (prompt.isBlank()) return

        _uiState.update {
            it.copy(
                isGenerating = true,
                generationStatusText = "Veo 3 ($model) generating situational scene...",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = veoVideoService.generateSituationalScenario(
                userPrompt = prompt,
                category = category,
                cefrLevel = level,
                aspectRatio = aspectRatio,
                model = model
            )

            result.onSuccess { newScenario ->
                _uiState.update { current ->
                    val updatedList = listOf(newScenario) + current.availableScenarios
                    current.copy(
                        isGenerating = false,
                        generationStatusText = null,
                        currentScenario = newScenario,
                        availableScenarios = updatedList,
                        aspectRatio = newScenario.aspectRatio,
                        currentPositionSec = 0,
                        isPlaying = false,
                        activeDialogueLine = newScenario.dialogueLines.firstOrNull(),
                        evaluatedResponse = null,
                        speechScore = null,
                        speechFeedback = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generationStatusText = null,
                        errorMessage = "Generation error: ${error.message}"
                    )
                }
            }
        }
    }

    /**
     * Evaluates a user's spoken or typed reply against a target learner response.
     */
    fun evaluateSpokenReply(userReply: String, target: VeoLearnerResponse) {
        val cleanUser = userReply.trim().lowercase().replace(Regex("[^a-zA-Z0-9 ]"), "")
        val cleanTarget = target.textEnglish.trim().lowercase().replace(Regex("[^a-zA-Z0-9 ]"), "")

        val userWords = cleanUser.split(" ").filter { it.isNotBlank() }
        val targetWords = cleanTarget.split(" ").filter { it.isNotBlank() }

        val matchCount = userWords.count { targetWords.contains(it) }
        val calculatedAccuracy = if (targetWords.isNotEmpty()) {
            ((matchCount.toFloat() / targetWords.size.toFloat()) * 100).toInt().coerceIn(40, 100)
        } else 85

        val feedback = when {
            calculatedAccuracy >= 85 -> "Outstanding! Your pronunciation and phrasing are natural and confident. 🌟"
            calculatedAccuracy >= 70 -> "Great attempt! Good conversational rhythm. Try linking phrases smoothly. 👍"
            else -> "Good effort! Practice saying the sentence again with the audio guide. 🎯"
        }

        // Award reward XP
        learningRepository?.let { repo ->
            viewModelScope.launch {
                repo.completeLesson(
                    lessonId = "veo_scenario_${_uiState.value.currentScenario.id}",
                    unitId = 99,
                    xpReward = 15,
                    gemReward = 5,
                    accuracy = calculatedAccuracy
                )
            }
        }

        _uiState.update {
            it.copy(
                userSpokenText = userReply,
                evaluatedResponse = target,
                speechScore = calculatedAccuracy,
                speechFeedback = feedback
            )
        }
    }

    fun dismissFeedback() {
        _uiState.update {
            it.copy(
                userSpokenText = null,
                evaluatedResponse = null,
                speechScore = null,
                speechFeedback = null,
                errorMessage = null
            )
        }
    }

    companion object {
        fun provideFactory(
            veoVideoService: VeoVideoService = VeoVideoService(),
            learningRepository: LearningRepository? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VeoVideoScenarioPlayerViewModel(veoVideoService, learningRepository) as T
            }
        }
    }
}
