package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.ScreenCaptureService
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
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
fun HostSessionScreen(
    viewModel: PocketLinkViewModel,
    onBack: () -> Unit,
    onSessionFinalized: () -> Unit
) {
    val context = LocalContext.current
    val game = viewModel.selectedHostGame.collectAsStateWithLifecycle().value
    val connectedPlayer by viewModel.server.connectedPlayerName.collectAsStateWithLifecycle()
    val spectatorCount by viewModel.server.spectatorCount.collectAsStateWithLifecycle()
    val latency by viewModel.server.currentLatencyMs.collectAsStateWithLifecycle()
    val fps by viewModel.server.currentFps.collectAsStateWithLifecycle()
    val elapsedSec by viewModel.activeSessionElapsedSec.collectAsStateWithLifecycle()
    val totalInputs by viewModel.server.totalInputsReceived.collectAsStateWithLifecycle()
    val isSessionActive = viewModel.activeSessionId.collectAsStateWithLifecycle().value != null

    var recordGameplay by remember { mutableStateOf(true) }
    var isPaused by remember { mutableStateOf(false) }

    // Screen Capture MediaProjection launcher
    val projectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(context, ScreenCaptureService::class.java).apply {
                putExtra("code", result.resultCode)
                putExtra("data", result.data)
            }
            context.startForegroundService(serviceIntent)
            viewModel.startHostGameplaySession(recordGameplay) {
                // Session started
            }
        } else {
            // Permission denied or cancelled, start session without physical capture
            viewModel.startHostGameplaySession(false) {
                // Session started
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "POCKETLINK HOST",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = if (isSessionActive) "Session running" else "Ready to host",
                            fontSize = 12.sp,
                            color = if (isSessionActive) NeonGreen else NeonCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberBackground
                )
            )
        },
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
            // Game Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x3306B6D4))
                            .border(1.5.dp, NeonCyan, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎮", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = game?.title ?: "Select a Game",
                            color = CyberTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = game?.packageName ?: "No package selected",
                            color = CyberTextMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Profile: ${game?.targetResolution} • ${game?.targetFps} FPS • ${game?.preferredControllerLayout} Controls",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Connection Status & Player Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSessionActive) NeonGreen.copy(alpha = 0.5f) else CyberBorder
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = null,
                            tint = if (isSessionActive) NeonGreen else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSessionActive) "POCKETLINK CONNECTED" else "HOST OFFLINE",
                            color = if (isSessionActive) NeonGreen else Color.Gray,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        if (isSessionActive) {
                            Text(
                                text = formatTime(elapsedSec),
                                color = NeonAmber,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Player & Spectators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberSurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("REMOTE PLAYER", color = CyberTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = connectedPlayer ?: "Waiting for player...",
                                    color = if (connectedPlayer != null) NeonCyan else Color.LightGray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberSurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("SPECTATORS", color = CyberTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$spectatorCount watching live",
                                    color = NeonViolet,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Stream & Recording Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SESSION CAPTURE & RECORDING",
                        color = CyberTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Record Gameplay to Storage", color = CyberTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Save MP4 recording from start to end", color = CyberTextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = recordGameplay,
                            onCheckedChange = { recordGameplay = it },
                            enabled = !isSessionActive,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = Color(0x3306B6D4)
                            )
                        )
                    }
                }
            }

            // Live Performance Metrics (When session is active)
            if (isSessionActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(title = "LATENCY", value = "${latency}ms", color = NeonGreen, modifier = Modifier.weight(1f))
                    MetricBox(title = "FPS", value = "$fps", color = NeonCyan, modifier = Modifier.weight(1f))
                    MetricBox(title = "INPUTS", value = "$totalInputs", color = NeonAmber, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Primary Action Buttons
            if (!isSessionActive) {
                Button(
                    onClick = {
                        val mgr = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                        projectionLauncher.launch(mgr.createScreenCaptureIntent())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_session_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START HOST SESSION",
                        color = CyberBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { isPaused = !isPaused },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            tint = NeonAmber
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPaused) "RESUME" else "PAUSE", color = NeonAmber, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.endCurrentGameplaySession()
                            onSessionFinalized()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("stop_session_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("STOP SESSION", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = CyberTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
    }
}

private fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
