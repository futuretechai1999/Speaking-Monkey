package com.example.speaklingo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val streakDays: Int = 3,
    val lastPracticeDate: Long = System.currentTimeMillis(),
    val xp: Int = 140,
    val gems: Int = 250,
    val hearts: Int = 5,
    val maxHearts: Int = 5,
    val league: String = "Silver League",
    val completedLessonsCount: Int = 4
)

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val unitId: Int,
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val accuracy: Int = 0,
    val completedAt: Long = 0L
)

@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val englishMeaning: String,
    val hindiMeaning: String,
    val exampleSentence: String,
    val isMastered: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scenarioId: String,
    val isUser: Boolean,
    val text: String,
    val grammarCorrection: String? = null,
    val betterPhrasing: String? = null,
    val hindiTranslation: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
