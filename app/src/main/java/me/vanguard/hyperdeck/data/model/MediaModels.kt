package me.vanguard.hyperdeck.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoItem(
    val id: String,
    val title: String,
    val author: String = "",
    val durationSeconds: Long = 0,
    val thumbnailUrl: String = "",
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Serializable
data class VideoFormat(
    val quality: String,
    val resolution: String,
    val mimeType: String,
    val url: String,
    val bitrate: Long = 0,
    val fps: Int = 30
)

@Serializable
data class AudioTrack(
    val language: String,
    val mimeType: String,
    val url: String,
    val bitrate: Long = 0
)

@Serializable
data class SubtitleTrack(
    val label: String,
    val languageCode: String,
    val url: String
)

data class PlayableMedia(
    val videoId: String,
    val title: String,
    val author: String,
    val formats: List<VideoFormat>,
    val audioTracks: List<AudioTrack>,
    val subtitles: List<SubtitleTrack>,
    val selectedFormat: VideoFormat? = null,
    val selectedSubtitle: SubtitleTrack? = null
)
