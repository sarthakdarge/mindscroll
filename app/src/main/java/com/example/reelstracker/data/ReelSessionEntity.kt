package com.example.reelstracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reel_sessions")
data class ReelSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val date: String,

    // 👇 IMPORTANT
    val reelNumber: Int
)
