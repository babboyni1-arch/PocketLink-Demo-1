package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LiveStreamView
import com.example.ui.components.VirtualControllerOverlay
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.NeonCyan
import com.example.viewmodel.PocketLinkViewModel

@Composable
fun RemotePlayScreen(
    viewModel: PocketLinkViewModel,
    onDisconnect: () -> Unit
) {
    val activeGameTitle by viewModel.client.activeGameTitle.collectAsStateWithLifecycle()
    val ping by viewModel.client.currentPingMs.collectAsStateWithLifecycle()
    val fps by viewModel.client.currentFps.collectAsStateWithLifecycle()
    val bitrate by viewModel.client.currentBitrateMbps.collectAsStateWithLifecycle()

    var controllerOpacity by remember { mutableFloatStateOf(0.85f) }

    Scaffold(
        containerColor = CyberBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Live Stream View
            LiveStreamView(
                modifier = Modifier.fillMaxSize(),
                gameTitle = activeGameTitle ?: "Host Game",
                fps = fps,
                latencyMs = ping,
                bitrateMbps = bitrate,
                spectatorCount = 1
            )

            // Virtual Gamepad Overlay (Sends touch & button inputs back to Host Phone)
            VirtualControllerOverlay(
                modifier = Modifier.fillMaxSize(),
                opacity = controllerOpacity,
                onInputEvent = { inputEvent ->
                    viewModel.client.sendInput(inputEvent)
                }
            )

            // Top Quick Bar: Status & Disconnect
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xDD0F172A))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🎮 PLAYING: ${activeGameTitle ?: "Host"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.client.disconnect()
                        onDisconnect()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xDD0F172A))
                        .testTag("disconnect_play_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Disconnect",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
