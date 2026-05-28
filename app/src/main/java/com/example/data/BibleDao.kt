package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleDao {

    @Query("SELECT * FROM bible_verses WHERE translation = :translation AND book = :book AND chapter = :chapter ORDER BY verse ASC")
    fun getVerses(translation: String, book: String, chapter: Int): Flow<List<BibleVerse>>

    @Query("SELECT * FROM bible_verses WHERE translation = :translation AND book = :book AND chapter = :chapter ORDER BY verse ASC")
    suspend fun getVersesDirect(translation: String, book: String, chapter: Int): List<BibleVerse>

    @Query("SELECT * FROM bible_verses WHERE translation = :translation AND text LIKE :query ORDER BY bookId ASC, chapter ASC, verse ASC LIMIT 100")
    fun searchVerses(translation: String, query: String): Flow<List<BibleVerse>>

    @Query("SELECT COUNT(*) FROM bible_verses")
    suspend fun getVerseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<BibleVerse>)

    // --- Highlights ---
    @Query("SELECT * FROM user_highlights ORDER BY addedAt DESC")
    fun getAllHighlights(): Flow<List<UserHighlight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: UserHighlight)

    @Query("DELETE FROM user_highlights WHERE id = :id")
    suspend fun deleteHighlight(id: String)

    // --- Notes ---
    @Query("SELECT * FROM user_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<UserNote>>

    @Query("SELECT * FROM user_notes WHERE id = :id LIMIT 1")
    fun getNoteFlow(id: String): Flow<UserNote?>

    @Query("SELECT * FROM user_notes WHERE id = :id LIMIT 1")
    suspend fun getNoteDirect(id: String): UserNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: UserNote)

    @Query("DELETE FROM user_notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    // --- Reading Plans ---
    @Query("SELECT * FROM reading_plans")
    fun getAllReadingPlans(): Flow<List<ReadingPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingPlans(plans: List<ReadingPlan>)

    // --- Reading Plan Progress ---
    @Query("SELECT * FROM reading_plan_progress WHERE planId = :planId")
    fun getProgressForPlan(planId: String): Flow<List<ReadingPlanProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ReadingPlanProgress)
}
