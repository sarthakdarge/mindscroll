package com.example.reelstracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReelDao {

    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    fun getStatsForDate(date: String): ReelSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(stats: ReelSessionEntity)
}
