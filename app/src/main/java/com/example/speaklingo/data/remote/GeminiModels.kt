package com.example.speaklingo.data.remote

data class AiTutorResponse(
    val replyText: String,
    val grammarCorrection: String? = null,
    val betterPhrasing: String? = null,
    val hindiTranslation: String? = null,
    val confidenceScore: Int = 95
)

data class WordPronunciationScore(
    val word: String,
    val isCorrect: Boolean,
    val feedback: String? = null
)

data class SpeechEvaluationResult(
    val score: Int, // 0 - 100
    val feedback: String,
    val pronunciationTip: String,
    val grammarRemark: String,
    val isExcellent: Boolean,
    val accuracyScore: Int = score,
    val fluencyScore: Int = score,
    val wordScores: List<WordPronunciationScore> = emptyList(),
    val hindiCoaching: String? = null,
    val nativeBetterPhrasing: String? = null
)

data class SpokenTranscriptionCoaching(
    val transcribedText: String,
    val correctedText: String,
    val naturalNativeAlternative: String,
    val grammarExplanation: String,
    val hindiTranslation: String,
    val pronunciationAdvice: String,
    val fluencyRating: Int = 85
)

data class WordLookupResult(
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val englishMeaning: String,
    val hindiMeaning: String,
    val exampleSentence: String
)
