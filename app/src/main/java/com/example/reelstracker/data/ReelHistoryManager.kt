package com.example.reelstracker.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log


class ReelHistoryManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("mindscroll_history", Context.MODE_PRIVATE)

    private val gson = Gson()
    private val HISTORY_KEY = "daily_history"

    private fun today(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun loadHistory(): MutableMap<String, DailyStats> {
        val json = prefs.getString(HISTORY_KEY, null) ?: return mutableMapOf()
        val type = object : TypeToken<MutableMap<String, DailyStats>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveHistory(map: Map<String, DailyStats>) {
        prefs.edit()
            .putString(HISTORY_KEY, gson.toJson(map))
            .apply()
    }

    private fun cleanupOldDays(map: MutableMap<String, DailyStats>) {
        if (map.size <= 7) return

        val sortedKeys = map.keys.sorted()
        val keysToRemove = sortedKeys.take(sortedKeys.size - 7)
        keysToRemove.forEach { map.remove(it) }
    }

    fun incrementReel() {
        Log.d("MINDSCROLL_HISTORY", "incrementReel() called")

        val map = loadHistory()
        val date = today()

        val stats = map[date] ?: DailyStats(date)
        stats.reelCount++

        map[date] = stats
        cleanupOldDays(map)
        saveHistory(map)
        Log.d("MINDSCROLL_HISTORY", "Saved $date -> ${stats.reelCount}")
    }

    fun getLast7Days(): List<DailyStats> {
        return loadHistory()
            .values
            .sortedBy { it.date }
            .toList()
    }
    fun addSessionTime(durationMs: Long) {
        val map = loadHistory()
        val date = today()

        val stats = map[date] ?: DailyStats(date)
        stats.timeSpentMs += durationMs

        map[date] = stats
        cleanupOldDays(map)
        saveHistory(map)
        Log.d(
            "MINDSCROLL_SESSION",
            "TOTAL reels time today = ${stats.timeSpentMs / 1000} seconds")
    }

}
