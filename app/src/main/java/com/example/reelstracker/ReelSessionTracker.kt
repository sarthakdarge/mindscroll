package com.example.reelstracker.data

import android.content.Context
import android.util.Log

class ReelSessionTracker(context: Context) {

    private val historyManager = ReelHistoryManager(context)

    private var sessionStartTime: Long = 0L
    private var isSessionActive = false

    fun startSession() {
        if (isSessionActive) return

        sessionStartTime = System.currentTimeMillis()
        isSessionActive = true

        Log.d("MINDSCROLL_SESSION", "Reels session started")
    }

    fun endSession() {
        if (!isSessionActive) return

        val endTime = System.currentTimeMillis()
        val duration = endTime - sessionStartTime

        historyManager.addSessionTime(duration)

        isSessionActive = false
        sessionStartTime = 0L

        Log.d(
            "MINDSCROLL_SESSION",
            "Reels session ended → ${(duration / 1000)} sec"
        )
    }
}
