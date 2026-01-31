package com.example.reelstracker

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.reelstracker.data.AppDatabase
import com.example.reelstracker.data.ReelSessionEntity
import com.example.reelstracker.data.ReelHistoryManager
import com.example.reelstracker.data.ReelSessionTracker
import java.time.LocalDate

class ReelsAccessibilityService : AccessibilityService() {

    private val INSTAGRAM_PACKAGE = "com.instagram.android"
    private val MIN_REEL_INTERVAL_MS = 1500L

    private var lastReelTimestamp = 0L

    // ⏱ Instagram session tracker
    private lateinit var sessionTracker: ReelSessionTracker

    override fun onServiceConnected() {
        super.onServiceConnected()
        sessionTracker = ReelSessionTracker(applicationContext)
        Log.d("MINDSCROLL_SESSION", "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString()

        // 🟢 Instagram in foreground → start session
        if (packageName == INSTAGRAM_PACKAGE) {
            sessionTracker.startSession()
        }
        // 🔴 Left Instagram → end session
        else {
            sessionTracker.endSession()
            return
        }

        // 🎯 Reel counting ONLY on scroll events
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) return

        val source = event.source ?: return
        if (isCommentScroll(source, event)) return

        val now = System.currentTimeMillis()
        if (now - lastReelTimestamp < MIN_REEL_INTERVAL_MS) return

        lastReelTimestamp = now
        saveTodayReel()
    }

    private fun saveTodayReel() {
        val today = LocalDate.now().toString()

        Thread {
            val dao = AppDatabase.get(applicationContext).reelDao()
            val existing = dao.getStatsForDate(today)

            val updated = if (existing == null) {
                ReelSessionEntity(
                    date = today,
                    reelCount = 1,
                    totalWatchTimeMs = 0
                )
            } else {
                existing.copy(
                    reelCount = existing.reelCount + 1
                )
            }

            dao.insertOrUpdate(updated)

            Log.d("REELS_TRACKER", "Reel counted → total = ${updated.reelCount}")

            // 📦 7-day history
            ReelHistoryManager(applicationContext).incrementReel()
        }.start()
    }

    private fun isCommentScroll(
        node: AccessibilityNodeInfo,
        event: AccessibilityEvent
    ): Boolean {
        val className = node.className?.toString()?.lowercase() ?: ""

        if (
            className.contains("recyclerview") ||
            className.contains("bottomsheet") ||
            className.contains("dialog")
        ) return true

        val text = event.text.joinToString(" ").lowercase()
        if (
            text.contains("add a comment") ||
            text.contains("comments") ||
            text.contains("replies")
        ) return true

        return false
    }

    override fun onInterrupt() {
        sessionTracker.endSession()
    }

    override fun onDestroy() {
        sessionTracker.endSession()
        super.onDestroy()
    }
}
