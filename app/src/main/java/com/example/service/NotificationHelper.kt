package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val ALERTS_CHANNEL_ID = "study_circle_alerts"
        const val PROGRESS_CHANNEL_ID = "study_circle_progress"
        const val NOTIFICATION_ID_ALERT = 1001
        const val NOTIFICATION_ID_PROGRESS = 1002
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertsChannel = NotificationChannel(
                ALERTS_CHANNEL_ID,
                "Study Session Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when study focus intervals or breaks complete"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            val progressChannel = NotificationChannel(
                PROGRESS_CHANNEL_ID,
                "Active Study Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the active Pomodoro timer countdown"
            }

            notificationManager.createNotificationChannel(alertsChannel)
            notificationManager.createNotificationChannel(progressChannel)
        }
    }

    private fun getLaunchPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showTimerProgress(
        modeName: String,
        timeLeftFormatted: String,
        subject: String,
        progressPercent: Int
    ) {
        val contentText = "$timeLeftFormatted remaining • $subject"
        val builder = NotificationCompat.Builder(context, PROGRESS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("StudyCircle: $modeName")
            .setContentText(contentText)
            .setProgress(100, progressPercent, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(getLaunchPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)

        try {
            notificationManager.notify(NOTIFICATION_ID_PROGRESS, builder.build())
        } catch (_: SecurityException) {
            // Permission might not be granted yet
        }
    }

    fun clearTimerProgress() {
        notificationManager.cancel(NOTIFICATION_ID_PROGRESS)
    }

    fun showSessionCompleteAlert(
        isFocusSession: Boolean,
        subject: String,
        durationMinutes: Int
    ) {
        val title = if (isFocusSession) {
            "🎉 Focus Session Complete!"
        } else {
            "☕ Break Ended!"
        }

        val message = if (isFocusSession) {
            "Great work on $subject! You completed $durationMinutes minutes of deep focus. Take a break."
        } else {
            "Time to get back in the zone! Ready for your next study sprint?"
        }

        val builder = NotificationCompat.Builder(context, ALERTS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getLaunchPendingIntent())

        try {
            notificationManager.notify(NOTIFICATION_ID_ALERT, builder.build())
        } catch (_: SecurityException) {
            // Permission might not be granted yet
        }
    }

    fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.let { vm ->
                    val effect = VibrationEffect.createWaveform(
                        longArrayOf(0, 300, 150, 400),
                        intArrayOf(0, 255, 0, 255),
                        -1
                    )
                    vm.vibrate(CombinedVibration.createParallel(effect))
                    return
                }
            }
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 300, 150, 400),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 300, 150, 400), -1)
            }
        } catch (_: Exception) {
            // Ignore vibration errors
        }
    }
}
