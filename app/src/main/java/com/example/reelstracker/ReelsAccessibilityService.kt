package com.example.reelstracker

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.reelstracker.data.AppDatabase
import com.example.reelstracker.data.LimitManager
import com.example.reelstracker.data.NotificationHelper
import com.example.reelstracker.data.ReelHistoryManager
import com.example.reelstracker.data.ReelSessionEntity
import java.time.LocalDate

class ReelsAccessibilityService : AccessibilityService() {

    private val INSTAGRAM_PACKAGE = "com.instagram.android"

    // 📅 Date tracking (midnight reset)
    private var currentDate: LocalDate = LocalDate.now()

    // ⏱ Watch time
    private val WATCH_TICK_MS = 2000L
    private var isInstagramActive = false

    private val handler = Handler(Looper.getMainLooper())
    private val watchRunnable = object : Runnable {
        override fun run() {
            if (isInstagramActive) {
                addWatchTime(WATCH_TICK_MS)
                handler.postDelayed(this, WATCH_TICK_MS)
            }
        }
    }

    // 🎞 Reel counting
    private val MIN_REEL_INTERVAL_MS = 1500L
    private var lastReelTimestamp = 0L

    // 🔔 20-reel binge tracking (per Instagram open)
    private var reelsSinceOpen = 0
    private var instagramSessionKey = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // 🕛 Midnight reset check
        val today = LocalDate.now()
        if (today != currentDate) {
            currentDate = today
            lastReelTimestamp = 0L
        }

        val packageName = event.packageName?.toString()

        // 🟢 Instagram foreground
        if (packageName == INSTAGRAM_PACKAGE) {
            if (!isInstagramActive) {
                isInstagramActive = true
                reelsSinceOpen = 0
                instagramSessionKey = System.currentTimeMillis().toString()
                handler.post(watchRunnable)
            }
        } else {
            stopWatchTimer()
            return
        }

        // 🎯 Reel counting only on scroll
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) return

        val source = event.source ?: return
        if (isCommentScroll(source, event)) return

        val now = System.currentTimeMillis()
        if (now - lastReelTimestamp < MIN_REEL_INTERVAL_MS) return

        lastReelTimestamp = now
        saveTodayReel()
    }

    // ⏹ Stop Instagram tracking
    private fun stopWatchTimer() {
        if (isInstagramActive) {
            isInstagramActive = false
            reelsSinceOpen = 0
            instagramSessionKey = ""
            handler.removeCallbacks(watchRunnable)
        }
    }

    // ⏱ Add watch time + daily limit notification
    private fun addWatchTime(durationMs: Long) {
        Thread {
            val historyManager = ReelHistoryManager(applicationContext)
            historyManager.addSessionTime(durationMs)

            val limitManager = LimitManager(applicationContext)
            val dailyLimitMs = limitManager.getDailyLimit()

            val today = historyManager.getLast7Days().lastOrNull()
            val todayTime = today?.timeSpentMs ?: 0L

            if (dailyLimitMs > 0 && todayTime >= dailyLimitMs) {
                NotificationHelper(applicationContext)
                    .showDailyLimitNotificationIfNeeded()
            }
        }.start()
    }

    // 🎞 Save reel count + 20-reel binge notification
    private fun saveTodayReel() {
        val todayStr = currentDate.toString()

        Thread {
            val dao = AppDatabase.get(applicationContext).reelDao()
            val existing = dao.getStatsForDate(todayStr)

            val updated = if (existing == null) {
                ReelSessionEntity(
                    date = todayStr,
                    reelCount = 1,
                    totalWatchTimeMs = 0
                )
            } else {
                existing.copy(
                    reelCount = existing.reelCount + 1
                )
            }

            dao.insertOrUpdate(updated)
            ReelHistoryManager(applicationContext).incrementReel()

            // 🔔 Binge detection
            reelsSinceOpen++

            if (reelsSinceOpen == 20) {
                NotificationHelper(applicationContext)
                    .showReelBingeNotificationIfNeeded(instagramSessionKey)
            }
        }.start()
    }

    // 🚫 Ignore comments scroll
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
        stopWatchTimer()
    }

    override fun onDestroy() {
        stopWatchTimer()
        super.onDestroy()
    }
}
