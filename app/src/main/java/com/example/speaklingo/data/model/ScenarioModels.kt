package com.example.speaklingo.data.model

data class PracticeScenario(
    val id: String,
    val title: String,
    val roleTitle: String,
    val initialAiGreeting: String,
    val initialAiHindi: String,
    val iconEmoji: String,
    val difficulty: String, // Beginner, Intermediate, Advanced
    val suggestedPhrases: List<String>
)

object ScenarioCatalog {
    val scenarios: List<PracticeScenario> = listOf(
        PracticeScenario(
            id = "grammar_tutor",
            title = "Grammar Doctor",
            roleTitle = "Aria (Grammar & Syntax Specialist)",
            initialAiGreeting = "Hello! I am Aria, your AI English Grammar Tutor. Ask me any doubt about English tenses, prepositions, active/passive voice, sentence structures, or paste a sentence for instant correction!",
            initialAiHindi = "नमस्ते! मैं आरिया हूँ, आपकी AI इंग्लिश ग्रामर ट्यूटर। मुझसे टेंस, प्रेपोजिशन, वाक्य सुधार या किसी भी व्याकरण नियम पर सवाल पूछें!",
            iconEmoji = "📝",
            difficulty = "All Levels",
            suggestedPhrases = listOf(
                "Explain 'Since' vs 'For' with rules",
                "Present Perfect vs Past Simple difference",
                "Is 'I am agree with you' grammatically correct?",
                "Rules for 'In', 'On', and 'At' for time and place"
            )
        ),
        PracticeScenario(
            id = "vocab_tutor",
            title = "Vocabulary Lab",
            roleTitle = "Aria (Vocabulary & Idioms Specialist)",
            initialAiGreeting = "Welcome to the Vocabulary Lab! I can help you discover rich synonyms, idioms, phrasal verbs, collocations, and pronunciation. What word or topic would you like to master today?",
            initialAiHindi = "वोकैबुलरी लैब में आपका स्वागत है! मैं आपको नए शब्द, पर्यायवाची, मुहावरे और उनका सही उच्चारण सिखाऊँगी। आज आप क्या सीखना चाहते हैं?",
            iconEmoji = "📚",
            difficulty = "All Levels",
            suggestedPhrases = listOf(
                "Give me 5 advanced synonyms for 'Happy'",
                "Explain the meaning and origin of 'Bite the bullet'",
                "Useful phrasal verbs with 'Break' and examples",
                "Smart words to replace 'Very' in daily speech"
            )
        ),
        PracticeScenario(
            id = "free_tutor",
            title = "Ask Aria Anything",
            roleTitle = "Aria (Friendly English Coach)",
            initialAiGreeting = "Hey friend! I am Aria, your personal English speaking coach. Chat with me about anything, practice daily English, or ask any question!",
            initialAiHindi = "नमस्ते दोस्त! मैं आरिया हूँ, आपकी पर्सनल इंग्लिश कोच। मुझसे किसी भी विषय पर बात करें या कोई भी सवाल पूछें!",
            iconEmoji = "🤖",
            difficulty = "All Levels",
            suggestedPhrases = listOf(
                "How can I improve my English fluency?",
                "Can you check if my grammar is natural?",
                "Let's talk about our favorite hobbies.",
                "Give me 3 useful idioms for conversation."
            )
        ),
        PracticeScenario(
            id = "cafe",
            title = "Ordering at a Cafe",
            roleTitle = "Barista at Green Beans Cafe",
            initialAiGreeting = "Hi there! Welcome to Green Beans Coffee. What can I get started for you today?",
            initialAiHindi = "नमस्ते! ग्रीन बीन्स कॉफ़ी में आपका स्वागत है। आज मैं आपके लिए क्या तैयार करूँ?",
            iconEmoji = "☕",
            difficulty = "Beginner",
            suggestedPhrases = listOf(
                "Can I get a medium cappuccino?",
                "Do you have almond milk?",
                "How much is a croissant?",
                "Could I have the check, please?"
            )
        ),
        PracticeScenario(
            id = "job_interview",
            title = "Job Interview Practice",
            roleTitle = "Hiring Manager at Apex Global",
            initialAiGreeting = "Welcome to your interview! It's a pleasure meeting you. Could you please introduce yourself and tell me about your background?",
            initialAiHindi = "आपके इंटरव्यू में स्वागत है! आपसे मिलकर खुशी हुई। क्या आप कृपया अपना परिचय दे सकते हैं?",
            iconEmoji = "💼",
            difficulty = "Intermediate",
            suggestedPhrases = listOf(
                "I have a background in management and communication.",
                "In my previous job, I solved customer challenges.",
                "I am eager to learn and contribute to this team."
            )
        ),
        PracticeScenario(
            id = "airport",
            title = "Airport Check-in & Security",
            roleTitle = "Airline Check-in Agent",
            initialAiGreeting = "Good afternoon! Where are you flying to today? May I please have your passport?",
            initialAiHindi = "शुभ दोपहर! आप आज कहाँ की यात्रा कर रहे हैं? क्या मैं आपका पासपोर्ट देख सकता हूँ?",
            iconEmoji = "✈️",
            difficulty = "Beginner",
            suggestedPhrases = listOf(
                "I am flying to New York.",
                "Here is my passport and ticket.",
                "Can I get a window seat?",
                "Is my baggage within the weight limit?"
            )
        ),
        PracticeScenario(
            id = "doctor",
            title = "At the Doctor's Clinic",
            roleTitle = "Dr. Sharma (Physician)",
            initialAiGreeting = "Hello! Please have a seat. What symptoms are you experiencing today?",
            initialAiHindi = "नमस्ते! कृपया बैठिए। आज आपको क्या तकलीफ़ या लक्षण महसूस हो रहे हैं?",
            iconEmoji = "🩺",
            difficulty = "Intermediate",
            suggestedPhrases = listOf(
                "I have had a mild headache since yesterday.",
                "I feel a bit feverish and tired.",
                "How often should I take this medicine?"
            )
        )
    )
}
