package com.hackra.ulanzi.companion.data.media

enum class MediaTextFormat {
    TITLE_ARTIST,
    ARTIST_TITLE
}

data class MediaPlaybackState(
    val isPlaying: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val packageName: String = "",
    val appName: String = "",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val progressPct: Int = 0,
    val icon: String = "spotify",
    val colorRgb: List<Int> = listOf(30, 215, 96)
) {
    val formattedText: String
        get() = when {
            title.isNotBlank() && artist.isNotBlank() -> "$title • $artist"
            title.isNotBlank() -> title
            artist.isNotBlank() -> artist
            else -> "Playing Media"
        }

    val positionFormatted: String
        get() = formatTime(positionMs)

    val durationFormatted: String
        get() = formatTime(durationMs)

    private fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return "%d:%02d".format(minutes, seconds)
    }
}

data class MediaSyncSettings(
    val isEnabled: Boolean = true,
    val customAppName: String = "spotify",
    val autoSwitchScreen: Boolean = false,
    val deleteOnPause: Boolean = false,
    val pushIntervalSec: Int = 5,
    val format: MediaTextFormat = MediaTextFormat.TITLE_ARTIST
)
