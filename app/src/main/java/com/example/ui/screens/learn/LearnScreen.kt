package com.example.ui.screens.learn

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.FlashcardFlipCard
import com.example.ui.components.SubjectCategoryFilterChips
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

enum class LearnSubView {
    ROADMAP,
    FLASHCARDS,
    DOCUMENTS
}

@Composable
fun LearnScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val allDecks by viewModel.allDecks.collectAsState()
    val knowledgeNodes by viewModel.allKnowledgeNodes.collectAsState()
    val allDocuments by viewModel.allDocuments.collectAsState()

    var activeSubView by remember { mutableStateOf(LearnSubView.ROADMAP) }

    // If deck selected for study, auto-switch to Flashcard player
    LaunchedEffect(uiState.selectedDeckForStudy) {
        if (uiState.selectedDeckForStudy != null) {
            activeSubView = LearnSubView.FLASHCARDS
        }
    }

    val activeDeck = uiState.selectedDeckForStudy ?: allDecks.find { it.category == uiState.selectedCategory } ?: allDecks.firstOrNull()
    val activeDeckCards by if (activeDeck != null) {
        viewModel.studyRepository.getCardsForDeck(activeDeck.id).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<Flashcard>()) }
    }

    val filteredNodes = knowledgeNodes.filter { it.category == uiState.selectedCategory }
    val filteredDocs = allDocuments.filter { it.category == uiState.selectedCategory }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Subview Tab Switcher (Roadmap / Flashcards / Documents)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LearnTabButton(
                    title = "Knowledge Map",
                    icon = Icons.Default.AccountTree,
                    isSelected = activeSubView == LearnSubView.ROADMAP,
                    onClick = { activeSubView = LearnSubView.ROADMAP },
                    modifier = Modifier.weight(1f)
                )
                LearnTabButton(
                    title = "Flashcards",
                    icon = Icons.Default.Style,
                    isSelected = activeSubView == LearnSubView.FLASHCARDS,
                    onClick = { activeSubView = LearnSubView.FLASHCARDS },
                    modifier = Modifier.weight(1f)
                )
                LearnTabButton(
                    title = "Study Guides",
                    icon = Icons.Default.MenuBook,
                    isSelected = activeSubView == LearnSubView.DOCUMENTS,
                    onClick = { activeSubView = LearnSubView.DOCUMENTS },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subject category filter
        SubjectCategoryFilterChips(
            selectedCategory = uiState.selectedCategory,
            onSelect = { viewModel.selectCategory(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (activeSubView) {
            LearnSubView.ROADMAP -> {
                KnowledgeMapRoadmapView(
                    nodes = filteredNodes,
                    selectedCategory = uiState.selectedCategory,
                    onNodeClick = { viewModel.selectKnowledgeNode(it) },
                    onGenerateMore = {
                        viewModel.generateCardsFromTopic(
                            "Advanced ${uiState.selectedCategory.displayName} Mechanisms",
                            uiState.selectedCategory
                        )
                    }
                )
            }
            LearnSubView.FLASHCARDS -> {
                FlashcardsStudyView(
                    deck = activeDeck,
                    cards = activeDeckCards,
                    currentIdx = uiState.currentFlashcardIndex,
                    isFlipped = uiState.isCardFlipped,
                    sessionReviewedCount = uiState.sessionReviewedCount,
                    sessionMasteredCount = uiState.sessionMasteredCount,
                    onFlip = { viewModel.flipCard() },
                    onRate = { rating ->
                        val card = activeDeckCards.getOrNull(uiState.currentFlashcardIndex)
                        if (card != null) {
                            viewModel.rateFlashcard(card, rating, activeDeckCards.size)
                        }
                    },
                    onRestart = {
                        if (activeDeck != null) {
                            viewModel.selectDeckForStudy(activeDeck)
                        }
                    },
                    onGenerateCards = {
                        viewModel.toggleSessionSetupModal(true)
                    }
                )
            }
            LearnSubView.DOCUMENTS -> {
                StudyDocumentsListView(
                    documents = filteredDocs,
                    category = uiState.selectedCategory,
                    onScanNew = { viewModel.toggleDocumentScannerModal(true) }
                )
            }
        }
    }
}

@Composable
fun LearnTabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("learn_tab_${title.lowercase().replace(" ", "_")}"),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun KnowledgeMapRoadmapView(
    nodes: List<KnowledgeNode>,
    selectedCategory: SubjectCategory,
    onNodeClick: (KnowledgeNode) -> Unit,
    onGenerateMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Curriculum Roadmap",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Master topics progressively to unlock advanced nodes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onGenerateMore,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Add Node", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(nodes) { node ->
            RoadmapNodeCard(
                node = node,
                onClick = { onNodeClick(node) }
            )
        }
    }
}

@Composable
fun RoadmapNodeCard(
    node: KnowledgeNode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = Color(node.category.colorHex)
    val statusColor = when (node.status) {
        MasteryStatus.MASTERED -> EmeraldSuccess
        MasteryStatus.REVIEWING -> CyanSecondary
        MasteryStatus.IN_PROGRESS -> AmberAccent
        MasteryStatus.LOCKED -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("node_card_${node.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (node.status != MasteryStatus.LOCKED) categoryColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mastery Progress Circle / Node Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (node.status == MasteryStatus.LOCKED) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    CircularProgressIndicator(
                        progress = { node.masteryScore / 100f },
                        modifier = Modifier.size(44.dp),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 3.5.dp
                    )
                    Text(
                        text = "${node.masteryScore}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CHAPTER ${node.chapterOrder}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = categoryColor
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = node.status.name.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = node.topicTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = node.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${node.flashcardCount} cards",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${node.estimatedMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun FlashcardsStudyView(
    deck: StudyDeck?,
    cards: List<Flashcard>,
    currentIdx: Int,
    isFlipped: Boolean,
    sessionReviewedCount: Int,
    sessionMasteredCount: Int,
    onFlip: () -> Unit,
    onRate: (Int) -> Unit,
    onRestart: () -> Unit,
    onGenerateCards: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (deck == null || cards.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Style,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(54.dp)
                )
                Text(
                    text = "No Flashcards in this Category",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Generate a custom high-yield flashcard deck using StudyMate's Gemini AI generator.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onGenerateCards,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Generate AI Deck")
                }
            }
        }
        return
    }

    if (currentIdx >= cards.size) {
        // Session Completed Screen
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(EmeraldSuccess.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Success",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Study Session Complete! 🎉",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Great job! You reviewed $sessionReviewedCount cards and advanced mastery on $sessionMasteredCount concepts in ${deck.title}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRestart,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Review Again")
                    }

                    FilledTonalButton(
                        onClick = onGenerateCards,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "+ AI Cards")
                    }
                }
            }
        }
        return
    }

    val currentCard = cards[currentIdx]

    FlashcardFlipCard(
        card = currentCard,
        isFlipped = isFlipped,
        onFlip = onFlip,
        onRate = onRate,
        currentIdx = currentIdx,
        totalCount = cards.size,
        modifier = modifier
    )
}

@Composable
fun StudyDocumentsListView(
    documents: List<StudyDocument>,
    category: SubjectCategory,
    onScanNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI Study Guides & Summaries",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Synthesized notes with high-yield concepts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onScanNew,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("scan_new_document_button")
                ) {
                    Icon(imageVector = Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (documents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No study documents yet for ${category.displayName}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(documents) { doc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(doc.category.colorHex).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = doc.category.displayName.substringBefore(" &"),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(doc.category.colorHex)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = doc.aiSummary,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Key Concepts:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = IndigoPrimary
                        )

                        doc.keyConcepts.take(3).forEach { concept ->
                            Row(
                                modifier = Modifier.padding(top = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "• ", color = IndigoPrimary, fontWeight = FontWeight.Bold)
                                Text(
                                    text = concept,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
