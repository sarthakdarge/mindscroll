package com.example.reelstracker.data

data class DailyStats(
    val date: String,
    var reelCount: Int = 0,
    var timeSpentMs: Long = 0L
)
