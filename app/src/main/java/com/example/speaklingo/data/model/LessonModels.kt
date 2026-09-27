package com.example.speaklingo.data.model

enum class QuestionType {
    WORD_SCRAMBLE,
    SPEAK_OUT_LOUD,
    FILL_IN_BLANK,
    LISTEN_AND_CHOOSE
}

data class LessonQuestion(
    val id: String,
    val type: QuestionType,
    val promptQuestion: String,
    val promptHindi: String? = null,
    val audioSentence: String? = null, // Text to speak via TTS
    val targetSentence: String, // Correct answer sentence
    val scrambleOptions: List<String> = emptyList(), // For WORD_SCRAMBLE
    val multipleChoiceOptions: List<String> = emptyList(), // For FILL_IN_BLANK or LISTEN
    val explanation: String = ""
)

data class Lesson(
    val id: String,
    val unitId: Int,
    val title: String,
    val subtitle: String,
    val xpReward: Int = 20,
    val gemReward: Int = 10,
    val questions: List<LessonQuestion>
)

data class UnitData(
    val id: Int,
    val title: String,
    val description: String,
    val bannerColorHex: Long = 0xFF58CC02, // Duolingo Green
    val lessons: List<Lesson>
)

object LessonCatalog {
    val units: List<UnitData> = listOf(
        UnitData(
            id = 1,
            title = "Unit 1: Introductions & Daily Basics",
            description = "Master essential greetings, introducing yourself, and everyday present tense.",
            bannerColorHex = 0xFF58CC02, // Duolingo Emerald Green
            lessons = listOf(
                Lesson(
                    id = "u1_l1",
                    unitId = 1,
                    title = "Greetings & Introductions",
                    subtitle = "Hello, nice to meet you, my name is...",
                    xpReward = 20,
                    gemReward = 10,
                    questions = listOf(
                        LessonQuestion(
                            id = "q1_1",
                            type = QuestionType.WORD_SCRAMBLE,
                            promptQuestion = "Arrange the words to say: 'My name is Rahul and I live in Delhi.'",
                            promptHindi = "मेरा नाम राहुल है और मैं दिल्ली में रहता हूँ।",
                            audioSentence = "My name is Rahul and I live in Delhi.",
                            targetSentence = "My name is Rahul and I live in Delhi",
                            scrambleOptions = listOf("My", "live", "is", "Rahul", "name", "and", "I", "in", "Delhi", "you", "are"),
                            explanation = "Use 'My name is [Name]' and 'I live in [City]'."
                        ),
                        LessonQuestion(
                            id = "q1_2",
                            type = QuestionType.SPEAK_OUT_LOUD,
                            promptQuestion = "Speak this greeting clearly into your microphone:",
                            promptHindi = "आपसे मिलकर बहुत खुशी हुई।",
                            audioSentence = "It is very nice to meet you today.",
                            targetSentence = "It is very nice to meet you today",
                            explanation = "Pronounce 'nice to meet you' smoothly as a single rhythmic phrase."
                        ),
                        LessonQuestion(
                            id = "q1_3",
                            type = QuestionType.FILL_IN_BLANK,
                            promptQuestion = "Choose the correct word: 'Where _____ you from?'",
                            promptHindi = "आप कहाँ से हैं?",
                            audioSentence = "Where are you from?",
                            targetSentence = "are",
                            multipleChoiceOptions = listOf("are", "is", "am", "do"),
                            explanation = "Use 'are' with second-person pronoun 'you'."
                        ),
                        LessonQuestion(
                            id = "q1_4",
                            type = QuestionType.LISTEN_AND_CHOOSE,
                            promptQuestion = "Listen to the audio and select what you hear:",
                            promptHindi = "ऑडियो सुनें और सही वाक्य चुनें:",
                            audioSentence = "Good morning! How are you feeling today?",
                            targetSentence = "Good morning! How are you feeling today?",
                            multipleChoiceOptions = listOf(
                                "Good morning! How are you feeling today?",
                                "Good evening! How was your day?",
                                "Good morning! Where are you going today?",
                                "Great morning! Are you feeling tired today?"
                            ),
                            explanation = "The speaker said 'Good morning! How are you feeling today?'"
                        )
                    )
                ),
                Lesson(
                    id = "u1_l2",
                    unitId = 1,
                    title = "Daily Habits & Routines",
                    subtitle = "Wake up, breakfast, going to work...",
                    xpReward = 25,
                    gemReward = 15,
                    questions = listOf(
                        LessonQuestion(
                            id = "q2_1",
                            type = QuestionType.WORD_SCRAMBLE,
                            promptQuestion = "Form the sentence: 'She drinks tea every morning.'",
                            promptHindi = "वह हर सुबह चाय पीती है।",
                            audioSentence = "She drinks tea every morning.",
                            targetSentence = "She drinks tea every morning",
                            scrambleOptions = listOf("She", "drinks", "morning", "tea", "every", "drink", "coffee"),
                            explanation = "Add 's' to the verb: 'drinks' for singular third-person (She/He)."
                        ),
                        LessonQuestion(
                            id = "q2_2",
                            type = QuestionType.SPEAK_OUT_LOUD,
                            promptQuestion = "Speak this sentence aloud:",
                            promptHindi = "मैं सुबह सात बजे उठता हूँ।",
                            audioSentence = "I wake up at seven o'clock in the morning.",
                            targetSentence = "I wake up at seven o'clock in the morning",
                            explanation = "Practice linking 'wake' and 'up' -> 'wake-up'."
                        ),
                        LessonQuestion(
                            id = "q2_3",
                            type = QuestionType.FILL_IN_BLANK,
                            promptQuestion = "He _____ his breakfast before leaving home.",
                            promptHindi = "वह घर से निकलने से पहले नाश्ता करता है।",
                            audioSentence = "He eats his breakfast before leaving home.",
                            targetSentence = "eats",
                            multipleChoiceOptions = listOf("eats", "eat", "eating", "ate"),
                            explanation = "Habitual actions in simple present take 'eats' for 'He'."
                        )
                    )
                ),
                Lesson(
                    id = "u1_l3",
                    unitId = 1,
                    title = "Asking Questions & Clarifications",
                    subtitle = "Could you please explain? What does that mean?",
                    xpReward = 25,
                    gemReward = 15,
                    questions = listOf(
                        LessonQuestion(
                            id = "q3_1",
                            type = QuestionType.WORD_SCRAMBLE,
                            promptQuestion = "Arrange: 'Could you please repeat that slowly?'",
                            promptHindi = "क्या आप कृपया इसे धीरे से दोहरा सकते हैं?",
                            audioSentence = "Could you please repeat that slowly?",
                            targetSentence = "Could you please repeat that slowly",
                            scrambleOptions = listOf("Could", "you", "please", "repeat", "that", "slowly", "can", "fast"),
                            explanation = "'Could you please...' is the most polite way to ask for repetition."
                        ),
                        LessonQuestion(
                            id = "q3_2",
                            type = QuestionType.SPEAK_OUT_LOUD,
                            promptQuestion = "Practice speaking this polite phrase:",
                            promptHindi = "क्या आप कृपया मेरी मदद कर सकते हैं?",
                            audioSentence = "Excuse me, could you help me with this?",
                            targetSentence = "Excuse me could you help me with this",
                            explanation = "Start with a soft, friendly 'Excuse me'."
                        )
                    )
                )
            )
        ),
        UnitData(
            id = 2,
            title = "Unit 2: Social & Cafe Conversations",
            description = "Order delicious food, chat about hobbies, and make plans with friends.",
            bannerColorHex = 0xFF1CB0F6, // Duolingo Sky Blue
            lessons = listOf(
                Lesson(
                    id = "u2_l1",
                    unitId = 2,
                    title = "Ordering at a Cafe",
                    subtitle = "I'd like a cappuccino with oat milk, please.",
                    xpReward = 30,
                    gemReward = 15,
                    questions = listOf(
                        LessonQuestion(
                            id = "q21_1",
                            type = QuestionType.WORD_SCRAMBLE,
                            promptQuestion = "Order politely: 'I would like a large iced coffee please.'",
                            promptHindi = "मुझे कृपया एक बड़ी आइस्ड कॉफ़ी चाहिए।",
                            audioSentence = "I would like a large iced coffee please.",
                            targetSentence = "I would like a large iced coffee please",
                            scrambleOptions = listOf("I", "would", "like", "a", "large", "iced", "coffee", "please", "want", "give"),
                            explanation = "Always prefer 'I would like...' over 'I want...' when ordering."
                        ),
                        LessonQuestion(
                            id = "q21_2",
                            type = QuestionType.SPEAK_OUT_LOUD,
                            promptQuestion = "Say this order with confidence:",
                            promptHindi = "क्या मुझे बिल मिल सकता है, धन्यवाद?",
                            audioSentence = "Can I please have the bill, thank you?",
                            targetSentence = "Can I please have the bill thank you",
                            explanation = "Stress 'bill' and 'thank you'."
                        ),
                        LessonQuestion(
                            id = "q21_3",
                            type = QuestionType.FILL_IN_BLANK,
                            promptQuestion = "How much _____ this slice of chocolate cake cost?",
                            promptHindi = "इस चॉकलेट केक के टुकड़े की क्या कीमत है?",
                            audioSentence = "How much does this slice of chocolate cake cost?",
                            targetSentence = "does",
                            multipleChoiceOptions = listOf("does", "do", "is", "are"),
                            explanation = "'Slice' is singular, so use 'does ... cost'."
                        )
                    )
                ),
                Lesson(
                    id = "u2_l2",
                    unitId = 2,
                    title = "Hobbies & Free Time",
                    subtitle = "I enjoy reading books and playing badminton.",
                    xpReward = 30,
                    gemReward = 15,
                    questions = listOf(
                        LessonQuestion(
                            id = "q22_1",
                            type = QuestionType.SPEAK_OUT_LOUD,
                            promptQuestion = "Speak about your weekend hobby:",
                            promptHindi = "मुझे सप्ताहांत पर संगीत सुनना बहुत पसंद है।",
                            audioSentence = "I really enjoy listening to music on weekends.",
                            targetSentence = "I really enjoy listening to music on weekends",
                            explanation = "Notice: 'listening TO music', always use 'to' after listen."
                        )
                    )
                )
            )
        ),
        UnitData(
            id = 3,
            title = "Unit 3: Professional & Career English",
            description = "Ace your job interviews, workplace emails, and meetings like a pro.",
            bannerColorHex = 0xFF9C27B0, // Duolingo / Royal Purple
            lessons = listOf(
                Lesson(
                    id = "u3_l1",
                    unitId = 3,
                    title = "Job Interview Self-Intro",
                    subtitle = "Tell me about yourself and your background.",
                    xpReward = 35,
                    gemReward = 20,
                    questions = listOf(
                        LessonQuestion(
                            id = "q31_1",
                            type = QuestionType.WORD_SCRAMBLE,
                            promptQuestion = "Introduce yourself: 'I have three years of experience in software development.'",
                            promptHindi = "मेरे पास सॉफ्टवेयर डेवलपमेंट में तीन साल का अनुभव है।",
                            audioSentence = "I have three years of experience in software development.",
                            targetSentence = "I have three years of experience in software development",
                            scrambleOptions = listOf("I", "have", "three", "years", "of", "experience", "in", "software", "development", "am", "with"),
                            explanation = "Say 'I have X years of experience in...'"
                        ),
                        LessonQuestion(
                            id = "q31_2",
                            type = QuestionType.SPEAK_OUT_LOUD,
                            promptQuestion = "Speak this interview closing sentence:",
                            promptHindi = "इस अवसर के लिए धन्यवाद।",
                            audioSentence = "Thank you so much for this wonderful opportunity.",
                            targetSentence = "Thank you so much for this wonderful opportunity",
                            explanation = "Speak clearly with warmth and professional confidence."
                        )
                    )
                )
            )
        )
    )
}
