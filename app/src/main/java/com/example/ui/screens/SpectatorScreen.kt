package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import com.example.viewmodel.PocketLinkViewModel

@Composable
fun SpectatorScreen(
    viewModel: PocketLinkViewModel,
    onExitWatch: () -> Unit
) {
    val targetGame by viewModel.spectatorTargetGame.collectAsStateWithLifecycle()
    val ping by viewModel.client.currentPingMs.collectAsStateWithLifecycle()
    val fps by viewModel.client.currentFps.collectAsStateWithLifecycle()
    val bitrate by viewModel.client.currentBitrateMbps.collectAsStateWithLifecycle()
    val emotes by viewModel.spectatorEmotesFeed.collectAsStateWithLifecycle()
    val liveInput by viewModel.liveActiveInputEvent.collectAsStateWithLifecycle()

    var isFullscreen by remember { mutableStateOf(false) }

    val reactionOptions = listOf("🔥", "👏", "🎮", "⚡", "😱", "🏆", "❤️")

    Scaffold(
        containerColor = CyberBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Live Stream Video Viewport
            LiveStreamView(
                modifier = Modifier.fillMaxSize(),
                gameTitle = targetGame?.gameTitle ?: "PocketLink Game",
                fps = fps,
                latencyMs = ping,
                bitrateMbps = bitrate,
                spectatorCount = (targetGame?.spectatorsCount ?: 1).coerceAtLeast(1),
                activeInput = liveInput,
                onToggleFullscreen = { isFullscreen = !isFullscreen }
            )

            // Top Bar: Exit button & Watcher header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WATCHING: ${targetGame?.gameTitle ?: "Game"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.client.disconnect()
                        onExitWatch()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xDD0F172A))
                        .testTag("exit_watch_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Watch",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom Floating Emote Reaction Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                // Emote Feed ticker
                if (emotes.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xAA0B0F19))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${emotes.first().senderName}: ${emotes.first().emote}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick Emote Send Bar
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD111827)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HYPE:",
                            color = NeonViolet,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        reactionOptions.forEach { emote ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x331E293B))
                                    .clickable { viewModel.sendSpectatorEmote(emote) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emote, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
