package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BibleVerse::class,
        UserHighlight::class,
        UserNote::class,
        ReadingPlan::class,
        ReadingPlanProgress::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BibleDatabase : RoomDatabase() {

    abstract fun bibleDao(): BibleDao

    companion object {
        @Volatile
        private var INSTANCE: BibleDatabase? = null

        fun getDatabase(context: Context): BibleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BibleDatabase::class.java,
                    "grace_bible_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
