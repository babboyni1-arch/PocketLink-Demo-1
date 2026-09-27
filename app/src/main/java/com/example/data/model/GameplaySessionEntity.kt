package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gameplay_sessions")
data class GameplaySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Long = 0,
    val gameTitle: String,
    val gamePackage: String,
    val role: String, // "HOST", "REMOTE_PLAYER", "VIEWER"
    val hostDeviceName: String,
    val playerDeviceName: String,
    val spectatorsCount: Int = 0,
    val startTimeMs: Long,
    val endTimeMs: Long = 0L,
    val durationSeconds: Long = 0L,
    val status: String = "ACTIVE", // "ACTIVE", "COMPLETED", "INTERRUPTED"
    val recordingPath: String? = null,
    val totalInputEvents: Int = 0,
    val avgLatencyMs: Int = 28,
    val peakLatencyMs: Int = 45,
    val avgFps: Int = 60,
    val totalDataMb: Double = 0.0,
    val sessionHighlightsJson: String = "", // Milestone events from start to end
    val notes: String = "",
    val rating: Int = 5
)

data class TimelineMilestone(
    val timestampOffsetSec: Long,
    val formattedTime: String,
    val eventType: String, // "SESSION_START", "CONTROLLER_SYNC", "RECORDING_START", "PING_SPIKE", "GAME_ACTION", "SESSION_END"
    val description: String,
    val latencyMs: Int? = null,
    val fps: Int? = null
)
