package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BibleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BibleRepository
    private val scope = viewModelScope

    init {
        val database = BibleDatabase.getDatabase(application)
        repository = BibleRepository(database.bibleDao())
        
        // Safely check and preload scriptures offline on start without database callback deadlocks
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (repository.getVerseCount() == 0) {
                    val preloadedVerses = PreloadedBibleData.getPreloadedVerses()
                    val preloadedPlans = PreloadedBibleData.getPreloadedPlans()
                    repository.insertPreloadedData(preloadedVerses, preloadedPlans)
                }
                updateLocalStats()
            } catch (e: Exception) {
                android.util.Log.e("BibleViewModel", "Failed to preload scriptures offline", e)
            }
        }
    }

    // --- State Variables ---
    private val _selectedTranslation = MutableStateFlow("WEB")
    val selectedTranslation: StateFlow<String> = _selectedTranslation.asStateFlow()

    private val _selectedBook = MutableStateFlow("Genesis")
    val selectedBook: StateFlow<String> = _selectedBook.asStateFlow()

    private val _selectedChapter = MutableStateFlow(1)
    val selectedChapter: StateFlow<Int> = _selectedChapter.asStateFlow()

    // Current active chapter verses
    val activeVerses: StateFlow<List<BibleVerse>> = combine(
        selectedTranslation,
        selectedBook,
        selectedChapter
    ) { trans, book, chap ->
        Triple(trans, book, chap)
    }.flatMapLatest { (trans, book, chap) ->
        repository.getVerses(trans, book, chap)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Highlights ---
    val allHighlights: StateFlow<List<UserHighlight>> = repository.allHighlights
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Notes ---
    val allNotes: StateFlow<List<UserNote>> = repository.allNotes
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Reading Plans ---
    val allReadingPlans: StateFlow<List<ReadingPlan>> = repository.allReadingPlans
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedReadingPlan = MutableStateFlow<ReadingPlan?>(null)
    val selectedReadingPlan: StateFlow<ReadingPlan?> = _selectedReadingPlan.asStateFlow()

    val selectedPlanProgress: StateFlow<List<ReadingPlanProgress>> = _selectedReadingPlan
        .flatMapLatest { plan ->
            if (plan != null) repository.getProgressForPlan(plan.planId)
            else flowOf(emptyList())
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Search ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<BibleVerse>> = combine(
        selectedTranslation,
        _searchQuery
    ) { trans, query ->
        Pair(trans, query)
    }.flatMapLatest { (trans, query) ->
        if (query.length >= 2) {
            repository.searchVerses(trans, query)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AI Interactive Assistant States ---
    private val _aiInsightText = MutableStateFlow("")
    val aiInsightText: StateFlow<String> = _aiInsightText.asStateFlow()

    private val _isAiInsightLoading = MutableStateFlow(false)
    val isAiInsightLoading: StateFlow<Boolean> = _isAiInsightLoading.asStateFlow()

    private val _illustrationText = MutableStateFlow("")
    val illustrationText: StateFlow<String> = _illustrationText.asStateFlow()

    private val _isIllustrationLoading = MutableStateFlow(false)
    val isIllustrationLoading: StateFlow<Boolean> = _isIllustrationLoading.asStateFlow()

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _isChapterDownloading = MutableStateFlow(false)
    val isChapterDownloading: StateFlow<Boolean> = _isChapterDownloading.asStateFlow()

    private val _chapterDownloadError = MutableStateFlow<String?>(null)
    val chapterDownloadError: StateFlow<String?> = _chapterDownloadError.asStateFlow()

    // --- Helper lists ---
    val booksList = BibleMeta.books.map { it.name }

    fun getChaptersForBook(book: String): List<Int> {
        val totalChaps = BibleMeta.bookMap[book]?.chapters ?: 1
        return (1..totalChaps).toList()
    }

    // --- Actions ---

    fun setTranslation(translation: String) {
        _selectedTranslation.value = translation
    }

    fun selectBook(book: String) {
        _selectedBook.value = book
        val chaps = getChaptersForBook(book)
        if (!chaps.contains(_selectedChapter.value)) {
            _selectedChapter.value = chaps.firstOrNull() ?: 1
        }
        clearChapterIllustration()
    }

    fun selectChapter(chapter: Int) {
        _selectedChapter.value = chapter
        clearChapterIllustration()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Highlights CRUD ---
    fun toggleHighlightVerse(verse: BibleVerse, colorHex: String) {
        viewModelScope.launch {
            val id = "${verse.translation}-${verse.book}-${verse.chapter}-${verse.verse}"
            val exists = allHighlights.value.any { it.id == id }
            if (exists) {
                // If it exists with the same color, we remove it. If a different color, update it!
                val currentHighlight = allHighlights.value.find { it.id == id }
                if (currentHighlight?.colorHex == colorHex) {
                    repository.removeHighlight(verse.translation, verse.book, verse.chapter, verse.verse)
                } else {
                    repository.addHighlight(verse.translation, verse.book, verse.chapter, verse.verse, colorHex)
                }
            } else {
                repository.addHighlight(verse.translation, verse.book, verse.chapter, verse.verse, colorHex)
            }
        }
    }

    // --- Notes CRUD ---
    fun saveNoteForVerse(verse: BibleVerse, noteText: String) {
        viewModelScope.launch {
            if (noteText.trim().isEmpty()) {
                repository.deleteNote(verse.translation, verse.book, verse.chapter, verse.verse)
            } else {
                repository.saveNote(verse.translation, verse.book, verse.chapter, verse.verse, noteText)
            }
        }
    }

    fun getNoteText(verse: BibleVerse): String {
        val id = "${verse.translation}-${verse.book}-${verse.chapter}-${verse.verse}"
        return allNotes.value.find { it.id == id }?.noteText ?: ""
    }

    // --- Reading Plans Actions ---
    fun selectReadingPlan(plan: ReadingPlan?) {
        _selectedReadingPlan.value = plan
    }

    fun toggleReadingDayCompleted(planId: String, day: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleDayCompleted(planId, day, completed)
        }
    }

    fun createCustomReadingPlan(topic: String, onSuccess: (ReadingPlan) -> Unit) {
        if (topic.trim().isEmpty() || _isGeneratingPlan.value) return
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            try {
                val newPlan = repository.createCustomAIPian(topic)
                selectReadingPlan(newPlan)
                onSuccess(newPlan)
            } catch (e: Exception) {
                // Fail-safe handles offline nicely in repo
            } finally {
                _isGeneratingPlan.value = false
            }
        }
    }

    // --- Gemini AI Request Triggers ---
    fun requestAiVerseInsight(verse: BibleVerse) {
        if (_isAiInsightLoading.value) return
        viewModelScope.launch {
            _isAiInsightLoading.value = true
            _aiInsightText.value = ""
            try {
                val insight = repository.getAiVerseInsight(verse.book, verse.chapter, verse.verse, verse.text)
                _aiInsightText.value = insight
            } catch (e: java.lang.Exception) {
                _aiInsightText.value = "Error fetching verse insights. Please check your internet connection."
            } finally {
                _isAiInsightLoading.value = false
            }
        }
    }

    fun requestChapterIllustration() {
        if (_isIllustrationLoading.value) return
        val book = _selectedBook.value
        val chapter = _selectedChapter.value
        viewModelScope.launch {
            _isIllustrationLoading.value = true
            _illustrationText.value = ""
            try {
                val desc = repository.getChapterIllustrationDescription(book, chapter)
                _illustrationText.value = desc
            } catch (e: java.lang.Exception) {
                _illustrationText.value = "Unable to fetch chapter illustration."
            } finally {
                _isIllustrationLoading.value = false
            }
        }
    }

    private fun clearChapterIllustration() {
        _illustrationText.value = ""
        _aiInsightText.value = ""
        _chapterDownloadError.value = null
    }

    fun downloadSelectedChapter() {
        if (_isChapterDownloading.value) return
        val book = _selectedBook.value
        val chapter = _selectedChapter.value
        val translation = _selectedTranslation.value
        viewModelScope.launch {
            _isChapterDownloading.value = true
            _chapterDownloadError.value = null
            try {
                repository.downloadChapterFromAi(book, chapter, translation)
            } catch (e: Exception) {
                _chapterDownloadError.value = "Failed to load chapter online. Please verify your Gemini API Key in AI Studio Secrets, check internet connectivity, and try again."
            } finally {
                _isChapterDownloading.value = false
            }
        }
    }

    fun clearChapterDownloadError() {
        _chapterDownloadError.value = null
    }

    // --- Cached Stats ---
    private val _webVerseCount = MutableStateFlow(0)
    val webVerseCount: StateFlow<Int> = _webVerseCount.asStateFlow()

    private val _kjvVerseCount = MutableStateFlow(0)
    val kjvVerseCount: StateFlow<Int> = _kjvVerseCount.asStateFlow()

    fun updateLocalStats() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _webVerseCount.value = repository.getVerseCountForTranslation("WEB")
            _kjvVerseCount.value = repository.getVerseCountForTranslation("KJV")
        }
    }

    // --- Bulk Caching Downloader ---
    private val _isBulkDownloading = MutableStateFlow(false)
    val isBulkDownloading: StateFlow<Boolean> = _isBulkDownloading.asStateFlow()

    private val _bulkDownloadProgress = MutableStateFlow(0f)
    val bulkDownloadProgress: StateFlow<Float> = _bulkDownloadProgress.asStateFlow()

    private val _bulkDownloadStatus = MutableStateFlow("")
    val bulkDownloadStatus: StateFlow<String> = _bulkDownloadStatus.asStateFlow()

    private val _bulkDownloadError = MutableStateFlow<String?>(null)
    val bulkDownloadError: StateFlow<String?> = _bulkDownloadError.asStateFlow()

    fun downloadEntireBibleOffline(translation: String, scope: String = "ALL") {
        if (_isBulkDownloading.value) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _isBulkDownloading.value = true
            _bulkDownloadError.value = null
            _bulkDownloadProgress.value = 0f
            _bulkDownloadStatus.value = "Initiating bulk caching stream..."
            
            try {
                val okClient = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(12, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(12, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val targetBooks = when(scope) {
                    "OT" -> BibleMeta.books.filter { it.testament == "Old Testament" }
                    "NT" -> BibleMeta.books.filter { it.testament == "New Testament" }
                    else -> BibleMeta.books
                }

                val totalChapters = targetBooks.sumOf { it.chapters }
                val completedChapters = java.util.concurrent.atomic.AtomicInteger(0)

                for (book in targetBooks) {
                    if (_bulkDownloadError.value != null) break
                    
                    val bookName = book.name
                    val bookId = book.id
                    val chapterCount = book.chapters
                    val versesToInsert = java.util.Collections.synchronizedList(mutableListOf<BibleVerse>())
                    
                    _bulkDownloadStatus.value = "Caching book: $bookName..."
                    
                    // Controlled parallel downloads
                    val semaphore = kotlinx.coroutines.sync.Semaphore(5)
                    
                    val jobs = (1..chapterCount).map { chapter ->
                        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            semaphore.acquire()
                            try {
                                if (_bulkDownloadError.value == null) {
                                    val url = "https://bible-api.com/${java.net.URLEncoder.encode("$bookName $chapter", "UTF-8")}?translation=${translation.lowercase()}"
                                    val request = okhttp3.Request.Builder().url(url).build()
                                    
                                    okClient.newCall(request).execute().use { response ->
                                        if (response.isSuccessful) {
                                            val bodyStr = response.body?.string() ?: ""
                                            if (bodyStr.isNotEmpty()) {
                                                val jsonObj = org.json.JSONObject(bodyStr)
                                                val versesArr = jsonObj.getJSONArray("verses")
                                                val tempVerses = mutableListOf<BibleVerse>()
                                                for (i in 0 until versesArr.length()) {
                                                    val vObj = versesArr.getJSONObject(i)
                                                    val verseNum = vObj.getInt("verse")
                                                    val verseText = vObj.getString("text").trim()
                                                    tempVerses.add(
                                                        BibleVerse(
                                                            translation = translation.uppercase(),
                                                            book = bookName,
                                                            bookId = bookId,
                                                            chapter = chapter,
                                                            verse = verseNum,
                                                            text = verseText
                                                        )
                                                    )
                                                }
                                                versesToInsert.addAll(tempVerses)
                                            }
                                        } else {
                                            android.util.Log.e("BibleViewModel", "Error fetch $bookName $chapter: ${response.code}")
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("BibleViewModel", "Failed chapter download: $bookName $chapter", e)
                            } finally {
                                semaphore.release()
                                val currentCount = completedChapters.incrementAndGet()
                                val currentProgress = currentCount.toFloat() / totalChapters.toFloat()
                                _bulkDownloadProgress.value = currentProgress
                                _bulkDownloadStatus.value = "Cached $bookName Chapter $chapter ($currentCount/$totalChapters)..."
                            }
                        }
                    }
                    
                    jobs.forEach { it.join() }

                    // Save this book's scriptures to database in a clean block transaction
                    if (versesToInsert.isNotEmpty()) {
                        repository.insertPreloadedData(versesToInsert.toList(), emptyList())
                        updateLocalStats()
                    }
                    
                    // Mild delay for API-friendliness
                    kotlinx.coroutines.delay(80)
                }
                
                if (_bulkDownloadError.value == null) {
                    _bulkDownloadStatus.value = "$translation Bible is fully cached and offline-ready!"
                    _bulkDownloadProgress.value = 1f
                }
                updateLocalStats()
                kotlinx.coroutines.delay(2000)
            } catch (e: Exception) {
                android.util.Log.e("BibleViewModel", "Bulk download failed", e)
                _bulkDownloadError.value = "Failed to cache offline scriptures: ${e.message}"
            } finally {
                _isBulkDownloading.value = false
            }
        }
    }
}
