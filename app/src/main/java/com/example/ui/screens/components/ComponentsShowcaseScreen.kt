package com.example.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CommunityPost
import com.example.data.model.SubjectCategory
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ComponentsShowcaseScreen(
    modifier: Modifier = Modifier
) {
    var sampleText by remember { mutableStateOf("StudyMate AI Component") }
    var samplePassword by remember { mutableStateOf("secret123") }
    var sampleSearch by remember { mutableStateOf("") }
    var buttonLoading by remember { mutableStateOf(false) }
    var selectedNavId by remember { mutableStateOf("home") }

    val scrollState = rememberScrollState()

    val samplePost = remember {
        CommunityPost(
            id = "demo_card_1",
            userId = "scholar_1",
            authorName = "Dr. Elena Vance",
            authorEmail = "elena.vance@stanford.edu",
            title = "✨ Reusable Content Card Component",
            content = "This is a demonstration of the standardized AppContentCard component with responsive like counters, comment counts, tag pills, and verified author badges.",
            category = SubjectCategory.BIOLOGY,
            tags = listOf("JetpackCompose", "Material3", "DesignSystem"),
            likesCount = 88,
            commentsCount = 14,
            isLikedByMe = true,
            isBookmarkedByMe = false,
            isPinned = true
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Reusable UI Component Library",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Standardized Material 3 buttons, inputs, cards, and navigation widgets",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Navigation Bar Showcase
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. App Navigation Bar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = IndigoPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                AppNavigationBar(
                    items = listOf(
                        NavigationItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home, badgeCount = 0, testTag = "nav_demo_home"),
                        NavigationItem("learn", "Learn", Icons.Filled.School, Icons.Outlined.School, badgeCount = 3, testTag = "nav_demo_learn"),
                        NavigationItem("community", "Feed", Icons.Filled.Forum, Icons.Outlined.Forum, badgeCount = 5, testTag = "nav_demo_feed"),
                        NavigationItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person, badgeCount = 0, testTag = "nav_demo_profile")
                    ),
                    selectedItemId = selectedNavId,
                    onItemSelected = { selectedNavId = it }
                )
            }
        }

        // 2. Primary & Secondary Buttons Showcase
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "2. Button Variants & States",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = IndigoPrimary
                )

                // Primary Button
                AppPrimaryButton(
                    text = "Primary Button (Default)",
                    onClick = { buttonLoading = !buttonLoading },
                    leadingIcon = Icons.Default.CheckCircle,
                    testTag = "demo_primary_button"
                )

                // Loading State Button
                AppPrimaryButton(
                    text = "Loading Button State",
                    onClick = {},
                    isLoading = true,
                    testTag = "demo_loading_button"
                )

                // Secondary Outlined Button
                AppSecondaryButton(
                    text = "Secondary Outlined Button",
                    onClick = {},
                    leadingIcon = Icons.Default.Edit,
                    testTag = "demo_secondary_button"
                )

                // Success & Danger Variants
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppPrimaryButton(
                        text = "Success",
                        onClick = {},
                        variant = ButtonVariant.SUCCESS,
                        leadingIcon = Icons.Default.Done,
                        modifier = Modifier.weight(1f),
                        testTag = "demo_success_button"
                    )
                    AppPrimaryButton(
                        text = "Danger",
                        onClick = {},
                        variant = ButtonVariant.DANGER,
                        leadingIcon = Icons.Default.Delete,
                        modifier = Modifier.weight(1f),
                        testTag = "demo_danger_button"
                    )
                }
            }
        }

        // 3. Input Forms Showcase
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "3. Form & Input Fields",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = IndigoPrimary
                )

                AppTextField(
                    value = sampleText,
                    onValueChange = { sampleText = it },
                    label = "Standard Text Input",
                    placeholder = "Enter value...",
                    leadingIcon = Icons.Default.TextFields,
                    helperText = "Includes auto clear button and leading icon",
                    testTag = "demo_text_input"
                )

                AppPasswordField(
                    value = samplePassword,
                    onValueChange = { samplePassword = it },
                    label = "Password Input with Visibility Toggle",
                    testTag = "demo_password_input"
                )

                AppSearchField(
                    query = sampleSearch,
                    onQueryChange = { sampleSearch = it },
                    placeholder = "Search field with embedded clear action...",
                    testTag = "demo_search_input"
                )

                AppTextField(
                    value = "invalid_input_error",
                    onValueChange = {},
                    label = "Input with Validation Error",
                    isError = true,
                    errorMessage = "This field requires a valid formatted input.",
                    testTag = "demo_error_input"
                )
            }
        }

        // 4. Content Cards Showcase
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4. App Content Card",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = IndigoPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                AppContentCard(
                    post = samplePost,
                    onPostClick = {},
                    onLikeClick = {},
                    onCommentClick = {},
                    onBookmarkClick = {}
                )
            }
        }

        // 5. Badge Chips Showcase
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "5. Status Badges & Chips",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = IndigoPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppBadgeChip("Biology", 0xFF10B981)
                    AppBadgeChip("AI Engine", 0xFF6366F1)
                    AppBadgeChip("Mastered", 0xFF06B6D4)
                    AppBadgeChip("Urgent", 0xFFF43F5E)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
