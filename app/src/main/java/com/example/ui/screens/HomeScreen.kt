package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.network.models.LiveGameUsageInfo
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import com.example.viewmodel.PocketLinkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PocketLinkViewModel,
    onNavigateToLibrary: () -> Unit,
    onNavigateToLiveWatch: () -> Unit,
    onNavigateToHost: () -> Unit,
    onNavigateToRemotePlay: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onWatchGame: (LiveGameUsageInfo) -> Unit
) {
    val liveGames by viewModel.activeLiveGames.collectAsStateWithLifecycle()
    val isHostRunning by viewModel.server.isServerRunning.collectAsStateWithLifecycle()
    val peers by viewModel.discoveredPeers.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showConnectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("PL", color = CyberBackground, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "POCKETLINK",
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                letterSpacing = 1.5.sp,
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "Two-Phone Remote Gameplay & Stream",
                                fontSize = 11.sp,
                                color = NeonCyan
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showConnectDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Pairing Code",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberBackground)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CyberBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device Status Header Pill
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.deviceDiscovery.myDeviceName,
                            color = CyberTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isHostRunning) "Hosting Active Session" else "Ready to Link / Host",
                            color = if (isHostRunning) NeonGreen else CyberTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x3306B6D4))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("LAN Discovery", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Live Game Usage Ticker ("A feature to watch if someone is using them")
            if (liveGames.isNotEmpty()) {
                val active = liveGames.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_game_banner"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1030)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonViolet)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                tint = NeonRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SOMEONE IS PLAYING A GAME NOW",
                                color = NeonRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎮 ${active.gameTitle}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${active.spectatorsCount} watching",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Hosted by ${active.hostDeviceName} • Controller: ${active.activeControllerName}",
                            color = CyberTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onWatchGame(active) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("quick_watch_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WATCH LIVE STREAM", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Section: Main Navigation Grid
            Text(
                text = "POCKETLINK FEATURES",
                color = CyberTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // 1. Put Games (Library)
            FeatureCard(
                title = "Put & Manage Games",
                subtitle = "Scan device or add custom games, configure resolution, FPS, and virtual controller mappings",
                badge = "LIBRARY",
                badgeColor = NeonCyan,
                icon = Icons.Default.SportsEsports,
                testTag = "card_put_games",
                onClick = onNavigateToLibrary
            )

            // 2. Watch if someone is using them
            FeatureCard(
                title = "Watch Live Games",
                subtitle = "See what games are actively running on paired phones, view live inputs, and spectate instantly",
                badge = if (liveGames.isNotEmpty()) "${liveGames.size} ACTIVE" else "MONITOR",
                badgeColor = if (liveGames.isNotEmpty()) NeonRed else NeonViolet,
                icon = Icons.Default.Visibility,
                testTag = "card_watch_live",
                onClick = onNavigateToLiveWatch
            )

            // 3. Host a Game
            FeatureCard(
                title = "Host Game Session",
                subtitle = "Run game on this phone, stream video, receive remote player controls, and record locally",
                badge = "HOST MODE",
                badgeColor = NeonGreen,
                icon = Icons.Default.PlayArrow,
                testTag = "card_host_game",
                onClick = onNavigateToHost
            )

            // 4. Remote Play / Controller
            FeatureCard(
                title = "Remote Play & Controller",
                subtitle = "Connect to another phone running a game, display live screen, and send virtual controls",
                badge = "CLIENT",
                badgeColor = NeonAmber,
                icon = Icons.Default.Gamepad,
                testTag = "card_remote_play",
                onClick = {
                    if (peers.isNotEmpty()) {
                        val hostPeer = peers.firstOrNull { it.isHostingGame } ?: peers.first()
                        viewModel.client.connect(
                            hostIp = hostPeer.ipAddress,
                            role = "PLAYER",
                            deviceName = viewModel.deviceDiscovery.myDeviceName
                        ) { success, _ ->
                            if (success) onNavigateToRemotePlay()
                        }
                    } else {
                        showConnectDialog = true
                    }
                }
            )

            // 5. Every Gameplay from Start to End (History & Timeline)
            FeatureCard(
                title = "Gameplay History (Start to End)",
                subtitle = "Full lifecycle logs, chronological milestone timelines, input stats, ratings, and replay player",
                badge = "HISTORY",
                badgeColor = NeonViolet,
                icon = Icons.Default.History,
                testTag = "card_gameplay_history",
                onClick = onNavigateToHistory
            )
        }
    }

    if (showConnectDialog) {
        DirectConnectDialog(
            peers = peers,
            onDismiss = { showConnectDialog = false },
            onConnect = { ip, role ->
                viewModel.client.connect(
                    hostIp = ip,
                    role = role,
                    deviceName = viewModel.deviceDiscovery.myDeviceName
                ) { success, _ ->
                    showConnectDialog = false
                    if (success) {
                        if (role == "PLAYER") onNavigateToRemotePlay() else onNavigateToLiveWatch()
                    }
                }
            }
        )
    }
}

@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor.copy(alpha = 0.2f))
                    .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = CyberTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = CyberTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun DirectConnectDialog(
    peers: List<com.example.network.models.DiscoveredDevice>,
    onDismiss: () -> Unit,
    onConnect: (String, String) -> Unit
) {
    var ipInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("PLAYER") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("CONNECT TO HOST PHONE", fontWeight = FontWeight.Black, color = CyberTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (peers.isNotEmpty()) {
                    Text("Discovered Nearby Phones:", color = CyberTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    peers.forEach { peer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurfaceVariant)
                                .clickable { ipInput = peer.ipAddress }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(peer.deviceName, color = CyberTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(peer.ipAddress, color = CyberTextMuted, fontSize = 11.sp)
                            }
                            if (peer.isHostingGame) {
                                Text("🎮 ${peer.currentGameTitle}", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    label = { Text("Host Phone IP (e.g. 192.168.1.15)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedRole = "PLAYER" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == "PLAYER") NeonCyan else CyberSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("As Player", color = if (selectedRole == "PLAYER") CyberBackground else CyberTextPrimary)
                    }

                    Button(
                        onClick = { selectedRole = "SPECTATOR" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == "SPECTATOR") NeonViolet else CyberSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("As Watcher", color = Color.White)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (ipInput.isNotBlank()) onConnect(ipInput.trim(), selectedRole) },
                enabled = ipInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("Connect", color = CyberBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CyberTextSecondary)
            }
        },
        containerColor = CyberSurfaceElevated
    )
}
