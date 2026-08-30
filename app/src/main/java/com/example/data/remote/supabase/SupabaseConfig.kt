package com.example.data.remote.supabase

object SupabaseConfig {
    // Default Supabase project endpoints - configurable in settings or .env
    const val DEFAULT_SUPABASE_URL = "https://wjcj3bw2np3pypus7nkd.supabase.co"
    const val DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndqY2ozYncybnAzcHlwdXM3bmtkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjUwMDAwMDAsImV4cCI6MjA0MDU3NjAwMH0.mock_anon_key_for_client_app"

    var currentSupabaseUrl: String = DEFAULT_SUPABASE_URL
    var currentAnonKey: String = DEFAULT_ANON_KEY
    var currentAccessToken: String? = null

    fun getAuthHeader(): Map<String, String> {
        val headers = mutableMapOf(
            "apikey" to currentAnonKey,
            "Content-Type" to "application/json"
        )
        val token = currentAccessToken
        if (!token.isNullOrBlank()) {
            headers["Authorization"] = "Bearer $token"
        } else {
            headers["Authorization"] = "Bearer $currentAnonKey"
        }
        return headers
    }
}
