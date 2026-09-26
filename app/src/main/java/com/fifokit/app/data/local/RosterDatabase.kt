package com.fifokit.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RosterEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RosterDatabase : RoomDatabase() {

    abstract fun rosterDao(): RosterDao

    companion object {

        private val MIGRATION_1_2 =
            object : Migration(1, 2) {

                override fun migrate(
                    database: SupportSQLiteDatabase
                ) {
                    database.execSQL(
                        "ALTER TABLE rosters ADD COLUMN cloudId TEXT"
                    )

                    database.execSQL(
                        """
                CREATE UNIQUE INDEX IF NOT EXISTS
                index_rosters_cloudId
                ON rosters(cloudId)
                """.trimIndent()
                    )
                }
            }
        @Volatile
        private var INSTANCE: RosterDatabase? = null

        fun getInstance(context: Context): RosterDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RosterDatabase::class.java,
                    "fifokit_roster.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }

    }
}