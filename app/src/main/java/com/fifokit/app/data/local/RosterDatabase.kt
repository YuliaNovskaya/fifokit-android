package com.fifokit.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        RosterEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RosterDatabase : RoomDatabase() {

    abstract fun rosterDao(): RosterDao

    companion object {

        @Volatile
        private var INSTANCE: RosterDatabase? = null

        fun getInstance(context: Context): RosterDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RosterDatabase::class.java,
                    "fifokit_roster.db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}