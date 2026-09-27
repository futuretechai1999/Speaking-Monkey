package com.example.speaklingo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET xp = xp + :gainedXp, gems = gems + :gainedGems WHERE id = 1")
    suspend fun addRewards(gainedXp: Int, gainedGems: Int)

    @Query("UPDATE user_profile SET hearts = MAX(0, hearts - 1) WHERE id = 1")
    suspend fun decrementHeart()

    @Query("UPDATE user_profile SET hearts = 5 WHERE id = 1")
    suspend fun refillHearts()

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Query("UPDATE user_profile SET streakDays = streakDays + 1, lastPracticeDate = :today WHERE id = 1")
    suspend fun incrementStreak(today: Long)

    @Query("UPDATE user_profile SET streakDays = :newStreak, lastPracticeDate = :completionDate, completedLessonsCount = completedLessonsCount + 1 WHERE id = 1")
    suspend fun updateStreak(newStreak: Int, completionDate: Long)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lesson_progress")
    fun getAllProgress(): Flow<List<LessonProgressEntity>>

    @Query("SELECT completedAt FROM lesson_progress WHERE isCompleted = 1 AND completedAt > 0 ORDER BY completedAt ASC")
    fun getCompletedTimestamps(): Flow<List<Long>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getProgressForLesson(lessonId: String): LessonProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: LessonProgressEntity)
}

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary ORDER BY addedAt DESC")
    fun getAllVocabulary(): Flow<List<VocabularyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<VocabularyEntity>)

    @Query("SELECT * FROM vocabulary WHERE LOWER(word) = LOWER(:word) LIMIT 1")
    suspend fun findWord(word: String): VocabularyEntity?

    @Query("UPDATE vocabulary SET isMastered = :isMastered WHERE id = :id")
    suspend fun setMastered(id: Int, isMastered: Boolean)

    @Query("DELETE FROM vocabulary WHERE id = :id")
    suspend fun deleteWord(id: Int)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE scenarioId = :scenarioId ORDER BY timestamp ASC")
    fun getMessagesForScenario(scenarioId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE scenarioId = :scenarioId")
    suspend fun clearMessages(scenarioId: String)
}
