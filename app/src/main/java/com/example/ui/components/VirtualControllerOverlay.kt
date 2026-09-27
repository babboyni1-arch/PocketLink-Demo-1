package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.models.ControllerInputEvent
import com.example.ui.theme.ControllerButtonA
import com.example.ui.theme.ControllerButtonB
import com.example.ui.theme.ControllerButtonX
import com.example.ui.theme.ControllerButtonY
import com.example.ui.theme.NeonCyan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualControllerOverlay(
    modifier: Modifier = Modifier,
    opacity: Float = 0.85f,
    onInputEvent: (ControllerInputEvent) -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun vibrateShort() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(18)
            }
        } catch (e: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(opacity)
    ) {
        // Top Shoulder Buttons (L1, R1)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShoulderButton(
                label = "L1",
                onPress = {
                    vibrateShort()
                    onInputEvent(ControllerInputEvent(actionType = "BUTTON_DOWN", buttonId = "L1"))
                },
                onRelease = {
                    onInputEvent(ControllerInputEvent(actionType = "BUTTON_UP", buttonId = "L1"))
                }
            )

            ShoulderButton(
                label = "R1",
                onPress = {
                    vibrateShort()
                    onInputEvent(ControllerInputEvent(actionType = "BUTTON_DOWN", buttonId = "R1"))
                },
                onRelease = {
                    onInputEvent(ControllerInputEvent(actionType = "BUTTON_UP", buttonId = "R1"))
                }
            )
        }

        // Bottom Left: Virtual Analog Joystick
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 28.dp)
        ) {
            VirtualJoystick(
                onJoystickMoved = { x, y ->
                    onInputEvent(
                        ControllerInputEvent(
                            actionType = "JOYSTICK_MOVE",
                            joystickX = x,
                            joystickY = y
                        )
                    )
                }
            )
        }

        // Bottom Right: Action Diamond Buttons (A, B, X, Y)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .size(170.dp)
        ) {
            // Y Button (Top)
            ActionButton(
                label = "Y",
                color = ControllerButtonY,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .testTag("button_y"),
                onPress = {
                    vibrateShort()
                    onInputEvent(ControllerInputEvent(actionType = "BUTTON_DOWN", buttonId = "Y"))
                },
                onRelease = {
                    onInputEvent(ControllerInputEvent(actionType = "BUTTON_UP", buttonId = "Y"))
                }
            )

            // A Button (Bottom)
            ActionButton(
                label = "A",
                color = ControllerButtonA,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .testTag("button_a"),
                onPress = {
                    vibrateShort()
                    onInputEvent(ControllerInputEvent(actionType = "BUTTON_DOWN", buttonId = "A"))
                },
                onRelease = {
                    onInputEvent(ControllerInputEvent(actionType = "BUTTON_UP", buttonId = "A"))
                }
            )

            // X Button (Left)
            ActionButton(
                label = "X",
                color = ControllerButtonX,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .testTag("button_x"),
                onPress = {
                    vibrateShort()
                    onInputEvent(ControllerInputEvent(actionType = "BUTTON_DOWN", buttonId = "X"))
                },
                onRelease = {
                    onInputEvent(ControllerInputEvent(actionType = "BUTTON_UP", buttonId = "X"))
                }
            )

            // B Button (Right)
            ActionButton(
                label = "B",
                color = ControllerButtonB,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .testTag("button_b"),
                onPress = {
                    vibrateShort()
                    onInputEvent(ControllerInputEvent(actionType = "BUTTON_DOWN", buttonId = "B"))
                },
                onRelease = {
                    onInputEvent(ControllerInputEvent(actionType = "BUTTON_UP", buttonId = "B"))
                }
            )
        }
    }
}

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    onJoystickMoved: (Float, Float) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val maxRadius = 60f

    Box(
        modifier = modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(Color(0x550F172A))
            .border(2.dp, NeonCyan.copy(alpha = 0.5f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        offsetX = 0f
                        offsetY = 0f
                        onJoystickMoved(0f, 0f)
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                        onJoystickMoved(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = offsetX + dragAmount.x
                        val newY = offsetY + dragAmount.y
                        val dist = sqrt(newX * newX + newY * newY)

                        if (dist <= maxRadius) {
                            offsetX = newX
                            offsetY = newY
                        } else {
                            val angle = atan2(newY, newX)
                            offsetX = cos(angle) * maxRadius
                            offsetY = sin(angle) * maxRadius
                        }

                        val normX = (offsetX / maxRadius).coerceIn(-1f, 1f)
                        val normY = (offsetY / maxRadius).coerceIn(-1f, 1f)
                        onJoystickMoved(normX, normY)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner thumb stick
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(54.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.85f))
                .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
            )
        }
    }
}

@Composable
fun ActionButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(if (isPressed) color else color.copy(alpha = 0.35f))
            .border(2.dp, color, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPress()
                        val released = tryAwaitRelease()
                        isPressed = false
                        onRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun ShoulderButton(
    label: String,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(84.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPressed) NeonCyan else Color(0x661E293B))
            .border(1.5.dp, NeonCyan.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPress()
                        tryAwaitRelease()
                        isPressed = false
                        onRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isPressed) Color(0xFF0B0F19) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}
