package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.NotificationHelper
import com.example.ui.screens.CirclesScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.StudyCircleTheme
import com.example.viewmodel.StudyTimerViewModel
import kotlinx.coroutines.launch

enum class AppNavDestination(val label: String) {
    TIMER("Timer"),
    STATS("Analytics"),
    CIRCLES("Circles"),
    SETTINGS("Settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyCircleApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyCircleApp(
    viewModel: StudyTimerViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val studyStats by viewModel.studyStats.collectAsStateWithLifecycle()
    val subjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(AppNavDestination.TIMER) }

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notifications enabled for study alerts!")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Handle back button on sub-screens
    BackHandler(enabled = currentScreen != AppNavDestination.TIMER) {
        currentScreen = AppNavDestination.TIMER
    }

    StudyCircleTheme(themeMode = userSettings.themeMode) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "StudyCircle",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        // Streak Badge
                        Surface(
                            color = Color(0xFFF97316).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "🔥", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${studyStats.currentStreakDays}d",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF97316)
                                )
                            }
                        }

                        // Theme Mode quick toggle
                        IconButton(
                            onClick = {
                                val nextTheme = when (userSettings.themeMode) {
                                    "DARK" -> "AMOLED"
                                    "AMOLED" -> "SUNSET"
                                    "SUNSET" -> "LIGHT"
                                    "LIGHT" -> "DARK"
                                    else -> "DARK"
                                }
                                viewModel.updateThemeMode(nextTheme)
                                coroutineScope.launch {
                                    val label = when (nextTheme) {
                                        "AMOLED" -> "AMOLED Pure Black"
                                        "SUNSET" -> "Sunset Warm"
                                        "LIGHT" -> "Crisp Light"
                                        else -> "Midnight Slate"
                                    }
                                    snackbarHostState.showSnackbar("Switched to $label theme")
                                }
                            },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brightness4,
                                contentDescription = "Toggle Night Theme",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppNavDestination.TIMER,
                        onClick = { currentScreen = AppNavDestination.TIMER },
                        icon = {
                            Icon(Icons.Default.Timer, contentDescription = "Timer")
                        },
                        label = { Text("Timer", fontSize = 11.sp) },
                        modifier = Modifier.testTag("tab_nav_timer"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavDestination.STATS,
                        onClick = { currentScreen = AppNavDestination.STATS },
                        icon = {
                            Icon(Icons.Default.BarChart, contentDescription = "Analytics")
                        },
                        label = { Text("Analytics", fontSize = 11.sp) },
                        modifier = Modifier.testTag("tab_nav_stats"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavDestination.CIRCLES,
                        onClick = { currentScreen = AppNavDestination.CIRCLES },
                        icon = {
                            Icon(Icons.Default.Groups, contentDescription = "Circles")
                        },
                        label = { Text("Circles", fontSize = 11.sp) },
                        modifier = Modifier.testTag("tab_nav_circles"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavDestination.SETTINGS,
                        onClick = { currentScreen = AppNavDestination.SETTINGS },
                        icon = {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        },
                        label = { Text("Settings", fontSize = 11.sp) },
                        modifier = Modifier.testTag("tab_nav_settings"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    AppNavDestination.TIMER -> {
                        TimerScreen(
                            timerState = timerState,
                            userSettings = userSettings,
                            subjects = subjects,
                            onStart = { viewModel.startTimer() },
                            onPause = { viewModel.pauseTimer() },
                            onResume = { viewModel.resumeTimer() },
                            onReset = { viewModel.resetTimer() },
                            onSkip = { viewModel.skipSession() },
                            onAddFiveMinutes = { viewModel.addFiveMinutes() },
                            onSelectMode = { viewModel.setSessionType(it) },
                            onSelectSubject = { viewModel.selectSubject(it) },
                            onAddSubject = { name, color -> viewModel.addSubject(name, color) },
                            onSelectAmbient = { viewModel.setAmbientSound(it) },
                            onSelectCircle = { name, count -> viewModel.setStudyCircle(name, count) },
                            onUpdateNotes = { viewModel.updateSessionNotes(it) }
                        )
                    }

                    AppNavDestination.STATS -> {
                        StatsScreen(
                            stats = studyStats,
                            sessions = sessions,
                            onDeleteSession = { viewModel.deleteSession(it) },
                            onClearAll = {
                                viewModel.clearAllData()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("All study logs cleared")
                                }
                            }
                        )
                    }

                    AppNavDestination.CIRCLES -> {
                        CirclesScreen(
                            currentCircle = timerState.currentCircle,
                            onSelectCircle = { name, count ->
                                viewModel.setStudyCircle(name, count)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Joined $name")
                                }
                            },
                            onNavigateToTimer = {
                                currentScreen = AppNavDestination.TIMER
                            }
                        )
                    }

                    AppNavDestination.SETTINGS -> {
                        SettingsScreen(
                            settings = userSettings,
                            subjects = subjects,
                            onUpdateIntervals = { focus, shortB, longB, cycles, goal ->
                                viewModel.updateIntervals(focus, shortB, longB, cycles, goal)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Interval settings saved")
                                }
                            },
                            onUpdateToggles = { sound, vib, autoB, autoF ->
                                viewModel.updateToggles(sound, vib, autoB, autoF)
                            },
                            onUpdateTheme = { viewModel.updateThemeMode(it) },
                            onDeleteSubject = { viewModel.deleteSubject(it) },
                            onTestNotification = {
                                val helper = NotificationHelper(context)
                                helper.showSessionCompleteAlert(
                                    isFocusSession = true,
                                    subject = timerState.selectedSubject.name,
                                    durationMinutes = userSettings.focusDurationMinutes
                                )
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Test alert notification triggered!")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
