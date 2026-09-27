package com.example

import com.example.speaklingo.data.model.ProficiencyCatalog
import com.example.speaklingo.data.model.ProficiencyStatus
import com.example.speaklingo.data.model.SkillFocus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProficiencyRoadmapTest {

    @Test
    fun `test all five CEFR levels exist in catalog`() {
        val levels = ProficiencyCatalog.levels
        assertEquals("There should be 5 CEFR levels (A1, A2, B1, B2, C1)", 5, levels.size)

        val codes = levels.map { it.code }
        assertTrue("Contains A1", codes.contains("A1"))
        assertTrue("Contains A2", codes.contains("A2"))
        assertTrue("Contains B1", codes.contains("B1"))
        assertTrue("Contains B2", codes.contains("B2"))
        assertTrue("Contains C1", codes.contains("C1"))
    }

    @Test
    fun `test each level has modules and checkpoints`() {
        ProficiencyCatalog.levels.forEach { level ->
            assertTrue("Level ${level.code} must have modules", level.modules.isNotEmpty())
            assertTrue("Level ${level.code} must have at least one checkpoint", level.modules.any { it.isCheckpoint })

            level.modules.forEach { module ->
                assertTrue("Module title must not be blank", module.title.isNotBlank())
                assertTrue("Module description must not be blank", module.description.isNotBlank())
                assertTrue("Module outcomes must not be empty", module.learningOutcomes.isNotEmpty())
                assertTrue("Reward XP must be positive", module.xpReward > 0)
                assertTrue("Reward gems must be positive", module.gemReward > 0)
            }
        }
    }

    @Test
    fun `test skill focus types are defined`() {
        val focuses = SkillFocus.entries
        assertTrue(focuses.contains(SkillFocus.SPEAKING))
        assertTrue(focuses.contains(SkillFocus.GRAMMAR))
        assertTrue(focuses.contains(SkillFocus.VOCABULARY))
        assertTrue(focuses.contains(SkillFocus.LISTENING))
        assertTrue(focuses.contains(SkillFocus.CONVERSATION))
        assertTrue(focuses.contains(SkillFocus.EXAM))
    }

    @Test
    fun `test proficiency statuses are complete`() {
        val statuses = ProficiencyStatus.entries
        assertEquals(3, statuses.size)
        assertTrue(statuses.contains(ProficiencyStatus.LOCKED))
        assertTrue(statuses.contains(ProficiencyStatus.IN_PROGRESS))
        assertTrue(statuses.contains(ProficiencyStatus.COMPLETED))
    }
}
