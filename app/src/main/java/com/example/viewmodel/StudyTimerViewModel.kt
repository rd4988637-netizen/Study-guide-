package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.SessionType
import com.example.data.model.StudySession
import com.example.data.model.StudySubject
import com.example.data.model.UserSettings
import com.example.data.repository.StudyRepository
import com.example.service.AmbientSoundType
import com.example.service.NotificationHelper
import com.example.service.SoundPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class TimerUiState(
    val sessionType: SessionType = SessionType.FOCUS,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val secondsRemaining: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val completedCycles: Int = 0,
    val selectedSubject: StudySubject = StudySubject(
        id = 1,
        name = "Computer Science",
        colorHex = "#06B6D4",
        iconName = "code"
    ),
    val sessionNotes: String = "",
    val activeAmbient: AmbientSoundType = AmbientSoundType.NONE,
    val currentCircle: String = "Late Night Study Hall 🌙",
    val activePeersCount: Int = 142
)

data class DayStudyData(
    val dayLabel: String,       // "Mon", "Tue", etc.
    val dateKey: String,        // "2026-10-04"
    val minutesStudied: Int,
    val isToday: Boolean = false
)

data class SubjectStudyStat(
    val subjectName: String,
    val colorHex: String,
    val totalMinutes: Int,
    val percentage: Float
)

data class StudyStats(
    val todayMinutes: Int = 0,
    val todayGoalMinutes: Int = 120,
    val todayGoalPercent: Float = 0f,
    val completedSessionsToday: Int = 0,
    val currentStreakDays: Int = 0,
    val totalFocusHoursAllTime: Float = 0f,
    val totalSessionsAllTime: Int = 0,
    val last7DaysData: List<DayStudyData> = emptyList(),
    val subjectBreakdown: List<SubjectStudyStat> = emptyList(),
    val hourlyActivity: List<Int> = List(24) { 0 }
)

class StudyTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository
    private val notificationHelper = NotificationHelper(application)
    private val soundPlayer = SoundPlayer()

    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTimeMs: Long = 0L
    private var targetEndTimeMs: Long = 0L

    init {
        val db = AppDatabase.getInstance(application)
        repository = StudyRepository(db.studyDao())
    }

    val userSettings: StateFlow<UserSettings> = repository.userSettings
        .map { it ?: UserSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserSettings()
        )

    val allSubjects: StateFlow<List<StudySubject>> = repository.allSubjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(
                StudySubject(id = 1, name = "Computer Science", colorHex = "#06B6D4", iconName = "code"),
                StudySubject(id = 2, name = "Mathematics", colorHex = "#8B5CF6", iconName = "calculate"),
                StudySubject(id = 3, name = "Literature & Writing", colorHex = "#F59E0B", iconName = "book"),
                StudySubject(id = 4, name = "Languages", colorHex = "#10B981", iconName = "translate"),
                StudySubject(id = 5, name = "Deep Focus Hub", colorHex = "#EC4899", iconName = "psychology")
            )
        )

    val allSessions: StateFlow<List<StudySession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Compute comprehensive statistics
    val studyStats: StateFlow<StudyStats> = combine(
        allSessions,
        userSettings
    ) { sessions, settings ->
        calculateStats(sessions, settings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudyStats()
    )

    init {
        // Observe settings changes to set default initial durations when timer is not running
        viewModelScope.launch {
            userSettings.collect { settings ->
                val current = _timerState.value
                if (!current.isRunning && !current.isPaused) {
                    val durationMin = when (current.sessionType) {
                        SessionType.FOCUS -> settings.focusDurationMinutes
                        SessionType.SHORT_BREAK -> settings.shortBreakMinutes
                        SessionType.LONG_BREAK -> settings.longBreakMinutes
                    }
                    val totalSec = durationMin * 60
                    _timerState.value = current.copy(
                        secondsRemaining = totalSec,
                        totalSeconds = totalSec
                    )
                }
            }
        }

        // Set initial subject once subjects load
        viewModelScope.launch {
            allSubjects.collect { subjects ->
                if (subjects.isNotEmpty() && _timerState.value.selectedSubject.id == 1L) {
                    val match = subjects.firstOrNull { it.id == _timerState.value.selectedSubject.id }
                        ?: subjects.first()
                    _timerState.value = _timerState.value.copy(selectedSubject = match)
                }
            }
        }
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return

        val state = _timerState.value
        val durationSec = state.secondsRemaining
        sessionStartTimeMs = System.currentTimeMillis()
        targetEndTimeMs = sessionStartTimeMs + (durationSec * 1000L)

        _timerState.value = state.copy(
            isRunning = true,
            isPaused = false
        )

        // Start ambient sound if selected
        if (state.activeAmbient != AmbientSoundType.NONE) {
            soundPlayer.startAmbient(state.activeAmbient)
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(500)
                val now = System.currentTimeMillis()
                val millisRemaining = targetEndTimeMs - now
                val secondsLeft = (millisRemaining / 1000).toInt().coerceAtLeast(0)

                val currentState = _timerState.value
                val total = currentState.totalSeconds.coerceAtLeast(1)
                val progressPercent = (((total - secondsLeft).toFloat() / total) * 100).toInt()

                _timerState.value = currentState.copy(
                    secondsRemaining = secondsLeft
                )

                // Update notification
                val formattedTime = formatMinutesSeconds(secondsLeft)
                val modeLabel = when (currentState.sessionType) {
                    SessionType.FOCUS -> "Deep Focus"
                    SessionType.SHORT_BREAK -> "Short Break"
                    SessionType.LONG_BREAK -> "Long Break"
                }
                notificationHelper.showTimerProgress(
                    modeName = modeLabel,
                    timeLeftFormatted = formattedTime,
                    subject = currentState.selectedSubject.name,
                    progressPercent = progressPercent
                )

                if (secondsLeft <= 0) {
                    handleSessionComplete()
                    break
                }
            }
        }
    }

    fun pauseTimer() {
        if (!_timerState.value.isRunning) return
        timerJob?.cancel()
        soundPlayer.stopAmbient()
        notificationHelper.clearTimerProgress()

        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = true
        )
    }

    fun resumeTimer() {
        startTimer()
    }

    fun resetTimer() {
        timerJob?.cancel()
        soundPlayer.stopAmbient()
        notificationHelper.clearTimerProgress()

        val settings = userSettings.value
        val defaultMin = when (_timerState.value.sessionType) {
            SessionType.FOCUS -> settings.focusDurationMinutes
            SessionType.SHORT_BREAK -> settings.shortBreakMinutes
            SessionType.LONG_BREAK -> settings.longBreakMinutes
        }
        val defaultSec = defaultMin * 60

        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = false,
            secondsRemaining = defaultSec,
            totalSeconds = defaultSec
        )
    }

    fun addFiveMinutes() {
        val additional = 5 * 60
        targetEndTimeMs += (additional * 1000L)
        val current = _timerState.value
        _timerState.value = current.copy(
            secondsRemaining = current.secondsRemaining + additional,
            totalSeconds = current.totalSeconds + additional
        )
    }

    fun setSessionType(type: SessionType) {
        timerJob?.cancel()
        soundPlayer.stopAmbient()
        notificationHelper.clearTimerProgress()

        val settings = userSettings.value
        val minutes = when (type) {
            SessionType.FOCUS -> settings.focusDurationMinutes
            SessionType.SHORT_BREAK -> settings.shortBreakMinutes
            SessionType.LONG_BREAK -> settings.longBreakMinutes
        }
        val sec = minutes * 60

        _timerState.value = _timerState.value.copy(
            sessionType = type,
            isRunning = false,
            isPaused = false,
            secondsRemaining = sec,
            totalSeconds = sec
        )
    }

    fun skipSession() {
        timerJob?.cancel()
        soundPlayer.stopAmbient()
        notificationHelper.clearTimerProgress()
        advanceToNextSession(recorded = false)
    }

    private fun handleSessionComplete() {
        val state = _timerState.value
        val settings = userSettings.value

        soundPlayer.stopAmbient()
        notificationHelper.clearTimerProgress()

        // Sound & haptic triggers
        if (settings.soundEnabled) {
            soundPlayer.playCompletionChime()
        }
        if (settings.vibrationEnabled) {
            notificationHelper.triggerVibration()
        }

        // Send local notification
        notificationHelper.showSessionCompleteAlert(
            isFocusSession = state.sessionType == SessionType.FOCUS,
            subject = state.selectedSubject.name,
            durationMinutes = state.totalSeconds / 60
        )

        // Log to Room database if it was a FOCUS session
        if (state.sessionType == SessionType.FOCUS) {
            viewModelScope.launch {
                repository.logSession(
                    StudySession(
                        subjectName = state.selectedSubject.name,
                        subjectColorHex = state.selectedSubject.colorHex,
                        sessionType = "FOCUS",
                        durationSeconds = state.totalSeconds,
                        targetDurationSeconds = state.totalSeconds,
                        completedAt = System.currentTimeMillis(),
                        notes = state.sessionNotes,
                        wasCompleted = true
                    )
                )
            }
        }

        advanceToNextSession(recorded = true)
    }

    private fun advanceToNextSession(recorded: Boolean) {
        val state = _timerState.value
        val settings = userSettings.value

        var nextType = SessionType.FOCUS
        var newCycles = state.completedCycles

        if (state.sessionType == SessionType.FOCUS) {
            if (recorded) newCycles += 1
            nextType = if (newCycles >= settings.sessionsUntilLongBreak) {
                newCycles = 0
                SessionType.LONG_BREAK
            } else {
                SessionType.SHORT_BREAK
            }
        } else {
            nextType = SessionType.FOCUS
        }

        val nextMinutes = when (nextType) {
            SessionType.FOCUS -> settings.focusDurationMinutes
            SessionType.SHORT_BREAK -> settings.shortBreakMinutes
            SessionType.LONG_BREAK -> settings.longBreakMinutes
        }
        val nextSec = nextMinutes * 60

        _timerState.value = state.copy(
            sessionType = nextType,
            isRunning = false,
            isPaused = false,
            secondsRemaining = nextSec,
            totalSeconds = nextSec,
            completedCycles = newCycles,
            sessionNotes = ""
        )

        // Check auto-start rules
        val shouldAutoStart = if (nextType == SessionType.FOCUS) {
            settings.autoStartFocus
        } else {
            settings.autoStartBreaks
        }

        if (shouldAutoStart) {
            startTimer()
        }
    }

    fun selectSubject(subject: StudySubject) {
        _timerState.value = _timerState.value.copy(selectedSubject = subject)
    }

    fun updateSessionNotes(notes: String) {
        _timerState.value = _timerState.value.copy(sessionNotes = notes)
    }

    fun setAmbientSound(ambient: AmbientSoundType) {
        _timerState.value = _timerState.value.copy(activeAmbient = ambient)
        if (_timerState.value.isRunning) {
            if (ambient == AmbientSoundType.NONE) {
                soundPlayer.stopAmbient()
            } else {
                soundPlayer.startAmbient(ambient)
            }
        }
    }

    fun setStudyCircle(circleName: String, peerCount: Int) {
        _timerState.value = _timerState.value.copy(
            currentCircle = circleName,
            activePeersCount = peerCount
        )
    }

    // Settings actions
    fun updateIntervals(
        focusMin: Int,
        shortBreakMin: Int,
        longBreakMin: Int,
        sessionsUntilLong: Int,
        dailyGoalMin: Int
    ) {
        val updated = userSettings.value.copy(
            focusDurationMinutes = focusMin.coerceIn(1, 120),
            shortBreakMinutes = shortBreakMin.coerceIn(1, 60),
            longBreakMinutes = longBreakMin.coerceIn(1, 60),
            sessionsUntilLongBreak = sessionsUntilLong.coerceIn(1, 10),
            dailyGoalMinutes = dailyGoalMin.coerceIn(15, 720)
        )
        viewModelScope.launch {
            repository.saveSettings(updated)
        }
    }

    fun updateToggles(
        sound: Boolean,
        vibration: Boolean,
        autoBreaks: Boolean,
        autoFocus: Boolean
    ) {
        val updated = userSettings.value.copy(
            soundEnabled = sound,
            vibrationEnabled = vibration,
            autoStartBreaks = autoBreaks,
            autoStartFocus = autoFocus
        )
        viewModelScope.launch {
            repository.saveSettings(updated)
        }
    }

    fun updateThemeMode(themeMode: String) {
        val updated = userSettings.value.copy(themeMode = themeMode)
        viewModelScope.launch {
            repository.saveSettings(updated)
        }
    }

    fun addSubject(name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.addSubject(
                StudySubject(
                    name = name.trim(),
                    colorHex = colorHex,
                    iconName = "school"
                )
            )
            _timerState.value = _timerState.value.copy(
                selectedSubject = StudySubject(id = id, name = name.trim(), colorHex = colorHex)
            )
        }
    }

    fun deleteSubject(subject: StudySubject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    fun deleteSession(session: StudySession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllSessions()
        }
    }

    private fun calculateStats(
        sessions: List<StudySession>,
        settings: UserSettings
    ): StudyStats {
        val calendar = Calendar.getInstance()
        val todayYear = calendar.get(Calendar.YEAR)
        val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        var todaySeconds = 0
        var completedTodayCount = 0
        var totalFocusSeconds = 0
        val hourlyCount = IntArray(24) { 0 }
        val subjectSecondsMap = mutableMapOf<String, Pair<String, Int>>() // subject -> (color, seconds)

        val activeDatesSet = mutableSetOf<String>()

        for (session in sessions) {
            if (session.sessionType == "FOCUS" && session.wasCompleted) {
                totalFocusSeconds += session.durationSeconds

                val cal = Calendar.getInstance().apply { timeInMillis = session.completedAt }
                val isToday = cal.get(Calendar.YEAR) == todayYear && cal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear

                if (isToday) {
                    todaySeconds += session.durationSeconds
                    completedTodayCount++
                }

                // Hourly activity
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                hourlyCount[hour]++

                // Date key for streak
                val dateKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
                activeDatesSet.add(dateKey)

                // Subject breakdown
                val existing = subjectSecondsMap[session.subjectName]
                val currentSec = existing?.second ?: 0
                subjectSecondsMap[session.subjectName] = Pair(session.subjectColorHex, currentSec + session.durationSeconds)
            }
        }

        val todayMin = todaySeconds / 60
        val goalMin = settings.dailyGoalMinutes
        val goalPercent = if (goalMin > 0) (todayMin.toFloat() / goalMin).coerceIn(0f, 1f) else 0f

        // Calculate 7-day data
        val last7Days = mutableListOf<DayStudyData>()
        val dayFormatter = Calendar.getInstance()

        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            val y = c.get(Calendar.YEAR)
            val doy = c.get(Calendar.DAY_OF_YEAR)

            val dayName = when (c.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "Sun"
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                else -> ""
            }

            var daySec = 0
            for (s in sessions) {
                if (s.sessionType == "FOCUS" && s.wasCompleted) {
                    dayFormatter.timeInMillis = s.completedAt
                    if (dayFormatter.get(Calendar.YEAR) == y && dayFormatter.get(Calendar.DAY_OF_YEAR) == doy) {
                        daySec += s.durationSeconds
                    }
                }
            }

            last7Days.add(
                DayStudyData(
                    dayLabel = dayName,
                    dateKey = "$y-$doy",
                    minutesStudied = daySec / 60,
                    isToday = i == 0
                )
            )
        }

        // Streak calculation
        var streak = 0
        val streakCal = Calendar.getInstance()
        // Check if studied today
        val todayKey = "${streakCal.get(Calendar.YEAR)}-${streakCal.get(Calendar.MONTH)}-${streakCal.get(Calendar.DAY_OF_MONTH)}"
        var checkDate = if (activeDatesSet.contains(todayKey)) {
            streak++
            streakCal.add(Calendar.DAY_OF_YEAR, -1)
            streakCal
        } else {
            // Check yesterday
            streakCal.add(Calendar.DAY_OF_YEAR, -1)
            streakCal
        }

        while (true) {
            val key = "${checkDate.get(Calendar.YEAR)}-${checkDate.get(Calendar.MONTH)}-${checkDate.get(Calendar.DAY_OF_MONTH)}"
            if (activeDatesSet.contains(key)) {
                streak++
                checkDate.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        // Subject stats list
        val subjectStats = subjectSecondsMap.map { (name, pair) ->
            val min = pair.second / 60
            val pct = if (totalFocusSeconds > 0) pair.second.toFloat() / totalFocusSeconds else 0f
            SubjectStudyStat(
                subjectName = name,
                colorHex = pair.first,
                totalMinutes = min,
                percentage = pct
            )
        }.sortedByDescending { it.totalMinutes }

        return StudyStats(
            todayMinutes = todayMin,
            todayGoalMinutes = goalMin,
            todayGoalPercent = goalPercent,
            completedSessionsToday = completedTodayCount,
            currentStreakDays = streak,
            totalFocusHoursAllTime = (totalFocusSeconds / 3600f),
            totalSessionsAllTime = sessions.count { it.sessionType == "FOCUS" && it.wasCompleted },
            last7DaysData = last7Days,
            subjectBreakdown = subjectStats,
            hourlyActivity = hourlyCount.toList()
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundPlayer.stopAmbient()
        notificationHelper.clearTimerProgress()
    }
}

fun formatMinutesSeconds(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}
