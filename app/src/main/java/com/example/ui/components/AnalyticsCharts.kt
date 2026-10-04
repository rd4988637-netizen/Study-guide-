package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DayStudyData
import com.example.viewmodel.StudyStats
import com.example.viewmodel.SubjectStudyStat

@Composable
fun SummaryMetricCards(
    stats: StudyStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Today's Goal Progress Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("today_goal_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S FOCUS GOAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${stats.todayMinutes}m / ${stats.todayGoalMinutes}m",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${(stats.todayGoalPercent * 100).toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { stats.todayGoalPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }
        }

        // 3-Column Quick Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Streak
            StatTile(
                title = "Streak",
                value = "${stats.currentStreakDays} Days",
                icon = "🔥",
                accentColor = Color(0xFFF97316),
                modifier = Modifier.weight(1f)
            )

            // Sessions
            StatTile(
                title = "Sessions",
                value = "${stats.completedSessionsToday} Done",
                icon = "🎯",
                accentColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )

            // Total Hours
            StatTile(
                title = "All-Time",
                value = String.format("%.1fh", stats.totalFocusHoursAllTime),
                icon = "⏳",
                accentColor = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatTile(
    title: String,
    value: String,
    icon: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = icon, fontSize = 18.sp)
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeeklyBarChart(
    days: List<DayStudyData>,
    dailyGoalMinutes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_bar_chart")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LAST 7 DAYS ACTIVITY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "Daily focus minutes comparison",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Goal: ${dailyGoalMinutes}m",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxMinutes = (days.maxOfOrNull { it.minutesStudied } ?: 0)
                .coerceAtLeast(dailyGoalMinutes)
                .coerceAtLeast(60)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val chartHeight = size.height - 30.dp.toPx()
                    val chartWidth = size.width
                    val barCount = days.size.coerceAtLeast(1)
                    val slotWidth = chartWidth / barCount
                    val barWidth = slotWidth * 0.45f

                    // Draw dashed goal line
                    val goalY = chartHeight - (dailyGoalMinutes.toFloat() / maxMinutes * chartHeight)
                    if (goalY in 0f..chartHeight) {
                        drawLine(
                            color = Color(0xFFFBBF24).copy(alpha = 0.45f),
                            start = Offset(0f, goalY),
                            end = Offset(chartWidth, goalY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )
                    }

                    // Draw each day's bar
                    days.forEachIndexed { index, day ->
                        val centerX = slotWidth * index + slotWidth / 2f
                        val barHeight = (day.minutesStudied.toFloat() / maxMinutes * chartHeight).coerceAtLeast(4.dp.toPx())
                        val topY = chartHeight - barHeight
                        val leftX = centerX - barWidth / 2f

                        val barBrush = if (day.isToday) {
                            Brush.verticalGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color(0xFF818CF8), Color(0xFF4F46E5).copy(alpha = 0.6f))
                            )
                        }

                        // Background pillar track
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.05f),
                            topLeft = Offset(leftX, 0f),
                            size = Size(barWidth, chartHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )

                        // Filled bar
                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(leftX, topY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }
                }

                // Day Labels underneath
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    days.forEach { day ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (day.minutesStudied > 0) "${day.minutesStudied}m" else "-",
                                fontSize = 9.sp,
                                color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (day.isToday) "Today" else day.dayLabel,
                                fontSize = 11.sp,
                                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                                color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectDonutChart(
    breakdown: List<SubjectStudyStat>,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_donut_chart")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "SUBJECT DISTRIBUTION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.1.sp
            )
            Text(
                text = "Where your study hours went",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (breakdown.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Complete your first focus session to see subject breakdown!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Canvas
                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                            val strokeWidth = 14.dp.toPx()
                            val diameter = size.minDimension - strokeWidth
                            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                            val arcSize = Size(diameter, diameter)

                            var currentStartAngle = -90f

                            breakdown.forEach { item ->
                                val sweep = (item.percentage * 360f).coerceAtLeast(3f)
                                val color = parseColor(item.colorHex)

                                drawArc(
                                    color = color,
                                    startAngle = currentStartAngle,
                                    sweepAngle = sweep - 2f, // slight gap
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                currentStartAngle += sweep
                            }
                        }

                        // Center total
                        val totalMin = breakdown.sumOf { it.totalMinutes }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${totalMin}m",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Legend Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        breakdown.take(4).forEach { item ->
                            val color = parseColor(item.colorHex)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.subjectName,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = "${item.totalMinutes}m (${(item.percentage * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyRhythmChart(
    hourly: List<Int>,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("hourly_rhythm_chart")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "PRODUCTIVITY RHYTHM",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.1.sp
            )
            Text(
                text = "Time of day focus density (24h)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            val maxHourVal = (hourly.maxOrNull() ?: 0).coerceAtLeast(1)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = size.width / 24f - 2.dp.toPx()
                    hourly.forEachIndexed { hour, count ->
                        val x = hour * (size.width / 24f)
                        val h = if (count > 0) {
                            (count.toFloat() / maxHourVal * size.height).coerceAtLeast(4.dp.toPx())
                        } else {
                            2.dp.toPx()
                        }
                        val y = size.height - h
                        val barColor = if (count > 0) {
                            Color(0xFF38BDF8)
                        } else {
                            Color.White.copy(alpha = 0.08f)
                        }

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "12 AM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "6 AM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "12 PM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "6 PM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "11 PM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color(0xFF38BDF8)
    }
}
