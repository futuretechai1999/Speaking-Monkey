package com.example.speaklingo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.speaklingo.data.local.LessonProgressEntity
import com.example.speaklingo.data.local.UserProfileEntity
import com.example.speaklingo.data.model.GeneratedFlashcard
import com.example.speaklingo.data.model.LessonCatalog
import com.example.speaklingo.data.model.ProficiencyCatalog
import com.example.speaklingo.data.repository.AiTutorRepository
import com.example.speaklingo.data.repository.LearningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State representing the roadmap progress and Gemini flashcard generator status.
 */
data class FlashcardGeneratorUiState(
    // Roadmap Progress Details
    val currentRoadmapLevel: String = "Beginner (A1)",
    val currentRoadmapLevelCode: String = "A1",
    val currentUnitTitle: String = "Unit 1: Introductions & Daily Basics",
    val currentMilestoneTopic: String = "Greetings & Introductions",
    val completedLessonsCount: Int = 0,
    val totalLessonsCount: Int = 10,
    val progressPercentage: Float = 0.2f,
    val suggestedRoadmapTopics: List<String> = emptyList(),

    // Generator Configuration
    val selectedTopic: String = "Greetings & Introductions",
    val selectedLevel: String = "Beginner (A1)",
    val cardCount: Int = 4,

    // Execution & Room Persistence
    val isGenerating: Boolean = false,
    val generatedCards: List<GeneratedFlashcard> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isAllSaved: Boolean = false
)

/**
 * ViewModel that computes user progress on the English learning roadmap,
 * communicates with the Gemini API to generate customized vocabulary flashcards,
 * and persists cards into the local Room database.
 */
class FlashcardGeneratorViewModel(
    private val learningRepository: LearningRepository,
    private val aiTutorRepository: AiTutorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardGeneratorUiState())
    val uiState: StateFlow<FlashcardGeneratorUiState> = _uiState.asStateFlow()

    init {
        observeRoadmapProgress()
    }

    /**
     * Observes Room database progress and user profile to determine current roadmap tier.
     */
    private fun observeRoadmapProgress() {
        viewModelScope.launch {
            learningRepository.allLessonProgress.collect { progressList ->
                learningRepository.userProfile.collect { profile ->
                    updateRoadmapProgress(progressList, profile)
                }
            }
        }
    }

    private fun updateRoadmapProgress(
        progressList: List<LessonProgressEntity>,
        profile: UserProfileEntity?
    ) {
        val completedLessons = progressList.filter { it.isCompleted }
        val completedCount = completedLessons.size.coerceAtLeast(profile?.completedLessonsCount ?: 0)

        // Find active Unit from LessonCatalog
        val allUnits = LessonCatalog.units
        val totalLessons = allUnits.sumOf { it.lessons.size }
        val activeUnit = allUnits.firstOrNull { unit ->
            unit.lessons.any { lesson ->
                completedLessons.none { it.lessonId == lesson.id }
            }
        } ?: allUnits.first()

        // Derive active CEFR Level based on completed count
        val activeCefrLevel = when {
            completedCount < 4 -> ProficiencyCatalog.levels.find { it.code == "A1" } ?: ProficiencyCatalog.levels[0]
            completedCount < 8 -> ProficiencyCatalog.levels.find { it.code == "A2" } ?: ProficiencyCatalog.levels[1]
            completedCount < 14 -> ProficiencyCatalog.levels.find { it.code == "B1" } ?: ProficiencyCatalog.levels[2]
            completedCount < 20 -> ProficiencyCatalog.levels.find { it.code == "B2" } ?: ProficiencyCatalog.levels[3]
            else -> ProficiencyCatalog.levels.find { it.code == "C1" } ?: ProficiencyCatalog.levels[4]
        }

        // Active milestone topic from current unit and level
        val currentTopic = activeUnit.lessons.firstOrNull { lesson ->
            completedLessons.none { it.lessonId == lesson.id }
        }?.title ?: activeUnit.title

        // Suggested roadmap topics for this tier
        val topicsForTier = mutableListOf<String>()
        topicsForTier.add(currentTopic)
        activeUnit.lessons.forEach { lesson ->
            if (!topicsForTier.contains(lesson.title)) {
                topicsForTier.add(lesson.title)
            }
        }
        activeCefrLevel.modules.take(3).forEach { module ->
            if (!topicsForTier.contains(module.title)) {
                topicsForTier.add(module.title)
            }
        }

        val progressFrac = if (totalLessons > 0) {
            (completedCount.toFloat() / totalLessons.toFloat()).coerceIn(0.05f, 1.0f)
        } else 0.1f

        _uiState.update { current ->
            current.copy(
                currentRoadmapLevel = "${activeCefrLevel.title} (${activeCefrLevel.code})",
                currentRoadmapLevelCode = activeCefrLevel.code,
                currentUnitTitle = activeUnit.title,
                currentMilestoneTopic = currentTopic,
                completedLessonsCount = completedCount,
                totalLessonsCount = totalLessons,
                progressPercentage = progressFrac,
                suggestedRoadmapTopics = topicsForTier,
                selectedTopic = if (current.selectedTopic.isBlank() || current.selectedTopic == "Greetings & Introductions") {
                    currentTopic
                } else current.selectedTopic,
                selectedLevel = "${activeCefrLevel.title} (${activeCefrLevel.code})"
            )
        }
    }

    /**
     * Generates vocabulary flashcards via Gemini API based on the user's current roadmap progress.
     */
    fun generateFlashcardsForCurrentProgress() {
        val state = _uiState.value
        val topic = state.selectedTopic.ifBlank { state.currentMilestoneTopic }
        val level = state.selectedLevel.ifBlank { state.currentRoadmapLevel }
        val count = state.cardCount

        generateFlashcards(topic = topic, level = level, count = count)
    }

    /**
     * Generate flashcards with explicit parameters via Gemini API.
     */
    fun generateFlashcards(topic: String, level: String, count: Int = 4) {
        _uiState.update {
            it.copy(
                isGenerating = true,
                errorMessage = null,
                successMessage = null,
                isAllSaved = false
            )
        }

        viewModelScope.launch {
            try {
                // Call Gemini via repository
                val result = aiTutorRepository.generateFlashcards(
                    topic = topic,
                    level = level,
                    count = count
                )

                // Check Room database to see if any words are already saved
                val updatedCards = result.map { card ->
                    val isSaved = learningRepository.isWordSaved(card.word)
                    card.copy(isSavedToRoom = isSaved)
                }

                _uiState.update { current ->
                    current.copy(
                        isGenerating = false,
                        generatedCards = updatedCards,
                        successMessage = "Generated ${updatedCards.size} flashcards for '${topic}' with Gemini!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { current ->
                    current.copy(
                        isGenerating = false,
                        errorMessage = "Flashcard generation error: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Save a single generated flashcard to the local Room database.
     */
    fun saveCardToVault(card: GeneratedFlashcard) {
        viewModelScope.launch {
            try {
                learningRepository.addVocabulary(card.toVocabularyEntity())
                _uiState.update { current ->
                    val newCards = current.generatedCards.map {
                        if (it.id == card.id || it.word.equals(card.word, ignoreCase = true)) {
                            it.copy(isSavedToRoom = true)
                        } else it
                    }
                    current.copy(
                        generatedCards = newCards,
                        successMessage = "Saved '${card.word}' to your Vocabulary Vault! ⭐"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Could not save word: ${e.message}") }
            }
        }
    }

    /**
     * Bulk save all generated flashcards into the Room database.
     */
    fun saveAllCardsToVault() {
        val cards = _uiState.value.generatedCards
        if (cards.isEmpty()) return

        viewModelScope.launch {
            try {
                val entities = cards.map { it.toVocabularyEntity() }
                learningRepository.addVocabularyList(entities)
                _uiState.update { current ->
                    val markedCards = current.generatedCards.map { it.copy(isSavedToRoom = true) }
                    current.copy(
                        generatedCards = markedCards,
                        isAllSaved = true,
                        successMessage = "Saved all ${entities.size} flashcards to Room Database! 📥⭐"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Could not save cards: ${e.message}") }
            }
        }
    }

    fun updateSelectedTopic(topic: String) {
        _uiState.update { it.copy(selectedTopic = topic) }
    }

    fun updateSelectedLevel(level: String) {
        _uiState.update { it.copy(selectedLevel = level) }
    }

    fun updateCardCount(count: Int) {
        _uiState.update { it.copy(cardCount = count.coerceIn(2, 8)) }
    }

    fun dismissMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    companion object {
        fun provideFactory(
            learningRepository: LearningRepository,
            aiTutorRepository: AiTutorRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FlashcardGeneratorViewModel(learningRepository, aiTutorRepository) as T
            }
        }
    }
}
