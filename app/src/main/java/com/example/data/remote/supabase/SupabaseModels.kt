package com.example.data.remote.supabase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Supabase GoTrue Auth DTOs ---

@JsonClass(generateAdapter = true)
data class SupabaseSignUpRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "data") val data: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseSignInRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    @Json(name = "expires_in") val expiresIn: Long? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "user") val user: SupabaseUserDto? = null,
    @Json(name = "error_description") val errorDescription: String? = null,
    @Json(name = "msg") val msg: String? = null,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseUserDto(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "user_metadata") val userMetadata: Map<String, String>? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

// --- Supabase PostgREST DTOs ---

@JsonClass(generateAdapter = true)
data class SupabaseProfileDto(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "streak_days") val streakDays: Int = 0,
    @Json(name = "mastery_score") val masteryScore: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabasePostDto(
    @Json(name = "id") val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "category") val category: String,
    @Json(name = "tags") val tags: List<String> = emptyList(),
    @Json(name = "likes_count") val likesCount: Int = 0,
    @Json(name = "comments_count") val commentsCount: Int = 0,
    @Json(name = "is_pinned") val isPinned: Boolean = false,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "profiles") val profile: SupabaseProfileDto? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseCreatePostRequest(
    @Json(name = "user_id") val userId: String,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "category") val category: String,
    @Json(name = "tags") val tags: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SupabaseCommentDto(
    @Json(name = "id") val id: String,
    @Json(name = "post_id") val postId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "content") val content: String,
    @Json(name = "likes_count") val likesCount: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "profiles") val profile: SupabaseProfileDto? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseCreateCommentRequest(
    @Json(name = "post_id") val postId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class SupabaseLikeDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "post_id") val postId: String,
    @Json(name = "user_id") val userId: String
)
