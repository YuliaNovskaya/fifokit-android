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
    version = 5,
    exportSchema = false
)
abstract class RosterDatabase : RoomDatabase() {

    abstract fun rosterDao(): RosterDao

    companion object {

        private val MIGRATION_1_2 =
            object : Migration(1, 2) {

                override fun migrate(
                    db: SupportSQLiteDatabase
                ) {
                    db.execSQL(
                        "ALTER TABLE rosters ADD COLUMN cloudId TEXT"
                    )

                    db.execSQL(
                        """
                CREATE UNIQUE INDEX IF NOT EXISTS
                index_rosters_cloudId
                ON rosters(cloudId)
                """.trimIndent()
                    )
                }
            }
        private val MIGRATION_2_3 =
            object : Migration(2, 3) {

                override fun migrate(
                    db: SupportSQLiteDatabase
                ) {
                    db.execSQL(
                        """
                ALTER TABLE rosters
                ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0
                """.trimIndent()
                    )

                    db.execSQL(
                        """
                ALTER TABLE rosters
                ADD COLUMN deletedAt INTEGER
                """.trimIndent()
                    )
                }
            }
        private val MIGRATION_3_4 =
            object : Migration(3, 4) {

                override fun migrate(
                    db: SupportSQLiteDatabase
                ) {
                    db.execSQL(
                        """
                ALTER TABLE rosters
                ADD COLUMN shutdownsJson TEXT NOT NULL DEFAULT '[]'
                """.trimIndent()
                    )
                }
            }

        private val MIGRATION_4_5 =
            object : Migration(4, 5) {

                override fun migrate(
                    db: SupportSQLiteDatabase
                ) {
                    db.execSQL(
                        """
                ALTER TABLE rosters
                ADD COLUMN isShutdownRoster INTEGER NOT NULL DEFAULT 0
                """.trimIndent()
                    )

                    db.execSQL(
                        """
                ALTER TABLE rosters
                ADD COLUMN endDate TEXT
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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build()
                    .also { INSTANCE = it }
            }
        }

    }
}