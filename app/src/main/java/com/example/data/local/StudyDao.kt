package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // Decks
    @Query("SELECT * FROM flashcard_decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<StudyDeck>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: StudyDeck)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecks(decks: List<StudyDeck>)

    // Flashcards
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    fun getCardsForDeck(deckId: String): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards ORDER BY nextReviewDue ASC")
    fun getAllCards(): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(card: Flashcard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(cards: List<Flashcard>)

    @Update
    suspend fun updateFlashcard(card: Flashcard)

    @Query("DELETE FROM flashcards WHERE id = :cardId")
    suspend fun deleteFlashcard(cardId: String)

    // Knowledge Nodes
    @Query("SELECT * FROM knowledge_nodes ORDER BY chapterOrder ASC")
    fun getAllKnowledgeNodes(): Flow<List<KnowledgeNode>>

    @Query("SELECT * FROM knowledge_nodes WHERE category = :category ORDER BY chapterOrder ASC")
    fun getKnowledgeNodesByCategory(category: SubjectCategory): Flow<List<KnowledgeNode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledgeNodes(nodes: List<KnowledgeNode>)

    @Update
    suspend fun updateKnowledgeNode(node: KnowledgeNode)

    // Study Sessions
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession)

    // Chat History
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    // User Profile Stats
    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    fun getUserStats(userId: String = "local_user"): Flow<UserProfileStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserStats(stats: UserProfileStats)

    // Documents
    @Query("SELECT * FROM study_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<StudyDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: StudyDocument)
}
