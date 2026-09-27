package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.models.ControllerInputEvent
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet

@Composable
fun LiveStreamView(
    modifier: Modifier = Modifier,
    gameTitle: String,
    fps: Int = 60,
    latencyMs: Int = 24,
    bitrateMbps: Double = 8.5,
    spectatorCount: Int = 0,
    activeInput: ControllerInputEvent? = null,
    onToggleFullscreen: () -> Unit = {}
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val gridOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B12))
    ) {
        // High fidelity simulated dynamic gameplay stream canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Cyber grid lines
            val step = 60f
            var x = (gridOffset % step)
            while (x < width) {
                drawLine(
                    color = Color(0x1538BDF8),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += step
            }
            var y = (gridOffset % step)
            while (y < height) {
                drawLine(
                    color = Color(0x1538BDF8),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += step
            }

            // Radial game horizon glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonViolet.copy(alpha = 0.25f),
                        NeonCyan.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(width / 2f, height / 2f),
                    radius = width.coerceAtLeast(height) * 0.4f
                )
            )

            // Dynamic scan line
            val scanY = (gridOffset * 8) % height
            drawLine(
                color = NeonCyan.copy(alpha = 0.35f),
                start = Offset(0f, scanY),
                end = Offset(width, scanY),
                strokeWidth = 2f
            )
        }

        // Center Game Badge watermark
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0x3306B6D4))
                    .border(2.dp, NeonCyan.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎮",
                    fontSize = 34.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = gameTitle.uppercase(),
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontSize = 17.sp
            )
            Text(
                text = "POCKETLINK ULTRA STREAM • 60 FPS",
                color = NeonCyan.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Top Stream HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live indicator & game title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xBB0F172A))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = "Live",
                    tint = NeonRed.copy(alpha = pulseAlpha),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
                if (spectatorCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Spectators",
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$spectatorCount",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Stream Diagnostics (Ping, FPS, Bitrate)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge(
                    label = "PING",
                    value = "${latencyMs}ms",
                    color = if (latencyMs < 40) NeonGreen else Color(0xFFF59E0B)
                )
                StatBadge(
                    label = "FPS",
                    value = "$fps",
                    color = NeonCyan
                )
                StatBadge(
                    label = "RATE",
                    value = "${String.format("%.1f", bitrateMbps)}M",
                    color = NeonViolet
                )

                IconButton(
                    onClick = onToggleFullscreen,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x990F172A))
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Bottom Left: Real-time Remote Controller Input Monitor
        activeInput?.let { input ->
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC0B0F19))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONTROLLER:",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        input.buttonId != null -> "Button [${input.buttonId}]"
                        input.actionType == "JOYSTICK_MOVE" -> "Joy (${String.format("%.1f", input.joystickX)}, ${String.format("%.1f", input.joystickY)})"
                        else -> input.actionType
                    },
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xBB0F172A))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label ",
            color = Color.LightGray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
