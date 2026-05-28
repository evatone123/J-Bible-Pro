package com.example.data

import com.example.api.GeminiClient
import com.example.api.GeneratedDayPlan
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class BibleRepository(private val bibleDao: BibleDao) {

    // --- Verses ---
    fun getVerses(translation: String, book: String, chapter: Int): Flow<List<BibleVerse>> {
        return bibleDao.getVerses(translation, book, chapter)
    }

    suspend fun getVersesDirect(translation: String, book: String, chapter: Int): List<BibleVerse> {
        return bibleDao.getVersesDirect(translation, book, chapter)
    }

    fun searchVerses(translation: String, query: String): Flow<List<BibleVerse>> {
        val formattedQuery = "%$query%"
        return bibleDao.searchVerses(translation, formattedQuery)
    }

    suspend fun getVerseCount(): Int {
        return bibleDao.getVerseCount()
    }

    // --- Highlights ---
    val allHighlights: Flow<List<UserHighlight>> = bibleDao.getAllHighlights()

    suspend fun addHighlight(translation: String, book: String, chapter: Int, verse: Int, colorHex: String) {
        val id = "$translation-$book-$chapter-$verse"
        val highlight = UserHighlight(
            id = id,
            translation = translation,
            book = book,
            chapter = chapter,
            verse = verse,
            colorHex = colorHex
        )
        bibleDao.insertHighlight(highlight)
    }

    suspend fun removeHighlight(translation: String, book: String, chapter: Int, verse: Int) {
        val id = "$translation-$book-$chapter-$verse"
        bibleDao.deleteHighlight(id)
    }

    // --- Notes ---
    val allNotes: Flow<List<UserNote>> = bibleDao.getAllNotes()

    fun getNote(translation: String, book: String, chapter: Int, verse: Int): Flow<UserNote?> {
        val id = "$translation-$book-$chapter-$verse"
        return bibleDao.getNoteFlow(id)
    }

    suspend fun saveNote(translation: String, book: String, chapter: Int, verse: Int, text: String) {
        val id = "$translation-$book-$chapter-$verse"
        val note = UserNote(
            id = id,
            translation = translation,
            book = book,
            chapter = chapter,
            verse = verse,
            noteText = text,
            updatedAt = System.currentTimeMillis()
        )
        bibleDao.insertNote(note)
    }

    suspend fun deleteNote(translation: String, book: String, chapter: Int, verse: Int) {
        val id = "$translation-$book-$chapter-$verse"
        bibleDao.deleteNote(id)
    }

    // --- Reading Plans ---
    val allReadingPlans: Flow<List<ReadingPlan>> = bibleDao.getAllReadingPlans()

    fun getProgressForPlan(planId: String): Flow<List<ReadingPlanProgress>> {
        return bibleDao.getProgressForPlan(planId)
    }

    suspend fun toggleDayCompleted(planId: String, day: Int, completed: Boolean) {
        val progressId = "$planId-$day"
        val progress = ReadingPlanProgress(
            progressId = progressId,
            planId = planId,
            day = day,
            completed = completed,
            completedAt = System.currentTimeMillis()
        )
        bibleDao.insertProgress(progress)
    }

    /**
     * Integrates Gemini to create a customized reading plan, inserting it locally.
     */
    suspend fun createCustomAIPian(topic: String): ReadingPlan {
        val generatedDays = GeminiClient.generateReadingPlan(topic)
        
        // Serialize to JSON to store in ReadingPlan
        val jsonArray = JSONArray()
        generatedDays.forEach { dayPlan ->
            val obj = JSONObject().apply {
                put("day", dayPlan.day)
                put("passage", dayPlan.passage)
                put("notes", dayPlan.notes)
            }
            jsonArray.put(obj)
        }

        val planId = "custom_" + topic.replace(" ", "_").lowercase() + "_" + System.currentTimeMillis()
        val customPlan = ReadingPlan(
            planId = planId,
            title = "$topic Plan",
            description = "A personalized 5-day daily reading companion tailored around the theme of $topic.",
            totalDays = generatedDays.size,
            versesPerDayJson = jsonArray.toString()
        )

        bibleDao.insertReadingPlans(listOf(customPlan))
        return customPlan
    }

    // --- Gemini Interactive Features ---
    suspend fun getAiVerseInsight(book: String, chapter: Int, verse: Int, text: String): String {
        return GeminiClient.getVerseInsight(book, chapter, verse, text)
    }

    suspend fun getChapterIllustrationDescription(book: String, chapter: Int): String {
        return GeminiClient.getChapterIllustration(book, chapter)
    }

    suspend fun downloadChapterFromAi(book: String, chapter: Int, translation: String) {
        val geminiVerses = GeminiClient.fetchChapterVerses(book, chapter, translation)
        val bookId = BibleMeta.bookMap[book]?.id ?: 1
        val dbVerses = geminiVerses.map { gv ->
            BibleVerse(
                translation = translation,
                book = book,
                bookId = bookId,
                chapter = chapter,
                verse = gv.v,
                text = gv.t
            )
        }
        if (dbVerses.isNotEmpty()) {
            bibleDao.insertVerses(dbVerses)
        }
    }
}
