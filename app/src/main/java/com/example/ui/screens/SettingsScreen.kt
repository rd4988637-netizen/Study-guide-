package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.StudySubject
import com.example.data.model.UserSettings
import com.example.ui.components.parseColor

@Composable
fun SettingsScreen(
    settings: UserSettings,
    subjects: List<StudySubject>,
    onUpdateIntervals: (focus: Int, shortB: Int, longB: Int, cycles: Int, dailyGoal: Int) -> Unit,
    onUpdateToggles: (sound: Boolean, vibration: Boolean, autoBreak: Boolean, autoFocus: Boolean) -> Unit,
    onUpdateTheme: (String) -> Unit,
    onDeleteSubject: (StudySubject) -> Unit,
    onTestNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focusMin by remember(settings.focusDurationMinutes) { mutableIntStateOf(settings.focusDurationMinutes) }
    var shortBreakMin by remember(settings.shortBreakMinutes) { mutableIntStateOf(settings.shortBreakMinutes) }
    var longBreakMin by remember(settings.longBreakMinutes) { mutableIntStateOf(settings.longBreakMinutes) }
    var cycles by remember(settings.sessionsUntilLongBreak) { mutableIntStateOf(settings.sessionsUntilLongBreak) }
    var dailyGoalMin by remember(settings.dailyGoalMinutes) { mutableIntStateOf(settings.dailyGoalMinutes) }

    fun commitIntervals() {
        onUpdateIntervals(focusMin, shortBreakMin, longBreakMin, cycles, dailyGoalMin)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = "Timer & Preferences",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Customize intervals, night mode, and notifications",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section 1: Dark Mode & Night Interface
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_selector_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "NIGHT STUDY INTERFACE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "Optimized color schemes for low-light night study sessions",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val themes = listOf(
                        Triple("DARK", "Midnight Slate 🌙", "Deep navy dark mode for eye comfort"),
                        Triple("AMOLED", "AMOLED Black 🌑", "Pure pitch black, zero screen glare"),
                        Triple("SUNSET", "Sunset Warm 🌆", "Warm tones for late night shift"),
                        Triple("LIGHT", "Crisp Light ☀️", "Daytime bright aesthetic"),
                        Triple("SYSTEM", "System Default ⚙️", "Follows device appearance")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        themes.forEach { (mode, label, desc) ->
                            val isSelected = settings.themeMode == mode
                            Surface(
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) {
                                    androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                } else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onUpdateTheme(mode) }
                                    .testTag("theme_option_$mode")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = label,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Interval Customization
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interval_customization_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "INTERVAL LENGTHS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "Fine-tune Pomodoro intervals to match your focus rhythm",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Focus Duration
                    StepperSettingRow(
                        label = "Focus Duration",
                        value = "${focusMin} min",
                        onDecrement = {
                            if (focusMin > 5) {
                                focusMin -= 5
                                commitIntervals()
                            }
                        },
                        onIncrement = {
                            if (focusMin < 120) {
                                focusMin += 5
                                commitIntervals()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Short Break Duration
                    StepperSettingRow(
                        label = "Short Break",
                        value = "${shortBreakMin} min",
                        onDecrement = {
                            if (shortBreakMin > 1) {
                                shortBreakMin -= 1
                                commitIntervals()
                            }
                        },
                        onIncrement = {
                            if (shortBreakMin < 30) {
                                shortBreakMin += 1
                                commitIntervals()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Long Break Duration
                    StepperSettingRow(
                        label = "Long Break",
                        value = "${longBreakMin} min",
                        onDecrement = {
                            if (longBreakMin > 5) {
                                longBreakMin -= 5
                                commitIntervals()
                            }
                        },
                        onIncrement = {
                            if (longBreakMin < 60) {
                                longBreakMin += 5
                                commitIntervals()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Long Break Cycles
                    StepperSettingRow(
                        label = "Long Break Interval",
                        value = "Every $cycles sessions",
                        onDecrement = {
                            if (cycles > 2) {
                                cycles -= 1
                                commitIntervals()
                            }
                        },
                        onIncrement = {
                            if (cycles < 8) {
                                cycles += 1
                                commitIntervals()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Target Goal
                    StepperSettingRow(
                        label = "Daily Focus Goal",
                        value = "${dailyGoalMin / 60}h ${if (dailyGoalMin % 60 > 0) "${dailyGoalMin % 60}m" else ""}".trim(),
                        onDecrement = {
                            if (dailyGoalMin > 30) {
                                dailyGoalMin -= 30
                                commitIntervals()
                            }
                        },
                        onIncrement = {
                            if (dailyGoalMin < 480) {
                                dailyGoalMin += 30
                                commitIntervals()
                            }
                        }
                    )
                }
            }
        }

        // Section 3: Sound & Local Notifications
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notifications_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "NOTIFICATIONS & SOUNDS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "Local alerts when focus sprints and breaks end",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ToggleSettingRow(
                        title = "Completion Chime Sound",
                        subtitle = "Harmonic bell chime when a session finishes",
                        checked = settings.soundEnabled,
                        onCheckedChange = {
                            onUpdateToggles(it, settings.vibrationEnabled, settings.autoStartBreaks, settings.autoStartFocus)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ToggleSettingRow(
                        title = "Vibration Alert",
                        subtitle = "Haptic pulse when session ends",
                        checked = settings.vibrationEnabled,
                        onCheckedChange = {
                            onUpdateToggles(settings.soundEnabled, it, settings.autoStartBreaks, settings.autoStartFocus)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ToggleSettingRow(
                        title = "Auto-Start Breaks",
                        subtitle = "Begin break countdown immediately after focus",
                        checked = settings.autoStartBreaks,
                        onCheckedChange = {
                            onUpdateToggles(settings.soundEnabled, settings.vibrationEnabled, it, settings.autoStartFocus)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ToggleSettingRow(
                        title = "Auto-Start Focus Rounds",
                        subtitle = "Begin next study sprint when break ends",
                        checked = settings.autoStartFocus,
                        onCheckedChange = {
                            onUpdateToggles(settings.soundEnabled, settings.vibrationEnabled, settings.autoStartBreaks, it)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onTestNotification,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("test_notification_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Test Notification",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Send Test Local Notification", fontSize = 13.sp)
                    }
                }
            }
        }

        // Section 4: Study Subjects Management
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subjects_management_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "MANAGE STUDY SUBJECTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "Active tags used for tracking subject analytics",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    subjects.forEach { subject ->
                        val color = parseColor(subject.colorHex)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = subject.name,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (subjects.size > 1) {
                                IconButton(
                                    onClick = { onDeleteSubject(subject) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Subject",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StepperSettingRow(
    label: String,
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(110.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF090D16),
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
