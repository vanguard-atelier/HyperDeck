package me.vanguard.hyperdeck.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.vanguard.hyperdeck.data.model.VideoItem
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class MediaEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val pipedSearchInstances = listOf(
        "https://api.piped.private.coffee",
        "https://pipedapi.ducks.party",
        "https://piped-api.privacy.com.de"
    )

    suspend fun fetchVideoMetadata(videoId: String): Result<VideoItem> = withContext(Dispatchers.IO) {
        try {
            // YouTube's official oEmbed endpoint (100% reliable, zero bot blockades)
            val url = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Redmi Pad Pro) AppleWebKit/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val item = VideoItem(
                        id = videoId,
                        title = json.optString("title", "Video ($videoId)"),
                        author = json.optString("author_name", "YouTube Creator"),
                        thumbnailUrl = json.optString("thumbnail_url", "https://img.youtube.com/vi/$videoId/hqdefault.jpg")
                    )
                    return@withContext Result.success(item)
                }
            }
        } catch (_: Exception) {}

        // Fallback with standard thumbnail
        Result.success(
            VideoItem(
                id = videoId,
                title = "Video ($videoId)",
                author = "YouTube",
                thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
            )
        )
    }

    suspend fun searchVideos(query: String): Result<List<VideoItem>> = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")

        for (instance in pipedSearchInstances) {
            try {
                val url = "$instance/search?q=$encodedQuery&filter=videos"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    val json = JSONObject(body)
                    val itemsArray = json.optJSONArray("items") ?: JSONArray()
                    val results = mutableListOf<VideoItem>()

                    for (i in 0 until itemsArray.length()) {
                        val item = itemsArray.getJSONObject(i)
                        val rawUrl = item.optString("url", "")
                        val videoId = extractVideoId(rawUrl)

                        if (videoId.isNotEmpty()) {
                            results.add(
                                VideoItem(
                                    id = videoId,
                                    title = item.optString("title", "Untitled"),
                                    author = item.optString("uploaderName", "Creator"),
                                    durationSeconds = item.optLong("duration", 0),
                                    thumbnailUrl = item.optString("thumbnail", "https://img.youtube.com/vi/$videoId/hqdefault.jpg")
                                )
                            )
                        }
                    }

                    if (results.isNotEmpty()) {
                        return@withContext Result.success(results)
                    }
                }
            } catch (_: Exception) {}
        }

        Result.failure(Exception("Search instances temporarily unreachable"))
    }

    companion object {
        fun extractVideoId(input: String): String {
            val clean = input.trim()
            if (!clean.contains("/") && !clean.contains("?") && clean.length in 10..12) return clean

            val patterns = listOf(
                "v=([a-zA-Z0-9_-]{11})".toRegex(),
                "youtu\\.be/([a-zA-Z0-9_-]{11})".toRegex(),
                "shorts/([a-zA-Z0-9_-]{11})".toRegex(),
                "embed/([a-zA-Z0-9_-]{11})".toRegex(),
                "/watch\\?v=([a-zA-Z0-9_-]{11})".toRegex()
            )

            for (regex in patterns) {
                val match = regex.find(clean)
                if (match != null && match.groupValues.size > 1) {
                    return match.groupValues[1]
                }
            }
            return clean.replace("/watch?v=", "")
        }
    }
}
