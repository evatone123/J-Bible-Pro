package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "bible_verses",
    indices = [
        Index(value = ["translation", "book", "chapter", "verse"], unique = true),
        Index(value = ["bookId", "chapter", "verse"])
    ]
)
data class BibleVerse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val translation: String, // "KJV" (King James Version) or "WEB" (World English Bible)
    val book: String,        // e.g., "Genesis"
    val bookId: Int,         // Ordering index (1 = Genesis, etc.)
    val chapter: Int,
    val verse: Int,
    val text: String
)

@Entity(tableName = "user_highlights")
data class UserHighlight(
    @PrimaryKey val id: String, // "$translation-$book-$chapter-$verse"
    val translation: String,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val colorHex: String, // "#FFF176" (Yellow), "#4DD0E1" (Teal), "#F06292" (Pink), "#AED581" (Green)
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_notes")
data class UserNote(
    @PrimaryKey val id: String, // "$translation-$book-$chapter-$verse"
    val translation: String,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val noteText: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_plans")
data class ReadingPlan(
    @PrimaryKey val planId: String,
    val title: String,
    val description: String,
    val totalDays: Int,
    val versesPerDayJson: String // Serialized array of verses/chapters per day
)

@Entity(tableName = "reading_plan_progress")
data class ReadingPlanProgress(
    @PrimaryKey val progressId: String, // "$planId-$day"
    val planId: String,
    val day: Int,
    val completed: Boolean,
    val completedAt: Long = System.currentTimeMillis()
)
