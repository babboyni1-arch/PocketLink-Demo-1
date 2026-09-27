package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.GameplaySessionEntity
import com.example.ui.components.LiveStreamView
import com.example.ui.components.SessionTimelineView
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import com.example.viewmodel.PocketLinkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameplayDetailScreen(
    session: GameplaySessionEntity,
    viewModel: PocketLinkViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val milestones = remember(session.sessionHighlightsJson) {
        viewModel.sessionRepo.parseMilestones(session.sessionHighlightsJson)
    }

    var isPlayingReplay by remember { mutableStateOf(false) }
    var scrubPositionSec by remember { mutableFloatStateOf(0f) }
    var userNotes by remember { mutableStateOf(session.notes) }
    var userRating by remember { mutableIntStateOf(session.rating) }

    val totalDuration = session.durationSeconds.toFloat().coerceAtLeast(1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = session.gameTitle.uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "Start-to-End Gameplay Record",
                            fontSize = 12.sp,
                            color = NeonCyan
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
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "🎮 PocketLink Gameplay Summary\nGame: ${session.gameTitle}\nHost: ${session.hostDeviceName}\nDuration: ${session.durationSeconds / 60}m ${session.durationSeconds % 60}s\nInputs: ${session.totalInputEvents}\nAvg Ping: ${session.avgLatencyMs}ms\nStreamed via PocketLink!"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Gameplay Summary"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = NeonCyan)
                    }
                    IconButton(onClick = {
                        viewModel.deleteSession(session.id)
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberBackground)
            )
        },
        containerColor = CyberBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Replay Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                LiveStreamView(
                    modifier = Modifier.fillMaxSize(),
                    gameTitle = session.gameTitle,
                    fps = session.avgFps,
                    latencyMs = session.avgLatencyMs,
                    bitrateMbps = 8.0,
                    spectatorCount = session.spectatorsCount
                )

                // Play / Pause overlay button
                IconButton(
                    onClick = { isPlayingReplay = !isPlayingReplay },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                ) {
                    Icon(
                        imageVector = if (isPlayingReplay) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Replay",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Scrubber Bar Overlay at Bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color(0xBB0F172A))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(scrubPositionSec.toLong()),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = scrubPositionSec,
                        onValueChange = { scrubPositionSec = it },
                        valueRange = 0f..totalDuration,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )
                    Text(
                        text = formatTime(totalDuration.toLong()),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Start-to-End Timeline View
            SessionTimelineView(
                session = session,
                milestones = milestones
            )

            // Rating & Notes Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SESSION REVIEW & NOTES",
                        color = CyberTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5-Star Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (star in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star $star",
                                tint = if (star <= userRating) NeonAmber else Color.DarkGray,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable {
                                        userRating = star
                                        viewModel.updateSessionNotes(session.id, userNotes, star)
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = userNotes,
                        onValueChange = {
                            userNotes = it
                            viewModel.updateSessionNotes(session.id, it, userRating)
                        },
                        placeholder = { Text("Add notes about this gameplay session...", color = CyberTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
