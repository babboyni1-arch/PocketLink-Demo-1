package com.example.network

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import com.example.network.models.DiscoveredDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.UUID

class DeviceDiscovery(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val DISCOVERY_PORT = 8890
    private val myDeviceId = UUID.randomUUID().toString().take(8)
    val myDeviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private var broadcastJob: Job? = null
    private var listenJob: Job? = null
    private var socket: DatagramSocket? = null

    // Local device state for broadcast
    private var isHosting: Boolean = false
    private var hostedGameTitle: String? = null
    private var hostedGamePackage: String? = null
    private var activePlayersCount: Int = 0
    private var activeSpectatorsCount: Int = 0
    private var sessionStartTimeMs: Long = 0L

    fun setHostingState(
        hosting: Boolean,
        gameTitle: String? = null,
        gamePackage: String? = null,
        players: Int = 0,
        spectators: Int = 0,
        startTimeMs: Long = 0L
    ) {
        this.isHosting = hosting
        this.hostedGameTitle = gameTitle
        this.hostedGamePackage = gamePackage
        this.activePlayersCount = players
        this.activeSpectatorsCount = spectators
        this.sessionStartTimeMs = startTimeMs
    }

    fun startDiscovery() {
        if (socket != null && !socket!!.isClosed) return

        try {
            socket = DatagramSocket(DISCOVERY_PORT).apply {
                broadcast = true
                reuseAddress = true
            }
        } catch (e: Exception) {
            try {
                socket = DatagramSocket()
                socket?.broadcast = true
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }

        startListening()
        startBroadcasting()
    }

    private fun startListening() {
        listenJob?.cancel()
        listenJob = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(2048)
            val currentSocket = socket ?: return@launch

            while (isActive && !currentSocket.isClosed) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    currentSocket.receive(packet)
                    val message = String(packet.data, 0, packet.length)
                    val senderIp = packet.address.hostAddress ?: continue
                    handleBeaconMessage(message, senderIp)
                } catch (e: Exception) {
                    if (!isActive) break
                    delay(500)
                }
            }
        }
    }

    private fun startBroadcasting() {
        broadcastJob?.cancel()
        broadcastJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val elapsed = if (sessionStartTimeMs > 0) {
                        (System.currentTimeMillis() - sessionStartTimeMs) / 1000
                    } else 0L

                    val json = JSONObject().apply {
                        put("type", "POCKETLINK_BEACON")
                        put("id", myDeviceId)
                        put("name", myDeviceName)
                        put("port", 8888)
                        put("role", if (isHosting) "HOST" else "IDLE")
                        put("isHosting", isHosting)
                        hostedGameTitle?.let { put("gameTitle", it) }
                        hostedGamePackage?.let { put("gamePkg", it) }
                        put("players", activePlayersCount)
                        put("spectators", activeSpectatorsCount)
                        put("elapsed", elapsed)
                    }.toString()

                    val data = json.toByteArray()
                    val broadcastAddress = getBroadcastAddress()
                    val packet = DatagramPacket(data, data.size, broadcastAddress, DISCOVERY_PORT)
                    socket?.send(packet)
                } catch (e: Exception) {
                    // Ignore broadcast transient fails
                }

                // Clean up stale devices (> 8s without beacon)
                val now = System.currentTimeMillis()
                _discoveredDevices.value = _discoveredDevices.value.filter {
                    now - it.lastSeenTimestamp < 8000
                }

                delay(2000)
            }
        }
    }

    private fun handleBeaconMessage(msg: String, senderIp: String) {
        try {
            val obj = JSONObject(msg)
            if (obj.optString("type") != "POCKETLINK_BEACON") return
            val id = obj.getString("id")
            if (id == myDeviceId) return // Ignore own beacon

            val name = obj.getString("name")
            val port = obj.optInt("port", 8888)
            val role = obj.optString("role", "IDLE")
            val isHosting = obj.optBoolean("isHosting", false)
            val gameTitle = if (obj.has("gameTitle")) obj.getString("gameTitle") else null
            val gamePkg = if (obj.has("gamePkg")) obj.getString("gamePkg") else null
            val players = obj.optInt("players", 0)
            val spectators = obj.optInt("spectators", 0)
            val elapsed = obj.optLong("elapsed", 0)

            val device = DiscoveredDevice(
                deviceId = id,
                deviceName = name,
                ipAddress = senderIp,
                port = port,
                role = role,
                isHostingGame = isHosting,
                currentGameTitle = gameTitle,
                currentGamePackage = gamePkg,
                activePlayersCount = players,
                activeSpectatorsCount = spectators,
                sessionElapsedSec = elapsed,
                lastSeenTimestamp = System.currentTimeMillis()
            )

            val current = _discoveredDevices.value.toMutableList()
            val index = current.indexOfFirst { it.deviceId == id }
            if (index >= 0) {
                current[index] = device
            } else {
                current.add(device)
            }
            _discoveredDevices.value = current
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getBroadcastAddress(): InetAddress {
        return try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                for (interfaceAddress in networkInterface.interfaceAddresses) {
                    val broadcast = interfaceAddress.broadcast
                    if (broadcast != null) {
                        return broadcast
                    }
                }
            }
            InetAddress.getByName("255.255.255.255")
        } catch (e: Exception) {
            InetAddress.getByName("255.255.255.255")
        }
    }

    fun stopDiscovery() {
        broadcastJob?.cancel()
        listenJob?.cancel()
        try {
            socket?.close()
        } catch (e: Exception) {}
        socket = null
    }
}
