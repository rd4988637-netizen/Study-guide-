package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SessionType
import com.example.viewmodel.formatMinutesSeconds
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularTimerView(
    sessionType: SessionType,
    secondsRemaining: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    completedCycles: Int,
    totalCyclesBeforeLongBreak: Int,
    subjectName: String,
    subjectColor: Color,
    onAddFiveMinutes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) {
        (totalSeconds - secondsRemaining).toFloat() / totalSeconds.toFloat()
    } else 0f

    // Color gradient based on mode
    val (primaryGlow, secondaryGlow) = when (sessionType) {
        SessionType.FOCUS -> Pair(Color(0xFF38BDF8), Color(0xFF06B6D4))
        SessionType.SHORT_BREAK -> Pair(Color(0xFF34D399), Color(0xFF10B981))
        SessionType.LONG_BREAK -> Pair(Color(0xFFFBBF24), Color(0xFFF97316))
    }

    // Pulse animation when running
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .size(310.dp)
            .testTag("circular_timer_box"),
        contentAlignment = Alignment.Center
    ) {
        // Custom Canvas for the Circular Ring and Ticks
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth - 16.dp.toPx()
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val arcTopLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(diameter, diameter)

            // Background Track Ring
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Outer Tick Marks (60 ticks for seconds / clock feeling)
            val tickRadius = radius + strokeWidth / 2f + 8.dp.toPx()
            for (i in 0 until 60) {
                val angleRad = Math.toRadians((i * 6 - 90).toDouble())
                val isMajor = i % 5 == 0
                val tickLen = if (isMajor) 7.dp.toPx() else 3.5.dp.toPx()
                val tickAlpha = if (isMajor) 0.35f else 0.12f

                val startX = (center.x + (tickRadius) * cos(angleRad)).toFloat()
                val startY = (center.y + (tickRadius) * sin(angleRad)).toFloat()
                val endX = (center.x + (tickRadius + tickLen) * cos(angleRad)).toFloat()
                val endY = (center.y + (tickRadius + tickLen) * sin(angleRad)).toFloat()

                drawLine(
                    color = Color.White.copy(alpha = tickAlpha),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Animated Pulse Glow (when active)
            if (isRunning) {
                drawArc(
                    color = primaryGlow.copy(alpha = pulseAlpha * 0.45f),
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth + 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Foreground Active Progress Arc
            val sweep = 360f * progress.coerceIn(0.001f, 1f)
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(secondaryGlow, primaryGlow, secondaryGlow)
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Current Progress Head Dot
            val endAngleRad = Math.toRadians((sweep - 90).toDouble())
            val headX = (center.x + radius * cos(endAngleRad)).toFloat()
            val headY = (center.y + radius * sin(endAngleRad)).toFloat()

            drawCircle(
                color = Color.White,
                radius = strokeWidth * 0.45f,
                center = Offset(headX, headY)
            )
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Mode Badge
            Surface(
                color = primaryGlow.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = when (sessionType) {
                        SessionType.FOCUS -> "FOCUS SPRINT"
                        SessionType.SHORT_BREAK -> "SHORT BREAK"
                        SessionType.LONG_BREAK -> "DEEP RECHARGE"
                    },
                    color = primaryGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Digital Clock
            Text(
                text = formatMinutesSeconds(secondsRemaining),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-1).sp
            )

            // Subject Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(subjectColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = subjectName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cycle progress indicators (e.g. 4 dots for pomodoro cycles)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (cycle in 0 until totalCyclesBeforeLongBreak) {
                    val isCompleted = cycle < completedCycles
                    val isCurrent = cycle == completedCycles && sessionType == SessionType.FOCUS
                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> primaryGlow
                                    isCurrent -> primaryGlow.copy(alpha = 0.5f)
                                    else -> Color.White.copy(alpha = 0.15f)
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick "+5m" boost button
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAddFiveMinutes() }
                    .testTag("add_5_minutes_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add 5 Minutes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "5 min",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
