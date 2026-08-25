package com.kthakare.aiscanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScanDocument::class], version = 1, exportSchema = false)
abstract class ScanDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    companion object {
        fun create(context: Context): ScanDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ScanDatabase::class.java,
                "ai_scanner.db",
            ).build()
        }
    }
}
