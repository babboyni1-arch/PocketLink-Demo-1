package com.example.network.models

import org.json.JSONObject

data class DiscoveredDevice(
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val port: Int,
    val role: String, // "HOST", "PLAYER", "IDLE"
    val isHostingGame: Boolean,
    val currentGameTitle: String? = null,
    val currentGamePackage: String? = null,
    val activePlayersCount: Int = 0,
    val activeSpectatorsCount: Int = 0,
    val sessionElapsedSec: Long = 0,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

data class LiveGameUsageInfo(
    val sessionId: String,
    val gameTitle: String,
    val gamePackage: String,
    val hostDeviceName: String,
    val hostIp: String,
    val activeControllerName: String? = null,
    val spectatorsCount: Int = 0,
    val elapsedSeconds: Long = 0,
    val resolution: String = "1080p",
    val fps: Int = 60,
    val bitrateMbps: Double = 8.5,
    val latencyMs: Int = 24,
    val isJoinableAsPlayer: Boolean = false,
    val isJoinableAsSpectator: Boolean = true
)

data class ControllerInputEvent(
    val actionType: String, // "BUTTON_DOWN", "BUTTON_UP", "JOYSTICK_MOVE", "TOUCH_EVENT"
    val buttonId: String? = null, // "A", "B", "X", "Y", "DPAD_UP", etc.
    val joystickX: Float = 0f,
    val joystickY: Float = 0f,
    val touchXPercent: Float = 0f,
    val touchYPercent: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): String {
        val obj = JSONObject()
        obj.put("action", actionType)
        buttonId?.let { obj.put("button", it) }
        obj.put("jx", joystickX.toDouble())
        obj.put("jy", joystickY.toDouble())
        obj.put("tx", touchXPercent.toDouble())
        obj.put("ty", touchYPercent.toDouble())
        obj.put("ts", timestamp)
        return obj.toString()
    }

    companion object {
        fun fromJson(json: String): ControllerInputEvent? {
            return try {
                val obj = JSONObject(json)
                ControllerInputEvent(
                    actionType = obj.getString("action"),
                    buttonId = if (obj.has("button")) obj.getString("button") else null,
                    joystickX = obj.optDouble("jx", 0.0).toFloat(),
                    joystickY = obj.optDouble("jy", 0.0).toFloat(),
                    touchXPercent = obj.optDouble("tx", 0.0).toFloat(),
                    touchYPercent = obj.optDouble("ty", 0.0).toFloat(),
                    timestamp = obj.optLong("ts", System.currentTimeMillis())
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class SpectatorEmote(
    val emote: String,
    val senderName: String,
    val timestamp: Long = System.currentTimeMillis()
)
