package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey val id: String,
    val userId: String,
    val authorName: String,
    val authorEmail: String,
    val authorAvatarUrl: String? = null,
    val title: String,
    val content: String,
    val category: SubjectCategory,
    val tags: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isBookmarkedByMe: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "post_comments")
data class PostComment(
    @PrimaryKey val id: String,
    val postId: String,
    val userId: String,
    val authorName: String,
    val authorEmail: String,
    val authorAvatarUrl: String? = null,
    val content: String,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class SupabaseTableSchema(
    val tableName: String,
    val description: String,
    val columns: List<SchemaColumn>,
    val relationships: List<SchemaRelationship>,
    val rlsPolicies: List<String>,
    val ddlSql: String
)

data class SchemaColumn(
    val name: String,
    val type: String,
    val isPrimaryKey: Boolean = false,
    val isNullable: Boolean = true,
    val defaultValue: String? = null,
    val foreignKey: String? = null
)

data class SchemaRelationship(
    val targetTable: String,
    val relationType: String, // "1:N", "N:1", "N:M"
    val foreignKeyColumn: String,
    val description: String
)
