package com.example.network

import com.example.network.models.ControllerInputEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class PocketLinkClient(
    private val scope: CoroutineScope
) {
    private val CONTROL_PORT = 8889
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var readJob: Job? = null
    private var pingJob: Job? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionStateText = MutableStateFlow("Disconnected")
    val connectionStateText: StateFlow<String> = _connectionStateText.asStateFlow()

    private val _currentPingMs = MutableStateFlow(24)
    val currentPingMs: StateFlow<Int> = _currentPingMs.asStateFlow()

    private val _currentFps = MutableStateFlow(60)
    val currentFps: StateFlow<Int> = _currentFps.asStateFlow()

    private val _currentBitrateMbps = MutableStateFlow(8.5)
    val currentBitrateMbps: StateFlow<Double> = _currentBitrateMbps.asStateFlow()

    private val _activeGameTitle = MutableStateFlow<String?>(null)
    val activeGameTitle: StateFlow<String?> = _activeGameTitle.asStateFlow()

    private val _activeRemoteInputs = MutableSharedFlow<ControllerInputEvent>(extraBufferCapacity = 32)
    val activeRemoteInputs: SharedFlow<ControllerInputEvent> = _activeRemoteInputs.asSharedFlow()

    fun connect(hostIp: String, role: String, deviceName: String, onConnectResult: (Boolean, String?) -> Unit) {
        disconnect()

        scope.launch(Dispatchers.IO) {
            _connectionStateText.value = "Connecting to $hostIp..."
            try {
                val s = Socket()
                s.connect(InetSocketAddress(hostIp, CONTROL_PORT), 4000)
                socket = s
                writer = PrintWriter(s.getOutputStream(), true)
                _isConnected.value = true
                _connectionStateText.value = "Connected"

                // Send join greeting
                val joinMsg = JSONObject().apply {
                    put("type", if (role == "PLAYER") "JOIN_AS_PLAYER" else "JOIN_AS_SPECTATOR")
                    put("deviceName", deviceName)
                }.toString()
                writer?.println(joinMsg)

                startPingLoop()
                startReading(s)

                withContext(Dispatchers.Main) {
                    onConnectResult(true, null)
                }
            } catch (e: Exception) {
                _isConnected.value = false
                _connectionStateText.value = "Connection failed: ${e.localizedMessage}"
                withContext(Dispatchers.Main) {
                    onConnectResult(false, e.localizedMessage ?: "Connection error")
                }
            }
        }
    }

    private fun startReading(s: Socket) {
        readJob?.cancel()
        readJob = scope.launch(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                while (isActive && !s.isClosed) {
                    val line = reader.readLine() ?: break
                    handleServerMessage(line)
                }
            } catch (e: Exception) {
                // Read closed
            } finally {
                _isConnected.value = false
                _connectionStateText.value = "Disconnected"
            }
        }
    }

    private fun handleServerMessage(line: String) {
        try {
            val json = JSONObject(line)
            when (json.optString("type")) {
                "WELCOME" -> {
                    val title = json.optString("gameTitle", "PocketLink Game")
                    _activeGameTitle.value = title
                }
                "STATS" -> {
                    _currentFps.value = json.optInt("fps", 60)
                    _currentPingMs.value = json.optInt("latency", 24)
                    _currentBitrateMbps.value = json.optDouble("bitrate", 8.5)
                }
                "PONG" -> {
                    val clientTs = json.optLong("clientTs", 0)
                    if (clientTs > 0) {
                        val rtt = (System.currentTimeMillis() - clientTs).toInt().coerceAtLeast(4)
                        _currentPingMs.value = rtt
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startPingLoop() {
        pingJob?.cancel()
        pingJob = scope.launch(Dispatchers.IO) {
            while (isActive && _isConnected.value) {
                try {
                    val ping = JSONObject().apply {
                        put("type", "PING")
                        put("ts", System.currentTimeMillis())
                    }.toString()
                    writer?.println(ping)
                } catch (e: Exception) {
                    break
                }
                delay(1200)
            }
        }
    }

    fun sendInput(event: ControllerInputEvent) {
        if (!_isConnected.value) return
        scope.launch(Dispatchers.IO) {
            try {
                val msg = JSONObject().apply {
                    put("type", "INPUT")
                    put("event", event.toJson())
                }.toString()
                writer?.println(msg)
            } catch (e: Exception) {
                // Ignore socket pipe drops
            }
        }
    }

    fun sendEmote(emote: String) {
        if (!_isConnected.value) return
        scope.launch(Dispatchers.IO) {
            try {
                val msg = JSONObject().apply {
                    put("type", "EMOTE")
                    put("emote", emote)
                }.toString()
                writer?.println(msg)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun disconnect() {
        readJob?.cancel()
        pingJob?.cancel()
        try {
            socket?.close()
        } catch (e: Exception) {}
        socket = null
        writer = null
        _isConnected.value = false
        _connectionStateText.value = "Disconnected"
    }
}
