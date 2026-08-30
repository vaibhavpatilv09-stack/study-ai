package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.AuthState
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID

import com.example.data.remote.supabase.SupabaseRepository

enum class AppTab(val title: String, val iconName: String) {
    HOME("Home", "home"),
    LEARN("Learn", "school"),
    COMMUNITY("Community", "forum"),
    CHAT("AI Tutor", "chat"),
    SCHEMA("DB Schema", "storage"),
    SHOWCASE("UI Library", "widgets"),
    PROFILE("Profile", "person")
}

enum class FocusTimerState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

enum class AmbientSound(val title: String, val desc: String) {
    LOFI_BEATS("Lo-Fi Study Beats", "Gentle relaxing downtempo rhythm"),
    RAIN_SOUNDS("Rain & Thunder", "Soothing ambient rainfall on glass"),
    ALPHA_WAVES("Alpha Brainwaves", "432Hz deep cognitive focus frequency"),
    WHITE_NOISE("Soft White Noise", "Consistent background noise blocker"),
    NONE("Silent Focus", "Pure uninterrupted silence")
}

data class UiState(
    val currentTab: AppTab = AppTab.HOME,
    val selectedCategory: SubjectCategory = SubjectCategory.BIOLOGY,
    // Active Modals & Overlays
    val showNotificationCenter: Boolean = false,
    val showSessionSetupModal: Boolean = false,
    val showDocumentScannerModal: Boolean = false,
    val showAudioPlayerModal: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val showExportChatDialog: Boolean = false,
    val selectedKnowledgeNode: KnowledgeNode? = null,
    val selectedDeckForStudy: StudyDeck? = null,
    // Focus Mode
    val isFocusModeActive: Boolean = false,
    val focusThemeIsDark: Boolean = true,
    val focusTimerState: FocusTimerState = FocusTimerState.IDLE,
    val focusDurationMinutes: Int = 25,
    val focusSecondsRemaining: Int = 25 * 60,
    val selectedAmbientSound: AmbientSound = AmbientSound.LOFI_BEATS,
    val focusScore: Int = 94,
    // Active Flashcard Session
    val currentFlashcardIndex: Int = 0,
    val isCardFlipped: Boolean = false,
    val sessionReviewedCount: Int = 0,
    val sessionMasteredCount: Int = 0,
    // AI Chat
    val chatInputText: String = "",
    val isChatGenerating: Boolean = false,
    val isFastMode: Boolean = false,
    val isThinkingMode: Boolean = false,
    val useSearchGrounding: Boolean = true,
    val selectedTutorRole: String = "Socratic Tutor",
    val attachedImageBitmap: Bitmap? = null,
    val attachedImageBase64: String? = null,
    // Audio Player
    val isAudioPlaying: Boolean = false,
    val audioProgress: Float = 0.35f,
    val audioPlaybackSpeed: Float = 1.0f,
    val currentAudioTopic: String = "Cellular Respiration & ATP Synthase Cascade",
    val currentAudioScript: String = "",
    // Search Query
    val searchQuery: String = "",
    val searchResults: List<String> = emptyList(),
    // Notifications
    val notifications: List<NotificationItem> = listOf(
        NotificationItem(
            id = "notif_1",
            title = "🔥 14-Day Streak on Fire!",
            message = "You're in the top 5% of active learners this week. Complete today's 15-min goal.",
            timeAgo = "10m ago",
            iconName = "streak"
        ),
        NotificationItem(
            id = "notif_2",
            title = "🧠 8 Flashcards Ready for Review",
            message = "Optimal spaced repetition interval reached for Cellular Biology & Genetics.",
            timeAgo = "1h ago",
            iconName = "cards"
        ),
        NotificationItem(
            id = "notif_3",
            title = "🏆 Mastery Milestone Achieved",
            message = "Promoted to Level 5 Quantum Scholar! +250 XP earned.",
            timeAgo = "3h ago",
            iconName = "trophy"
        ),
        NotificationItem(
            id = "notif_4",
            title = "📚 AI Summary Ready",
            message = "StudyMate summarized 'Molecular Biology & Cellular Genetics Study Guide'.",
            timeAgo = "Yesterday",
            iconName = "doc"
        )
    ),
    // Status Toast / SnackBar
    val userNotice: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val studyRepository = StudyRepository(db.studyDao())
    val authRepository = AuthRepository(application)
    val supabaseRepository = SupabaseRepository(application, db.communityDao())

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val allDecks: StateFlow<List<StudyDeck>> = studyRepository.allDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKnowledgeNodes: StateFlow<List<KnowledgeNode>> = studyRepository.allKnowledgeNodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<StudySession>> = studyRepository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = studyRepository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserProfileStats?> = studyRepository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allDocuments: StateFlow<List<StudyDocument>> = studyRepository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityPosts: StateFlow<List<CommunityPost>> = supabaseRepository.getPostsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _commentsMap = MutableStateFlow<Map<String, List<PostComment>>>(emptyMap())
    val commentsMap: StateFlow<Map<String, List<PostComment>>> = _commentsMap.asStateFlow()

    val authState: StateFlow<AuthState> = authRepository.authState

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            studyRepository.initializeStarterDataIfEmpty()
            supabaseRepository.seedInitialPostsIfEmpty()
            supabaseRepository.fetchPostsFromSupabase()
        }
    }

    fun setTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectCategory(category: SubjectCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    // Modal toggles
    fun toggleNotificationCenter(show: Boolean) {
        _uiState.update { it.copy(showNotificationCenter = show) }
    }

    fun toggleSessionSetupModal(show: Boolean) {
        _uiState.update { it.copy(showSessionSetupModal = show) }
    }

    fun toggleDocumentScannerModal(show: Boolean) {
        _uiState.update { it.copy(showDocumentScannerModal = show) }
    }

    fun toggleAudioPlayerModal(show: Boolean, topic: String? = null) {
        _uiState.update {
            it.copy(
                showAudioPlayerModal = show,
                currentAudioTopic = topic ?: it.currentAudioTopic
            )
        }
        if (show && topic != null) {
            viewModelScope.launch {
                val script = studyRepository.generateStudyAudioScript(topic, 5)
                _uiState.update { it.copy(currentAudioScript = script) }
            }
        }
    }

    fun toggleSignOutDialog(show: Boolean) {
        _uiState.update { it.copy(showSignOutDialog = show) }
    }

    fun toggleExportChatDialog(show: Boolean) {
        _uiState.update { it.copy(showExportChatDialog = show) }
    }

    fun selectKnowledgeNode(node: KnowledgeNode?) {
        _uiState.update { it.copy(selectedKnowledgeNode = node) }
    }

    fun selectDeckForStudy(deck: StudyDeck?) {
        _uiState.update {
            it.copy(
                selectedDeckForStudy = deck,
                currentFlashcardIndex = 0,
                isCardFlipped = false,
                sessionReviewedCount = 0,
                sessionMasteredCount = 0
            )
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isCardFlipped = !it.isCardFlipped) }
    }

    fun rateFlashcard(card: Flashcard, rating: Int, totalCards: Int) {
        viewModelScope.launch {
            studyRepository.reviewFlashcard(card, rating)
            _uiState.update { state ->
                val nextIdx = state.currentFlashcardIndex + 1
                val isDone = nextIdx >= totalCards
                state.copy(
                    currentFlashcardIndex = nextIdx,
                    isCardFlipped = false,
                    sessionReviewedCount = state.sessionReviewedCount + 1,
                    sessionMasteredCount = state.sessionMasteredCount + if (rating >= 2) 1 else 0
                )
            }
        }
    }

    // Focus Timer Operations
    fun toggleFocusMode(active: Boolean) {
        _uiState.update { it.copy(isFocusModeActive = active) }
    }

    fun toggleFocusTheme() {
        _uiState.update { it.copy(focusThemeIsDark = !it.focusThemeIsDark) }
    }

    fun setFocusDuration(minutes: Int) {
        _uiState.update {
            it.copy(
                focusDurationMinutes = minutes,
                focusSecondsRemaining = minutes * 60,
                focusTimerState = FocusTimerState.IDLE
            )
        }
        timerJob?.cancel()
    }

    fun selectAmbientSound(sound: AmbientSound) {
        _uiState.update { it.copy(selectedAmbientSound = sound) }
    }

    fun startFocusTimer() {
        _uiState.update { it.copy(focusTimerState = FocusTimerState.RUNNING) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.focusSecondsRemaining > 0 && _uiState.value.focusTimerState == FocusTimerState.RUNNING) {
                delay(1000)
                _uiState.update { it.copy(focusSecondsRemaining = it.focusSecondsRemaining - 1) }
            }
            if (_uiState.value.focusSecondsRemaining <= 0) {
                _uiState.update { it.copy(focusTimerState = FocusTimerState.COMPLETED) }
                // Log completed focus session
                val minutes = _uiState.value.focusDurationMinutes
                studyRepository.logStudySession(
                    StudySession(
                        id = UUID.randomUUID().toString(),
                        subject = _uiState.value.selectedCategory,
                        durationMinutes = minutes,
                        focusScore = _uiState.value.focusScore,
                        flashcardsReviewed = 12,
                        cardsMastered = 8,
                        sessionType = "Pomodoro Deep Work (${_uiState.value.selectedAmbientSound.title})"
                    )
                )
            }
        }
    }

    fun pauseFocusTimer() {
        _uiState.update { it.copy(focusTimerState = FocusTimerState.PAUSED) }
        timerJob?.cancel()
    }

    fun resetFocusTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                focusTimerState = FocusTimerState.IDLE,
                focusSecondsRemaining = it.focusDurationMinutes * 60
            )
        }
    }

    // AI Chat Operations
    fun updateChatInput(text: String) {
        _uiState.update { it.copy(chatInputText = text) }
    }

    fun toggleFastMode() {
        _uiState.update { it.copy(isFastMode = !it.isFastMode, isThinkingMode = false) }
    }

    fun toggleThinkingMode() {
        _uiState.update { it.copy(isThinkingMode = !it.isThinkingMode, isFastMode = false) }
    }

    fun toggleSearchGrounding() {
        _uiState.update { it.copy(useSearchGrounding = !it.useSearchGrounding) }
    }

    fun setTutorRole(role: String) {
        _uiState.update { it.copy(selectedTutorRole = role) }
    }

    fun attachImage(bitmap: Bitmap?) {
        if (bitmap == null) {
            _uiState.update { it.copy(attachedImageBitmap = null, attachedImageBase64 = null) }
            return
        }
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        _uiState.update { it.copy(attachedImageBitmap = bitmap, attachedImageBase64 = base64) }
    }

    fun sendChatMessage() {
        val prompt = _uiState.value.chatInputText.trim()
        if (prompt.isBlank() && _uiState.value.attachedImageBase64 == null) return

        val imageBase64 = _uiState.value.attachedImageBase64
        val isFast = _uiState.value.isFastMode
        val isThinking = _uiState.value.isThinkingMode
        val useSearch = _uiState.value.useSearchGrounding
        val role = _uiState.value.selectedTutorRole

        val systemPrompt = when (role) {
            "Socratic Tutor" -> "You are a master Socratic study tutor on StudyMate. Guide the student to discover solutions through strategic questions, analogies, and active recall. Format responses with bold key principles."
            "Exam Prep Coach" -> "You are an elite AP / MCAT / STEM exam prep coach. Provide high-yield breakdowns, common trap misconceptions, mnemonics, and test strategy tips."
            "Feynman Explainer" -> "You are Richard Feynman explaining this concept to a curious learner. Use plain, vivid language, zero unnecessary jargon, and visual real-world metaphors."
            else -> "You are StudyMate AI, an expert academic mastery companion. Be structured, encouraging, pedagogical, and accurate."
        }

        _uiState.update {
            it.copy(
                chatInputText = "",
                attachedImageBitmap = null,
                attachedImageBase64 = null,
                isChatGenerating = true
            )
        }

        viewModelScope.launch {
            val history = chatMessages.value
            studyRepository.sendChatMessage(
                history = history,
                userPrompt = if (prompt.isNotBlank()) prompt else "Please analyze this study diagram / document thoroughly.",
                imageBase64 = imageBase64,
                isFastMode = isFast,
                isThinkingMode = isThinking,
                systemRolePrompt = systemPrompt,
                useSearchGrounding = useSearch
            )
            _uiState.update { it.copy(isChatGenerating = false) }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            db.studyDao().clearChatHistory()
        }
    }

    // AI Generation tools
    fun generateCardsFromTopic(topic: String, category: SubjectCategory) {
        viewModelScope.launch {
            _uiState.update { it.copy(userNotice = "Generating AI flashcards for $topic...") }
            studyRepository.generateFlashcardsForTopic(topic, category)
            _uiState.update { it.copy(userNotice = "✨ 4 New Flashcards added to $category!") }
        }
    }

    fun analyzeAndSaveDocument(title: String, text: String, category: SubjectCategory) {
        viewModelScope.launch {
            _uiState.update { it.copy(userNotice = "Analyzing document with Gemini AI...") }
            val doc = studyRepository.analyzeDocument(title, text, category)
            _uiState.update {
                it.copy(
                    showDocumentScannerModal = false,
                    userNotice = "✨ Document '${doc.title}' summarized and indexed!"
                )
            }
        }
    }

    // Audio Player controls
    fun toggleAudioPlayback() {
        _uiState.update { it.copy(isAudioPlaying = !it.isAudioPlaying) }
    }

    fun setAudioSpeed(speed: Float) {
        _uiState.update { it.copy(audioPlaybackSpeed = speed) }
    }

    fun setAudioProgress(progress: Float) {
        _uiState.update { it.copy(audioProgress = progress.coerceIn(0f, 1f)) }
    }

    // Search
    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val results = if (query.isBlank()) emptyList() else {
                listOf(
                    "Cellular Respiration & Krebs Cycle (Biology)",
                    "Gibbs Free Energy & Thermodynamic Equilibrium (Chemistry)",
                    "Dijkstra Shortest Path Algorithm (Computer Science)",
                    "Athenian Democracy & Cleisthenes Reforms (History)",
                    "Quantum Wave-Particle Duality & Schrödinger (Physics)"
                ).filter { it.contains(query, ignoreCase = true) }
            }
            state.copy(searchQuery = query, searchResults = results)
        }
    }

    // Auth actions
    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(userNotice = "Connecting to Google Sign-In...") }
            authRepository.signInWithGoogle()
            _uiState.update { it.copy(userNotice = "✓ Successfully signed in with Google!") }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            authRepository.signInWithEmail(email, pass)
            supabaseRepository.signIn(email, pass)
            _uiState.update { it.copy(userNotice = "✓ Signed in to Supabase & local session!") }
        }
    }

    fun registerWithEmail(email: String, pass: String, name: String) {
        viewModelScope.launch {
            authRepository.registerWithEmail(email, pass, name)
            supabaseRepository.signUp(email, pass, name)
            _uiState.update { it.copy(userNotice = "✓ Registered Supabase account for $name!") }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            supabaseRepository.signOut()
            _uiState.update {
                it.copy(
                    showSignOutDialog = false,
                    userNotice = "Signed out from Supabase."
                )
            }
        }
    }

    fun updateSupabaseConfig(url: String, anonKey: String) {
        supabaseRepository.updateSupabaseCredentials(url, anonKey)
        viewModelScope.launch {
            supabaseRepository.fetchPostsFromSupabase()
            _uiState.update { it.copy(userNotice = "✓ Supabase configuration saved and reconnected!") }
        }
    }

    // Community Feed Actions
    fun createCommunityPost(
        title: String,
        content: String,
        category: SubjectCategory,
        tags: List<String>
    ) {
        viewModelScope.launch {
            val user = userStats.value ?: UserProfileStats(
                userId = "user_default",
                displayName = "Alex Rivera",
                email = "scholar@studymate.ai"
            )
            supabaseRepository.createPost(title, content, category, tags, user)
            _uiState.update { it.copy(userNotice = "✓ Post published to Supabase community!") }
        }
    }

    fun toggleCommunityPostLike(post: CommunityPost) {
        viewModelScope.launch {
            val currentUserId = userStats.value?.userId ?: "user_default"
            supabaseRepository.toggleLike(post, currentUserId)
        }
    }

    fun toggleCommunityPostBookmark(post: CommunityPost) {
        viewModelScope.launch {
            supabaseRepository.toggleBookmark(post)
            _uiState.update {
                it.copy(userNotice = if (!post.isBookmarkedByMe) "🔖 Post saved to bookmarks" else "Bookmark removed")
            }
        }
    }

    fun addCommunityPostComment(postId: String, content: String) {
        viewModelScope.launch {
            val user = userStats.value ?: UserProfileStats(
                userId = "user_default",
                displayName = "Alex Rivera",
                email = "scholar@studymate.ai"
            )
            val newComment = supabaseRepository.addComment(postId, content, user)
            _commentsMap.update { current ->
                val existing = current[postId] ?: emptyList()
                current + (postId to (existing + newComment))
            }
            _uiState.update { it.copy(userNotice = "✓ Comment posted!") }
        }
    }

    fun loadCommentsForPost(postId: String) {
        viewModelScope.launch {
            supabaseRepository.getCommentsFlow(postId).collect { comments ->
                _commentsMap.update { current -> current + (postId to comments) }
            }
        }
    }

    fun refreshCommunityPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(userNotice = "Refreshing community feed from Supabase...") }
            supabaseRepository.fetchPostsFromSupabase()
            _uiState.update { it.copy(userNotice = "✓ Community feed up to date!") }
        }
    }

    fun dismissNotice() {
        _uiState.update { it.copy(userNotice = null) }
    }
}
