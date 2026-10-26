package com.hackra.ulanzi.companion.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.hackra.ulanzi.companion.UlanziCompanionApp
import com.hackra.ulanzi.companion.data.ConnectionState
import com.hackra.ulanzi.companion.data.model.NotifyPayload
import com.hackra.ulanzi.companion.data.notification.ForwardedNotificationLog
import com.hackra.ulanzi.companion.data.notification.NotificationDispatchStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class UlanziNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val recentDeduplicationCache = ConcurrentHashMap<String, Long>()

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val app = runCatching { UlanziCompanionApp.get() }.getOrNull() ?: return
        val settingsManager = app.notificationSettingsManager
        val settings = settingsManager.settings.value

        // 1. Check Master Forwarding Toggle
        if (!settings.isForwardingEnabled) {
            return
        }

        // 2. Ignore self notifications
        if (sbn.packageName == packageName) {
            return
        }

        // 3. Ignore ongoing / persistent notifications if configured
        if (settings.ignoreOngoing && sbn.isOngoing) {
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // 4. Extract Title, Text, SubText, and App Name
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim().orEmpty()
        val ticker = notification.tickerText?.toString()?.trim().orEmpty()

        if (title.isBlank() && text.isBlank() && ticker.isBlank()) {
            return
        }

        val appInfo = try {
            packageManager.getApplicationInfo(sbn.packageName, 0)
        } catch (_: Exception) {
            null
        }
        val appLabel = appInfo?.let { packageManager.getApplicationLabel(it).toString() } ?: sbn.packageName

        // 5. Categorize and determine icon & filter matching
        val category = determineCategory(sbn, notification)
        val icon = determineIcon(sbn, notification, category)

        val isCategoryEnabled = when (category) {
            AppCategory.CALLS -> settings.forwardCalls
            AppCategory.MESSAGING -> settings.forwardMessaging
            AppCategory.CALENDAR -> settings.forwardCalendar
            AppCategory.OTHER -> settings.forwardOther
        }

        if (!isCategoryEnabled) {
            settingsManager.recordLog(
                ForwardedNotificationLog(
                    appName = appLabel,
                    packageName = sbn.packageName,
                    title = title,
                    text = text,
                    icon = icon,
                    status = NotificationDispatchStatus.SKIPPED_FILTERED
                )
            )
            return
        }

        // 6. Deduplication filter (2.5 seconds window per content fingerprint)
        val fingerprint = "${sbn.packageName}|$title|$text"
        val now = System.currentTimeMillis()
        val lastSeen = recentDeduplicationCache[fingerprint] ?: 0L

        // Clean stale cache entries older than 10 seconds
        if (recentDeduplicationCache.size > 50) {
            val staleThreshold = now - 10_000L
            recentDeduplicationCache.entries.removeIf { it.value < staleThreshold }
        }

        if (now - lastSeen < 2500L) {
            settingsManager.recordLog(
                ForwardedNotificationLog(
                    appName = appLabel,
                    packageName = sbn.packageName,
                    title = title,
                    text = text,
                    icon = icon,
                    status = NotificationDispatchStatus.SKIPPED_DUPLICATE
                )
            )
            return
        }
        recentDeduplicationCache[fingerprint] = now

        // 7. Verify device is connected
        val connectionState = app.repository.connectionState.value
        if (connectionState !is ConnectionState.Connected) {
            settingsManager.recordLog(
                ForwardedNotificationLog(
                    appName = appLabel,
                    packageName = sbn.packageName,
                    title = title,
                    text = text,
                    icon = icon,
                    status = NotificationDispatchStatus.SKIPPED_DISCONNECTED
                )
            )
            return
        }

        // 8. Build formatted ticker text
        val messageBody = when {
            title.isNotBlank() && text.isNotBlank() -> "$title: $text"
            title.isNotBlank() -> title
            text.isNotBlank() -> text
            else -> ticker
        }

        val fullMessage = if (settings.includeAppName) {
            "$appLabel: $messageBody"
        } else {
            messageBody
        }.take(120)

        // 9. Dispatch to clock asynchronously
        serviceScope.launch {
            val result = app.repository.sendNotification(
                NotifyPayload(
                    text = fullMessage,
                    icon = icon,
                    sound = settings.sound,
                    duration = settings.duration
                )
            )

            result.onSuccess {
                settingsManager.recordLog(
                    ForwardedNotificationLog(
                        appName = appLabel,
                        packageName = sbn.packageName,
                        title = title,
                        text = text,
                        icon = icon,
                        status = NotificationDispatchStatus.SENT
                    )
                )
            }.onFailure { err ->
                settingsManager.recordLog(
                    ForwardedNotificationLog(
                        appName = appLabel,
                        packageName = sbn.packageName,
                        title = title,
                        text = text,
                        icon = icon,
                        status = NotificationDispatchStatus.ERROR,
                        errorMessage = err.message
                    )
                )
            }
        }
    }

    private enum class AppCategory {
        CALLS,
        MESSAGING,
        CALENDAR,
        OTHER
    }

    private fun determineCategory(sbn: StatusBarNotification, notification: Notification): AppCategory {
        val category = notification.category.orEmpty()
        val pkg = sbn.packageName.lowercase()

        return when {
            category == Notification.CATEGORY_CALL ||
                    pkg.contains("dialer") || pkg.contains("telecom") || pkg.contains("phone") ||
                    pkg.contains("whatsapp") && category == Notification.CATEGORY_CALL -> AppCategory.CALLS

            category == Notification.CATEGORY_MESSAGE ||
                    isMessagingPackage(pkg) -> AppCategory.MESSAGING

            category == Notification.CATEGORY_EVENT ||
                    pkg.contains("calendar") || pkg.contains("tasks") || pkg.contains("remind") -> AppCategory.CALENDAR

            else -> AppCategory.OTHER
        }
    }

    private fun determineIcon(sbn: StatusBarNotification, notification: Notification, category: AppCategory): String {
        val pkg = sbn.packageName.lowercase()

        return when {
            category == AppCategory.CALLS -> "bell"
            category == AppCategory.CALENDAR -> "clock"
            pkg.contains("spotify") -> "spotify"
            pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("signal") || pkg.contains("messaging") -> "mail"
            notification.category == Notification.CATEGORY_ALARM || notification.category == Notification.CATEGORY_ERROR -> "warning"
            pkg.contains("health") || pkg.contains("fitness") -> "heart"
            else -> "bell"
        }
    }

    private fun isMessagingPackage(pkg: String): Boolean {
        return pkg.contains("whatsapp") ||
                pkg.contains("telegram") ||
                pkg.contains("signal") ||
                pkg.contains("messaging") ||
                pkg.contains("mms") ||
                pkg.contains("discord") ||
                pkg.contains("slack") ||
                pkg.contains("viber") ||
                pkg.contains("skype") ||
                pkg.contains("teams") ||
                pkg.contains("wechat") ||
                pkg.contains("line") ||
                pkg.contains("gmail") ||
                pkg.contains("mail")
    }
}
