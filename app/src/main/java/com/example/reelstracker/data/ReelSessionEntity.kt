package com.example.reelstracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class ReelSessionEntity(

    @PrimaryKey
    val date: String, // YYYY-MM-DD

    val reelCount: Int,
    val totalWatchTimeMs: Long
)
