package com.example.speaklingo.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.speaklingo.data.model.GeneratedFlashcard
import com.example.speaklingo.data.model.DetailedWordPronunciation
import com.example.speaklingo.data.model.PhonemeFeedback
import com.example.speaklingo.data.model.PronunciationAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun chatWithAiTutor(
        scenarioContext: String,
        history: List<Pair<String, Boolean>>, // (text, isUser)
        userMessage: String,
        modelId: String = "gemini-3.5-flash",
        customRoleInstruction: String? = null
    ): AiTutorResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext getFallbackAiResponse(scenarioContext, userMessage)
        }

        try {
            val roleIntro = customRoleInstruction ?: "You are Aria, a friendly, encouraging AI English tutor like on SpeakX, Duolingo, and Supernova who specializes in English grammar rules, vocabulary, idioms, and natural speaking."
            val systemPrompt = """
                $roleIntro
                Scenario / Role Focus: $scenarioContext.
                Your goals:
                1. If the user asks a question about English grammar or vocabulary (e.g. tenses, prepositions, word meanings, synonyms, idioms, phrasal verbs, sentence correction), provide a crystal-clear, structured explanation with rules, bullet points, and real-life examples.
                2. If the user is practicing dialogue, respond naturally (1-3 sentences) to keep the conversation flowing.
                3. Check if the user made any grammar, spelling, or vocabulary mistakes in their message.
                4. If there is a mistake or awkward phrasing, explain gently in 'correction' and show the natural native way in 'betterPhrasing'.
                5. Provide a helpful Hindi translation or explanation in 'hindiTranslation' for learners.
                
                You MUST return ONLY a valid JSON object formatted exactly as:
                {
                   "reply": "Your response or grammar/vocabulary explanation in English",
                   "hasMistake": true/false,
                   "correction": "Grammar rule or explanation if any mistake, otherwise null",
                   "betterPhrasing": "Better natural way to say user's sentence, or null",
                   "hindiTranslation": "Hindi translation or explanation of your reply"
                }
            """.trimIndent()

            val contentsArray = JSONArray()

            // Add previous history
            for ((msg, isUser) in history.takeLast(6)) {
                val role = if (isUser) "user" else "model"
                contentsArray.put(
                    JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().put(JSONObject().put("text", msg)))
                    }
                )
            }

            // Add current user turn
            contentsArray.put(
                JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
                }
            )

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", if (modelId.contains("pro")) 0.3 else 0.7)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("GeminiApiClient", "API call failed code=${response.code}, falling back")
                return@withContext getFallbackAiResponse(scenarioContext, userMessage)
            }

            val bodyString = response.body?.string() ?: return@withContext getFallbackAiResponse(scenarioContext, userMessage)
            val jsonRoot = JSONObject(bodyString)
            val candidate = jsonRoot.getJSONArray("candidates").getJSONObject(0)
            val textContent = candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")

            val parsed = JSONObject(textContent)
            AiTutorResponse(
                replyText = parsed.optString("reply", "That sounds great! Keep practicing."),
                grammarCorrection = if (parsed.optBoolean("hasMistake", false)) {
                    parsed.optString("correction").takeIf { it.isNotBlank() && it != "null" }
                } else null,
                betterPhrasing = parsed.optString("betterPhrasing").takeIf { it.isNotBlank() && it != "null" },
                hindiTranslation = parsed.optString("hindiTranslation").takeIf { it.isNotBlank() && it != "null" },
                confidenceScore = 96
            )
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Error in chatWithAiTutor", e)
            getFallbackAiResponse(scenarioContext, userMessage)
        }
    }

    suspend fun evaluateSpeech(
        targetSentence: String,
        spokenSentence: String
    ): SpeechEvaluationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext getFallbackSpeechEvaluation(targetSentence, spokenSentence)
        }

        try {
            val prompt = """
                The English learner was practicing pronunciation for the target sentence:
                Target: "$targetSentence"
                Actual Spoken: "$spokenSentence"

                Analyze their spoken pronunciation, accuracy, rhythm, and clarity.
                Return ONLY a valid JSON object formatted as:
                {
                  "score": (integer 0 to 100 overall score),
                  "accuracyScore": (integer 0 to 100 word accuracy),
                  "fluencyScore": (integer 0 to 100 flow and cadence),
                  "feedback": "Encouraging, direct feedback in 1-2 sentences",
                  "pronunciationTip": "Specific phonetic tip (e.g. tongue position, vowel elongation, consonant clarity)",
                  "grammarRemark": "Remarks on spoken syntax or missing words",
                  "hindiCoaching": "Hindi explanation/coaching for the learner",
                  "nativeBetterPhrasing": "The most natural native way to speak it",
                  "isExcellent": (boolean, true if score >= 85)
                }
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.3)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext getFallbackSpeechEvaluation(targetSentence, spokenSentence)
            }

            val bodyString = response.body?.string() ?: return@withContext getFallbackSpeechEvaluation(targetSentence, spokenSentence)
            val jsonRoot = JSONObject(bodyString)
            val candidate = jsonRoot.getJSONArray("candidates").getJSONObject(0)
            val textContent = candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            val parsed = JSONObject(textContent)

            val targetWords = targetSentence.split(" ")
            val spokenTokens = spokenSentence.lowercase().replace(Regex("[^a-zA-Z0-9 ]"), "").split(" ").toSet()
            val wordScores = targetWords.map { originalWord ->
                val clean = originalWord.lowercase().replace(Regex("[^a-zA-Z0-9]"), "")
                WordPronunciationScore(
                    word = originalWord,
                    isCorrect = spokenTokens.contains(clean),
                    feedback = if (spokenTokens.contains(clean)) "Clear" else "Check sound"
                )
            }

            val overallScore = parsed.optInt("score", 85)
            SpeechEvaluationResult(
                score = overallScore,
                feedback = parsed.optString("feedback", "Great job! Keep your vocal cadence steady."),
                pronunciationTip = parsed.optString("pronunciationTip", "Focus on clear vowel sounds and gentle consonants."),
                grammarRemark = parsed.optString("grammarRemark", "Matches closely with the prompt."),
                isExcellent = parsed.optBoolean("isExcellent", overallScore >= 80),
                accuracyScore = parsed.optInt("accuracyScore", overallScore),
                fluencyScore = parsed.optInt("fluencyScore", (overallScore + 5).coerceAtMost(100)),
                wordScores = wordScores,
                hindiCoaching = parsed.optString("hindiCoaching").takeIf { it.isNotBlank() && it != "null" },
                nativeBetterPhrasing = parsed.optString("nativeBetterPhrasing").takeIf { it.isNotBlank() && it != "null" }
            )
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Error in evaluateSpeech", e)
            getFallbackSpeechEvaluation(targetSentence, spokenSentence)
        }
    }

    suspend fun transcribeAndCoachSpeech(
        spokenText: String
    ): SpokenTranscriptionCoaching = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured() || spokenText.isBlank()) {
            return@withContext getFallbackTranscriptionCoaching(spokenText)
        }

        try {
            val prompt = """
                The user spoke the following English sentence captured by real-time speech-to-text:
                "$spokenText"

                Act as Aria AI, their expert English vocal and fluency coach.
                Transcribe, analyze, correct, and provide native English coaching.
                Return ONLY a valid JSON object:
                {
                   "transcribedText": "$spokenText",
                   "correctedText": "Grammatically clean version of what they said",
                   "naturalNativeAlternative": "How a fluent native speaker would naturally say this idea",
                   "grammarExplanation": "Short explanation of any corrections or idioms",
                   "hindiTranslation": "Hindi translation of the natural native sentence",
                   "pronunciationAdvice": "Key pronunciation, intonation or word stress tips",
                   "fluencyRating": (integer 0 to 100)
                }
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.3)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext getFallbackTranscriptionCoaching(spokenText)
            }

            val bodyString = response.body?.string() ?: return@withContext getFallbackTranscriptionCoaching(spokenText)
            val jsonRoot = JSONObject(bodyString)
            val candidate = jsonRoot.getJSONArray("candidates").getJSONObject(0)
            val textContent = candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            val parsed = JSONObject(textContent)

            SpokenTranscriptionCoaching(
                transcribedText = spokenText,
                correctedText = parsed.optString("correctedText", spokenText),
                naturalNativeAlternative = parsed.optString("naturalNativeAlternative", spokenText),
                grammarExplanation = parsed.optString("grammarExplanation", "Natural conversational phrasing."),
                hindiTranslation = parsed.optString("hindiTranslation", "अच्छा प्रयास! ऐसे ही अभ्यास करते रहें।"),
                pronunciationAdvice = parsed.optString("pronunciationAdvice", "Maintain a steady breathing pace and stress the key content words."),
                fluencyRating = parsed.optInt("fluencyRating", 88)
            )
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Error in transcribeAndCoachSpeech", e)
            getFallbackTranscriptionCoaching(spokenText)
        }
    }

    /**
     * Multimodal & Voice-to-Text Pronunciation Analyzer using Gemini (gemini-3.5-flash).
     * Analyzes learner speech or audio input against a reference sentence,
     * diagnosing phoneme placement, word stress, vowel lengths, cadence,
     * and actionable mouth/tongue articulation advice.
     */
    suspend fun analyzeAudioPronunciation(
        referenceSentence: String,
        audioBase64: String? = null,
        audioMimeType: String = "audio/wav",
        spokenText: String? = null
    ): PronunciationAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext getFallbackPronunciationAnalysis(referenceSentence, spokenText)
        }

        try {
            val prompt = """
                You are an expert English Phonetician and Pronunciation Coach.
                Analyze the user's spoken audio/speech against the following reference sentence:
                TARGET REFERENCE: "$referenceSentence"
                ${if (!spokenText.isNullOrBlank()) "SPEECH TRANSCRIPTION: \"$spokenText\"" else ""}

                Listen carefully to the audio and evaluate pronunciation, phoneme accuracy, vowel length, consonant articulation, word stress, and rhythm.
                Identify specific phoneme errors (e.g. /θ/ vs /t/, /ð/ vs /d/, /v/ vs /w/, /r/ vs /l/, short /ɪ/ vs long /iː/, schwa /ə/ reduction).
                Provide clear, actionable articulation advice (mouth, tongue, lips, breath) and minimal pair drills.

                Return ONLY a valid JSON object formatted strictly as:
                {
                  "referenceSentence": "$referenceSentence",
                  "transcribedSentence": "Exact transcription of what was spoken in the audio",
                  "overallScore": (integer 0 to 100),
                  "pronunciationScore": (integer 0 to 100),
                  "fluencyScore": (integer 0 to 100),
                  "rhythmIntonationScore": (integer 0 to 100),
                  "expectedIpa": "Standard IPA transcription of the reference sentence",
                  "spokenIpa": "IPA representation of what the learner actually produced",
                  "isNearPerfect": (boolean, true if overallScore >= 90),
                  "actionableAdvice": [
                    "Clear actionable tip 1 (e.g. tongue placement or breathing)",
                    "Clear actionable tip 2",
                    "Clear actionable tip 3"
                  ],
                  "wordBreakdown": [
                    {
                      "word": "word",
                      "expectedIpa": "/.../",
                      "spokenIpa": "/.../",
                      "score": (integer 0 to 100),
                      "status": "EXCELLENT" | "GOOD" | "NEEDS_WORK" | "MISSED",
                      "actionableTip": "Actionable mouth or tongue placement tip for this word"
                    }
                  ],
                  "phonemeFeedbacks": [
                    {
                      "sound": "/θ/",
                      "targetWord": "think",
                      "issueDescription": "Substituted dental stop /t/ for voiceless dental fricative /θ/",
                      "actionableMouthPositionTip": "Place the tip of your tongue lightly between your upper and lower teeth. Exhale a steady stream of air without pressing your tongue too hard.",
                      "hindiContrastTip": "Hindi speakers often use 'थ' with a stop. In English /θ/ is friction with continuous breath."
                    }
                  ],
                  "intonationCommentary": "Commentary on pitch pattern, sentence stress, and natural rhythm",
                  "suggestedPracticeDrill": "Minimal pair drill or rhythm tongue-twister",
                  "hindiSummary": "Bilingual coaching summary in Hindi explaining the core fix"
                }
            """.trimIndent()

            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))

            if (!audioBase64.isNullOrBlank()) {
                partsArray.put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", audioMimeType)
                        put("data", audioBase64)
                    })
                })
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", partsArray)
                }))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext getFallbackPronunciationAnalysis(referenceSentence, spokenText)
            }

            val bodyString = response.body?.string() ?: return@withContext getFallbackPronunciationAnalysis(referenceSentence, spokenText)
            val jsonRoot = JSONObject(bodyString)
            val candidate = jsonRoot.getJSONArray("candidates").getJSONObject(0)
            val textContent = candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            val parsed = JSONObject(textContent)

            val actionableAdviceList = mutableListOf<String>()
            parsed.optJSONArray("actionableAdvice")?.let { arr ->
                for (i in 0 until arr.length()) {
                    actionableAdviceList.add(arr.getString(i))
                }
            }
            if (actionableAdviceList.isEmpty()) {
                actionableAdviceList.add("Keep your vocal rhythm steady and stress the content words.")
            }

            val wordBreakdownList = mutableListOf<DetailedWordPronunciation>()
            parsed.optJSONArray("wordBreakdown")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    wordBreakdownList.add(
                        DetailedWordPronunciation(
                            word = obj.optString("word", ""),
                            expectedIpa = obj.optString("expectedIpa", ""),
                            spokenIpa = obj.optString("spokenIpa", ""),
                            score = obj.optInt("score", 85),
                            status = obj.optString("status", "EXCELLENT"),
                            actionableTip = obj.optString("actionableTip").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }

            val phonemeFeedbackList = mutableListOf<PhonemeFeedback>()
            parsed.optJSONArray("phonemeFeedbacks")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    phonemeFeedbackList.add(
                        PhonemeFeedback(
                            sound = obj.optString("sound", ""),
                            targetWord = obj.optString("targetWord", ""),
                            issueDescription = obj.optString("issueDescription", ""),
                            actionableMouthPositionTip = obj.optString("actionableMouthPositionTip", ""),
                            hindiContrastTip = obj.optString("hindiContrastTip", "")
                        )
                    )
                }
            }

            val overall = parsed.optInt("overallScore", 85)
            PronunciationAnalysisResult(
                referenceSentence = referenceSentence,
                transcribedSentence = parsed.optString("transcribedSentence", spokenText ?: referenceSentence),
                overallScore = overall,
                pronunciationScore = parsed.optInt("pronunciationScore", overall),
                fluencyScore = parsed.optInt("fluencyScore", overall),
                rhythmIntonationScore = parsed.optInt("rhythmIntonationScore", overall),
                expectedIpa = parsed.optString("expectedIpa", ""),
                spokenIpa = parsed.optString("spokenIpa", ""),
                isNearPerfect = parsed.optBoolean("isNearPerfect", overall >= 90),
                actionableAdvice = actionableAdviceList,
                wordBreakdown = wordBreakdownList.ifEmpty {
                    referenceSentence.split(" ").map {
                        DetailedWordPronunciation(word = it, expectedIpa = "/$it/", score = 90)
                    }
                },
                phonemeFeedbacks = phonemeFeedbackList,
                intonationCommentary = parsed.optString("intonationCommentary", "Natural intonation pattern."),
                suggestedPracticeDrill = parsed.optString("suggestedPracticeDrill", "Repeat with slower cadence focusing on clear vowels."),
                hindiSummary = parsed.optString("hindiSummary", "बहुत बढ़िया उच्चारण! ताल और स्पष्टता पर ध्यान दें।")
            )
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Error in analyzeAudioPronunciation", e)
            getFallbackPronunciationAnalysis(referenceSentence, spokenText)
        }
    }

    suspend fun lookupWord(word: String): WordLookupResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext getFallbackWordLookup(word)
        }

        try {
            val prompt = """
                Provide English vocabulary details for the word: "$word".
                Return ONLY a JSON object:
                {
                   "word": "$word",
                   "phonetic": "phonetic transcription e.g. /ˈkɒnfɪdənt/",
                   "partOfSpeech": "noun/verb/adjective/adverb",
                   "englishMeaning": "Clear concise English definition",
                   "hindiMeaning": "Accurate Hindi meaning with translation",
                   "exampleSentence": "A modern, natural English example sentence."
                }
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext getFallbackWordLookup(word)
            }

            val bodyString = response.body?.string() ?: return@withContext getFallbackWordLookup(word)
            val jsonRoot = JSONObject(bodyString)
            val candidate = jsonRoot.getJSONArray("candidates").getJSONObject(0)
            val textContent = candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            val parsed = JSONObject(textContent)

            WordLookupResult(
                word = parsed.optString("word", word.replaceFirstChar { it.uppercase() }),
                phonetic = parsed.optString("phonetic", "/.../"),
                partOfSpeech = parsed.optString("partOfSpeech", "vocabulary"),
                englishMeaning = parsed.optString("englishMeaning", "A useful English word."),
                hindiMeaning = parsed.optString("hindiMeaning", "एक महत्वपूर्ण अंग्रेज़ी शब्द"),
                exampleSentence = parsed.optString("exampleSentence", "He used '$word' in conversation.")
            )
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Error in lookupWord", e)
            getFallbackWordLookup(word)
        }
    }

    suspend fun generateFlashcards(
        topic: String,
        level: String,
        count: Int = 5
    ): List<GeneratedFlashcard> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext getFallbackFlashcards(topic, level)
        }

        try {
            val prompt = """
                You are an expert English language tutor and vocabulary specialist.
                Generate exactly $count high-utility English vocabulary flashcards for an ESL learner.
                Topic/Theme: "$topic"
                Proficiency Level: "$level"
                
                For each flashcard provide:
                - word: The English term or expression
                - phonetic: Standard IPA pronunciation (e.g. /ˈkɒnfɪdənt/)
                - partOfSpeech: noun, adjective, verb, adverb, idiom, or phrasal verb
                - englishMeaning: Clear, accurate, concise English definition
                - hindiMeaning: Natural Hindi translation/meaning (हिन्दी अर्थ)
                - exampleSentence: Realistic, practical example sentence
                - mnemonicOrTip: A memorable memory hook, usage note, or mnemonic
                - synonyms: Array of 2-3 synonyms
                
                Return ONLY a JSON array with this schema:
                [
                  {
                    "word": "string",
                    "phonetic": "string",
                    "partOfSpeech": "string",
                    "englishMeaning": "string",
                    "hindiMeaning": "string",
                    "exampleSentence": "string",
                    "mnemonicOrTip": "string",
                    "synonyms": ["string", "string"]
                  }
                ]
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.4)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("GeminiApiClient", "Failed flashcards generation HTTP ${response.code}")
                return@withContext getFallbackFlashcards(topic, level)
            }

            val bodyString = response.body?.string() ?: return@withContext getFallbackFlashcards(topic, level)
            val jsonRoot = JSONObject(bodyString)
            val candidate = jsonRoot.getJSONArray("candidates").getJSONObject(0)
            val textContent = candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")

            val jsonArray = JSONArray(textContent)
            val cards = mutableListOf<GeneratedFlashcard>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val synArray = obj.optJSONArray("synonyms")
                val synonymsList = mutableListOf<String>()
                if (synArray != null) {
                    for (j in 0 until synArray.length()) {
                        synonymsList.add(synArray.getString(j))
                    }
                }

                cards.add(
                    GeneratedFlashcard(
                        word = obj.optString("word", "Vocabulary").trim(),
                        phonetic = obj.optString("phonetic", "/.../"),
                        partOfSpeech = obj.optString("partOfSpeech", "word"),
                        englishMeaning = obj.optString("englishMeaning", "Important vocabulary term."),
                        hindiMeaning = obj.optString("hindiMeaning", "महत्वपूर्ण शब्द"),
                        exampleSentence = obj.optString("exampleSentence", "Practice using this word daily."),
                        mnemonicOrTip = obj.optString("mnemonicOrTip").takeIf { it.isNotBlank() && it != "null" },
                        synonyms = synonymsList,
                        isSavedToRoom = false
                    )
                )
            }

            if (cards.isEmpty()) getFallbackFlashcards(topic, level) else cards
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Error in generateFlashcards", e)
            getFallbackFlashcards(topic, level)
        }
    }

    private fun getFallbackFlashcards(topic: String, level: String): List<GeneratedFlashcard> {
        val lower = topic.lowercase()
        return when {
            lower.contains("interview") || lower.contains("corporate") || lower.contains("work") -> listOf(
                GeneratedFlashcard(
                    word = "Spearhead",
                    phonetic = "/ˈspɪə.hɛd/",
                    partOfSpeech = "verb",
                    englishMeaning = "To lead or take the initiative in an attack, movement, or project.",
                    hindiMeaning = "नेतृत्व करना, अगुवाई करना",
                    exampleSentence = "She spearheaded the company's digital transformation initiative.",
                    mnemonicOrTip = "Think of the sharp head of a spear that goes first into action.",
                    synonyms = listOf("Lead", "Pioneer", "Champion")
                ),
                GeneratedFlashcard(
                    word = "Leverage",
                    phonetic = "/ˈliː.vər.ɪdʒ/",
                    partOfSpeech = "verb",
                    englishMeaning = "To use something to maximum advantage to achieve a result.",
                    hindiMeaning = "लाभ उठाना, प्रभावी उपयोग करना",
                    exampleSentence = "We can leverage our team's existing technical skills for the new client project.",
                    mnemonicOrTip = "Like using a lever to lift a heavy rock with minimal effort.",
                    synonyms = listOf("Utilize", "Exploit", "Capitalize on")
                ),
                GeneratedFlashcard(
                    word = "Conscientious",
                    phonetic = "/ˌkɒn.ʃiˈɛn.ʃəs/",
                    partOfSpeech = "adjective",
                    englishMeaning = "Wishing to do what is right, especially to do one's work thoroughly.",
                    hindiMeaning = "कर्तव्यनिष्ठ, ईमानदार और मेहनती",
                    exampleSentence = "A conscientious employee always checks reports thoroughly before submission.",
                    mnemonicOrTip = "Related to 'conscience' - someone who listens to their inner voice to do good work.",
                    synonyms = listOf("Meticulous", "Diligent", "Thorough")
                ),
                GeneratedFlashcard(
                    word = "Seamless",
                    phonetic = "/ˈsiːm.ləs/",
                    partOfSpeech = "adjective",
                    englishMeaning = "Smooth and without interruptions or apparent gaps.",
                    hindiMeaning = "निर्बाध, बिना किसी रुकावट के",
                    exampleSentence = "The handover between shifts was completely seamless.",
                    mnemonicOrTip = "No seams, like a smooth piece of woven fabric.",
                    synonyms = listOf("Flawless", "Smooth", "Effortless")
                ),
                GeneratedFlashcard(
                    word = "Benchmark",
                    phonetic = "/ˈbɛntʃ.mɑːk/",
                    partOfSpeech = "noun / verb",
                    englishMeaning = "A standard or point of reference against which things may be compared.",
                    hindiMeaning = "मानक, तुलना का पैमाना",
                    exampleSentence = "Our customer satisfaction score is a benchmark for the whole industry.",
                    mnemonicOrTip = "A mark made on a carpenter's workbench as a measurement guide.",
                    synonyms = listOf("Standard", "Criterion", "Yardstick")
                )
            )
            lower.contains("travel") || lower.contains("airport") || lower.contains("flight") -> listOf(
                GeneratedFlashcard(
                    word = "Itinerary",
                    phonetic = "/aɪˈtɪn.ər.ər.i/",
                    partOfSpeech = "noun",
                    englishMeaning = "A planned route or journey schedule.",
                    hindiMeaning = "यात्रा कार्यक्रम, भ्रमण योजना",
                    exampleSentence = "Our vacation itinerary includes visits to three historical cities.",
                    mnemonicOrTip = "Sounds like 'journey' - your journey route planner.",
                    synonyms = listOf("Schedule", "Route", "Program")
                ),
                GeneratedFlashcard(
                    word = "Layover",
                    phonetic = "/ˈleɪˌoʊ.vər/",
                    partOfSpeech = "noun",
                    englishMeaning = "A period of resting or waiting between stages of a long journey.",
                    hindiMeaning = "यात्रा के बीच का ठहराव",
                    exampleSentence = "We had a four-hour layover in Dubai before catching our connecting flight.",
                    mnemonicOrTip = "You 'lay over' and rest between flights.",
                    synonyms = listOf("Stopover", "Transit wait", "Break")
                ),
                GeneratedFlashcard(
                    word = "Baggage Claim",
                    phonetic = "/ˈbæɡ.ɪdʒ kleɪm/",
                    partOfSpeech = "noun",
                    englishMeaning = "The airport area where arriving passengers collect checked-in luggage.",
                    hindiMeaning = "सामान प्राप्ति क्षेत्र",
                    exampleSentence = "Proceed directly to baggage claim carousel number 4.",
                    mnemonicOrTip = "Where you 'claim' back your bags.",
                    synonyms = listOf("Luggage carousel", "Arrival hall")
                ),
                GeneratedFlashcard(
                    word = "Scenic",
                    phonetic = "/ˈsiː.nɪk/",
                    partOfSpeech = "adjective",
                    englishMeaning = "Providing or relating to beautiful views of natural scenery.",
                    hindiMeaning = "मनोरम, दर्शनीय",
                    exampleSentence = "We chose the scenic mountain route instead of the highway.",
                    mnemonicOrTip = "Full of beautiful 'scenes'.",
                    synonyms = listOf("Picturesque", "Breathtaking", "Panoramic")
                ),
                GeneratedFlashcard(
                    word = "Customs",
                    phonetic = "/ˈkʌs.təmz/",
                    partOfSpeech = "noun",
                    englishMeaning = "Official checkpoint where imported goods and baggage are inspected.",
                    hindiMeaning = "सीमा शुल्क कार्यालय / कस्टम जांच",
                    exampleSentence = "We cleared customs quickly after showing our declarations form.",
                    mnemonicOrTip = "Where authorities check that your items conform to country customs and laws.",
                    synonyms = listOf("Border control", "Inspection")
                )
            )
            else -> listOf(
                GeneratedFlashcard(
                    word = "Resilient",
                    phonetic = "/rɪˈzɪl.jənt/",
                    partOfSpeech = "adjective",
                    englishMeaning = "Able to withstand or recover quickly from difficult conditions.",
                    hindiMeaning = "मुसीबतों से जल्द उबरने वाला, लचीला",
                    exampleSentence = "Children are remarkably resilient and adapt to changes quickly.",
                    mnemonicOrTip = "Think of a spring that bounces back whenever pressed down.",
                    synonyms = listOf("Tough", "Tenacious", "Adaptable")
                ),
                GeneratedFlashcard(
                    word = "Articulate",
                    phonetic = "/ɑːˈtɪk.jə.lət/",
                    partOfSpeech = "adjective",
                    englishMeaning = "Having or showing the ability to speak fluently and coherently.",
                    hindiMeaning = "साफ़ और स्पष्ट बोलने वाला",
                    exampleSentence = "She gave a very articulate presentation that convinced the board.",
                    mnemonicOrTip = "Each word is articulated clearly with no mumbling.",
                    synonyms = listOf("Eloquent", "Coherent", "Expressive")
                ),
                GeneratedFlashcard(
                    word = "Serendipity",
                    phonetic = "/ˌsɛr.ənˈdɪp.ɪ.ti/",
                    partOfSpeech = "noun",
                    englishMeaning = "The occurrence of events by chance in a happy or beneficial way.",
                    hindiMeaning = "सुखद संयोग, अचानक मिला शुभ अवसर",
                    exampleSentence = "Finding my best friend in the same airport was pure serendipity.",
                    mnemonicOrTip = "Sounds pleasant and sunny - stumbling upon joy unexpectedly.",
                    synonyms = listOf("Fortuity", "Happy accident", "Stroke of luck")
                ),
                GeneratedFlashcard(
                    word = "Pragmatic",
                    phonetic = "/præɡˈmæt.ɪk/",
                    partOfSpeech = "adjective",
                    englishMeaning = "Dealing with things sensibly and realistically based on practical considerations.",
                    hindiMeaning = "व्यावहारिक, यथार्थवादी",
                    exampleSentence = "We need a pragmatic solution rather than theoretical debates.",
                    mnemonicOrTip = "Practical = Pragmatic (both start with Pr-).",
                    synonyms = listOf("Practical", "Sensible", "Realistic")
                ),
                GeneratedFlashcard(
                    word = "Meticulous",
                    phonetic = "/məˈtɪk.jə.ləs/",
                    partOfSpeech = "adjective",
                    englishMeaning = "Showing great attention to detail; very careful and precise.",
                    hindiMeaning = "बारीक, सूक्ष्म और अत्यधिक सावधान",
                    exampleSentence = "The architect was meticulous in his blueprint designs.",
                    mnemonicOrTip = "Remember: 'Me-Tick-U-Lous' -> ticks all the boxes carefully.",
                    synonyms = listOf("Scrupulous", "Painstaking", "Accurate")
                )
            )
        }
    }

    private fun getFallbackAiResponse(scenarioContext: String, userMessage: String): AiTutorResponse {
        val lower = userMessage.lowercase().trim()
        val hasCommonMistake = lower.contains("i am agree") || lower.contains("he go ") || lower.contains("i am have") || lower.contains("i want to drink the water")
        
        var correction: String? = null
        var better: String? = null

        if (lower.contains("i am agree")) {
            correction = "In English, 'agree' is a verb. Say 'I agree', not 'I am agree'."
            better = "I agree with you completely."
        } else if (lower.contains("he go ")) {
            correction = "Use third person singular 'goes' with he/she/it."
            better = userMessage.replace("he go", "he goes", ignoreCase = true)
        } else if (lower.contains("i am have")) {
            correction = "Say 'I have' for possession, not 'I am have'."
            better = "I have a question."
        }

        val (reply, hindi) = when {
            lower.contains("since") && lower.contains("for") -> {
                "Rule for 'Since' vs 'For':\n• Use 'Since' for a specific point in time (since 2010, since 9 AM, since Monday).\n• Use 'For' for a duration/length of time (for 3 hours, for 5 days, for 10 years).\n\nExample:\n'I have been learning English since January.'\n'I have studied English for 6 months.'" to "'Since' का प्रयोग निश्चित समय बिंदु (जैसे 2010 से, सुबह से) के लिए और 'For' का प्रयोग समय की अवधि (जैसे 2 घंटे से, 5 साल से) के लिए होता है।"
            }
            lower.contains("present perfect") || lower.contains("past simple") -> {
                "Present Perfect vs Past Simple:\n• Past Simple: Completed actions at a stated past time.\n  Example: 'I visited London in 2021.'\n• Present Perfect: Life experiences or actions connected to now without a specific time.\n  Example: 'I have visited London three times.'" to "Past Simple बीते हुए निश्चित समय के लिए प्रयोग होता है, जबकि Present Perfect जीवन के अनुभवों या वर्तमान से जुड़े कार्यों के लिए प्रयोग होता है।"
            }
            lower.contains("happy") || lower.contains("words for happy") || lower.contains("synonym") && lower.contains("happy") -> {
                "Top 5 Advanced Synonyms for 'Happy':\n1. Elated: Extremely happy and proud.\n2. Ecstatic: Overwhelmed with joyful excitement.\n3. Content: Peacefully satisfied with life.\n4. Delighted: Feeling great pleasure or joy.\n5. Euphoric: In an intensely heightened state of happiness." to "'Happy' के 5 बेहतरीन पर्यायवाची: 1. Elated (अत्यधिक खुश), 2. Ecstatic (रोमांचित), 3. Content (संतुष्ट), 4. Delighted (प्रसन्न), 5. Euphoric (आनंदित)।"
            }
            lower.contains("bite the bullet") -> {
                "Idiom Meaning: 'Bite the bullet'\n• Meaning: To force yourself to perform a painful, difficult, or unpleasant task with courage.\n• Origin: Historical military practice before anesthetics.\n• Example: 'I dislike going to the dentist, but I just need to bite the bullet and go.'" to "मुहावरा 'Bite the bullet' का अर्थ है किसी अप्रिय या कठिन परिस्थिति का साहसपूर्वक सामना करना।"
            }
            lower.contains("phrasal verb") || lower.contains("break") -> {
                "Essential Phrasal Verbs with 'Break':\n• Break down: Stop functioning (e.g., 'My laptop broke down').\n• Break out: Escape or start suddenly (e.g., 'A fire broke out').\n• Break up: End a relationship (e.g., 'They decided to break up amicably').\n• Break in: Enter illegally or soften new shoes/equipment." to "'Break' के मुख्य Phrasal Verbs: Break down (खराब होना), Break out (अचानक फैलना/भागना), Break up (रिश्ता समाप्त होना)।"
            }
            lower.contains("conditional") || lower.contains("if i was") || lower.contains("if i were") -> {
                "Second Conditional Rule:\nIn hypothetical, imaginary, or unreal scenarios, standard English prefers 'If I were' for all subjects (I, he, she, it):\n• Formal / Standard: 'If I were you, I would take that offer.'\n• Example: 'If she were here, she would explain everything.'" to "काल्पनिक या अवास्तविक परिस्थितियों में 'If I was' की जगह 'If I were' का प्रयोग मानक अंग्रेज़ी में बेहतर माना जाता है।"
            }
            lower.contains("active") && lower.contains("passive") -> {
                "Active vs Passive Voice:\n• Active Voice: Focus on the DOER.\n  Example: 'The chef cooked a delicious pasta.'\n• Passive Voice: Focus on the RECEIVER/ACTION.\n  Example: 'A delicious pasta was cooked by the chef.'\nUse passive when the action matters more than who did it." to "Active Voice में कर्ता (Doer) मुख्य होता है, जबकि Passive Voice में कर्म (Object/Action) पर ज़ोर दिया जाता है।"
            }
            lower.contains("hello") || lower.contains("hi") -> {
                "Hello! I am Aria, your AI English Tutor. You can ask me any question about grammar rules, tenses, vocabulary, or idioms, or we can have a natural conversation!" to "नमस्ते! मैं आरिया हूँ, आपकी AI इंग्लिश ट्यूटर। आप मुझसे व्याकरण, शब्दार्थ या मुहावरों से जुड़ा कोई भी सवाल पूछ सकते हैं!"
            }
            scenarioContext.contains("Coffee", ignoreCase = true) -> {
                "Welcome to the coffee shop! Would you like a hot latte, cappuccino, or an iced brew today?" to "कॉफ़ी शॉप में आपका स्वागत है! क्या आप आज गर्म लट्टे, कैपुचीनो या आइस्ड कॉफ़ी लेना पसंद करेंगे?"
            }
            scenarioContext.contains("Interview", ignoreCase = true) -> {
                "Thank you for sharing that. Could you tell me about a project where you solved an interesting challenge?" to "यह साझा करने के लिए धन्यवाद। क्या आप किसी ऐसे प्रोजेक्ट के बारे में बता सकते हैं जहाँ आपने किसी रोचक चुनौती को हल किया हो?"
            }
            scenarioContext.contains("Airport", ignoreCase = true) -> {
                "May I please see your passport and boarding pass? Are you checking in any luggage today?" to "क्या मैं कृपया आपका पासपोर्ट और बोर्डिंग पास देख सकता हूँ? क्या आप आज कोई सामान चेक-इन कर रहे हैं?"
            }
            lower.contains("weather") -> {
                "The weather seems lovely today! Do you prefer sunny or rainy afternoons?" to "आज मौसम बहुत सुहावना लग रहा है! क्या आपको धूप वाले या बारिश वाले दिन पसंद हैं?"
            }
            else -> {
                "That's very interesting! Could you tell me a little more about that?" to "यह बहुत दिलचस्प है! क्या आप इसके बारे में थोड़ा और बता सकते हैं?"
            }
        }

        return AiTutorResponse(
            replyText = reply,
            grammarCorrection = correction,
            betterPhrasing = better,
            hindiTranslation = hindi,
            confidenceScore = 90
        )
    }

    private fun getFallbackSpeechEvaluation(targetSentence: String, spokenSentence: String): SpeechEvaluationResult {
        val targetTokens = targetSentence.lowercase().replace(Regex("[^a-zA-Z0-9 ]"), "").split(" ")
        val spokenTokens = spokenSentence.lowercase().replace(Regex("[^a-zA-Z0-9 ]"), "").split(" ").toSet()

        val matchedWords = targetTokens.count { it in spokenTokens }
        val ratio = if (targetTokens.isNotEmpty()) matchedWords.toFloat() / targetTokens.size else 0.8f
        val calculatedScore = (ratio * 100).toInt().coerceIn(40, 100)

        val targetWords = targetSentence.split(" ")
        val wordScores = targetWords.map { word ->
            val clean = word.lowercase().replace(Regex("[^a-zA-Z0-9]"), "")
            WordPronunciationScore(
                word = word,
                isCorrect = spokenTokens.contains(clean),
                feedback = if (spokenTokens.contains(clean)) "Accurate" else "Needs clarity"
            )
        }

        val isExcellent = calculatedScore >= 80

        return SpeechEvaluationResult(
            score = calculatedScore,
            feedback = if (isExcellent) "Brilliant pronunciation! Your rhythm and pace sounded very natural." 
                       else "Good attempt! Try speaking with slightly more emphasis on key words.",
            pronunciationTip = "Keep your mouth relaxed and elongate vowel sounds naturally.",
            grammarRemark = if (isExcellent) "Sentence structure matched the prompt perfectly." else "Some words were missed, but your intonation was clear.",
            isExcellent = isExcellent,
            accuracyScore = calculatedScore,
            fluencyScore = (calculatedScore + 4).coerceAtMost(100),
            wordScores = wordScores,
            hindiCoaching = "शब्दों के उच्चारण पर ध्यान दें और गति सामान्य रखें।",
            nativeBetterPhrasing = targetSentence
        )
    }

    private fun getFallbackTranscriptionCoaching(spokenText: String): SpokenTranscriptionCoaching {
        val cleaned = spokenText.trim().ifBlank { "I want to improve my English speaking." }
        return SpokenTranscriptionCoaching(
            transcribedText = cleaned,
            correctedText = cleaned.replaceFirstChar { it.uppercase() },
            naturalNativeAlternative = cleaned.replaceFirstChar { it.uppercase() },
            grammarExplanation = "Your spoken structure is understandable and communicative.",
            hindiTranslation = "आपका उच्चारण और वाक्य समझने योग्य है। निरंतर अभ्यास करते रहें।",
            pronunciationAdvice = "Practice speaking full sentences in a single continuous breath.",
            fluencyRating = 85
        )
    }

    private fun getFallbackWordLookup(word: String): WordLookupResult {
        return WordLookupResult(
            word = word.replaceFirstChar { it.uppercase() },
            phonetic = "/ˈ${word.lowercase()}/",
            partOfSpeech = "vocabulary",
            englishMeaning = "A commonly used English word to express ideas clearly.",
            hindiMeaning = "एक उपयोगी अंग्रेज़ी शब्द",
            exampleSentence = "Practicing the word '$word' will boost your conversational confidence."
        )
    }

    internal fun getFallbackPronunciationAnalysis(
        referenceSentence: String,
        spokenText: String?
    ): PronunciationAnalysisResult {
        val actualSpoken = spokenText?.trim().orEmpty().ifBlank { referenceSentence }
        val refTokens = referenceSentence.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val spokenTokens = actualSpoken.lowercase().replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .split(Regex("\\s+")).filter { it.isNotBlank() }.toSet()

        var matchedCount = 0
        val wordBreakdowns = refTokens.map { token ->
            val clean = token.lowercase().replace(Regex("[^a-zA-Z0-9]"), "")
            val isMatched = spokenTokens.contains(clean)
            if (isMatched) matchedCount++

            val score = if (isMatched) (88..98).random() else (50..70).random()
            val status = when {
                score >= 90 -> "EXCELLENT"
                score >= 75 -> "GOOD"
                score >= 50 -> "NEEDS_WORK"
                else -> "MISSED"
            }

            DetailedWordPronunciation(
                word = token,
                expectedIpa = "/ˈ${clean}/",
                spokenIpa = if (isMatched) "/ˈ${clean}/" else "/ˈ${clean}*?/",
                score = score,
                status = status,
                actionableTip = if (isMatched) "Crisp articulation." else "Focus on vowel length and clear ending consonant."
            )
        }

        val accuracyRatio = if (refTokens.isNotEmpty()) matchedCount.toFloat() / refTokens.size.toFloat() else 0.85f
        val overall = (accuracyRatio * 85f + 12f).toInt().coerceIn(40, 98)
        val isNearPerfect = overall >= 88

        val phonemeFeedbacks = mutableListOf<PhonemeFeedback>()
        if (referenceSentence.contains("th", ignoreCase = true)) {
            phonemeFeedbacks.add(
                PhonemeFeedback(
                    sound = "/θ/",
                    targetWord = refTokens.firstOrNull { it.contains("th", ignoreCase = true) } ?: "think",
                    issueDescription = "Tendency to substitute alveolar stop /t/ or /d/ for dental fricative /θ/",
                    actionableMouthPositionTip = "Place the flat tip of your tongue lightly between your upper and lower teeth. Exhale a steady friction of air.",
                    hindiContrastTip = "Hindi 'थ' has an abrupt release; English /θ/ maintains continuous breath."
                )
            )
        }
        if (referenceSentence.contains("v", ignoreCase = true) || referenceSentence.contains("w", ignoreCase = true)) {
            phonemeFeedbacks.add(
                PhonemeFeedback(
                    sound = "/v/ vs /w/",
                    targetWord = refTokens.firstOrNull { it.contains("v", ignoreCase = true) || it.contains("w", ignoreCase = true) } ?: "water",
                    issueDescription = "Distinguish labiodental /v/ (teeth on lip) from bilabial /w/ (rounded lips)",
                    actionableMouthPositionTip = "For /w/, round your lips like a small circle. For /v/, gently touch your top teeth to your lower lip.",
                    hindiContrastTip = "Hindi 'व' is midway between v and w. English makes a sharp acoustic difference."
                )
            )
        }
        if (phonemeFeedbacks.isEmpty()) {
            phonemeFeedbacks.add(
                PhonemeFeedback(
                    sound = "/r/",
                    targetWord = refTokens.firstOrNull { it.contains("r", ignoreCase = true) } ?: "great",
                    issueDescription = "Smooth post-alveolar approximant /r/",
                    actionableMouthPositionTip = "Curl the tip of your tongue back slightly toward the roof of your mouth without touching it.",
                    hindiContrastTip = "Do not roll or tap the tongue like Hindi 'र'."
                )
            )
        }

        val adviceList = if (isNearPerfect) {
            listOf(
                "Excellent native-like rhythm and clear phoneme articulation.",
                "Maintain this smooth breath continuity across clause boundaries.",
                "Natural sentence stress is well placed on the key content words."
            )
        } else {
            listOf(
                "Focus on lingering on vowel sounds rather than clipping words too quickly.",
                "Pay attention to final consonant stops (e.g. -t, -d, -k) without dropping them.",
                "Use the minimal pair drills below to build muscle memory in your tongue and lips."
            )
        }

        return PronunciationAnalysisResult(
            referenceSentence = referenceSentence,
            transcribedSentence = actualSpoken,
            overallScore = overall,
            pronunciationScore = overall,
            fluencyScore = (overall + 2).coerceAtMost(99),
            rhythmIntonationScore = (overall - 3).coerceAtLeast(50),
            expectedIpa = "/${refTokens.joinToString(" ") { it.lowercase().replace(Regex("[^a-zA-Z]"), "") }}/",
            spokenIpa = "/${actualSpoken.lowercase().replace(Regex("[^a-zA-Z\\s]"), "")}/",
            isNearPerfect = isNearPerfect,
            actionableAdvice = adviceList,
            wordBreakdown = wordBreakdowns,
            phonemeFeedbacks = phonemeFeedbacks,
            intonationCommentary = if (isNearPerfect) "Natural melodic sentence contour with correct falling intonation at the end of the phrase." 
                                   else "Try using a slight pitch drop at the end of statements to sound more confident and native.",
            suggestedPracticeDrill = "Minimal pair drill: Practice saying '${refTokens.take(2).joinToString(" ")}' 3 times slowly, then once at natural conversational speed.",
            hindiSummary = "बहुत अच्छा प्रयास! मुख्य शब्दों के उच्चारण और ताल पर थोड़ा और ध्यान देने से आपका प्रवाह और बेहतर होगा।"
        )
    }
}
