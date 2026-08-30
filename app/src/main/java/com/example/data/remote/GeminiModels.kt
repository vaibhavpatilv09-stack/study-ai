package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "tools") val tools: List<Tool>? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "role") val role: String? = null,
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class Tool(
    @Json(name = "googleSearch") val googleSearch: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null,
    @Json(name = "thinkingConfig") val thinkingConfig: ThinkingConfig? = null,
    @Json(name = "responseMimeType") val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    @Json(name = "thinkingLevel") val thinkingLevel: String // "high" or "low"
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null,
    @Json(name = "groundingMetadata") val groundingMetadata: GroundingMetadata? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null,
    @Json(name = "groundingMetadata") val groundingMetadata: GroundingMetadata? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GroundingMetadata(
    @Json(name = "webSearchQueries") val webSearchQueries: List<String>? = null,
    @Json(name = "searchEntryPoint") val searchEntryPoint: SearchEntryPoint? = null,
    @Json(name = "groundingChunks") val groundingChunks: List<GroundingChunk>? = null
)

@JsonClass(generateAdapter = true)
data class SearchEntryPoint(
    @Json(name = "renderedContent") val renderedContent: String? = null
)

@JsonClass(generateAdapter = true)
data class GroundingChunk(
    @Json(name = "web") val web: WebChunk? = null
)

@JsonClass(generateAdapter = true)
data class WebChunk(
    @Json(name = "uri") val uri: String? = null,
    @Json(name = "title") val title: String? = null
)
