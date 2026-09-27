package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameplaySessionEntity
import com.example.data.model.TimelineMilestone
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

@Composable
fun SessionTimelineView(
    session: GameplaySessionEntity,
    milestones: List<TimelineMilestone>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Timeline Header: Start to End Summary
        Text(
            text = "GAMEPLAY LIFECYCLE TIMELINE",
            color = NeonCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Chronological record from Start to End",
            color = CyberTextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Performance Stat Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LifecycleMetricCard(
                modifier = Modifier.weight(1f),
                title = "DURATION",
                value = formatDuration(session.durationSeconds),
                color = NeonCyan
            )
            LifecycleMetricCard(
                modifier = Modifier.weight(1f),
                title = "INPUTS",
                value = "${session.totalInputEvents}",
                color = NeonGreen
            )
            LifecycleMetricCard(
                modifier = Modifier.weight(1f),
                title = "AVG PING",
                value = "${session.avgLatencyMs}ms",
                color = NeonAmber
            )
            LifecycleMetricCard(
                modifier = Modifier.weight(1f),
                title = "AVG FPS",
                value = "${session.avgFps}",
                color = NeonViolet
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Milestone Timeline
        Text(
            text = "EVENT LOG (${milestones.size} MILESTONES)",
            color = CyberTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        milestones.forEachIndexed { index, milestone ->
            val isFirst = index == 0
            val isLast = index == milestones.lastIndex

            TimelineItemRow(
                milestone = milestone,
                isFirst = isFirst,
                isLast = isLast
            )
        }
    }
}

@Composable
private fun TimelineItemRow(
    milestone: TimelineMilestone,
    isFirst: Boolean,
    isLast: Boolean
) {
    val (icon, color) = when (milestone.eventType) {
        "SESSION_START" -> Pair(Icons.Default.PlayArrow, NeonGreen)
        "CONTROLLER_SYNC" -> Pair(Icons.Default.SportsEsports, NeonCyan)
        "RECORDING_START" -> Pair(Icons.Default.FiberManualRecord, NeonRed)
        "PING_SPIKE" -> Pair(Icons.Default.Bolt, NeonAmber)
        "SESSION_END" -> Pair(Icons.Default.Flag, NeonViolet)
        else -> Pair(Icons.Default.CheckCircle, NeonCyan)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timestamp badge
        Box(
            modifier = Modifier
                .width(52.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x331E293B))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = milestone.formattedTime,
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Vertical timeline bar & dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f))
                    .border(1.5.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(11.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(Color(0x3338BDF8))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Description Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 6.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = milestone.description,
                    color = CyberTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                if (milestone.latencyMs != null || milestone.fps != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        milestone.latencyMs?.let {
                            Text(
                                text = "Ping: ${it}ms",
                                color = NeonAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        milestone.fps?.let {
                            Text(
                                text = "FPS: $it",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LifecycleMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = CyberTextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
