package com.example.reelstracker.data

import android.content.Context

class LimitManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("mindscroll_limits", Context.MODE_PRIVATE)

    private val KEY_DAILY_LIMIT = "daily_limit_ms"

    // Default: 30 minutes
    fun getDailyLimit(): Long {
        return prefs.getLong(KEY_DAILY_LIMIT, 30 * 60 * 1000L)
    }

    fun setDailyLimit(limitMs: Long) {
        prefs.edit()
            .putLong(KEY_DAILY_LIMIT, limitMs)
            .apply()
    }
}
