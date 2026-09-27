package com.example

import com.example.data.model.TimelineMilestone
import com.example.data.repository.GameplaySessionRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GameplayLifecycleTest {

    @Test
    fun testMilestoneParsingAndSerialization() {
        val json = """
            [
                {"sec": 0, "time": "00:00", "type": "SESSION_START", "desc": "Started", "latency": 24, "fps": 60},
                {"sec": 120, "time": "02:00", "type": "CONTROLLER_SYNC", "desc": "Controller linked", "latency": 25, "fps": 60}
            ]
        """.trimIndent()

        // Create a dummy repository without needing full Android Context for JSON test
        val repo = GameplaySessionRepository(
            sessionDao = object : com.example.data.dao.GameplaySessionDao {
                override fun getAllSessions() = kotlinx.coroutines.flow.emptyFlow<List<com.example.data.model.GameplaySessionEntity>>()
                override fun getSessionsForGame(gameId: Long) = kotlinx.coroutines.flow.emptyFlow<List<com.example.data.model.GameplaySessionEntity>>()
                override suspend fun getSessionById(id: Long) = null
                override suspend fun getActiveSession() = null
                override suspend fun insertSession(session: com.example.data.model.GameplaySessionEntity) = 1L
                override suspend fun updateSession(session: com.example.data.model.GameplaySessionEntity) {}
                override suspend fun deleteSession(session: com.example.data.model.GameplaySessionEntity) {}
                override suspend fun updateNotesAndRating(id: Long, notes: String, rating: Int) {}
                override suspend fun deleteSessionById(id: Long) {}
            }
        )

        val parsed = repo.parseMilestones(json)
        assertEquals(2, parsed.size)
        assertEquals("00:00", parsed[0].formattedTime)
        assertEquals("SESSION_START", parsed[0].eventType)
        assertEquals(24, parsed[0].latencyMs)
        assertEquals(60, parsed[0].fps)
        assertEquals("02:00", parsed[1].formattedTime)
    }
}
