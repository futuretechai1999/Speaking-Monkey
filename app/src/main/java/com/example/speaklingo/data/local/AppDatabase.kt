package com.example.speaklingo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        LessonProgressEntity::class,
        VocabularyEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun lessonDao(): LessonDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "speaklingo_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            // Initial User Profile
            database.userDao().insertOrUpdate(
                UserProfileEntity(
                    id = 1,
                    streakDays = 3,
                    xp = 180,
                    gems = 320,
                    hearts = 5,
                    maxHearts = 5,
                    league = "Silver League",
                    completedLessonsCount = 2
                )
            )

            // Seed initial completed lesson progress
            database.lessonDao().saveProgress(
                LessonProgressEntity(
                    lessonId = "u1_l1",
                    unitId = 1,
                    isCompleted = true,
                    stars = 3,
                    accuracy = 100,
                    completedAt = System.currentTimeMillis() - 86400000L
                )
            )
            database.lessonDao().saveProgress(
                LessonProgressEntity(
                    lessonId = "u1_l2",
                    unitId = 1,
                    isCompleted = true,
                    stars = 3,
                    accuracy = 95,
                    completedAt = System.currentTimeMillis() - 43200000L
                )
            )

            // Seed initial vocabulary items (English + Hindi meaning + Phonetics + Example)
            val initialWords = listOf(
                VocabularyEntity(
                    word = "Fluent",
                    phonetic = "/ˈfluː.ənt/",
                    partOfSpeech = "adjective",
                    englishMeaning = "Able to express oneself easily and articulately",
                    hindiMeaning = "धाराप्रवाह (बिना रुके बोलने वाला)",
                    exampleSentence = "She practiced daily to become fluent in English.",
                    isMastered = true
                ),
                VocabularyEntity(
                    word = "Confident",
                    phonetic = "/ˈkɒn.fɪ.dənt/",
                    partOfSpeech = "adjective",
                    englishMeaning = "Feeling or showing certainty about something",
                    hindiMeaning = "आत्मविश्वासी (निडर)",
                    exampleSentence = "Speak with a confident voice during the interview.",
                    isMastered = false
                ),
                VocabularyEntity(
                    word = "Pronunciation",
                    phonetic = "/prəˌnʌn.siˈeɪ.ʃən/",
                    partOfSpeech = "noun",
                    englishMeaning = "The way in which a word is pronounced",
                    hindiMeaning = "उच्चारण (शब्द बोलने का ढंग)",
                    exampleSentence = "Listen to native speakers to improve your pronunciation.",
                    isMastered = false
                ),
                VocabularyEntity(
                    word = "Hesitate",
                    phonetic = "/ˈhez.ɪ.teɪt/",
                    partOfSpeech = "verb",
                    englishMeaning = "Pause before saying or doing something that you are unsure about",
                    hindiMeaning = "हिचकिचाना",
                    exampleSentence = "Do not hesitate to ask questions in English class.",
                    isMastered = false
                ),
                VocabularyEntity(
                    word = "Opportunity",
                    phonetic = "/ˌɒp.əˈtʃuː.nə.ti/",
                    partOfSpeech = "noun",
                    englishMeaning = "A set of circumstances that makes it possible to do something",
                    hindiMeaning = "अवसर / मौक़ा",
                    exampleSentence = "Speaking English opens up wonderful job opportunities.",
                    isMastered = true
                )
            )
            initialWords.forEach { database.vocabularyDao().insertWord(it) }
        }
    }
}
