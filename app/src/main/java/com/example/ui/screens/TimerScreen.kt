package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.model.SessionType
import com.example.data.model.StudySubject
import com.example.data.model.UserSettings
import com.example.service.AmbientSoundType
import com.example.ui.components.AmbientSoundBar
import com.example.ui.components.CircularTimerView
import com.example.ui.components.StudyCircleBanner
import com.example.ui.components.SubjectSelectorBar
import com.example.ui.components.parseColor
import com.example.viewmodel.TimerUiState

@Composable
fun TimerScreen(
    timerState: TimerUiState,
    userSettings: UserSettings,
    subjects: List<StudySubject>,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    onAddFiveMinutes: () -> Unit,
    onSelectMode: (SessionType) -> Unit,
    onSelectSubject: (StudySubject) -> Unit,
    onAddSubject: (String, String) -> Unit,
    onSelectAmbient: (AmbientSoundType) -> Unit,
    onSelectCircle: (String, Int) -> Unit,
    onUpdateNotes: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val subjectColor = parseColor(timerState.selectedSubject.colorHex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Study Circle banner
        StudyCircleBanner(
            currentCircle = timerState.currentCircle,
            peerCount = timerState.activePeersCount,
            onSelectCircle = onSelectCircle,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Session Mode Selector Tabs (Focus, Short Break, Long Break)
        val selectedTabIndex = when (timerState.sessionType) {
            SessionType.FOCUS -> 0
            SessionType.SHORT_BREAK -> 1
            SessionType.LONG_BREAK -> 2
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .testTag("mode_tab_row"),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = when (timerState.sessionType) {
                        SessionType.FOCUS -> MaterialTheme.colorScheme.primary
                        SessionType.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
                        SessionType.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
                    },
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { onSelectMode(SessionType.FOCUS) },
                text = {
                    Text(
                        "Focus (${userSettings.focusDurationMinutes}m)",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("tab_focus")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { onSelectMode(SessionType.SHORT_BREAK) },
                text = {
                    Text(
                        "Short (${userSettings.shortBreakMinutes}m)",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("tab_short_break")
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { onSelectMode(SessionType.LONG_BREAK) },
                text = {
                    Text(
                        "Long (${userSettings.longBreakMinutes}m)",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("tab_long_break")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Circular Timer Canvas
        CircularTimerView(
            sessionType = timerState.sessionType,
            secondsRemaining = timerState.secondsRemaining,
            totalSeconds = timerState.totalSeconds,
            isRunning = timerState.isRunning,
            completedCycles = timerState.completedCycles,
            totalCyclesBeforeLongBreak = userSettings.sessionsUntilLongBreak,
            subjectName = timerState.selectedSubject.name,
            subjectColor = subjectColor,
            onAddFiveMinutes = onAddFiveMinutes
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Timer Primary Controls Row (Start / Pause / Resume / Skip / Reset)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            FilledTonalIconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("timer_reset_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Main Action Button (Start / Pause / Resume)
            val buttonColor = when (timerState.sessionType) {
                SessionType.FOCUS -> MaterialTheme.colorScheme.primary
                SessionType.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
                SessionType.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
            }

            Button(
                onClick = {
                    when {
                        timerState.isRunning -> onPause()
                        timerState.isPaused -> onResume()
                        else -> onStart()
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .width(180.dp)
                    .testTag("timer_primary_action_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color(0xFF090D16)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timerState.isRunning) "Pause" else "Start",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            timerState.isRunning -> "PAUSE"
                            timerState.isPaused -> "RESUME"
                            timerState.sessionType == SessionType.FOCUS -> "START FOCUS"
                            else -> "START BREAK"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Skip Button
            FilledTonalIconButton(
                onClick = onSkip,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("timer_skip_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip Session",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Ambient Sound Controls
        AmbientSoundBar(
            selectedSound = timerState.activeAmbient,
            onSelectSound = onSelectAmbient,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Subject selector horizontal chips
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "STUDY SUBJECT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
            SubjectSelectorBar(
                subjects = subjects,
                selectedSubject = timerState.selectedSubject,
                onSelectSubject = onSelectSubject,
                onAddSubject = onAddSubject
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Session Goal / Notes TextField
        OutlinedTextField(
            value = timerState.sessionNotes,
            onValueChange = onUpdateNotes,
            label = { Text("Session Goal (e.g. Chapter 4 Exercises)") },
            placeholder = { Text("What are you focusing on right now?") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("session_goal_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        )
    }
}
