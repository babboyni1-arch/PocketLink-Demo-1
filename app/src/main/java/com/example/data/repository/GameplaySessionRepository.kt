package com.example.data.repository

import com.example.data.dao.GameplaySessionDao
import com.example.data.model.GameplaySessionEntity
import com.example.data.model.TimelineMilestone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GameplaySessionRepository(
    private val sessionDao: GameplaySessionDao
) {
    val allSessions: Flow<List<GameplaySessionEntity>> = sessionDao.getAllSessions()

    suspend fun getSessionById(id: Long): GameplaySessionEntity? = sessionDao.getSessionById(id)

    suspend fun startSession(
        gameId: Long,
        gameTitle: String,
        gamePackage: String,
        role: String,
        hostDeviceName: String,
        playerDeviceName: String,
        spectatorsCount: Int = 0
    ): Long = withContext(Dispatchers.IO) {
        val initialMilestones = listOf(
            TimelineMilestone(
                timestampOffsetSec = 0,
                formattedTime = "00:00",
                eventType = "SESSION_START",
                description = "PocketLink session initialized for $gameTitle",
                latencyMs = 24,
                fps = 60
            ),
            TimelineMilestone(
                timestampOffsetSec = 1,
                formattedTime = "00:01",
                eventType = "CONTROLLER_SYNC",
                description = "Virtual gamepad synced with $playerDeviceName",
                latencyMs = 25,
                fps = 60
            )
        )

        val entity = GameplaySessionEntity(
            gameId = gameId,
            gameTitle = gameTitle,
            gamePackage = gamePackage,
            role = role,
            hostDeviceName = hostDeviceName,
            playerDeviceName = playerDeviceName,
            spectatorsCount = spectatorsCount,
            startTimeMs = System.currentTimeMillis(),
            status = "ACTIVE",
            sessionHighlightsJson = serializeMilestones(initialMilestones)
        )
        sessionDao.insertSession(entity)
    }

    suspend fun addMilestoneToSession(
        sessionId: Long,
        eventType: String,
        description: String,
        latencyMs: Int? = null,
        fps: Int? = null
    ) = withContext(Dispatchers.IO) {
        val session = sessionDao.getSessionById(sessionId) ?: return@withContext
        val currentSec = ((System.currentTimeMillis() - session.startTimeMs) / 1000).coerceAtLeast(0)
        val minutes = currentSec / 60
        val seconds = currentSec % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

        val list = parseMilestones(session.sessionHighlightsJson).toMutableList()
        list.add(
            TimelineMilestone(
                timestampOffsetSec = currentSec,
                formattedTime = formattedTime,
                eventType = eventType,
                description = description,
                latencyMs = latencyMs,
                fps = fps
            )
        )

        val updated = session.copy(
            sessionHighlightsJson = serializeMilestones(list)
        )
        sessionDao.updateSession(updated)
    }

    suspend fun endSession(
        sessionId: Long,
        totalInputs: Int,
        avgLatency: Int,
        peakLatency: Int,
        avgFps: Int,
        recordingPath: String? = null
    ) = withContext(Dispatchers.IO) {
        val session = sessionDao.getSessionById(sessionId) ?: return@withContext
        val endTime = System.currentTimeMillis()
        val durationSec = ((endTime - session.startTimeMs) / 1000).coerceAtLeast(1)

        val minutes = durationSec / 60
        val seconds = durationSec % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

        val list = parseMilestones(session.sessionHighlightsJson).toMutableList()
        list.add(
            TimelineMilestone(
                timestampOffsetSec = durationSec,
                formattedTime = formattedTime,
                eventType = "SESSION_END",
                description = "Gameplay ended. Total duration: $formattedTime ($totalInputs inputs)",
                latencyMs = avgLatency,
                fps = avgFps
            )
        )

        val dataMb = (durationSec * 1.8).coerceAtLeast(0.5) // ~1.8 MB/s stream estimate

        val finalized = session.copy(
            endTimeMs = endTime,
            durationSeconds = durationSec,
            status = "COMPLETED",
            recordingPath = recordingPath,
            totalInputEvents = totalInputs,
            avgLatencyMs = avgLatency,
            peakLatencyMs = peakLatency,
            avgFps = avgFps,
            totalDataMb = dataMb,
            sessionHighlightsJson = serializeMilestones(list)
        )
        sessionDao.updateSession(finalized)
    }

    suspend fun deleteSession(id: Long) = sessionDao.deleteSessionById(id)

    suspend fun updateNotesAndRating(id: Long, notes: String, rating: Int) =
        sessionDao.updateNotesAndRating(id, notes, rating)

    fun parseMilestones(json: String): List<TimelineMilestone> {
        if (json.isBlank()) return emptyList()
        val results = mutableListOf<TimelineMilestone>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                results.add(
                    TimelineMilestone(
                        timestampOffsetSec = obj.optLong("sec", 0),
                        formattedTime = obj.optString("time", "00:00"),
                        eventType = obj.optString("type", "EVENT"),
                        description = obj.optString("desc", ""),
                        latencyMs = if (obj.has("latency")) obj.getInt("latency") else null,
                        fps = if (obj.has("fps")) obj.getInt("fps") else null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }

    private fun serializeMilestones(list: List<TimelineMilestone>): String {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("sec", item.timestampOffsetSec)
            obj.put("time", item.formattedTime)
            obj.put("type", item.eventType)
            obj.put("desc", item.description)
            item.latencyMs?.let { obj.put("latency", it) }
            item.fps?.let { obj.put("fps", it) }
            array.put(obj)
        }
        return array.toString()
    }
}
