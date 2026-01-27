package com.example.reelstracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReelDao {

    @Insert
    suspend fun insertSession(session: ReelSessionEntity)

    // 👇 THIS IS WHAT WE SHOW
    @Query("SELECT COUNT(*) FROM reel_sessions")
    suspend fun getTotalReelsWatched(): Int
}
