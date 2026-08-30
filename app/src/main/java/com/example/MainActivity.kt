package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileStats
import com.example.ui.components.TopStudyBar
import com.example.ui.screens.audio.AudioStudyModal
import com.example.ui.screens.chat.AIChatScreen
import com.example.ui.screens.community.CommunityFeedScreen
import com.example.ui.screens.components.ComponentsShowcaseScreen
import com.example.ui.screens.focus.FocusModeScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.learn.LearnScreen
import com.example.ui.screens.modals.*
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.screens.schema.SupabaseSchemaScreen
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val stats = userStats ?: UserProfileStats()

    // Full screen Focus Mode takes over if active
    if (uiState.isFocusModeActive) {
        FocusModeScreen(viewModel = viewModel)
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show User Notice Snackbar
    LaunchedEffect(uiState.userNotice) {
        uiState.userNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.dismissNotice()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopStudyBar(
                streakDays = stats.streakDays,
                unreadNotifs = uiState.notifications.count { !it.isRead },
                onNotifClick = { viewModel.toggleNotificationCenter(true) },
                onProfileClick = { viewModel.setTab(AppTab.PROFILE) },
                onSearchClick = { viewModel.setTab(AppTab.LEARN) }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = uiState.currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(tab) },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    AppTab.HOME -> if (isSelected) Icons.Default.Home else Icons.Outlined.Home
                                    AppTab.LEARN -> if (isSelected) Icons.Default.School else Icons.Outlined.School
                                    AppTab.COMMUNITY -> if (isSelected) Icons.Default.Forum else Icons.Outlined.Forum
                                    AppTab.CHAT -> if (isSelected) Icons.Default.Psychology else Icons.Outlined.Psychology
                                    AppTab.SCHEMA -> if (isSelected) Icons.Default.Storage else Icons.Outlined.Storage
                                    AppTab.SHOWCASE -> if (isSelected) Icons.Default.Widgets else Icons.Outlined.Widgets
                                    AppTab.PROFILE -> if (isSelected) Icons.Default.Person else Icons.Outlined.Person
                                },
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoPrimary,
                            selectedTextColor = IndigoPrimary,
                            indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val posts by viewModel.communityPosts.collectAsState()
            val commentsMap by viewModel.commentsMap.collectAsState()

            AnimatedContent(
                targetState = uiState.currentTab,
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    AppTab.HOME -> HomeScreen(viewModel = viewModel)
                    AppTab.LEARN -> LearnScreen(viewModel = viewModel)
                    AppTab.COMMUNITY -> CommunityFeedScreen(
                        posts = posts,
                        currentUser = stats,
                        commentsMap = commentsMap,
                        onLikePost = { viewModel.toggleCommunityPostLike(it) },
                        onBookmarkPost = { viewModel.toggleCommunityPostBookmark(it) },
                        onCreatePost = { title, content, category, tags ->
                            viewModel.createCommunityPost(title, content, category, tags)
                        },
                        onAddComment = { postId, content ->
                            viewModel.addCommunityPostComment(postId, content)
                        },
                        onRefresh = { viewModel.refreshCommunityPosts() }
                    )
                    AppTab.CHAT -> AIChatScreen(viewModel = viewModel)
                    AppTab.SCHEMA -> SupabaseSchemaScreen(
                        supabaseRepository = viewModel.supabaseRepository
                    )
                    AppTab.SHOWCASE -> ComponentsShowcaseScreen()
                    AppTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modals and Dialogs
    if (uiState.showNotificationCenter) {
        NotificationCenterModal(viewModel = viewModel)
    }

    if (uiState.showSessionSetupModal) {
        SessionSetupModal(viewModel = viewModel)
    }

    if (uiState.showDocumentScannerModal) {
        DocumentScannerModal(viewModel = viewModel)
    }

    if (uiState.showAudioPlayerModal) {
        AudioStudyModal(viewModel = viewModel)
    }

    if (uiState.showExportChatDialog) {
        ExportChatModal(viewModel = viewModel)
    }

    if (uiState.selectedKnowledgeNode != null) {
        KnowledgeNodeDetailModal(
            node = uiState.selectedKnowledgeNode!!,
            onDismiss = { viewModel.selectKnowledgeNode(null) },
            onStartStudy = {
                val node = uiState.selectedKnowledgeNode!!
                viewModel.selectKnowledgeNode(null)
                viewModel.generateCardsFromTopic(node.topicTitle, node.category)
                viewModel.setTab(AppTab.LEARN)
            }
        )
    }

    if (uiState.showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleSignOutDialog(false) },
            title = { Text(text = "Sign Out?") },
            text = { Text(text = "Are you sure you want to sign out? Your study cards and stats remain safely saved in local offline storage.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.signOut() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = "Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleSignOutDialog(false) }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}
