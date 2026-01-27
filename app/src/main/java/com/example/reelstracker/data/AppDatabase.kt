package com.example.reelstracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ReelSessionEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reelDao(): ReelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reels_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
