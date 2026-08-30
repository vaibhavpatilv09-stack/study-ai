package com.example.data.local

import androidx.room.*
import com.example.data.model.CommunityPost
import com.example.data.model.PostComment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityDao {

    @Query("SELECT * FROM community_posts ORDER BY isPinned DESC, createdAt DESC")
    fun getAllPosts(): Flow<List<CommunityPost>>

    @Query("SELECT * FROM community_posts WHERE category = :category ORDER BY isPinned DESC, createdAt DESC")
    fun getPostsByCategory(category: String): Flow<List<CommunityPost>>

    @Query("SELECT * FROM community_posts WHERE id = :postId LIMIT 1")
    fun getPostById(postId: String): Flow<CommunityPost?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CommunityPost>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost)

    @Delete
    suspend fun deletePost(post: CommunityPost)

    @Query("DELETE FROM community_posts WHERE id = :postId")
    suspend fun deletePostById(postId: String)

    @Query("UPDATE community_posts SET likesCount = :newCount, isLikedByMe = :isLiked WHERE id = :postId")
    suspend fun updatePostLike(postId: String, newCount: Int, isLiked: Boolean)

    @Query("UPDATE community_posts SET isBookmarkedByMe = :isBookmarked WHERE id = :postId")
    suspend fun updatePostBookmark(postId: String, isBookmarked: Boolean)

    // Comments
    @Query("SELECT * FROM post_comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: String): Flow<List<PostComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<PostComment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: PostComment)

    @Query("DELETE FROM post_comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: String)
}
