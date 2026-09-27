package com.example.network

import android.graphics.Bitmap
import com.example.network.models.ControllerInputEvent
import com.example.network.models.SpectatorEmote
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
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

class PocketLinkServer(
    private val scope: CoroutineScope
) {
    private val CONTROL_PORT = 8889
    private var serverSocket: ServerSocket? = null
    private var listenJob: Job? = null
    private val clientConnections = CopyOnWriteArrayList<ClientSession>()

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    private val _connectedPlayerName = MutableStateFlow<String?>(null)
    val connectedPlayerName: StateFlow<String?> = _connectedPlayerName.asStateFlow()

    private val _spectatorCount = MutableStateFlow(0)
    val spectatorCount: StateFlow<Int> = _spectatorCount.asStateFlow()

    private val _incomingInputs = MutableSharedFlow<ControllerInputEvent>(extraBufferCapacity = 64)
    val incomingInputs: SharedFlow<ControllerInputEvent> = _incomingInputs.asSharedFlow()

    private val _incomingEmotes = MutableSharedFlow<SpectatorEmote>(extraBufferCapacity = 16)
    val incomingEmotes: SharedFlow<SpectatorEmote> = _incomingEmotes.asSharedFlow()

    private val _lastInputEvent = MutableStateFlow<ControllerInputEvent?>(null)
    val lastInputEvent: StateFlow<ControllerInputEvent?> = _lastInputEvent.asStateFlow()

    // Host session metrics
    private val _currentLatencyMs = MutableStateFlow(22)
    val currentLatencyMs: StateFlow<Int> = _currentLatencyMs.asStateFlow()

    private val _currentFps = MutableStateFlow(60)
    val currentFps: StateFlow<Int> = _currentFps.asStateFlow()

    private val _totalInputsReceived = MutableStateFlow(0)
    val totalInputsReceived: StateFlow<Int> = _totalInputsReceived.asStateFlow()

    fun startServer(gameTitle: String) {
        if (_isServerRunning.value) return

        listenJob = scope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(CONTROL_PORT).apply {
                    reuseAddress = true
                }
                _isServerRunning.value = true

                while (isActive && serverSocket != null && !serverSocket!!.isClosed) {
                    val socket = serverSocket!!.accept()
                    val clientSession = ClientSession(socket)
                    clientConnections.add(clientSession)
                    handleClient(clientSession, gameTitle)
                }
            } catch (e: Exception) {
                // Server closed or network change
            } finally {
                _isServerRunning.value = false
            }
        }
    }

    private fun handleClient(client: ClientSession, gameTitle: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(client.socket.getInputStream()))
                val writer = PrintWriter(client.socket.getOutputStream(), true)
                client.writer = writer

                // Send welcome greeting with game info
                val welcome = JSONObject().apply {
                    put("type", "WELCOME")
                    put("gameTitle", gameTitle)
                    put("fps", 60)
                }.toString()
                writer.println(welcome)

                while (isActive && !client.socket.isClosed) {
                    val line = reader.readLine() ?: break
                    handleClientMessage(client, line)
                }
            } catch (e: Exception) {
                // Client disconnected
            } finally {
                clientConnections.remove(client)
                updateClientCounts()
                try { client.socket.close() } catch (e: Exception) {}
            }
        }
    }

    private suspend fun handleClientMessage(client: ClientSession, line: String) {
        try {
            val json = JSONObject(line)
            val msgType = json.optString("type", "")

            when (msgType) {
                "JOIN_AS_PLAYER" -> {
                    client.role = "PLAYER"
                    client.deviceName = json.optString("deviceName", "Remote Player")
                    _connectedPlayerName.value = client.deviceName
                    updateClientCounts()
                }
                "JOIN_AS_SPECTATOR" -> {
                    client.role = "SPECTATOR"
                    client.deviceName = json.optString("deviceName", "Spectator")
                    updateClientCounts()
                }
                "INPUT" -> {
                    val event = ControllerInputEvent.fromJson(json.getString("event"))
                    if (event != null) {
                        _incomingInputs.emit(event)
                        _lastInputEvent.value = event
                        _totalInputsReceived.value += 1
                    }
                }
                "EMOTE" -> {
                    val emote = SpectatorEmote(
                        emote = json.optString("emote", "🔥"),
                        senderName = client.deviceName ?: "Viewer"
                    )
                    _incomingEmotes.emit(emote)
                }
                "PING" -> {
                    val clientTs = json.optLong("ts", System.currentTimeMillis())
                    val pong = JSONObject().apply {
                        put("type", "PONG")
                        put("clientTs", clientTs)
                        put("serverTs", System.currentTimeMillis())
                    }.toString()
                    client.writer?.println(pong)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateClientCounts() {
        val spectators = clientConnections.count { it.role == "SPECTATOR" }
        _spectatorCount.value = spectators
        val player = clientConnections.firstOrNull { it.role == "PLAYER" }
        _connectedPlayerName.value = player?.deviceName
    }

    fun broadcastStats(fps: Int, latency: Int, bitrate: Double) {
        _currentFps.value = fps
        _currentLatencyMs.value = latency
        val stats = JSONObject().apply {
            put("type", "STATS")
            put("fps", fps)
            put("latency", latency)
            put("bitrate", bitrate)
        }.toString()

        for (client in clientConnections) {
            client.writer?.println(stats)
        }
    }

    fun stopServer() {
        listenJob?.cancel()
        for (client in clientConnections) {
            try { client.socket.close() } catch (e: Exception) {}
        }
        clientConnections.clear()
        try {
            serverSocket?.close()
        } catch (e: Exception) {}
        serverSocket = null
        _isServerRunning.value = false
        _connectedPlayerName.value = null
        _spectatorCount.value = 0
    }

    private class ClientSession(val socket: Socket) {
        var role: String = "PENDING"
        var deviceName: String? = null
        var writer: PrintWriter? = null
    }
}
