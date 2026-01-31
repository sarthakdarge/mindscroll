package com.example.reelstracker.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.reelstracker.R

class AlertManager(private val context: Context) {

    private val CHANNEL_ID = "mindscroll_alerts"

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MindScroll Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            context
                .getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun showLimitCrossedAlert(message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("MindScroll Alert 🚨")
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(1001, notification)
    }
}
