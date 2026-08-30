package com.example.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectCategory
import com.example.data.model.UserProfileStats
import com.example.ui.components.StudyEngagementHeatmap
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ProgressScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val userStats by viewModel.userStats.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val stats = userStats ?: UserProfileStats()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // 1. Overall Mastery Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("progress_overview_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Mastery Analytics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time cognitive retention and spaced recall tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Metric 1: Total Study Time
                        StatMetricBlock(
                            label = "Study Time",
                            value = "${stats.totalStudyMinutes / 60}h ${stats.totalStudyMinutes % 60}m",
                            icon = Icons.Default.Schedule,
                            color = IndigoPrimary
                        )

                        // Metric 2: Flashcards Mastered
                        StatMetricBlock(
                            label = "Mastered Cards",
                            value = "${stats.flashcardsMastered}",
                            icon = Icons.Default.CheckCircle,
                            color = EmeraldSuccess
                        )

                        // Metric 3: Active Streak
                        StatMetricBlock(
                            label = "Active Streak",
                            value = "${stats.streakDays} Days",
                            icon = Icons.Default.LocalFireDepartment,
                            color = AmberAccent
                        )
                    }
                }
            }
        }

        // 2. Study Consistency Heatmap Calendar
        item {
            StudyEngagementHeatmap()
        }

        // 3. Subject-by-Subject Mastery Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Domain Mastery Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val categoryMasteries = listOf(
                        Triple(SubjectCategory.BIOLOGY, 0.88f, "88% · Advanced"),
                        Triple(SubjectCategory.CHEMISTRY, 0.65f, "65% · Intermediate"),
                        Triple(SubjectCategory.HISTORY, 0.92f, "92% · Mastered"),
                        Triple(SubjectCategory.COMPUTER_SCIENCE, 0.74f, "74% · Proficient"),
                        Triple(SubjectCategory.PHYSICS, 0.50f, "50% · Developing")
                    )

                    categoryMasteries.forEach { (cat, pct, label) ->
                        val catColor = Color(cat.colorHex)
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = cat.displayName.substringBefore(" &"),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = catColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = catColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. Badges & Milestones
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Scholar Milestones & Badges",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MilestoneBadge(
                            icon = "🔥",
                            title = "Two-Week Streak",
                            desc = "14 days active",
                            isUnlocked = true
                        )
                        MilestoneBadge(
                            icon = "🧠",
                            title = "Recall Titan",
                            desc = "200+ cards mastered",
                            isUnlocked = true
                        )
                        MilestoneBadge(
                            icon = "⚡",
                            title = "Deep Focus",
                            desc = "20+ hours logged",
                            isUnlocked = true
                        )
                        MilestoneBadge(
                            icon = "🎓",
                            title = "Polymath",
                            desc = "5 domains >80%",
                            isUnlocked = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricBlock(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MilestoneBadge(
    icon: String,
    title: String,
    desc: String,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) AmberAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    if (isUnlocked) AmberAccent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = if (isUnlocked) 22.sp else 18.sp,
                color = if (isUnlocked) Color.Unspecified else Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1
        )
    }
}
