package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BibleVerse
import com.example.data.ReadingPlan
import com.example.api.GeneratedDayPlan
import androidx.compose.foundation.BorderStroke
import com.example.viewmodel.BibleViewModel
import org.json.JSONArray
import org.json.JSONObject

// Main destinations
enum class BibleTab(val label: String) {
    READER("Reader"),
    PLANS("Plans"),
    EXPLORER("Explorer"),
    SEARCH("Search")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleApp(
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(BibleTab.READER) }
    val currentBook by viewModel.selectedBook.collectAsStateWithLifecycle()
    val currentChapter by viewModel.selectedChapter.collectAsStateWithLifecycle()
    val currentTranslation by viewModel.selectedTranslation.collectAsStateWithLifecycle()

    val selectedPlan by viewModel.selectedReadingPlan.collectAsStateWithLifecycle()

    // Handle back button on reading plans detail page
    if (activeTab == BibleTab.PLANS && selectedPlan != null) {
        BackHandler {
            viewModel.selectReadingPlan(null)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Grace Bible",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Sustenance for the Soul",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                },
                actions = {
                    if (activeTab == BibleTab.READER) {
                        // Translation switch capsule
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("WEB", "KJV").forEach { trans ->
                                val isSelected = trans == currentTranslation
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else Color.Transparent
                                        )
                                        .clickable { viewModel.setTranslation(trans) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("translation_tab_$trans")
                                ) {
                                    Text(
                                        text = trans,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == BibleTab.READER,
                    onClick = { activeTab = BibleTab.READER },
                    icon = { Icon(Icons.Outlined.Book, contentDescription = "Bible Reader") },
                    label = { Text("Reader") },
                    modifier = Modifier.testTag("nav_reader")
                )
                NavigationBarItem(
                    selected = activeTab == BibleTab.PLANS,
                    onClick = { activeTab = BibleTab.PLANS },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Reading Plans") },
                    label = { Text("Plans") },
                    modifier = Modifier.testTag("nav_plans")
                )
                NavigationBarItem(
                    selected = activeTab == BibleTab.EXPLORER,
                    onClick = { activeTab = BibleTab.EXPLORER },
                    icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Highlights and Notes") },
                    label = { Text("Explorer") },
                    modifier = Modifier.testTag("nav_explorer")
                )
                NavigationBarItem(
                    selected = activeTab == BibleTab.SEARCH,
                    onClick = { activeTab = BibleTab.SEARCH },
                    icon = { Icon(Icons.Outlined.Search, contentDescription = "Keyword Search") },
                    label = { Text("Search") },
                    modifier = Modifier.testTag("nav_search")
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
            },
            label = "tab_navigation"
        ) { targetTab ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (targetTab) {
                    BibleTab.READER -> ReaderScreen(viewModel)
                    BibleTab.PLANS -> ReadingPlansScreen(viewModel)
                    BibleTab.EXPLORER -> ExplorerScreen(viewModel, onNavigateToVerse = { book, chap ->
                        viewModel.selectBook(book)
                        viewModel.selectChapter(chap)
                        activeTab = BibleTab.READER
                    })
                    BibleTab.SEARCH -> SearchScreen(viewModel, onNavigateToVerse = { book, chap ->
                        viewModel.selectBook(book)
                        viewModel.selectChapter(chap)
                        activeTab = BibleTab.READER
                    })
                }
            }
        }
    }
}

// ==================== destination 1: READER SCREEN ====================

@Composable
fun ReaderScreen(viewModel: BibleViewModel) {
    val verses by viewModel.activeVerses.collectAsStateWithLifecycle()
    val selectedBook by viewModel.selectedBook.collectAsStateWithLifecycle()
    val selectedChapter by viewModel.selectedChapter.collectAsStateWithLifecycle()
    val highlights by viewModel.allHighlights.collectAsStateWithLifecycle()
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()

    val illustration by viewModel.illustrationText.collectAsStateWithLifecycle()
    val isIllustrationLoading by viewModel.isIllustrationLoading.collectAsStateWithLifecycle()

    var selectedVerseForAction by remember { mutableStateOf<BibleVerse?>(null) }
    var bookDropdownExpanded by remember { mutableStateOf(false) }
    var chapterDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Book & Chapter Pickers Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Book Selector Button
            Box(modifier = Modifier.weight(1.5f)) {
                OutlinedButton(
                    onClick = { bookDropdownExpanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_picker"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedBook, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = bookDropdownExpanded,
                    onDismissRequest = { bookDropdownExpanded = false }
                ) {
                    viewModel.booksList.forEach { bk ->
                        DropdownMenuItem(
                            text = { Text(bk) },
                            onClick = {
                                viewModel.selectBook(bk)
                                bookDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Chapter Selector Button
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { chapterDropdownExpanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chapter_picker"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ch. $selectedChapter")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = chapterDropdownExpanded,
                    onDismissRequest = { chapterDropdownExpanded = false }
                ) {
                    viewModel.getChaptersForBook(selectedBook).forEach { chap ->
                        DropdownMenuItem(
                            text = { Text("Chapter $chap") },
                            onClick = {
                                viewModel.selectChapter(chap)
                                chapterDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Chapter Illustration Card (Artistic gradient with optional Gemini interpretation on top)
            item {
                ChapterIllustrationCard(
                    book = selectedBook,
                    chapter = selectedChapter,
                    illustratedDescription = illustration,
                    isLoading = isIllustrationLoading,
                    onTriggerIllustration = { viewModel.requestChapterIllustration() }
                )
            }

            if (verses.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Initializing offline scriptures...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                items(verses) { verse ->
                    val highlightId = "${verse.translation}-${verse.book}-${verse.chapter}-${verse.verse}"
                    val activeHighlight = highlights.find { it.id == highlightId }
                    val hasNote = notes.any { it.id == highlightId }

                    VerseRow(
                        verse = verse,
                        highlightColorHex = activeHighlight?.colorHex,
                        hasNote = hasNote,
                        isSelected = selectedVerseForAction == verse,
                        onClick = {
                            selectedVerseForAction = if (selectedVerseForAction == verse) null else verse
                        }
                    )
                }
            }
        }
    }

    // Interactive Verse bottom/modal sheet drawer
    selectedVerseForAction?.let { verse ->
        InteractiveVerseDrawer(
            verse = verse,
            viewModel = viewModel,
            onDismiss = { selectedVerseForAction = null }
        )
    }
}

@Composable
fun ChapterIllustrationCard(
    book: String,
    chapter: Int,
    illustratedDescription: String,
    isLoading: Boolean,
    onTriggerIllustration: () -> Unit
) {
    // Elegant custom painted background depending on Book type
    val colorBrush = remember(book) {
        when (book) {
            "Genesis" -> Brush.linearGradient(listOf(Color(0xFFE5A05E), Color(0xFF2E7B82)))
            "Psalms" -> Brush.linearGradient(listOf(Color(0xFF4DA1A9), Color(0xFF2E4F4F)))
            "Proverbs" -> Brush.linearGradient(listOf(Color(0xFFB38B43), Color(0xFF5C2C42)))
            "Matthew" -> Brush.linearGradient(listOf(Color(0xFFD67A41), Color(0xFFEBC173)))
            "John" -> Brush.linearGradient(listOf(Color(0xFF432D7A), Color(0xFF8F51A1)))
            else -> Brush.linearGradient(listOf(Color(0xFF6F7D8C), Color(0xFF384353)))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("chapter_illustration_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .drawBehind {
                    drawRect(brush = colorBrush)
                }
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = book,
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = Color.White,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Text(
                            text = "Chapter $chapter",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }

                    // Compact load button
                    IconButton(
                        onClick = onTriggerIllustration,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("load_illustration_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Query Gemini for art concept",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (illustratedDescription.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.padding(bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                               )
                               Spacer(modifier = Modifier.width(6.dp))
                               Text(
                                   "GEMINI ILLUSTRATIVE VIEW",
                                   style = MaterialTheme.typography.labelSmall.copy(
                                       color = Color(0xFFFFD700),
                                       fontWeight = FontWeight.Bold
                                   )
                               )
                            }
                            Text(
                                text = illustratedDescription,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTriggerIllustration() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Brush,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Tap to generate chapter description with Gemini AI",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VerseRow(
    verse: BibleVerse,
    highlightColorHex: String?,
    hasNote: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val highlightColor = remember(highlightColorHex) {
        if (highlightColorHex != null) {
            Color(android.graphics.Color.parseColor(highlightColorHex))
        } else {
            Color.Transparent
        }
    }

    val surfaceSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                if (isSelected) surfaceSelectedColor
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("verse_${verse.verse}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Verse Number
            Text(
                text = verse.verse.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif
                ),
                modifier = Modifier
                    .width(28.dp)
                    .padding(top = 1.dp)
            )

            // Verse Text (highlighted if color chosen)
            Text(
                text = verse.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    lineHeight = 26.sp
                ),
                modifier = Modifier
                    .weight(1f)
                    .drawBehind {
                        if (highlightColor != Color.Transparent) {
                            drawRect(
                                color = highlightColor.copy(alpha = 0.35f),
                                size = size
                            )
                        }
                    }
            )

            if (hasNote) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Has attachment note",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(start = 6.dp)
                )
            }
        }
    }
}

@Composable
fun InteractiveVerseDrawer(
    verse: BibleVerse,
    viewModel: BibleViewModel,
    onDismiss: () -> Unit
) {
    val highlights by viewModel.allHighlights.collectAsStateWithLifecycle()
    val isInsightLoading by viewModel.isAiInsightLoading.collectAsStateWithLifecycle()
    val aiInsightText by viewModel.aiInsightText.collectAsStateWithLifecycle()

    var showNotesDialog by remember { mutableStateOf(false) }
    val initialNote = remember(verse) { viewModel.getNoteText(verse) }

    val context = LocalContext.current

    // Detect if this verse is already highlighted
    val activeHighlight = remember(highlights, verse) {
        val id = "${verse.translation}-${verse.book}-${verse.chapter}-${verse.verse}"
        highlights.find { it.id == id }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("verse_action_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header (e.g. Genesis 1:1 KJV)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${verse.book} ${verse.chapter}:${verse.verse} (${verse.translation})",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close actions panel")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                // Highlight Color Capsule Options
                Text(
                    "Highlight Verse",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Standard premium highlighter colors (Yellow, Blue, Pink, Green)
                    val colors = listOf(
                        "#FFF176" to "Yellow",
                        "#80DEEA" to "Teal",
                        "#F48FB1" to "Pink",
                        "#C5E1A5" to "Green"
                    )

                    colors.forEach { (hex, name) ->
                        val colorVal = Color(android.graphics.Color.parseColor(hex))
                        val isHighlit = activeHighlight?.colorHex == hex

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorVal)
                                .border(
                                    width = if (isHighlit) 3.dp else 1.dp,
                                    color = if (isHighlit) MaterialTheme.colorScheme.primary else Color.Black.copy(
                                        alpha = 0.2f
                                    ),
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.toggleHighlightVerse(verse, hex)
                                }
                                .testTag("highlight_color_$name"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isHighlit) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.DarkGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (activeHighlight != null) {
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                viewModel.toggleHighlightVerse(verse, activeHighlight.colorHex)
                            },
                            modifier = Modifier.testTag("clear_highlight")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete highlit marker")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Study Actions grid/column
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Add Notes button
                    Button(
                        onClick = { showNotesDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_note_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            if (initialNote.isNotEmpty()) Icons.Default.Edit else Icons.Default.NoteAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (initialNote.isNotEmpty()) "Edit Note" else "Add Note", fontSize = 13.sp)
                    }

                    // Share button
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                val shareText = """
                                    "${verse.text}"
                                    - ${verse.book} ${verse.chapter}:${verse.verse} (${verse.translation})
                                    
                                    ${if (initialNote.isNotEmpty()) "My Study Insights:\n$initialNote" else ""}
                                    
                                    Shared via Grace Bible App.
                                """.trimIndent()
                                putExtra(Intent.EXTRA_SUBJECT, "Bible Insight")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Insight via"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_verse_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gemini Theological commentary section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_insight_section"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Gemini Verse Insights",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            if (aiInsightText.isEmpty() && !isInsightLoading) {
                                Button(
                                    onClick = { viewModel.requestAiVerseInsight(verse) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Ask AI", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (isInsightLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Pondering commentary context...", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else if (aiInsightText.isNotEmpty()) {
                            Text(
                                aiInsightText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 20.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        } else {
                            Text(
                                "Request theological and historical depth for this specific verse using Gemini AI.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to add a note
    if (showNotesDialog) {
        var noteInputText by remember { mutableStateOf(initialNote) }

        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Study Notes") },
            text = {
                Column {
                    Text(
                        "${verse.book} ${verse.chapter}:${verse.verse} (${verse.translation})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = noteInputText,
                        onValueChange = { noteInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("note_input_field"),
                        placeholder = { Text("Type key study insights here...") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveNoteForVerse(verse, noteInputText)
                        showNotesDialog = false
                    },
                    modifier = Modifier.testTag("save_note_confirm")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==================== destination 2: PLANS SCREEN ====================

@Composable
fun ReadingPlansScreen(viewModel: BibleViewModel) {
    val plans by viewModel.allReadingPlans.collectAsStateWithLifecycle()
    val selectedPlan by viewModel.selectedReadingPlan.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingPlan.collectAsStateWithLifecycle()

    var customTopicInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    if (selectedPlan != null) {
        // Deep Plan Detail Progress checklist view
        ReadingPlanDetailView(plan = selectedPlan!!, viewModel = viewModel)
    } else {
        // Dashboard Overview of Plans & Generate Custom Plans Container
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Scripture Study Plans",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif)
                )
                Text(
                    "Build habits, check off targets, and request customizable daily journeys.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Gemini Plan generator capsule card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_plan_generator_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Create Personalized Reading Plan",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            "Type in any devotional theme (e.g., Guidance, Overcoming stress, Parenting) and Gemini will instantly build a 5-day Scripture plan tailored for you offline.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customTopicInput,
                                onValueChange = { customTopicInput = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_topic_input"),
                                placeholder = { Text("E.g., Patience, Hope, Comfort", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f),
                                    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                    viewModel.createCustomReadingPlan(customTopicInput) {
                                        customTopicInput = ""
                                    }
                                })
                            )

                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    viewModel.createCustomReadingPlan(customTopicInput) {
                                        customTopicInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("generate_plan_btn")
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Generate Plan")
                                }
                            }
                        }
                    }
                }
            }

            // Existing Plans header
            item {
                Text(
                    "Select Study Journey",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (plans.isEmpty()) {
                item {
                    Text(
                        "Loading preloaded reading plans...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                items(plans.size) { index ->
                    val plan = plans[index]
                    val isSystemDark = isSystemInDarkTheme()
                    val (bgColor, textColor) = when (index % 4) {
                        0 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                        1 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                        2 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                        else -> {
                            val infoColor = if (isSystemDark) com.example.ui.theme.BentoDarkInfoContainer else com.example.ui.theme.BentoLightInfoContainer
                            val onInfoColor = if (isSystemDark) com.example.ui.theme.BentoDarkOnInfoContainer else com.example.ui.theme.BentoLightOnInfoContainer
                            infoColor to onInfoColor
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectReadingPlan(plan) }
                            .testTag("plan_card_${plan.planId}"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = bgColor,
                            contentColor = textColor
                        )
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = plan.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Badge(
                                    containerColor = textColor.copy(alpha = 0.15f),
                                    contentColor = textColor
                                ) {
                                    Text(
                                        "${plan.totalDays} Days",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = plan.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = textColor.copy(alpha = 0.8f)
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingPlanDetailView(plan: ReadingPlan, viewModel: BibleViewModel) {
    val progressList by viewModel.selectedPlanProgress.collectAsStateWithLifecycle()

    // Parse the Json array of days/verses
    val dayPlans = remember(plan) {
        val list = mutableListOf<GeneratedDayPlan>()
        try {
            val arr = JSONArray(plan.versesPerDayJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    GeneratedDayPlan(
                        day = obj.getInt("day"),
                        passage = obj.getString("passage"),
                        notes = obj.optString("notes", "")
                    )
                )
            }
        } catch (e: Exception) {
            // Safe fallback
        }
        list
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header navigation back row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.selectReadingPlan(null) },
                modifier = Modifier.testTag("plan_detail_back")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to reading plan list")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = plan.title,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Overview",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(plan.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                Text(
                    "Your Progress Companion",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(dayPlans) { dayPlan ->
                val isCompleted = progressList.any { it.day == dayPlan.day && it.completed }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reading_day_${dayPlan.day}"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox to complete day progress
                        Checkbox(
                            checked = isCompleted,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleReadingDayCompleted(plan.planId, dayPlan.day, isChecked)
                            },
                            modifier = Modifier.testTag("chk_completed_${dayPlan.day}")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "DAY ${dayPlan.day}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                dayPlan.passage,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                modifier = Modifier
                                    .clickable {
                                        // Navigate reader directly to this passage!
                                        // Parse book and chapter out of passage representation
                                        // E.g. "John 1:1-5" or "Psalms 23:1-3"
                                        val parts = dayPlan.passage.split(" ")
                                        if (parts.size >= 2) {
                                            val potentialBook = parts[0]
                                            val rawChapAndVerses = parts[1]
                                            val chapNo = rawChapAndVerses.split(":")[0].toIntOrNull() ?: 1
                                            viewModel.selectBook(potentialBook)
                                            viewModel.selectChapter(chapNo)
                                        }
                                    }
                                    .testTag("passage_link_${dayPlan.day}")
                            )
                            if (dayPlan.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    dayPlan.notes,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }

                        // Arrow link helper
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Read scriptures for day",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==================== destination 3: EXPLORER SCREEN ====================

@Composable
fun ExplorerScreen(
    viewModel: BibleViewModel,
    onNavigateToVerse: (String, Int) -> Unit
) {
    val highlights by viewModel.allHighlights.collectAsStateWithLifecycle()
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("notes") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("notes" to "My Notes (✍️)", "highlights" to "Highlights (🎨)").forEach { (t, lbl) ->
                val isSelected = activeSubTab == t
                Button(
                    onClick = { activeSubTab = t },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("explorer_subtab_$t"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(lbl, fontSize = 13.sp)
                }
            }
        }

        if (activeSubTab == "notes") {
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Note,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "No study notes yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tap any verse in the Reader to append personalized notes and reflections.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_card_${note.id}"),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${note.book} ${note.chapter}:${note.verse} (${note.translation})",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    IconButton(
                                        onClick = {
                                            viewModel.saveNoteForVerse(
                                                BibleVerse(
                                                    translation = note.translation,
                                                    book = note.book,
                                                    bookId = 1,
                                                    chapter = note.chapter,
                                                    verse = note.verse,
                                                    text = ""
                                                ),
                                                ""
                                            )
                                        },
                                        modifier = Modifier.testTag("delete_note_explorer_${note.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete note",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = note.noteText,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Button(
                                    onClick = { onNavigateToVerse(note.book, note.chapter) },
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .testTag("navigate_from_note_btn_${note.id}"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp)
                                ) {
                                    Text("Open Passage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Highlights list
            if (highlights.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.BorderColor,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "No highlights yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Mark key scriptures with beautiful highlighter markers in the Reader.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(highlights) { highlit ->
                        val highlightColor = remember(highlit.colorHex) {
                            Color(android.graphics.Color.parseColor(highlit.colorHex))
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("highlight_card_${highlit.id}"),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = highlightColor.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(highlightColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${highlit.book} ${highlit.chapter}:${highlit.verse} (${highlit.translation})",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                IconButton(
                                    onClick = { onNavigateToVerse(highlit.book, highlit.chapter) },
                                    modifier = Modifier.testTag("navigate_from_highlight_${highlit.id}")
                                ) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = "Go to verse in context",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== destination 4: SEARCH SCREEN ====================

@Composable
fun SearchScreen(
    viewModel: BibleViewModel,
    onNavigateToVerse: (String, Int) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("search_text_input"),
            placeholder = { Text("Search terms (e.g., creation, shepherd, light)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (searchQuery.length < 2) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Enter at least 2 characters to trigger offline search...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No matching verses found for \"$searchQuery\".\nTo query deeper, navigate to context in the Reader.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Found ${searchResults.size} Matching Passages",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(searchResults) { verse ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToVerse(verse.book, verse.chapter) }
                            .testTag("search_result_${verse.id}"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${verse.book} ${verse.chapter}:${verse.verse}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ) {
                                    Text(verse.translation, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = verse.text,
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// Icon Box wrapper with custom dimensions for fallback usage
@Composable
fun IconBox(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        tint = tint
    )
}
