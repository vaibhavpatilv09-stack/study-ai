package com.example.data.remote.supabase

import retrofit2.Response
import retrofit2.http.*

interface SupabasePostgrestService {

    // --- Profiles ---
    @GET("rest/v1/profiles")
    suspend fun getProfiles(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("id") idFilter: String? = null
    ): Response<List<SupabaseProfileDto>>

    @POST("rest/v1/profiles")
    @Headers("Prefer: return=representation, resolution=merge-duplicates")
    suspend fun upsertProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body profile: SupabaseProfileDto
    ): Response<List<SupabaseProfileDto>>

    // --- Posts ---
    @GET("rest/v1/posts")
    suspend fun getPosts(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*,profiles(*)",
        @Query("order") order: String = "created_at.desc",
        @Query("category") categoryFilter: String? = null
    ): Response<List<SupabasePostDto>>

    @POST("rest/v1/posts")
    @Headers("Prefer: return=representation")
    suspend fun createPost(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body post: SupabaseCreatePostRequest
    ): Response<List<SupabasePostDto>>

    @DELETE("rest/v1/posts")
    suspend fun deletePost(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") idFilter: String
    ): Response<Unit>

    // --- Comments ---
    @GET("rest/v1/comments")
    suspend fun getComments(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("post_id") postIdFilter: String,
        @Query("select") select: String = "*,profiles(*)",
        @Query("order") order: String = "created_at.asc"
    ): Response<List<SupabaseCommentDto>>

    @POST("rest/v1/comments")
    @Headers("Prefer: return=representation")
    suspend fun createComment(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body comment: SupabaseCreateCommentRequest
    ): Response<List<SupabaseCommentDto>>

    @DELETE("rest/v1/comments")
    suspend fun deleteComment(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") idFilter: String
    ): Response<Unit>

    // --- Likes ---
    @POST("rest/v1/post_likes")
    suspend fun likePost(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body like: SupabaseLikeDto
    ): Response<Unit>

    @DELETE("rest/v1/post_likes")
    suspend fun unlikePost(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("post_id") postIdFilter: String,
        @Query("user_id") userIdFilter: String
    ): Response<Unit>
}
