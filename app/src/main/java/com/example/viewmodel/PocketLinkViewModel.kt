package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PocketLinkApp
import com.example.data.model.GameEntity
import com.example.data.model.GameplaySessionEntity
import com.example.network.DeviceDiscovery
import com.example.network.PocketLinkClient
import com.example.network.PocketLinkServer
import com.example.network.models.ControllerInputEvent
import com.example.network.models.DiscoveredDevice
import com.example.network.models.LiveGameUsageInfo
import com.example.network.models.SpectatorEmote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PocketLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PocketLinkApp
    val gameRepo = app.gameRepository
    val sessionRepo = app.gameplaySessionRepository

    // Discovery & Networking
    val deviceDiscovery = DeviceDiscovery(application, viewModelScope)
    val server = PocketLinkServer(viewModelScope)
    val client = PocketLinkClient(viewModelScope)

    // Data streams
    val allGames: StateFlow<List<GameEntity>> = gameRepo.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<GameplaySessionEntity>> = sessionRepo.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val discoveredPeers: StateFlow<List<DiscoveredDevice>> = deviceDiscovery.discoveredDevices

    // Live Game Usage Monitor ("Watch if someone is using them")
    val activeLiveGames: StateFlow<List<LiveGameUsageInfo>> = combine(
        discoveredPeers,
        server.isServerRunning,
        server.connectedPlayerName,
        server.spectatorCount
    ) { peers, isHostRunning, hostPlayerName, hostSpectatorCount ->
        val list = mutableListOf<LiveGameUsageInfo>()

        // 1. If this phone is hosting a game, it is live
        if (isHostRunning && selectedHostGame.value != null) {
            val game = selectedHostGame.value!!
            val elapsed = if (activeSessionStartTimeMs.value > 0) {
                (System.currentTimeMillis() - activeSessionStartTimeMs.value) / 1000
            } else 0L

            list.add(
                LiveGameUsageInfo(
                    sessionId = "host-local",
                    gameTitle = game.title,
                    gamePackage = game.packageName,
                    hostDeviceName = "${deviceDiscovery.myDeviceName} (This Device)",
                    hostIp = "127.0.0.1",
                    activeControllerName = hostPlayerName ?: "Waiting for player...",
                    spectatorsCount = hostSpectatorCount,
                    elapsedSeconds = elapsed,
                    resolution = game.targetResolution,
                    fps = server.currentFps.value,
                    bitrateMbps = 8.5,
                    latencyMs = server.currentLatencyMs.value,
                    isJoinableAsPlayer = hostPlayerName == null,
                    isJoinableAsSpectator = true
                )
            )
        }

        // 2. Add peers that are actively hosting games on the network
        for (peer in peers) {
            if (peer.isHostingGame && peer.currentGameTitle != null) {
                list.add(
                    LiveGameUsageInfo(
                        sessionId = peer.deviceId,
                        gameTitle = peer.currentGameTitle,
                        gamePackage = peer.currentGamePackage ?: "unknown.package",
                        hostDeviceName = peer.deviceName,
                        hostIp = peer.ipAddress,
                        activeControllerName = if (peer.activePlayersCount > 0) "Remote Player" else "Open Slot",
                        spectatorsCount = peer.activeSpectatorsCount,
                        elapsedSeconds = peer.sessionElapsedSec,
                        resolution = "1080p",
                        fps = 60,
                        bitrateMbps = 9.2,
                        latencyMs = 28,
                        isJoinableAsPlayer = peer.activePlayersCount == 0,
                        isJoinableAsSpectator = true
                    )
                )
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active session state ("Every gameplay from start to end")
    val selectedHostGame = MutableStateFlow<GameEntity?>(null)
    val activeSessionId = MutableStateFlow<Long?>(null)
    val activeSessionStartTimeMs = MutableStateFlow(0L)
    val activeSessionElapsedSec = MutableStateFlow(0L)
    val isRecordingActive = MutableStateFlow(false)
    val currentRecordingFilePath = MutableStateFlow<String?>(null)
    private var sessionTimerJob: Job? = null

    // Controller input stats & live display
    val liveActiveInputEvent = MutableStateFlow<ControllerInputEvent?>(null)
    val totalInputsInSession = MutableStateFlow(0)

    // Spectator view state
    val spectatorTargetGame = MutableStateFlow<LiveGameUsageInfo?>(null)
    val spectatorEmotesFeed = MutableStateFlow<List<SpectatorEmote>>(emptyList())

    // UI feedback
    private val _snackMessage = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackMessage: SharedFlow<String> = _snackMessage.asSharedFlow()

    init {
        deviceDiscovery.startDiscovery()

        // Forward server incoming inputs
        viewModelScope.launch {
            server.incomingInputs.collect { event ->
                liveActiveInputEvent.value = event
                totalInputsInSession.value += 1
            }
        }

        // Forward server incoming emotes
        viewModelScope.launch {
            server.incomingEmotes.collect { emote ->
                val current = spectatorEmotesFeed.value.toMutableList()
                current.add(0, emote)
                if (current.size > 15) current.removeAt(current.lastIndex)
                spectatorEmotesFeed.value = current
            }
        }
    }

    // ==========================================
    // 1. FEATURE: PUT GAMES (Library & Management)
    // ==========================================

    fun addCustomGame(
        title: String,
        packageName: String,
        category: String,
        accentColor: String,
        targetFps: Int,
        resolution: String,
        controllerLayout: String
    ) {
        viewModelScope.launch {
            val game = GameEntity(
                title = title.trim(),
                packageName = packageName.trim(),
                category = category,
                accentColorHex = accentColor,
                isCustom = true,
                targetFps = targetFps,
                targetResolution = resolution,
                preferredControllerLayout = controllerLayout,
                supportStatusNote = "Custom Game Profile Ready"
            )
            gameRepo.addGame(game)
            _snackMessage.emit("Game '${game.title}' added to PocketLink library!")
        }
    }

    fun toggleFavorite(gameId: Long, currentFav: Boolean) {
        viewModelScope.launch {
            gameRepo.toggleFavorite(gameId, !currentFav)
        }
    }

    fun deleteGame(game: GameEntity) {
        viewModelScope.launch {
            gameRepo.deleteGame(game)
            _snackMessage.emit("Removed '${game.title}'")
        }
    }

    fun scanInstalledGames() {
        viewModelScope.launch {
            val scanned = gameRepo.scanInstalledApps()
            var addedCount = 0
            for (game in scanned) {
                val existing = gameRepo.allGames
                gameRepo.addGame(game)
                addedCount++
            }
            _snackMessage.emit("Scanned device: synchronized $addedCount applications")
        }
    }

    fun selectGameForHosting(game: GameEntity) {
        selectedHostGame.value = game
    }

    // ==========================================
    // 2. FEATURE: WATCH IF SOMEONE IS USING THEM
    // ==========================================

    fun startWatchingGame(gameUsage: LiveGameUsageInfo, onConnected: (Boolean) -> Unit) {
        spectatorTargetGame.value = gameUsage

        // Connect client as spectator
        client.connect(
            hostIp = gameUsage.hostIp,
            role = "SPECTATOR",
            deviceName = "${deviceDiscovery.myDeviceName} (Watcher)"
        ) { success, error ->
            if (success) {
                viewModelScope.launch {
                    _snackMessage.emit("Joined live spectator stream for ${gameUsage.gameTitle}")
                }
                onConnected(true)
            } else {
                viewModelScope.launch {
                    _snackMessage.emit("Failed to connect to ${gameUsage.hostDeviceName}: $error")
                }
                onConnected(false)
            }
        }
    }

    fun sendSpectatorEmote(emote: String) {
        client.sendEmote(emote)
        // Add to local feed as well
        val newEmote = SpectatorEmote(emote, "You")
        val current = spectatorEmotesFeed.value.toMutableList()
        current.add(0, newEmote)
        spectatorEmotesFeed.value = current
    }

    // ==========================================
    // 3. FEATURE: EVERY GAMEPLAY FROM START TO END
    // ==========================================

    fun startHostGameplaySession(
        enableRecording: Boolean,
        onSessionStarted: (Long) -> Unit
    ) {
        val game = selectedHostGame.value ?: return

        viewModelScope.launch {
            gameRepo.updateLastPlayed(game.id)

            // Start socket server for video & controller
            server.startServer(game.title)

            // Announce to network that this device is hosting this game
            val startTime = System.currentTimeMillis()
            activeSessionStartTimeMs.value = startTime
            activeSessionElapsedSec.value = 0L
            totalInputsInSession.value = 0
            isRecordingActive.value = enableRecording

            deviceDiscovery.setHostingState(
                hosting = true,
                gameTitle = game.title,
                gamePackage = game.packageName,
                players = 0,
                spectators = 0,
                startTimeMs = startTime
            )

            // Create start-to-end session entry in Room
            val sessionId = sessionRepo.startSession(
                gameId = game.id,
                gameTitle = game.title,
                gamePackage = game.packageName,
                role = "HOST",
                hostDeviceName = deviceDiscovery.myDeviceName,
                playerDeviceName = "Waiting for remote player...",
                spectatorsCount = 0
            )
            activeSessionId.value = sessionId

            if (enableRecording) {
                sessionRepo.addMilestoneToSession(
                    sessionId = sessionId,
                    eventType = "RECORDING_START",
                    description = "Local gameplay screen recording initiated in 1080p/60fps"
                )
            }

            // Launch elapsed timer loop
            sessionTimerJob?.cancel()
            sessionTimerJob = viewModelScope.launch {
                while (isActive) {
                    delay(1000)
                    activeSessionElapsedSec.value = (System.currentTimeMillis() - startTime) / 1000
                }
            }

            // Attempt to launch the actual game on device if installed
            launchGameApp(game.packageName)

            onSessionStarted(sessionId)
        }
    }

    fun recordMilestone(type: String, description: String) {
        val id = activeSessionId.value ?: return
        viewModelScope.launch {
            sessionRepo.addMilestoneToSession(
                sessionId = id,
                eventType = type,
                description = description,
                latencyMs = server.currentLatencyMs.value,
                fps = server.currentFps.value
            )
        }
    }

    fun endCurrentGameplaySession() {
        val id = activeSessionId.value
        sessionTimerJob?.cancel()
        sessionTimerJob = null

        // Stop server & reset network beacon
        server.stopServer()
        deviceDiscovery.setHostingState(false)

        if (id != null) {
            viewModelScope.launch {
                sessionRepo.endSession(
                    sessionId = id,
                    totalInputs = totalInputsInSession.value,
                    avgLatency = server.currentLatencyMs.value,
                    peakLatency = server.currentLatencyMs.value + 16,
                    avgFps = server.currentFps.value,
                    recordingPath = currentRecordingFilePath.value
                )
                _snackMessage.emit("Gameplay session #$id successfully finalized and saved to history!")
            }
        }

        activeSessionId.value = null
        activeSessionStartTimeMs.value = 0L
        activeSessionElapsedSec.value = 0L
        isRecordingActive.value = false
        currentRecordingFilePath.value = null
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            sessionRepo.deleteSession(sessionId)
            _snackMessage.emit("Session record deleted")
        }
    }

    fun updateSessionNotes(sessionId: Long, notes: String, rating: Int) {
        viewModelScope.launch {
            sessionRepo.updateNotesAndRating(sessionId, notes, rating)
            _snackMessage.emit("Session notes updated!")
        }
    }

    private fun launchGameApp(packageName: String) {
        try {
            val pm = app.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                app.startActivity(intent)
            }
        } catch (e: Exception) {
            // App might not be physically installed, handle gracefully
        }
    }

    override fun onCleared() {
        super.onCleared()
        deviceDiscovery.stopDiscovery()
        server.stopServer()
        client.disconnect()
    }
}
