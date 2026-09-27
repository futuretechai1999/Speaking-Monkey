package com.example

import com.example.speaklingo.data.model.LessonCatalog
import com.example.speaklingo.ui.components.RoadmapNodeStatus
import com.example.speaklingo.ui.components.getLessonIconData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoadmapUnitTest {

    @Test
    fun `test units and lessons catalog has valid structure`() {
        val units = LessonCatalog.units
        assertTrue("Catalog should contain units", units.isNotEmpty())

        val allLessons = units.flatMap { it.lessons }
        assertTrue("Catalog should contain lessons", allLessons.isNotEmpty())

        units.forEach { unit ->
            assertTrue("Unit title should not be empty", unit.title.isNotBlank())
            assertTrue("Unit should have lessons", unit.lessons.isNotEmpty())
        }
    }

    @Test
    fun `test lesson icon mapping returns valid icons and emojis`() {
        val firstLesson = LessonCatalog.units.first().lessons.first()
        val (iconVector, emoji) = getLessonIconData(firstLesson)

        assertNotNull("Icon vector should not be null", iconVector)
        assertTrue("Emoji should not be empty", emoji.isNotBlank())
        assertEquals("🗣️", emoji)
    }

    @Test
    fun `test roadmap node statuses are distinct`() {
        val statuses = RoadmapNodeStatus.values()
        assertEquals(3, statuses.size)
        assertTrue(statuses.contains(RoadmapNodeStatus.LOCKED))
        assertTrue(statuses.contains(RoadmapNodeStatus.IN_PROGRESS))
        assertTrue(statuses.contains(RoadmapNodeStatus.COMPLETED))
    }
}
