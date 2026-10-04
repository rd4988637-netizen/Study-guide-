package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SessionType {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK
}

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectName: String,
    val subjectColorHex: String,
    val sessionType: String, // FOCUS, SHORT_BREAK, LONG_BREAK
    val durationSeconds: Int,
    val targetDurationSeconds: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val wasCompleted: Boolean = true
)

@Entity(tableName = "study_subjects")
data class StudySubject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val iconName: String = "book"
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val focusDurationMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsUntilLongBreak: Int = 4,
    val dailyGoalMinutes: Int = 120, // 2 hours daily goal
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val autoStartBreaks: Boolean = false,
    val autoStartFocus: Boolean = false,
    val themeMode: String = "DARK", // DARK, AMOLED, SUNSET, LIGHT
    val selectedAmbientSound: String = "NONE" // NONE, RAIN, WHITE_NOISE, WAVES, LOFI_PULSE
)
