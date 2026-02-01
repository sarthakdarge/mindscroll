package com.example.reelstracker.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.reelstracker.R
import java.time.LocalDate

class NotificationHelper(private val context: Context) {

    private val prefs =
        context.getSharedPreferences("mindscroll_notifications", Context.MODE_PRIVATE)

    private val CHANNEL_ID = "mindscroll_limit_channel"

    private val DAILY_LIMIT_SHOWN_KEY = "daily_limit_shown_date"
    private val BINGE_SHOWN_KEY = "binge_shown_session"

    init {
        createChannel()
    }

    /* ============================================================
       DAILY LIMIT NOTIFICATION (once per day)
       ============================================================ */
    fun showDailyLimitNotificationIfNeeded() {
        val today = LocalDate.now().toString()
        val lastShown = prefs.getString(DAILY_LIMIT_SHOWN_KEY, null)

        // Already shown today → do nothing
        if (today == lastShown) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("MindScroll")
            .setContentText(
                "You’ve reached today’s Instagram limit. A short break can help reset your focus."
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)

        prefs.edit()
            .putString(DAILY_LIMIT_SHOWN_KEY, today)
            .apply()
    }

    /* ============================================================
       20-REEL BINGE NOTIFICATION (once per Instagram open)
       ============================================================ */
    fun showReelBingeNotificationIfNeeded(sessionKey: String) {
        val lastShown = prefs.getString(BINGE_SHOWN_KEY, null)

        // Already shown for this Instagram open
        if (lastShown == sessionKey) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("MindScroll")
            .setContentText(
                "You’ve watched 20 reels continuously. Want to pause for a moment?"
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1002, notification)

        prefs.edit()
            .putString(BINGE_SHOWN_KEY, sessionKey)
            .apply()
    }

    /* ============================================================
       NOTIFICATION CHANNEL (Android 8+)
       ============================================================ */
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MindScroll usage alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for Instagram usage limits and binge warnings"
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
