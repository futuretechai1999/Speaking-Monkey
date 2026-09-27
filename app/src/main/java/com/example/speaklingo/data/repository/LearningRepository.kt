package com.example.speaklingo.data.repository

import com.example.speaklingo.data.local.AppDatabase
import com.example.speaklingo.data.local.LessonProgressEntity
import com.example.speaklingo.data.local.UserProfileEntity
import com.example.speaklingo.data.local.VocabularyEntity
import com.example.speaklingo.data.model.StreakCalculator
import kotlinx.coroutines.flow.Flow

class LearningRepository(private val db: AppDatabase) {

    val userProfile: Flow<UserProfileEntity?> = db.userDao().getUserProfile()
    val allLessonProgress: Flow<List<LessonProgressEntity>> = db.lessonDao().getAllProgress()
    val allVocabulary: Flow<List<VocabularyEntity>> = db.vocabularyDao().getAllVocabulary()
    val completedLessonTimestamps: Flow<List<Long>> = db.lessonDao().getCompletedTimestamps()

    suspend fun completeLesson(
        lessonId: String,
        unitId: Int,
        xpReward: Int,
        gemReward: Int,
        accuracy: Int,
        completionDate: Long = System.currentTimeMillis()
    ) {
        val stars = when {
            accuracy >= 90 -> 3
            accuracy >= 70 -> 2
            else -> 1
        }

        // 1. Record completed lesson in Room database
        db.lessonDao().saveProgress(
            LessonProgressEntity(
                lessonId = lessonId,
                unitId = unitId,
                isCompleted = true,
                stars = stars,
                accuracy = accuracy,
                completedAt = completionDate
            )
        )

        // 2. Fetch current profile from Room to calculate consecutive days
        val profile = db.userDao().getUserProfileOnce()
        val currentStreak = profile?.streakDays ?: 0
        val lastCompletionDate = profile?.lastPracticeDate ?: 0L

        // 3. Compute updated consecutive streak
        val calculatedStreak = StreakCalculator.calculateNewStreak(
            lastCompletionDate = lastCompletionDate,
            newCompletionDate = completionDate,
            currentStreak = currentStreak
        )

        // 4. Update XP, Gems, streakDays, and lastPracticeDate in Room
        db.userDao().addRewards(xpReward, gemReward)
        db.userDao().updateStreak(calculatedStreak, completionDate)
    }

    /**
     * Records a quick practice activity (e.g. from the StreakTracker or speech lab)
     * updating the last practice timestamp and streak calculation in Room.
     */
    suspend fun recordQuickPractice(
        xpReward: Int = 10,
        gemReward: Int = 5,
        practiceTimestamp: Long = System.currentTimeMillis()
    ) {
        val profile = db.userDao().getUserProfileOnce()
        val currentStreak = profile?.streakDays ?: 0
        val lastDate = profile?.lastPracticeDate ?: 0L

        val updatedStreak = StreakCalculator.calculateNewStreak(
            lastCompletionDate = lastDate,
            newCompletionDate = practiceTimestamp,
            currentStreak = currentStreak
        )

        db.userDao().addRewards(xpReward, gemReward)
        db.userDao().updateStreak(updatedStreak, practiceTimestamp)
    }

    suspend fun loseHeart() {
        db.userDao().decrementHeart()
    }

    suspend fun refillHearts() {
        db.userDao().refillHearts()
    }

    suspend fun addVocabulary(word: VocabularyEntity) {
        db.vocabularyDao().insertWord(word)
    }

    suspend fun addVocabularyList(words: List<VocabularyEntity>) {
        db.vocabularyDao().insertWords(words)
    }

    suspend fun isWordSaved(word: String): Boolean {
        return db.vocabularyDao().findWord(word) != null
    }

    suspend fun toggleMastered(id: Int, isMastered: Boolean) {
        db.vocabularyDao().setMastered(id, isMastered)
    }

    suspend fun deleteVocabulary(id: Int) {
        db.vocabularyDao().deleteWord(id)
    }
}
