package me.vanguard.hyperdeck.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.vanguard.hyperdeck.data.model.AudioTrack
import me.vanguard.hyperdeck.data.model.PlayableMedia
import me.vanguard.hyperdeck.data.model.SubtitleTrack
import me.vanguard.hyperdeck.data.model.VideoFormat
import me.vanguard.hyperdeck.data.model.VideoItem
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class InvidiousClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Highly reliable public Invidious instances with CORS and API enabled
    private val instances = listOf(
        "https://inv.nadeko.net",
        "https://invidious.nerdvpn.de",
        "https://yewtu.be",
        "https://invidious.jing.rocks",
        "https://invidious.io.lol"
    )

    suspend fun fetchVideoDetails(videoId: String): Result<PlayableMedia> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        for (instance in instances) {
            try {
                val url = "$instance/api/v1/videos/$videoId"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "HyperDeck/1.0 (Android; HyperOS)")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@use
                    }

                    val body = response.body?.string() ?: return@use
                    val json = JSONObject(body)

                    val title = json.optString("title", "Untitled")
                    val author = json.optString("author", "Unknown Creator")

                    // Video formats (combined + adaptive)
                    val formatList = mutableListOf<VideoFormat>()
                    val formatStreams = json.optJSONArray("formatStreams") ?: JSONArray()
                    for (i in 0 until formatStreams.length()) {
                        val stream = formatStreams.getJSONObject(i)
                        formatList.add(
                            VideoFormat(
                                quality = stream.optString("qualityLabel", stream.optString("resolution", "Default")),
                                resolution = stream.optString("resolution", "720p"),
                                mimeType = stream.optString("type", "video/mp4"),
                                url = stream.optString("url"),
                                fps = stream.optInt("fps", 30)
                            )
                        )
                    }

                    // Adaptive video formats (1080p, 1440p, 4k etc.)
                    val adaptiveFormats = json.optJSONArray("adaptiveFormats") ?: JSONArray()
                    for (i in 0 until adaptiveFormats.length()) {
                        val stream = adaptiveFormats.getJSONObject(i)
                        val type = stream.optString("type", "")
                        if (type.startsWith("video/")) {
                            formatList.add(
                                VideoFormat(
                                    quality = stream.optString("qualityLabel", stream.optString("resolution", "Adaptive")),
                                    resolution = stream.optString("resolution", ""),
                                    mimeType = type,
                                    url = stream.optString("url"),
                                    bitrate = stream.optLong("bitrate", 0),
                                    fps = stream.optInt("fps", 30)
                                )
                            )
                        }
                    }

                    // Audio tracks
                    val audioTracks = mutableListOf<AudioTrack>()
                    for (i in 0 until adaptiveFormats.length()) {
                        val stream = adaptiveFormats.getJSONObject(i)
                        val type = stream.optString("type", "")
                        if (type.startsWith("audio/")) {
                            audioTracks.add(
                                AudioTrack(
                                    language = stream.optString("audioTrackId", "Default Audio"),
                                    mimeType = type,
                                    url = stream.optString("url"),
                                    bitrate = stream.optLong("bitrate", 0)
                                )
                            )
                        }
                    }

                    // Subtitles / Captions
                    val subtitles = mutableListOf<SubtitleTrack>()
                    val captions = json.optJSONArray("captions") ?: JSONArray()
                    for (i in 0 until captions.length()) {
                        val cap = captions.getJSONObject(i)
                        val rawUrl = cap.optString("url")
                        val finalUrl = if (rawUrl.startsWith("/")) "$instance$rawUrl" else rawUrl
                        subtitles.add(
                            SubtitleTrack(
                                label = cap.optString("label", "English"),
                                languageCode = cap.optString("language_code", "en"),
                                url = finalUrl
                            )
                        )
                    }

                    val media = PlayableMedia(
                        videoId = videoId,
                        title = title,
                        author = author,
                        formats = formatList,
                        audioTracks = audioTracks,
                        subtitles = subtitles,
                        selectedFormat = formatList.firstOrNull()
                    )

                    return@withContext Result.success(media)
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        Result.failure(lastException ?: Exception("All Invidious instances timed out"))
    }

    suspend fun search(query: String): Result<List<VideoItem>> = withContext(Dispatchers.IO) {
        for (instance in instances) {
            try {
                val url = "$instance/api/v1/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}&type=video"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "HyperDeck/1.0 (Android; HyperOS)")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    val array = JSONArray(body)
                    val results = mutableListOf<VideoItem>()

                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val videoId = item.optString("videoId")
                        if (videoId.isNotEmpty()) {
                            results.add(
                                VideoItem(
                                    id = videoId,
                                    title = item.optString("title", "Untitled"),
                                    author = item.optString("author", "Unknown"),
                                    durationSeconds = item.optLong("lengthSeconds", 0),
                                    thumbnailUrl = item.optJSONArray("videoThumbnails")
                                        ?.optJSONObject(0)?.optString("url")
                                        ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
                                )
                            )
                        }
                    }
                    return@withContext Result.success(results)
                }
            } catch (_: Exception) {}
        }
        Result.failure(Exception("Search failed across all instances"))
    }

    companion object {
        fun extractVideoId(input: String): String {
            val clean = input.trim()
            if (!clean.contains("/") && !clean.contains("?")) return clean

            val patterns = listOf(
                "v=([^&]+)".toRegex(),
                "youtu\\.be/([^?&]+)".toRegex(),
                "shorts/([^?&]+)".toRegex(),
                "embed/([^?&]+)".toRegex()
            )

            for (regex in patterns) {
                val match = regex.find(clean)
                if (match != null && match.groupValues.size > 1) {
                    return match.groupValues[1]
                }
            }
            return clean
        }
    }
}
