package com.hackra.ulanzi.companion.data.media

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import com.hackra.ulanzi.companion.data.ConnectionState
import com.hackra.ulanzi.companion.data.UlanziRepository
import com.hackra.ulanzi.companion.data.model.CustomAppPayload
import com.hackra.ulanzi.companion.data.notification.NotificationSettingsManager
import com.hackra.ulanzi.companion.service.UlanziNotificationListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

open class MediaSyncManager {

    private val context: Context?
    private val repository: UlanziRepository
    private val notificationSettingsManager: NotificationSettingsManager?
    private val scope: CoroutineScope
    private val prefs: SharedPreferences?

    private val _settings: MutableStateFlow<MediaSyncSettings>
    val settings: StateFlow<MediaSyncSettings>

    private val _mediaState: MutableStateFlow<MediaPlaybackState>
    val mediaState: StateFlow<MediaPlaybackState>

    private var activeController: MediaController? = null
    private var syncJob: Job? = null
    private var lastPushedText: String = ""
    private var lastPushedProgress: Int = -1

    private val mainHandler by lazy {
        runCatching { Handler(Looper.getMainLooper()) }.getOrNull()
    }

    private fun postToMain(action: () -> Unit) {
        val h = mainHandler
        if (h != null) {
            h.post(action)
        } else {
            action()
        }
    }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            postToMain {
                handlePlaybackStateUpdate(state)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            postToMain {
                handleMetadataUpdate(metadata)
            }
        }
    }

    private val sessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        postToMain {
            updateActiveController(controllers)
        }
    }

    constructor(
        context: Context,
        repository: UlanziRepository,
        notificationSettingsManager: NotificationSettingsManager,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) {
        this.context = context
        this.repository = repository
        this.notificationSettingsManager = notificationSettingsManager
        this.scope = scope
        val sp = context.getSharedPreferences("ulanzi_media_sync_prefs", Context.MODE_PRIVATE)
        this.prefs = sp
        this._settings = MutableStateFlow(loadSettings(sp))
        this.settings = _settings.asStateFlow()
        this._mediaState = MutableStateFlow(MediaPlaybackState())
        this.mediaState = _mediaState.asStateFlow()

        initSessionMonitoring()
    }

    constructor(
        repository: UlanziRepository,
        initialSettings: MediaSyncSettings = MediaSyncSettings(),
        initialPlaybackState: MediaPlaybackState = MediaPlaybackState(),
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) {
        this.context = null
        this.repository = repository
        this.notificationSettingsManager = null
        this.scope = scope
        this.prefs = null
        this._settings = MutableStateFlow(initialSettings)
        this.settings = _settings.asStateFlow()
        this._mediaState = MutableStateFlow(initialPlaybackState)
        this.mediaState = _mediaState.asStateFlow()
    }

    private fun initSessionMonitoring() {
        val ctx = context ?: return
        val notifManager = notificationSettingsManager ?: return

        scope.launch {
            notifManager.hasListenerPermission.collect { hasPerm ->
                if (hasPerm) {
                    registerMediaSessionListener(ctx)
                }
            }
        }
    }

    private fun registerMediaSessionListener(ctx: Context) {
        try {
            val sessionManager = ctx.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
            val component = ComponentName(ctx, UlanziNotificationListenerService::class.java)

            sessionManager.removeOnActiveSessionsChangedListener(sessionsChangedListener)
            sessionManager.addOnActiveSessionsChangedListener(sessionsChangedListener, component)

            val initialSessions = sessionManager.getActiveSessions(component)
            updateActiveController(initialSessions)
        } catch (_: SecurityException) {
            // Notification access not yet fully active
        } catch (_: Exception) {
            // Defensive catch for OEM variations
        }
    }

    fun refreshActiveSessions() {
        val ctx = context ?: return
        try {
            val sessionManager = ctx.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
            val component = ComponentName(ctx, UlanziNotificationListenerService::class.java)
            val sessions = sessionManager.getActiveSessions(component)
            updateActiveController(sessions)
        } catch (_: Exception) {
        }
    }

    private fun updateActiveController(controllers: List<MediaController>?) {
        val newController = controllers?.firstOrNull { c ->
            val s = c.playbackState?.state
            s == PlaybackState.STATE_PLAYING || s == PlaybackState.STATE_BUFFERING
        } ?: controllers?.firstOrNull()

        if (activeController?.sessionToken != newController?.sessionToken) {
            activeController?.unregisterCallback(controllerCallback)
            activeController = newController
            activeController?.registerCallback(controllerCallback)

            handleMetadataUpdate(activeController?.metadata)
            handlePlaybackStateUpdate(activeController?.playbackState)
        }
    }

    private fun handleMetadataUpdate(metadata: MediaMetadata?) {
        val current = _mediaState.value
        if (metadata == null) return

        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: current.title
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: current.artist
        val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: current.album
        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).coerceAtLeast(0L)

        val pkg = activeController?.packageName.orEmpty()
        val appName = resolveAppName(pkg)
        val (icon, color) = resolveAppTheming(pkg)

        val progress = if (duration > 0 && current.positionMs > 0) {
            ((current.positionMs.toDouble() / duration) * 100).toInt().coerceIn(0, 100)
        } else current.progressPct

        val updated = current.copy(
            title = title,
            artist = artist,
            album = album,
            packageName = pkg,
            appName = appName,
            durationMs = duration,
            progressPct = progress,
            icon = icon,
            colorRgb = color
        )
        _mediaState.value = updated

        if (updated.isPlaying && _settings.value.isEnabled) {
            triggerSyncToClock(updated)
        }
    }

    private fun handlePlaybackStateUpdate(state: PlaybackState?) {
        val current = _mediaState.value
        val isPlaying = state?.state == PlaybackState.STATE_PLAYING || state?.state == PlaybackState.STATE_BUFFERING
        val position = state?.position?.coerceAtLeast(0L) ?: current.positionMs

        val progress = if (current.durationMs > 0) {
            ((position.toDouble() / current.durationMs) * 100).toInt().coerceIn(0, 100)
        } else current.progressPct

        val updated = current.copy(
            isPlaying = isPlaying,
            positionMs = position,
            progressPct = progress
        )
        _mediaState.value = updated

        if (isPlaying && _settings.value.isEnabled) {
            startPeriodicSync()
        } else {
            stopPeriodicSync()
            if (_settings.value.deleteOnPause && !isPlaying) {
                scope.launch {
                    repository.deleteCustomApp(_settings.value.customAppName)
                }
            }
        }
    }

    private fun startPeriodicSync() {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                val state = _mediaState.value
                if (state.isPlaying && _settings.value.isEnabled) {
                    val controller = activeController
                    val currentPos = controller?.playbackState?.position ?: state.positionMs
                    val dur = state.durationMs
                    val progress = if (dur > 0) {
                        ((currentPos.toDouble() / dur) * 100).toInt().coerceIn(0, 100)
                    } else 0

                    val updated = state.copy(positionMs = currentPos, progressPct = progress)
                    _mediaState.value = updated
                    triggerSyncToClock(updated)
                }
                delay((_settings.value.pushIntervalSec.coerceIn(2, 30)) * 1000L)
            }
        }
    }

    private fun stopPeriodicSync() {
        syncJob?.cancel()
        syncJob = null
    }

    fun triggerSyncToClock(state: MediaPlaybackState = _mediaState.value) {
        if (!_settings.value.isEnabled) return
        if (repository.connectionState.value !is ConnectionState.Connected) return
        if (state.title.isBlank() && state.artist.isBlank()) return

        val text = when (_settings.value.format) {
            MediaTextFormat.TITLE_ARTIST -> state.formattedText
            MediaTextFormat.ARTIST_TITLE -> if (state.artist.isNotBlank() && state.title.isNotBlank()) "${state.artist} - ${state.title}" else state.formattedText
        }

        // Avoid re-pushing identical text & progress
        if (text == lastPushedText && state.progressPct == lastPushedProgress) return
        lastPushedText = text
        lastPushedProgress = state.progressPct

        scope.launch {
            val payload = CustomAppPayload(
                text = text,
                icon = state.icon,
                color = state.colorRgb,
                progress = state.progressPct,
                duration = _settings.value.pushIntervalSec + 3,
                lifetime = (_settings.value.pushIntervalSec * 3).coerceAtLeast(60),
                scroll = true
            )
            repository.pushCustomApp(_settings.value.customAppName, payload)
        }
    }

    fun play() {
        activeController?.transportControls?.play()
    }

    fun pause() {
        activeController?.transportControls?.pause()
    }

    fun skipNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    fun setSyncEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_ENABLED, enabled)?.apply()
        _settings.value = _settings.value.copy(isEnabled = enabled)
        if (enabled && _mediaState.value.isPlaying) {
            startPeriodicSync()
        } else if (!enabled) {
            stopPeriodicSync()
            scope.launch {
                repository.deleteCustomApp(_settings.value.customAppName)
            }
        }
    }

    fun setAutoSwitch(autoSwitch: Boolean) {
        prefs?.edit()?.putBoolean(KEY_AUTO_SWITCH, autoSwitch)?.apply()
        _settings.value = _settings.value.copy(autoSwitchScreen = autoSwitch)
    }

    fun setDeleteOnPause(deleteOnPause: Boolean) {
        prefs?.edit()?.putBoolean(KEY_DELETE_ON_PAUSE, deleteOnPause)?.apply()
        _settings.value = _settings.value.copy(deleteOnPause = deleteOnPause)
    }

    fun setPushInterval(intervalSec: Int) {
        val clamped = intervalSec.coerceIn(2, 30)
        prefs?.edit()?.putInt(KEY_INTERVAL, clamped)?.apply()
        _settings.value = _settings.value.copy(pushIntervalSec = clamped)
        if (_mediaState.value.isPlaying) {
            startPeriodicSync()
        }
    }

    fun setFormat(format: MediaTextFormat) {
        prefs?.edit()?.putString(KEY_FORMAT, format.name)?.apply()
        _settings.value = _settings.value.copy(format = format)
        triggerSyncToClock()
    }

    private fun resolveAppName(packageName: String): String {
        val ctx = context ?: return packageName
        return try {
            val pm = ctx.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            when {
                packageName.contains("spotify") -> "Spotify"
                packageName.contains("youtube") -> "YouTube Music"
                packageName.contains("vlc") -> "VLC"
                packageName.contains("audio") || packageName.contains("music") -> "Media Player"
                else -> "Audio"
            }
        }
    }

    private fun resolveAppTheming(packageName: String): Pair<String, List<Int>> {
        return when {
            packageName.contains("spotify") -> "spotify" to listOf(30, 215, 96) // Spotify Green
            packageName.contains("youtube") -> "spotify" to listOf(255, 0, 0) // YouTube Red
            packageName.contains("apple") -> "spotify" to listOf(252, 60, 68) // Apple Music Pink
            packageName.contains("soundcloud") -> "spotify" to listOf(255, 85, 0) // SoundCloud Orange
            packageName.contains("tidal") -> "spotify" to listOf(0, 0, 0)
            else -> "spotify" to listOf(0, 229, 255) // Cyan Default
        }
    }

    private fun loadSettings(sp: SharedPreferences): MediaSyncSettings {
        val formatStr = sp.getString(KEY_FORMAT, MediaTextFormat.TITLE_ARTIST.name)
        val format = runCatching { MediaTextFormat.valueOf(formatStr!!) }.getOrDefault(MediaTextFormat.TITLE_ARTIST)

        return MediaSyncSettings(
            isEnabled = sp.getBoolean(KEY_ENABLED, true),
            customAppName = sp.getString(KEY_APP_NAME, "spotify") ?: "spotify",
            autoSwitchScreen = sp.getBoolean(KEY_AUTO_SWITCH, false),
            deleteOnPause = sp.getBoolean(KEY_DELETE_ON_PAUSE, false),
            pushIntervalSec = sp.getInt(KEY_INTERVAL, 5),
            format = format
        )
    }

    companion object {
        private const val KEY_ENABLED = "media_sync_enabled"
        private const val KEY_APP_NAME = "media_custom_app_name"
        private const val KEY_AUTO_SWITCH = "media_auto_switch"
        private const val KEY_DELETE_ON_PAUSE = "media_delete_on_pause"
        private const val KEY_INTERVAL = "media_push_interval"
        private const val KEY_FORMAT = "media_text_format"
    }
}
