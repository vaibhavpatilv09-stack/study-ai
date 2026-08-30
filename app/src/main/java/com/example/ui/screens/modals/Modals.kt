package com.example.ui.screens.modals

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KnowledgeNode
import com.example.data.model.SubjectCategory
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@Composable
fun NotificationCenterModal(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.toggleNotificationCenter(false) },
        modifier = modifier.fillMaxWidth(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications & Reminders",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { viewModel.toggleNotificationCenter(false) }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.notifications) { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(IndigoPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (notif.iconName) {
                                        "streak" -> "🔥"
                                        "cards" -> "🧠"
                                        "trophy" -> "🏆"
                                        else -> "📚"
                                    },
                                    fontSize = 18.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = notif.timeAgo,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.toggleNotificationCenter(false) }) {
                Text(text = "Done")
            }
        }
    )
}

@Composable
fun SessionSetupModal(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var topicText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(uiState.selectedCategory) }

    AlertDialog(
        onDismissRequest = { viewModel.toggleSessionSetupModal(false) },
        modifier = modifier.fillMaxWidth(),
        title = {
            Text(
                text = "Generate AI Flashcard Deck",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enter any subject topic, chapter, or exam objective to generate 4 high-yield active recall flashcards with Gemini AI.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = topicText,
                    onValueChange = { topicText = it },
                    label = { Text("Topic (e.g., Photosynthesis Dark Reactions)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_deck_topic_input")
                )

                Text(
                    text = "Select Category:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SubjectCategory.entries.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = cat.displayName, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topicText.isNotBlank()) {
                        viewModel.generateCardsFromTopic(topicText, selectedCategory)
                        viewModel.toggleSessionSetupModal(false)
                    }
                },
                enabled = topicText.isNotBlank(),
                modifier = Modifier.testTag("confirm_generate_deck_button")
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Generate Cards")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.toggleSessionSetupModal(false) }) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
fun DocumentScannerModal(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(uiState.selectedCategory) }

    AlertDialog(
        onDismissRequest = { viewModel.toggleDocumentScannerModal(false) },
        modifier = modifier.fillMaxWidth(),
        title = {
            Text(
                text = "Study Document & Notes Scanner",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Paste or type your study notes, syllabus syllabus excerpts, or lecture transcripts for Gemini to synthesize and generate active recall questions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title") },
                    modifier = Modifier.fillMaxWidth().testTag("doc_title_input")
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Notes Content / Text") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("doc_text_input"),
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && noteText.isNotBlank()) {
                        viewModel.analyzeAndSaveDocument(title, noteText, selectedCategory)
                    }
                },
                enabled = title.isNotBlank() && noteText.isNotBlank(),
                modifier = Modifier.testTag("analyze_save_doc_button")
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Analyze & Summarize")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.toggleDocumentScannerModal(false) }) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
fun KnowledgeNodeDetailModal(
    node: KnowledgeNode,
    onDismiss: () -> Unit,
    onStartStudy: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Chapter ${node.chapterOrder}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(node.category.colorHex)
                    )
                    Text(
                        text = node.topicTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = node.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Current Mastery: ${node.masteryScore}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (node.masteryScore > 75) EmeraldSuccess else IndigoPrimary
                    )
                    Text(
                        text = "${node.flashcardCount} Active Cards",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Core Takeaways:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                node.keyTakeaways.forEach { takeaway ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✓ ", color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        Text(text = takeaway, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onStartStudy,
                colors = ButtonDefaults.buttonColors(containerColor = Color(node.category.colorHex))
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Practice Flashcards")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close")
            }
        }
    )
}

@Composable
fun ExportChatModal(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsState()

    val formattedTranscript = remember(messages) {
        buildString {
            appendLine("# StudyMate AI Tutor Session Transcript")
            appendLine("Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
            appendLine("----------------------------------------\n")
            messages.forEach { msg ->
                if (msg.role == "user") {
                    appendLine("## 👤 User:")
                    appendLine(msg.text)
                    appendLine()
                } else {
                    appendLine("## 🤖 StudyMate AI:")
                    appendLine(msg.text)
                    if (!msg.thinkingProcess.isNullOrBlank()) {
                        appendLine("\n> **Thinking Trace:** ${msg.thinkingProcess}")
                    }
                    if (msg.citations.isNotEmpty()) {
                        appendLine("\n**Sources:**")
                        msg.citations.forEach { c -> appendLine("- [${c.title}](${c.url}): ${c.snippet}") }
                    }
                    appendLine()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { viewModel.toggleExportChatDialog(false) },
        modifier = modifier.fillMaxWidth(),
        title = {
            Text(
                text = "Export Conversation Transcript",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Export your full multi-turn AI study session in Markdown format to save into Obsidian, Notion, or text notes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = formattedTranscript,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("StudyMate Transcript", formattedTranscript)
                    clipboard.setPrimaryClip(clip)
                    viewModel.toggleExportChatDialog(false)
                },
                modifier = Modifier.testTag("copy_transcript_button")
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Copy to Clipboard")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.toggleExportChatDialog(false) }) {
                Text(text = "Close")
            }
        }
    )
}
