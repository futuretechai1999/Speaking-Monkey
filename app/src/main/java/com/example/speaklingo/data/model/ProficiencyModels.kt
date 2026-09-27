package com.example.speaklingo.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Skill classification for proficiency modules.
 */
enum class SkillFocus(val label: String, val emoji: String) {
    SPEAKING("Speaking & Pronunciation", "🎙️"),
    GRAMMAR("Grammar Rules & Syntax", "📝"),
    VOCABULARY("Vocabulary & Idioms", "📚"),
    LISTENING("Listening & Comprehension", "🎧"),
    CONVERSATION("Real-World Dialogue", "💬"),
    EXAM("Certification Exam", "🏆")
}

/**
 * Level status on the interactive proficiency path.
 */
enum class ProficiencyStatus {
    LOCKED,
    IN_PROGRESS,
    COMPLETED
}

/**
 * A discrete learning module / checkpoint along a proficiency tier.
 */
data class ProficiencyModule(
    val id: String,
    val levelCode: String, // "A1", "A2", "B1", "B2", "C1"
    val title: String,
    val description: String,
    val iconEmoji: String,
    val skillFocus: SkillFocus,
    val xpReward: Int,
    val gemReward: Int,
    val estimatedMinutes: Int,
    val learningOutcomes: List<String>,
    val isCheckpoint: Boolean = false
)

/**
 * Overall CEFR English Proficiency Level definition.
 */
data class ProficiencyLevel(
    val code: String,              // "A1", "A2", "B1", "B2", "C1"
    val title: String,             // "Beginner"
    val subtitle: String,          // "Foundations & Phonics"
    val cefrStandard: String,      // "CEFR A1 Breakthrough"
    val summary: String,
    val iconEmoji: String,
    val iconVector: ImageVector,
    val themeColorHex: Long,
    val darkColorHex: Long,
    val targetXp: Int,
    val modules: List<ProficiencyModule>
)

/**
 * Catalog of standard CEFR English proficiency tiers and curriculum.
 */
object ProficiencyCatalog {
    val levels: List<ProficiencyLevel> = listOf(
        // Level 1: A1 Beginner
        ProficiencyLevel(
            code = "A1",
            title = "Beginner",
            subtitle = "Foundations & Essentials",
            cefrStandard = "CEFR A1 Breakthrough",
            summary = "Understand everyday phrases, introduce yourself, count, and ask simple personal questions.",
            iconEmoji = "🌱",
            iconVector = Icons.Default.Spa,
            themeColorHex = 0xFF58CC02, // Duolingo Green
            darkColorHex = 0xFF46A302,
            targetXp = 250,
            modules = listOf(
                ProficiencyModule(
                    id = "a1_m1",
                    levelCode = "A1",
                    title = "Phonics & Sound Mastery",
                    description = "Master vowels, consonants, and standard English syllable stress.",
                    iconEmoji = "🗣️",
                    skillFocus = SkillFocus.SPEAKING,
                    xpReward = 30,
                    gemReward = 15,
                    estimatedMinutes = 4,
                    learningOutcomes = listOf(
                        "Differentiate long and short vowel sounds",
                        "Clear pronunciation of 'th' and 'r' sounds",
                        "Word stress patterns for common 2-syllable nouns"
                    )
                ),
                ProficiencyModule(
                    id = "a1_m2",
                    levelCode = "A1",
                    title = "Greetings & Introductions",
                    description = "Learn warm welcomes, polite answers, and telling others your name and origin.",
                    iconEmoji = "👋",
                    skillFocus = SkillFocus.CONVERSATION,
                    xpReward = 35,
                    gemReward = 15,
                    estimatedMinutes = 5,
                    learningOutcomes = listOf(
                        "Introduce yourself with confidence",
                        "Ask: 'Where are you from?' & answer politely",
                        "Distinguish between 'Good morning', 'Good evening' and casual 'Hey'"
                    )
                ),
                ProficiencyModule(
                    id = "a1_m3",
                    levelCode = "A1",
                    title = "Numbers, Time & Calendar",
                    description = "Count 1-100, tell clock time, and schedule days of the week.",
                    iconEmoji = "⏰",
                    skillFocus = SkillFocus.VOCABULARY,
                    xpReward = 35,
                    gemReward = 15,
                    estimatedMinutes = 5,
                    learningOutcomes = listOf(
                        "Tell time: 'half past', 'quarter to', and digital time",
                        "Pronounce all 7 days of the week and 12 months correctly",
                        "Talk about birthdays and basic appointment schedules"
                    )
                ),
                ProficiencyModule(
                    id = "a1_m4",
                    levelCode = "A1",
                    title = "Family & People Around You",
                    description = "Describe family relations, occupations, and physical appearance.",
                    iconEmoji = "👨‍👩‍👧",
                    skillFocus = SkillFocus.GRAMMAR,
                    xpReward = 40,
                    gemReward = 20,
                    estimatedMinutes = 6,
                    learningOutcomes = listOf(
                        "Use possessive pronouns: 'my', 'his', 'her', 'their'",
                        "Describe family members and basic professions",
                        "Form basic sentences with 'to be' (am, is, are)"
                    )
                ),
                ProficiencyModule(
                    id = "a1_m5",
                    levelCode = "A1",
                    title = "A1 Breakthrough Milestone Exam",
                    description = "Comprehensive speaking and grammar test to certify A1 English proficiency.",
                    iconEmoji = "🏆",
                    skillFocus = SkillFocus.EXAM,
                    xpReward = 60,
                    gemReward = 35,
                    estimatedMinutes = 8,
                    learningOutcomes = listOf(
                        "Score 80%+ on oral pronunciation challenge",
                        "Pass basic grammar syntax placement",
                        "Unlock A2 Elementary tier and earn A1 Certificate Badge"
                    ),
                    isCheckpoint = true
                )
            )
        ),

        // Level 2: A2 Elementary
        ProficiencyLevel(
            code = "A2",
            title = "Elementary",
            subtitle = "Daily Life & Practical Communication",
            cefrStandard = "CEFR A2 Waystage",
            summary = "Communicate in routine tasks, order meals, ask directions, shop, and discuss hobbies.",
            iconEmoji = "🚶",
            iconVector = Icons.Default.DirectionsWalk,
            themeColorHex = 0xFF1CB0F6, // Speak Blue
            darkColorHex = 0xFF1899D6,
            targetXp = 450,
            modules = listOf(
                ProficiencyModule(
                    id = "a2_m1",
                    levelCode = "A2",
                    title = "Daily Habits & Present Tenses",
                    description = "Talk about morning routines, commuting, and everyday schedules effortlessly.",
                    iconEmoji = "🌅",
                    skillFocus = SkillFocus.GRAMMAR,
                    xpReward = 40,
                    gemReward = 20,
                    estimatedMinutes = 5,
                    learningOutcomes = listOf(
                        "Use Present Simple for habits vs Present Continuous for now",
                        "Frequency adverbs: 'always', 'rarely', 'frequently'",
                        "Talk smoothly about daily morning and evening routines"
                    )
                ),
                ProficiencyModule(
                    id = "a2_m2",
                    levelCode = "A2",
                    title = "Ordering at Cafes & Dining Out",
                    description = "Polite ordering, asking menu questions, allergies, and splitting the bill.",
                    iconEmoji = "☕",
                    skillFocus = SkillFocus.CONVERSATION,
                    xpReward = 45,
                    gemReward = 20,
                    estimatedMinutes = 6,
                    learningOutcomes = listOf(
                        "Master polite requests: 'Could I please have...', 'I'd like...'",
                        "Ask about ingredients, spice levels, and dietary preferences",
                        "Handle paying, tips, and asking for the bill"
                    )
                ),
                ProficiencyModule(
                    id = "a2_m3",
                    levelCode = "A2",
                    title = "Directions & City Navigation",
                    description = "Ask for landmarks, understand street navigation, and take public transit.",
                    iconEmoji = "🧭",
                    skillFocus = SkillFocus.LISTENING,
                    xpReward = 45,
                    gemReward = 20,
                    estimatedMinutes = 5,
                    learningOutcomes = listOf(
                        "Prepositions of place: 'next to', 'opposite', 'cross the street'",
                        "Understand rapid transit announcements (train/bus)",
                        "Ask locals for help politely when lost"
                    )
                ),
                ProficiencyModule(
                    id = "a2_m4",
                    levelCode = "A2",
                    title = "Shopping, Bargaining & Prices",
                    description = "Inquire about sizes, prices, discounts, and return policies.",
                    iconEmoji = "🛍️",
                    skillFocus = SkillFocus.VOCABULARY,
                    xpReward = 50,
                    gemReward = 25,
                    estimatedMinutes = 6,
                    learningOutcomes = listOf(
                        "Compare items: 'cheaper than', 'more comfortable'",
                        "Discuss sizes, colors, fabrics, and fitting rooms",
                        "Polite negotiation and asking for store receipts"
                    )
                ),
                ProficiencyModule(
                    id = "a2_m5",
                    levelCode = "A2",
                    title = "A2 Elementary Diploma Challenge",
                    description = "Test practical conversational fluency across daily scenarios.",
                    iconEmoji = "🏆",
                    skillFocus = SkillFocus.EXAM,
                    xpReward = 75,
                    gemReward = 40,
                    estimatedMinutes = 8,
                    learningOutcomes = listOf(
                        "Complete simulated cafe and directions dialogues",
                        "Demonstrate error-free routine descriptions",
                        "Unlock B1 Intermediate tier and earn A2 Verified Badge"
                    ),
                    isCheckpoint = true
                )
            )
        ),

        // Level 3: B1 Intermediate
        ProficiencyLevel(
            code = "B1",
            title = "Intermediate",
            subtitle = "Independent & Fluent Conversationalist",
            cefrStandard = "CEFR B1 Threshold",
            summary = "Deal with most situations while traveling, express opinions, dreams, and past experiences.",
            iconEmoji = "🚀",
            iconVector = Icons.Default.FlightTakeoff,
            themeColorHex = 0xFFFFC800, // Gold Yellow
            darkColorHex = 0xFFE5B300,
            targetXp = 750,
            modules = listOf(
                ProficiencyModule(
                    id = "b1_m1",
                    levelCode = "B1",
                    title = "Storytelling & Past Experiences",
                    description = "Narrate events, past adventures, and life milestones using linked past tenses.",
                    iconEmoji = "📖",
                    skillFocus = SkillFocus.SPEAKING,
                    xpReward = 50,
                    gemReward = 25,
                    estimatedMinutes = 6,
                    learningOutcomes = listOf(
                        "Master Past Simple vs Past Continuous vs Past Perfect",
                        "Use narrative transition words: 'meanwhile', 'suddenly', 'eventually'",
                        "Keep listeners engaged while narrating a personal story"
                    )
                ),
                ProficiencyModule(
                    id = "b1_m2",
                    levelCode = "B1",
                    title = "Expressing Opinions & Debates",
                    description = "Share viewpoints, agreement, gentle disagreement, and personal beliefs.",
                    iconEmoji = "💬",
                    skillFocus = SkillFocus.CONVERSATION,
                    xpReward = 55,
                    gemReward = 25,
                    estimatedMinutes = 7,
                    learningOutcomes = listOf(
                        "Phrases for nuance: 'From my perspective', 'In my experience'",
                        "Diplomatic disagreement: 'I see your point, however...'",
                        "Defend viewpoints with logical reasoning"
                    )
                ),
                ProficiencyModule(
                    id = "b1_m3",
                    levelCode = "B1",
                    title = "International Travel & Flights",
                    description = "Handle customs, flight delays, hotel reservations, and emergency situations.",
                    iconEmoji = "✈️",
                    skillFocus = SkillFocus.VOCABULARY,
                    xpReward = 55,
                    gemReward = 25,
                    estimatedMinutes = 6,
                    learningOutcomes = listOf(
                        "Airport vocabulary: boarding gates, baggage claims, layovers",
                        "Hotel check-in negotiations and special amenity requests",
                        "Explain emergencies clearly to officials or medical staff"
                    )
                ),
                ProficiencyModule(
                    id = "b1_m4",
                    levelCode = "B1",
                    title = "Hypotheticals & Future Plans",
                    description = "Talk about future ambitions, possibilities, and conditional scenarios.",
                    iconEmoji = "💡",
                    skillFocus = SkillFocus.GRAMMAR,
                    xpReward = 60,
                    gemReward = 30,
                    estimatedMinutes = 6,
                    learningOutcomes = listOf(
                        "Second Conditional ('If I won... I would...')",
                        "Distinguish 'will', 'going to', and modal verbs of possibility",
                        "Express dreams and career goals with clarity"
                    )
                ),
                ProficiencyModule(
                    id = "b1_m5",
                    levelCode = "B1",
                    title = "B1 Independent Speaker Exam",
                    description = "Live assessment measuring conversational continuity and vocabulary range.",
                    iconEmoji = "🏆",
                    skillFocus = SkillFocus.EXAM,
                    xpReward = 90,
                    gemReward = 50,
                    estimatedMinutes = 10,
                    learningOutcomes = listOf(
                        "Spontaneous 3-minute oral monologue without pausing",
                        "Pass IELTS/TOEFL Band 5.5 equivalent grammar assessment",
                        "Unlock B2 Professional tier & earn B1 Certificate"
                    ),
                    isCheckpoint = true
                )
            )
        ),

        // Level 4: B2 Upper-Intermediate
        ProficiencyLevel(
            code = "B2",
            title = "Upper-Intermediate",
            subtitle = "Professional Workplace & Nuance",
            cefrStandard = "CEFR B2 Vantage",
            summary = "Interact with native speakers fluently, produce clear detailed text, and ace job interviews.",
            iconEmoji = "💼",
            iconVector = Icons.Default.BusinessCenter,
            themeColorHex = 0xFFCE82FF, // Feather Purple
            darkColorHex = 0xFFA55EEA,
            targetXp = 1100,
            modules = listOf(
                ProficiencyModule(
                    id = "b2_m1",
                    levelCode = "B2",
                    title = "Job Interview & Career Pitching",
                    description = "Ace behavioral questions using the STAR technique and articulate career achievements.",
                    iconEmoji = "👔",
                    skillFocus = SkillFocus.SPEAKING,
                    xpReward = 65,
                    gemReward = 30,
                    estimatedMinutes = 7,
                    learningOutcomes = listOf(
                        "Master STAR method (Situation, Task, Action, Result)",
                        "Professional buzzwords and impact verbs (spearheaded, delivered)",
                        "Negotiate salary and benefits professionally"
                    )
                ),
                ProficiencyModule(
                    id = "b2_m2",
                    levelCode = "B2",
                    title = "Business Meetings & Presentations",
                    description = "Lead presentations, handle tough Q&A, and align stakeholders effectively.",
                    iconEmoji = "📊",
                    skillFocus = SkillFocus.CONVERSATION,
                    xpReward = 70,
                    gemReward = 30,
                    estimatedMinutes = 8,
                    learningOutcomes = listOf(
                        "Slide presentation signposting: 'Turning our attention to...'",
                        "Diplomatically handle challenging stakeholder questions",
                        "Summarize action items and meeting minutes succinctly"
                    )
                ),
                ProficiencyModule(
                    id = "b2_m3",
                    levelCode = "B2",
                    title = "Idioms, Collocations & Slang",
                    description = "Decode figurative language, native phrasal verbs, and workplace expressions.",
                    iconEmoji = "🧠",
                    skillFocus = SkillFocus.VOCABULARY,
                    xpReward = 70,
                    gemReward = 35,
                    estimatedMinutes = 7,
                    learningOutcomes = listOf(
                        "Master 50+ common professional idioms (e.g. 'cut to the chase')",
                        "Natural phrasal verbs in place of formal verbs",
                        "Recognize common conversational sarcasm and cultural humor"
                    )
                ),
                ProficiencyModule(
                    id = "b2_m4",
                    levelCode = "B2",
                    title = "B2 Professional Master Checkpoint",
                    description = "Comprehensive workplace fluency evaluation and presentation assessment.",
                    iconEmoji = "🏆",
                    skillFocus = SkillFocus.EXAM,
                    xpReward = 110,
                    gemReward = 60,
                    estimatedMinutes = 12,
                    learningOutcomes = listOf(
                        "Deliver a 4-minute fluent business pitch",
                        "Achieve IELTS Band 6.5 - 7.0 proficiency rating",
                        "Unlock C1 Master tier & earn B2 Professional Diploma"
                    ),
                    isCheckpoint = true
                )
            )
        ),

        // Level 5: C1/C2 Advanced & Mastery
        ProficiencyLevel(
            code = "C1",
            title = "Advanced & Mastery",
            subtitle = "Native-Level Expression & Eloquence",
            cefrStandard = "CEFR C1/C2 Mastery",
            summary = "Express ideas fluently and spontaneously, flexible language for academic and public speaking.",
            iconEmoji = "👑",
            iconVector = Icons.Default.WorkspacePremium,
            themeColorHex = 0xFFFF9600, // Fire Orange
            darkColorHex = 0xFFE08200,
            targetXp = 1600,
            modules = listOf(
                ProficiencyModule(
                    id = "c1_m1",
                    levelCode = "C1",
                    title = "Public Speaking & Rhetoric",
                    description = "Master persuasive techniques, vocal modulation, and captivating speeches.",
                    iconEmoji = "🎙️",
                    skillFocus = SkillFocus.SPEAKING,
                    xpReward = 80,
                    gemReward = 40,
                    estimatedMinutes = 8,
                    learningOutcomes = listOf(
                        "Rhetorical devices: anaphora, triadic grouping, rhetorical questions",
                        "Master pauses, tempo changes, and vocal emphasis",
                        "Captivate audiences with inspirational storytelling"
                    )
                ),
                ProficiencyModule(
                    id = "c1_m2",
                    levelCode = "C1",
                    title = "Complex Syntax & Inversions",
                    description = "Express subtle distinctions with inversions, subjunctive mood, and cleft sentences.",
                    iconEmoji = "📜",
                    skillFocus = SkillFocus.GRAMMAR,
                    xpReward = 85,
                    gemReward = 40,
                    estimatedMinutes = 8,
                    learningOutcomes = listOf(
                        "Negative inversions: 'Rarely have I seen...', 'Seldom did we...'",
                        "Cleft sentences: 'What matters most is...'",
                        "Sophisticated academic transitions and hedging expressions"
                    )
                ),
                ProficiencyModule(
                    id = "c1_m3",
                    levelCode = "C1",
                    title = "Cultural Nuances & Fast Native Speech",
                    description = "Decode regional accents, rapid connected speech, elision, and subtext.",
                    iconEmoji = "⚡",
                    skillFocus = SkillFocus.LISTENING,
                    xpReward = 90,
                    gemReward = 45,
                    estimatedMinutes = 8,
                    learningOutcomes = listOf(
                        "Understand fast-talking native podcasts at 1.25x speed",
                        "Master reduction: 'wanna', 'gonna', 'coulda', glottal stops",
                        "Identify implicit subtext and double meanings in discussions"
                    )
                ),
                ProficiencyModule(
                    id = "c1_m4",
                    levelCode = "C1",
                    title = "C1/C2 Grand Master Certification",
                    description = "The ultimate English proficiency challenge. Earn the SpeakLingo Grand Master Laurel.",
                    iconEmoji = "🏆",
                    skillFocus = SkillFocus.EXAM,
                    xpReward = 150,
                    gemReward = 100,
                    estimatedMinutes = 15,
                    learningOutcomes = listOf(
                        "Live spontaneous debate challenge with Aria AI",
                        "Attain near-native IELTS 8.0+ / TOEFL 110+ fluency benchmark",
                        "Receive SpeakLingo Grand Master Gold Laurel & Certificate"
                    ),
                    isCheckpoint = true
                )
            )
        )
    )
}
