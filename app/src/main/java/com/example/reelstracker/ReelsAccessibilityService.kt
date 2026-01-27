package com.example.reelstracker

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.reelstracker.data.AppDatabase
import com.example.reelstracker.data.ReelSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs

class ReelsAccessibilityService : AccessibilityService() {

    private val INSTAGRAM_PACKAGE = "com.instagram.android"

    private var reelCount = 0
    private var lastReelTimestamp = 0L

    private val MIN_SCROLL_DISTANCE = 250
    private val MIN_REEL_INTERVAL_MS = 1200

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

        // count reel
        reelCount++

        if (lastReelTimestamp != 0L) {
            saveSession(lastReelTimestamp, now, reelCount)
        }

        lastReelTimestamp = now

        Log.d("REELS_TRACKER", "Reels watched = $reelCount")
    }

    private fun saveSession(start: Long, end: Long, reelNumber: Int) {
        val session = ReelSessionEntity(
            startTime = start,
            endTime = end,
            durationMs = end - start,
            date = LocalDate.now().toString(),
            reelNumber = reelNumber
        )

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.get(this@ReelsAccessibilityService)
                .reelDao()
                .insertSession(session)
        }
    }

    private fun isCommentScroll(
        node: AccessibilityNodeInfo,
        event: AccessibilityEvent
    ): Boolean {
        val className = node.className?.toString()?.lowercase() ?: ""
        if (className.contains("recyclerview")) return true

        val texts = event.text.joinToString(" ").lowercase()
        if (
            texts.contains("comment") ||
            texts.contains("reply")
        ) return true

        return false
    }

    override fun onInterrupt() {}
}
