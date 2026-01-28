package com.example.reelstracker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import com.example.reelstracker.data.AppDatabase
import com.example.reelstracker.data.ReelSessionEntity
import java.time.LocalDate
import kotlin.math.abs

class ReelsAccessibilityService : AccessibilityService() {

    private val INSTAGRAM_PACKAGE = "com.instagram.android"

    private val MIN_SCROLL_DISTANCE = 250
    private val MIN_REEL_INTERVAL_MS = 1200

    private var lastReelTimestamp = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.packageName != INSTAGRAM_PACKAGE) return
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) return

        val source = event.source ?: return
        if (isCommentScroll(source, event)) return

        val scrollDelta = abs(event.scrollDeltaY)
        if (scrollDelta < MIN_SCROLL_DISTANCE) return

        val now = System.currentTimeMillis()
        if (now - lastReelTimestamp < MIN_REEL_INTERVAL_MS) return

        lastReelTimestamp = now
        saveTodayReel()
    }

    private fun saveTodayReel() {
        val today = LocalDate.now().toString()

        Thread {
            val dao = AppDatabase.get(this).reelDao()
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

            Log.d(
                "REELS_TRACKER",
                "Today reels = ${updated.reelCount}"
            )
        }.start()
    }

    private fun isCommentScroll(
        node: AccessibilityNodeInfo,
        event: AccessibilityEvent
    ): Boolean {
        val className = node.className?.toString()?.lowercase() ?: ""
        if (className.contains("recyclerview")) return true
        if (className.contains("bottomsheet") || className.contains("dialog")) return true

        val texts = event.text.joinToString(" ").lowercase()
        if (
            texts.contains("add a comment") ||
            texts.contains("comments") ||
            texts.contains("replies")
        ) return true

        return false
    }

    override fun onInterrupt() {}
}
