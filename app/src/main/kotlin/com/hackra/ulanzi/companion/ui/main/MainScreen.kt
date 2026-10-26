package com.hackra.ulanzi.companion.ui.main

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.hackra.ulanzi.companion.data.ConnectionState
import com.hackra.ulanzi.companion.data.discovery.DiscoveredDevice
import com.hackra.ulanzi.companion.data.discovery.DiscoveryState
import com.hackra.ulanzi.companion.data.media.MediaPlaybackState
import com.hackra.ulanzi.companion.data.media.MediaSyncSettings
import com.hackra.ulanzi.companion.data.media.MediaTextFormat
import com.hackra.ulanzi.companion.data.model.DeviceStatus
import com.hackra.ulanzi.companion.data.model.DisplayMatrixResponse
import com.hackra.ulanzi.companion.data.model.WifiProfile
import com.hackra.ulanzi.companion.data.model.WifiResponse
import com.hackra.ulanzi.companion.data.notification.ForwardedNotificationLog
import com.hackra.ulanzi.companion.data.notification.NotificationDispatchStatus
import com.hackra.ulanzi.companion.data.notification.NotificationSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    onItemClick: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var manualIpInput by remember(uiState.devicePreferences.lastDeviceHost) {
        mutableStateOf(uiState.devicePreferences.lastDeviceHost.ifBlank { "ulanzi-tc001.local" })
    }
    var showRebootConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val (dotColor, statusText) = when (val conn = uiState.connectionState) {
                            is ConnectionState.Connected -> Color(0xFF00E676) to conn.device.ip
                            is ConnectionState.Connecting -> Color(0xFFFFD600) to "CONNECTING..."
                            is ConnectionState.Error -> Color(0xFFFF1744) to "OFFLINE"
                            ConnectionState.Disconnected -> Color(0xFF757575) to "DISCONNECTED"
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Column {
                            Text(
                                text = "ULANZI TC001",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Text(
                                text = "FW v2.2.0 • $statusText",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.connectionState is ConnectionState.Connected) {
                        IconButton(onClick = { viewModel.refreshStatus() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh Telemetry")
                        }
                        IconButton(onClick = { showRebootConfirm = true }) {
                            Icon(Icons.Filled.RestartAlt, contentDescription = "Reboot Clock", tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { viewModel.disconnect() }) {
                            Icon(Icons.Filled.PowerSettingsNew, contentDescription = "Disconnect")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationTab.entries.forEach { tab ->
                    val selected = uiState.selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.selectTab(tab) },
                        alwaysShowLabel = true,
                        icon = {
                            when (tab) {
                                NavigationTab.DASHBOARD -> Icon(Icons.Filled.Dashboard, contentDescription = tab.title)
                                NavigationTab.CONTROLS -> Icon(Icons.Filled.Timer, contentDescription = tab.title)
                                NavigationTab.MEDIA -> Icon(Icons.Filled.MusicNote, contentDescription = tab.title)
                                NavigationTab.ALERTS -> Icon(Icons.Filled.Notifications, contentDescription = tab.title)
                                NavigationTab.SETTINGS -> Icon(Icons.Filled.Tune, contentDescription = tab.title)
                            }
                        },
                        label = {
                            Text(
                                text = tab.shortTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Action message snackbar / pill
            AnimatedVisibility(visible = uiState.actionMessage != null) {
                uiState.actionMessage?.let { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            IconButton(
                                onClick = { viewModel.clearActionMessage() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Rebooting progress banner
            AnimatedVisibility(visible = uiState.isRebooting) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Rebooting clock hardware... Waiting for device to restart.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            // If disconnected, always show the Discovery & Connection deck prominently at top
            if (uiState.connectionState !is ConnectionState.Connected) {
                DiscoveryControlCard(
                    discoveryState = uiState.discoveryState,
                    manualIp = manualIpInput,
                    onManualIpChange = { manualIpInput = it },
                    onConnectManual = { viewModel.connect(manualIpInput) },
                    onStartScan = { viewModel.startDiscovery() },
                    onStopScan = { viewModel.stopDiscovery() },
                    onSelectDevice = { dev -> viewModel.connect(dev.ip) },
                    autoReconnect = uiState.devicePreferences.autoReconnect,
                    onAutoReconnectChange = { viewModel.setAutoReconnect(it) },
                    lastSavedHost = uiState.devicePreferences.lastDeviceHost
                )
            }

            // Tab Content Dispatcher
            when (uiState.selectedTab) {
                NavigationTab.DASHBOARD -> {
                    DashboardTabContent(uiState = uiState, viewModel = viewModel)
                }
                NavigationTab.CONTROLS -> {
                    ControlsTabContent(uiState = uiState, viewModel = viewModel)
                }
                NavigationTab.MEDIA -> {
                    MediaTabContent(uiState = uiState, viewModel = viewModel)
                }
                NavigationTab.ALERTS -> {
                    AlertsTabContent(uiState = uiState, viewModel = viewModel)
                }
                NavigationTab.SETTINGS -> {
                    SettingsTabContent(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }

    if (showRebootConfirm) {
        AlertDialog(
            onDismissRequest = { showRebootConfirm = false },
            title = { Text("Reboot Ulanzi TC001?") },
            text = { Text("This will trigger a hardware restart of the clock via /reboot. The matrix will reconnect automatically after booting.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRebootConfirm = false
                        viewModel.rebootClock()
                    }
                ) {
                    Text("Reboot Now", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 1: DASHBOARD & LIVE MATRIX
// -------------------------------------------------------------

@Composable
fun DashboardTabContent(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    val status = uiState.deviceStatus

    // Live Display Matrix
    LiveMatrixCard(
        matrix = uiState.displayMatrix,
        onRefresh = { viewModel.refreshStatus() }
    )

    // Live Telemetry Deck
    if (status != null) {
        TelemetryDeckCard(status = status)
    }

    // Rapid 9-Screen Switcher
    ScreenSwitcherCard(
        activeApp = status?.activeApp ?: 0,
        autoRotate = status?.autoRotate ?: true,
        onSwitch = { appIdx -> viewModel.switchScreen(appIdx) },
        onToggleAutoRotate = { viewModel.toggleAutoRotate(it) },
        onStepNext = { viewModel.stepScreenNext() },
        onStepPrev = { viewModel.stepScreenPrev() }
    )
}

// -------------------------------------------------------------
// TAB 2: CONTROLS & SCREENSAVERS
// -------------------------------------------------------------

@Composable
fun ControlsTabContent(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    // 1. Hardware Timer & Stopwatch Deck
    TimerDeckCard(uiState = uiState, viewModel = viewModel)

    // 2. Ambient Screensaver Deck
    ScreensaverDeckCard(uiState = uiState, viewModel = viewModel)
}

// -------------------------------------------------------------
// TAB 3: MEDIA SYNC & SPOTIFY
// -------------------------------------------------------------

@Composable
fun MediaTabContent(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    MediaSyncDeckCard(uiState = uiState, viewModel = viewModel)
}

// -------------------------------------------------------------
// TAB 4: ALERTS & NOTIFICATIONS
// -------------------------------------------------------------

@Composable
fun AlertsTabContent(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    // 1. Notification Forwarding Service
    NotificationForwardingDeckCard(uiState = uiState, viewModel = viewModel)

    // 2. Instant Push Notification Tester
    var testText by remember { mutableStateOf("Test alert from Android Companion!") }
    var testIcon by remember { mutableStateOf("bell") }
    var testSound by remember { mutableIntStateOf(1) }
    var testDuration by remember { mutableIntStateOf(5) }

    NotificationPushCard(
        text = testText,
        onTextChange = { testText = it },
        selectedIcon = testIcon,
        onIconChange = { testIcon = it },
        sound = testSound,
        onSoundChange = { testSound = it },
        duration = testDuration,
        onDurationChange = { testDuration = it },
        onSend = { viewModel.sendNotification(testText, testIcon, testSound, testDuration) }
    )
}

// -------------------------------------------------------------
// TAB 5: HARDWARE SETUP & EEPROM
// -------------------------------------------------------------

@Composable
fun SettingsTabContent(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    HardwareSetupDeckCard(uiState = uiState, viewModel = viewModel)
}

// =============================================================
// COMPONENT CARDS
// =============================================================

@Composable
fun LiveMatrixCard(
    matrix: DisplayMatrixResponse?,
    onRefresh: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE 32×8 NEOPIXEL MATRIX STREAM",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Direct hardware mirror (/api/display)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh Matrix")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(32f / 8f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A0E14))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                val pixels = matrix?.pixels
                if (pixels != null && pixels.size == 8) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val cellW = size.width / 32f
                        val cellH = size.height / 8f
                        val gapX = cellW * 0.14f
                        val gapY = cellH * 0.14f
                        val dotW = cellW - gapX
                        val dotH = cellH - gapY
                        val corner = CornerRadius(minOf(dotW, dotH) * 0.25f)

                        for (y in 0 until 8) {
                            val row = pixels.getOrNull(y).orEmpty()
                            for (x in 0 until 32) {
                                val hex = row.getOrNull(x) ?: "#000000"
                                val color = try {
                                    Color(android.graphics.Color.parseColor(hex))
                                } catch (_: Exception) {
                                    Color(0xFF14181F)
                                }
                                drawRoundRect(
                                    color = color,
                                    topLeft = Offset(
                                        x * cellW + gapX / 2f,
                                        y * cellH + gapY / 2f
                                    ),
                                    size = Size(dotW, dotH),
                                    cornerRadius = corner
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Awaiting matrix stream data...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryDeckCard(status: DeviceStatus) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "REAL-TIME TELEMETRY DECK",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStatTile(
                    label = "TEMP",
                    value = "${status.temperatureIn}°C",
                    sub = "SHT30 Ambient",
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "HUMIDITY",
                    value = "${status.humidityIn}%",
                    sub = "Rel. Humidity",
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "BATTERY",
                    value = "${status.batteryPct}%",
                    sub = "${status.batteryVolts}V",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStatTile(
                    label = "WI-FI RSSI",
                    value = "${status.rssi} dBm",
                    sub = status.ssid ?: "Unknown",
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "LDR SENSOR",
                    value = "${status.ldrRaw}",
                    sub = if (status.autoBri) "Auto-Bri ON" else "Fixed",
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "ACTIVE APP",
                    value = status.appName,
                    sub = "Slot #${status.activeApp}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TelemetryStatTile(
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScreenSwitcherCard(
    activeApp: Int,
    autoRotate: Boolean,
    onSwitch: (Int) -> Unit,
    onToggleAutoRotate: (Boolean) -> Unit,
    onStepNext: () -> Unit,
    onStepPrev: () -> Unit
) {
    val screens = listOf(
        0 to "🕐 Clock",
        1 to "⛅ Weather",
        2 to "🪙 Crypto",
        3 to "▶ YouTube",
        4 to "🔋 Battery",
        5 to "🌡️ Ambient SHT30",
        6 to "⏱️ Timer",
        7 to "✨ Screensaver",
        8 to "📶 Wi-Fi Roaming"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RAPID SCREEN SWITCHER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Switch active screen or toggle carousel rotation",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (autoRotate) "Auto: ON" else "Auto: OFF",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = autoRotate,
                        onCheckedChange = onToggleAutoRotate
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStepPrev,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("◀ Prev", maxLines = 1)
                }
                OutlinedButton(
                    onClick = onStepNext,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Next ▶", maxLines = 1)
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                screens.forEach { (idx, title) ->
                    val isSelected = activeApp == idx
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSwitch(idx) },
                        label = { Text(title, fontSize = 12.sp) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerDeckCard(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    val status = uiState.deviceStatus
    var mode by remember { mutableStateOf("countdown") }
    var durationSec by remember(uiState.devicePreferences.timerDefaultDurationSec) {
        mutableIntStateOf(uiState.devicePreferences.timerDefaultDurationSec)
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = "HARDWARE TIMER & STOPWATCH",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Live countdown & stopwatch controller (/api/timer)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = mode == "countdown",
                    onClick = { mode = "countdown" },
                    label = { Text("⏳ Countdown Mode") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = mode == "stopwatch",
                    onClick = { mode = "stopwatch" },
                    label = { Text("⏱ Stopwatch Mode") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Digital Readout Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF101418))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(vertical = 18.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val liveRemaining = status?.timerRemaining
                    val displayTime = if (mode == "countdown") {
                        if (status?.timerState == "running" && liveRemaining != null) {
                            String.format(Locale.US, "%02d:%02d", liveRemaining / 60, liveRemaining % 60)
                        } else {
                            val minutes = durationSec / 60
                            val seconds = durationSec % 60
                            String.format(Locale.US, "%02d:%02d", minutes, seconds)
                        }
                    } else {
                        "00:00.0"
                    }

                    Text(
                        text = displayTime,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (mode == "countdown") Color(0xFF00E676) else Color(0xFF29B6F6)
                        )
                    )

                    Text(
                        text = if (mode == "countdown") "TARGET DURATION ($durationSec s)" else "ACTIVE STOPWATCH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF9E9E9E)
                        )
                    )
                }
            }

            // Countdown Quick Presets
            if (mode == "countdown") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "QUICK PRESETS:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    val presets = listOf(
                        60 to "1m",
                        180 to "3m",
                        300 to "5m",
                        600 to "10m",
                        900 to "15m",
                        1500 to "25m (Pomodoro)"
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        presets.forEach { (sec, label) ->
                            FilterChip(
                                selected = durationSec == sec,
                                onClick = {
                                    durationSec = sec
                                    viewModel.setTimerDefaultDuration(sec)
                                },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (mode == "countdown") {
                            viewModel.startCountdown(durationSec)
                        } else {
                            viewModel.startStopwatch()
                        }
                    },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("▶ Start")
                }
                OutlinedButton(
                    onClick = { viewModel.pauseTimer() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("⏸ Pause")
                }
                OutlinedButton(
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("↺ Reset")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScreensaverDeckCard(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    val prefs = uiState.devicePreferences
    var selectedStyle by remember(prefs.screensaverStyle) { mutableIntStateOf(prefs.screensaverStyle) }
    var selectedTimeout by remember(prefs.screensaverTimeoutMin) { mutableIntStateOf(prefs.screensaverTimeoutMin) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = "AMBIENT SCREENSAVER CONTROLLER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Configure screensaver presets & trigger effects (/api/screensaver)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "VISUAL PRESET STYLE:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                val styles = listOf(
                    0 to "Matrix Rain 🟢",
                    1 to "Starfield ✨",
                    2 to "Fire Flames 🔥",
                    3 to "Retro Clock ⏳",
                    4 to "Plasma Flow 🌊"
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    styles.forEach { (styleId, label) ->
                        FilterChip(
                            selected = selectedStyle == styleId,
                            onClick = {
                                selectedStyle = styleId
                                viewModel.setScreensaverDefaults(styleId, selectedTimeout)
                            },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "IDLE ACTIVATION TIMEOUT:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                val timeouts = listOf(1, 3, 5, 10, 15, 30)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    timeouts.forEach { min ->
                        FilterChip(
                            selected = selectedTimeout == min,
                            onClick = {
                                selectedTimeout = min
                                viewModel.setScreensaverDefaults(selectedStyle, min)
                            },
                            label = { Text("${min}m", fontSize = 12.sp) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.triggerScreensaver(selectedStyle, selectedTimeout) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("▶ Start Screensaver")
                }
                OutlinedButton(
                    onClick = { viewModel.stopScreensaver() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("⏹ Exit Screensaver")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// NOTIFICATIONS & PUSH CARDS
// -------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationForwardingDeckCard(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    val context = LocalContext.current
    val settings = uiState.notificationSettings
    val hasPermission = uiState.hasListenerPermission

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ANDROID NOTIFICATION FORWARDER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Stream Android system alerts to clock display",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Switch(
                    checked = settings.isForwardingEnabled,
                    onCheckedChange = { viewModel.setNotificationForwarding(it) }
                )
            }

            if (!hasPermission) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Notification Access Permission Required",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val intent = viewModel.getNotificationSettingsIntent()
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant Access")
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ACTIVE CATEGORIES:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = settings.forwardCalls,
                        onClick = { viewModel.setForwardCalls(!settings.forwardCalls) },
                        label = { Text("📞 Incoming Calls", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = settings.forwardMessaging,
                        onClick = { viewModel.setForwardMessaging(!settings.forwardMessaging) },
                        label = { Text("💬 Messaging / Chat", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = settings.forwardCalendar,
                        onClick = { viewModel.setForwardCalendar(!settings.forwardCalendar) },
                        label = { Text("📅 Calendar Events", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = settings.forwardOther,
                        onClick = { viewModel.setForwardOther(!settings.forwardOther) },
                        label = { Text("🔔 Other Apps", fontSize = 12.sp) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("BUZZER CHIME:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    val sounds = listOf(0 to "Silent", 1 to "Beep", 2 to "Chime", 3 to "Alarm")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        sounds.forEach { (snd, label) ->
                            FilterChip(
                                selected = settings.sound == snd,
                                onClick = { viewModel.setNotificationSound(snd) },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Suppress Ongoing (Music, Downloads)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = settings.ignoreOngoing,
                    onCheckedChange = { viewModel.setIgnoreOngoing(it) }
                )
            }

            // Notification log list
            if (uiState.notificationLogs.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("RECENT FORWARDED ALERTS:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    TextButton(onClick = { viewModel.clearNotificationLogs() }) {
                        Text("Clear Logs", fontSize = 11.sp)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    uiState.notificationLogs.takeLast(4).reversed().forEach { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${log.appName}: ${log.title}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = log.text,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        maxLines = 1
                                    )
                                }
                                val statusText = when (log.status) {
                                    NotificationDispatchStatus.SENT -> "✓ Sent"
                                    NotificationDispatchStatus.SKIPPED_DUPLICATE -> "⏱ Debounced"
                                    NotificationDispatchStatus.SKIPPED_FILTERED -> "⊘ Filtered"
                                    NotificationDispatchStatus.SKIPPED_DISABLED -> "⊘ Disabled"
                                    NotificationDispatchStatus.SKIPPED_DISCONNECTED -> "⊘ Offline"
                                    NotificationDispatchStatus.ERROR -> "✕ Failed"
                                }
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = if (log.status == NotificationDispatchStatus.SENT) Color(0xFF00E676) else Color(0xFFFF9800)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationPushCard(
    text: String,
    onTextChange: (String) -> Unit,
    selectedIcon: String,
    onIconChange: (String) -> Unit,
    sound: Int,
    onSoundChange: (Int) -> Unit,
    duration: Int,
    onDurationChange: (Int) -> Unit,
    onSend: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "MANUAL PUSH ALERT TESTER",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                label = { Text("Notification Message") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("ICON PICKER:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            val icons = listOf("bell", "mail", "clock", "spotify", "heart", "warning", "wifi", "flame")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                icons.forEach { icon ->
                    FilterChip(
                        selected = selectedIcon == icon,
                        onClick = { onIconChange(icon) },
                        label = { Text(icon, fontSize = 11.sp) }
                    )
                }
            }

            Button(
                onClick = onSend,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("⚡ Send Instant Notification to Clock")
            }
        }
    }
}

// -------------------------------------------------------------
// MEDIA PLAYER CARD
// -------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaSyncDeckCard(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    val mediaState = uiState.mediaState
    val settings = uiState.mediaSyncSettings

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NOW PLAYING & SPOTIFY LIVE BROADCASTER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Real-time track progress & transport controls",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Switch(
                    checked = settings.isEnabled,
                    onCheckedChange = { viewModel.setMediaSyncEnabled(it) }
                )
            }

            if (mediaState.title.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101418))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mediaState.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E676)
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = mediaState.artist.ifBlank { "Unknown Artist" },
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFB0BEC5)),
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "${mediaState.progressPct}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                            )
                        }

                        LinearProgressIndicator(
                            progress = { mediaState.progressPct / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF00E676),
                            trackColor = Color(0xFF263238)
                        )

                        // Transport Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.mediaSkipPrevious() }) {
                                Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous")
                            }
                            if (mediaState.isPlaying) {
                                IconButton(onClick = { viewModel.mediaPause() }) {
                                    Icon(Icons.Filled.Pause, contentDescription = "Pause", tint = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                IconButton(onClick = { viewModel.mediaPlay() }) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(onClick = { viewModel.mediaSkipNext() }) {
                                Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                            }
                            Button(
                                onClick = { viewModel.forceSyncMedia() },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("⚡ Push")
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active media playback detected.\nPlay tracks in Spotify, YouTube Music, or Apple Music to broadcast live.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            // Sync Settings
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("REFRESH INTERVAL:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                val intervals = listOf(3 to "3s (Fast)", 5 to "5s (Balanced)", 10 to "10s (Eco)")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    intervals.forEach { (sec, label) ->
                        FilterChip(
                            selected = settings.pushIntervalSec == sec,
                            onClick = { viewModel.setMediaPushInterval(sec) },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("TEXT FORMAT:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = settings.format == MediaTextFormat.TITLE_ARTIST,
                        onClick = { viewModel.setMediaFormat(MediaTextFormat.TITLE_ARTIST) },
                        label = { Text("Title • Artist", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = settings.format == MediaTextFormat.ARTIST_TITLE,
                        onClick = { viewModel.setMediaFormat(MediaTextFormat.ARTIST_TITLE) },
                        label = { Text("Artist - Title", fontSize = 12.sp) }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 5: HARDWARE SETUP & EEPROM FULL CONFIGURATION
// -------------------------------------------------------------

private val weekdayActiveColors = listOf(
    0 to ("Cyan" to Color(0xFF00F0FF)),
    1 to ("Orange" to Color(0xFFFF8800)),
    2 to ("Green" to Color(0xFF00FF00)),
    3 to ("Yellow" to Color(0xFFFFFF00)),
    4 to ("Red" to Color(0xFFFF2200)),
    5 to ("White" to Color(0xFFFFFFFF)),
    6 to ("Magenta" to Color(0xFFFF00FF)),
    7 to ("Electric Blue" to Color(0xFF0066FF)),
    8 to ("Lime Green" to Color(0xFF76FF03)),
    9 to ("Amber Gold" to Color(0xFFFFC107)),
    10 to ("Soft Pink" to Color(0xFFFF4081))
)

private val weekdayInactiveColors = listOf(
    0 to ("Dim Auto" to Color(0xFF4A5568)),
    1 to ("Slate Gray" to Color(0xFF708090)),
    2 to ("Dim Amber" to Color(0xFF906810)),
    3 to ("Dark Blue" to Color(0xFF001A40)),
    4 to ("Deep Wine" to Color(0xFF3A000A)),
    5 to ("Forest Green" to Color(0xFF002A0D)),
    6 to ("Charcoal" to Color(0xFF252525)),
    7 to ("Muted Purple" to Color(0xFF2A1035)),
    8 to ("Dark Cyan" to Color(0xFF003038)),
    9 to ("Off (Black)" to Color(0xFF0B0B0A))
)

private val weekendColors = listOf(
    0 to ("Same as Active" to Color(0xFF00F0FF)),
    1 to ("Bright Red" to Color(0xFFFF2200)),
    2 to ("Coral Orange" to Color(0xFFFF6B4A)),
    3 to ("Gold" to Color(0xFFFFD700)),
    4 to ("Hot Pink" to Color(0xFFFF1493)),
    5 to ("Purple" to Color(0xFF9C27B0)),
    6 to ("Cyan" to Color(0xFF00F0FF)),
    7 to ("Lime" to Color(0xFF76FF03)),
    8 to ("Pure White" to Color(0xFFFFFFFF)),
    9 to ("Sky Blue" to Color(0xFF00BFFF)),
    10 to ("Emerald" to Color(0xFF00E676))
)

private val cityPresets = listOf(
    "Bratislava" to (48.1486f to 17.1077f),
    "Košice" to (48.7164f to 21.2611f),
    "Praha" to (50.0755f to 14.4378f),
    "Brno" to (49.1951f to 16.6068f),
    "Vienna" to (48.2082f to 16.3738f),
    "Budapest" to (47.4979f to 19.0402f),
    "Warsaw" to (52.2297f to 21.0122f),
    "Berlin" to (52.5200f to 13.4050f),
    "London" to (51.5074f to -0.1278f),
    "Paris" to (48.8566f to 2.3522f),
    "New York" to (40.7128f to -74.0060f)
)

private val assetPresets = listOf("BTC", "ETH", "SOL", "BNB", "AAPL", "TSLA", "NVDA", "SPX", "GOLD", "EUR", "USD")

private val screenNames = listOf(
    0 to "Clock (Heart / Time)",
    1 to "Indoor SHT30",
    2 to "Outdoor Weather",
    3 to "Crypto & Stocks",
    4 to "YouTube Analytics",
    5 to "Live Airspace Radar",
    6 to "Battery & Power",
    7 to "Stopwatch & Timer",
    8 to "Ambient Screensaver"
)

private val transitionFxList = listOf(
    0 to "Instant Cut",
    1 to "Slide Left",
    2 to "Slide Right",
    3 to "Slide Up",
    4 to "Slide Down",
    5 to "Crossfade",
    6 to "Curtain Wipe",
    7 to "Random FX"
)

private val screensaverStyles = listOf(
    0 to "Matrix Digital Rain",
    1 to "NeoPixel Fire / Lava",
    2 to "Pac-Man & Blinky",
    3 to "Conway's Game of Life",
    4 to "Dynamic Shuffle"
)

private val screensaverTimeouts = listOf(
    0 to "Disabled",
    1 to "1m",
    2 to "2m",
    5 to "5m",
    10 to "10m",
    15 to "15m",
    30 to "30m"
)

private val clockModes = listOf(
    0 to "Time Only",
    1 to "Time + Date",
    2 to "Date Only"
)

private val dateFormats = listOf(
    0 to "DD.MM.",
    1 to "DD MMM",
    2 to "MM/DD",
    3 to "DAY DD"
)

private val clockIcons = listOf(
    0 to "Dynamic Calendar (1-31)",
    1 to "Heart",
    2 to "Clock",
    3 to "None"
)

private val textAlignments = listOf(
    0 to "Left (with Icon)",
    1 to "Centered"
)

private val batteryModes = listOf(
    2 to "Alternating (% & V)",
    0 to "Percentage Only",
    1 to "Voltage Only"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HardwareSetupDeckCard(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    val prefs = uiState.devicePreferences
    val status = uiState.deviceStatus

    // State variables with reactive sync
    var clkMode by remember { mutableIntStateOf(status?.clkMode ?: 0) }
    var dateFormat by remember { mutableIntStateOf(status?.dateFormat ?: 0) }
    var clockIcon by remember { mutableIntStateOf(status?.clockIcon ?: 0) }
    var textAlign by remember { mutableIntStateOf(status?.textAlign ?: 0) }
    var showSeconds by remember { mutableStateOf(status?.showSeconds ?: false) }
    var weekdayBar by remember { mutableStateOf(status?.weekdayBar ?: false) }
    var weekdayStartMon by remember { mutableStateOf(status?.weekdayStartMon ?: true) }
    var weekdayActiveColIdx by remember { mutableIntStateOf(status?.weekdayActiveColIdx ?: 0) }
    var weekdayInactColIdx by remember { mutableIntStateOf(status?.weekdayInactColIdx ?: 0) }
    var weekdayWeekendColIdx by remember { mutableIntStateOf(status?.weekdayWeekendColIdx ?: 0) }

    var autoBri by remember { mutableStateOf(status?.autoBri ?: prefs.autoBrightness) }
    var dayBri by remember { mutableFloatStateOf((status?.dayBri ?: prefs.dayBrightness).toFloat()) }
    var nightMode by remember { mutableStateOf(status?.nightMode ?: false) }
    var nightBri by remember { mutableFloatStateOf((status?.nightBri ?: prefs.nightBrightness).toFloat()) }
    var nightStart by remember { mutableIntStateOf(status?.nightStart ?: 23) }
    var nightEnd by remember { mutableIntStateOf(status?.nightEnd ?: 7) }

    var autoRotate by remember { mutableStateOf(status?.autoRotate ?: true) }
    var rotateSec by remember { mutableFloatStateOf((status?.rotateSec ?: prefs.carouselRotateSec).toFloat()) }
    var transitionFx by remember { mutableIntStateOf(status?.transitionFx ?: 0) }
    var activeApp by remember { mutableIntStateOf(status?.activeApp ?: 0) }
    var carouselMask by remember { mutableIntStateOf(status?.carouselMask ?: 0x1FF) }

    var batMode by remember { mutableIntStateOf(status?.batMode ?: 2) }
    var ssStyle by remember { mutableIntStateOf(status?.screensaverStyle ?: 0) }
    var ssTime by remember { mutableIntStateOf(status?.screensaverTimeout ?: 5) }

    var weatherCity by remember { mutableStateOf(status?.weatherCity ?: "Bratislava") }
    var weatherLat by remember { mutableFloatStateOf(status?.weatherLat ?: 48.1486f) }
    var weatherLon by remember { mutableFloatStateOf(status?.weatherLon ?: 17.1077f) }
    var cryptoSymbol by remember { mutableStateOf(status?.cryptoSymbol ?: "BTC") }
    var ytChannel by remember { mutableStateOf(status?.ytChannel ?: "") }
    var ytKey by remember { mutableStateOf("") }

    var mqttEn by remember { mutableStateOf(status?.mqttEn ?: false) }
    var mqttSrv by remember { mutableStateOf(status?.mqttSrv ?: "") }
    var mqttPort by remember { mutableIntStateOf(status?.mqttPort ?: 1883) }
    var mqttUser by remember { mutableStateOf(status?.mqttUser ?: "") }
    var mqttPass by remember { mutableStateOf("") }
    var mqttPre by remember { mutableStateOf(status?.mqttPre ?: "ulanzi-tc001") }

    var selectedWifiSlot by remember { mutableIntStateOf(1) } // 1..4
    var wifiSsidInput by remember { mutableStateOf("") }
    var wifiPassInput by remember { mutableStateOf("") }

    // Re-sync all fields whenever status arrives from device or refreshes
    LaunchedEffect(status) {
        if (status != null) {
            clkMode = status.clkMode
            dateFormat = status.dateFormat
            clockIcon = status.clockIcon
            textAlign = status.textAlign
            showSeconds = status.showSeconds
            weekdayBar = status.weekdayBar
            weekdayStartMon = status.weekdayStartMon
            weekdayActiveColIdx = status.weekdayActiveColIdx
            weekdayInactColIdx = status.weekdayInactColIdx
            weekdayWeekendColIdx = status.weekdayWeekendColIdx
            autoBri = status.autoBri
            dayBri = status.dayBri.toFloat()
            nightMode = status.nightMode
            nightBri = status.nightBri.toFloat()
            nightStart = status.nightStart
            nightEnd = status.nightEnd
            autoRotate = status.autoRotate
            rotateSec = status.rotateSec.toFloat()
            transitionFx = status.transitionFx
            activeApp = status.activeApp
            carouselMask = status.carouselMask
            batMode = status.batMode
            ssStyle = status.screensaverStyle ?: 0
            ssTime = status.screensaverTimeout ?: 5
            weatherCity = status.weatherCity ?: "Bratislava"
            weatherLat = status.weatherLat ?: 48.1486f
            weatherLon = status.weatherLon ?: 17.1077f
            cryptoSymbol = status.cryptoSymbol ?: "BTC"
            ytChannel = status.ytChannel ?: ""
            mqttEn = status.mqttEn
            mqttSrv = status.mqttSrv ?: ""
            mqttPort = status.mqttPort
            mqttUser = status.mqttUser ?: ""
            mqttPre = status.mqttPre
        }
    }

    val profiles = status?.wifiProfiles.takeIf { !it.isNullOrEmpty() } ?: uiState.wifiStatus?.profiles.orEmpty()
    val currentSlotProfile = profiles.find { it.slot == selectedWifiSlot }

    LaunchedEffect(selectedWifiSlot, currentSlotProfile?.ssid) {
        wifiSsidInput = currentSlotProfile?.ssid.orEmpty()
        wifiPassInput = ""
    }

    val saveAllAction = {
        viewModel.saveHardwareSettings(
            clkMode = clkMode,
            dateFormat = dateFormat,
            clockIcon = clockIcon,
            textAlign = textAlign,
            showSeconds = showSeconds,
            weekdayBar = weekdayBar,
            weekdayStartMon = weekdayStartMon,
            weekdayActiveColIdx = weekdayActiveColIdx,
            weekdayInactColIdx = weekdayInactColIdx,
            weekdayWeekendColIdx = weekdayWeekendColIdx,
            autoBri = autoBri,
            dayBri = dayBri.toInt(),
            nightMode = nightMode,
            nightBri = nightBri.toInt(),
            nightStart = nightStart,
            nightEnd = nightEnd,
            autoRotate = autoRotate,
            rotateSec = rotateSec.toInt(),
            transitionFx = transitionFx,
            activeApp = activeApp,
            carouselMask = carouselMask,
            batMode = batMode,
            ssStyle = ssStyle,
            ssTime = ssTime,
            weatherCity = weatherCity,
            weatherLat = weatherLat,
            weatherLon = weatherLon,
            cryptoSymbol = cryptoSymbol,
            ytChannel = ytChannel,
            ytKey = ytKey.ifBlank { null },
            mqttEn = mqttEn,
            mqttSrv = mqttSrv,
            mqttPort = mqttPort,
            mqttUser = mqttUser,
            mqttPass = mqttPass.ifBlank { null },
            mqttPre = mqttPre,
            wifiSlot = selectedWifiSlot,
            wifiSsid = wifiSsidInput.ifBlank { null },
            wifiPass = wifiPassInput.ifBlank { null }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Quick Action Bar
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FIELD INSTRUMENT TERMINAL",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = if (status != null) "Synchronized with clock IP: ${status.ip}" else "Offline / Connecting...",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = { viewModel.refreshStatus(); viewModel.refreshWifi() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Reload from Clock")
                        }
                        IconButton(onClick = { viewModel.rebootClock() }) {
                            Icon(Icons.Filled.RestartAlt, contentDescription = "Reboot Device", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Button(
                    onClick = saveAllAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("💾 Save All Settings to Clock EEPROM & Flash", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 1. CLOCK & DATE ARCHITECTURE (Dedicated Deck)
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "1. CLOCK & DATE ARCHITECTURE",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                )

                // Clock Mode
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CLOCK DISPLAY MODE:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        clockModes.forEach { (m, label) ->
                            FilterChip(
                                selected = clkMode == m,
                                onClick = { clkMode = m },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Date Format & Clock Icon
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("DATE NOTATION FORMAT:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        dateFormats.forEach { (fmt, label) ->
                            FilterChip(
                                selected = dateFormat == fmt,
                                onClick = { dateFormat = fmt },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CLOCK ICON STYLE:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        clockIcons.forEach { (iconId, label) ->
                            FilterChip(
                                selected = clockIcon == iconId,
                                onClick = { clockIcon = iconId },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("TEXT ALIGNMENT:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        textAlignments.forEach { (alignId, label) ->
                            FilterChip(
                                selected = textAlign == alignId,
                                onClick = { textAlign = alignId },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // High Precision Seconds Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Seconds (HH:mm:ss)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("High-precision 3x5 seconds counter on display", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Switch(checked = showSeconds, onCheckedChange = { showSeconds = it })
                }

                // Weekday Bar Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Weekday Indicators (7 Micro-Bars)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Visual day indicators rendered along bottom row 7", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Switch(checked = weekdayBar, onCheckedChange = { weekdayBar = it })
                }

                // Weekday Customization Options
                AnimatedVisibility(visible = weekdayBar) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Week Starting Day
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Week Starting Day:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = weekdayStartMon,
                                    onClick = { weekdayStartMon = true },
                                    label = { Text("Mon (ISO)", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = !weekdayStartMon,
                                    onClick = { weekdayStartMon = false },
                                    label = { Text("Sun (US)", fontSize = 11.sp) }
                                )
                            }
                        }

                        // Live Weekday Bar Visualizer
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("LIVE ROW 7 INDICATOR PREVIEW:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0B0B0A), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val days = if (weekdayStartMon) listOf("M", "T", "W", "T", "F", "S", "S") else listOf("S", "M", "T", "W", "T", "F", "S")
                                val actCol = weekdayActiveColors.find { it.first == weekdayActiveColIdx }?.second?.second ?: Color(0xFF00F0FF)
                                val inactCol = weekdayInactiveColors.find { it.first == weekdayInactColIdx }?.second?.second ?: Color(0xFF4A5568)
                                val wkndCol = if (weekdayWeekendColIdx == 0) actCol else (weekendColors.find { it.first == weekdayWeekendColIdx }?.second?.second ?: actCol)

                                days.forEachIndexed { idx, day ->
                                    val isToday = (idx == 2) // demo active Wednesday
                                    val isWeekend = if (weekdayStartMon) (idx >= 5) else (idx == 0 || idx == 6)
                                    val barCol = when {
                                        isToday -> actCol
                                        isWeekend -> wkndCol
                                        else -> inactCol
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(day, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (isToday) actCol else Color.Gray)
                                        Box(
                                            modifier = Modifier
                                                .width(22.dp)
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(barCol)
                                        )
                                    }
                                }
                            }
                        }

                        // Active Day Color Palette
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("ACTIVE DAY PHOSPHOR COLOR:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                weekdayActiveColors.forEach { (idx, pair) ->
                                    FilterChip(
                                        selected = weekdayActiveColIdx == idx,
                                        onClick = { weekdayActiveColIdx = idx },
                                        leadingIcon = {
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(pair.second))
                                        },
                                        label = { Text(pair.first, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }

                        // Inactive Day Color Palette
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("INACTIVE DAY CONTRAST COLOR:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                weekdayInactiveColors.forEach { (idx, pair) ->
                                    FilterChip(
                                        selected = weekdayInactColIdx == idx,
                                        onClick = { weekdayInactColIdx = idx },
                                        leadingIcon = {
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(pair.second))
                                        },
                                        label = { Text(pair.first, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }

                        // Weekend Color Palette
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("WEEKEND PHOSPHOR COLOR:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                weekendColors.forEach { (idx, pair) ->
                                    FilterChip(
                                        selected = weekdayWeekendColIdx == idx,
                                        onClick = { weekdayWeekendColIdx = idx },
                                        leadingIcon = {
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(pair.second))
                                        },
                                        label = { Text(pair.first, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. AMBIENT PHOTOMETRY & NIGHT CYCLE
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "2. AMBIENT PHOTOMETRY & NIGHT CYCLE",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Brightness via LDR Sensor", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Dynamically adapts matrix gamma via onboard light sensor", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Switch(checked = autoBri, onCheckedChange = { autoBri = it })
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Day Brightness Ceiling", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        Text("${dayBri.toInt()}%", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                    }
                    Slider(value = dayBri, onValueChange = { dayBri = it }, valueRange = 5f..100f, steps = 19)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Night Mode (Quiet Dim Red Clock)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Ultra-dim red display during quiet hours to avoid sleep disturbance", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Switch(checked = nightMode, onCheckedChange = { nightMode = it })
                }

                AnimatedVisibility(visible = nightMode) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Night Brightness Floor", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                Text("${nightBri.toInt()}%", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                            }
                            Slider(value = nightBri, onValueChange = { nightBri = it }, valueRange = 1f..25f, steps = 24)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Quiet Hours Schedule:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = nightStart.toString(),
                                    onValueChange = { v -> v.toIntOrNull()?.let { nightStart = it.coerceIn(0, 23) } },
                                    label = { Text("Start") },
                                    modifier = Modifier.width(70.dp),
                                    singleLine = true
                                )
                                Text("→", fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = nightEnd.toString(),
                                    onValueChange = { v -> v.toIntOrNull()?.let { nightEnd = it.coerceIn(0, 23) } },
                                    label = { Text("End") },
                                    modifier = Modifier.width(70.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. CAROUSEL ENGINE & SCREEN SEQUENCE
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "3. CAROUSEL ENGINE & SCREEN ROTATION",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Rotate Carousel", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Cycles enabled screens sequentially", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Switch(checked = autoRotate, onCheckedChange = { autoRotate = it })
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Screen Dwell Interval", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        Text("${rotateSec.toInt()}s", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                    }
                    Slider(value = rotateSec, onValueChange = { rotateSec = it }, valueRange = 3f..120f, steps = 117)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("TRANSITION ANIMATION FX:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        transitionFxList.forEach { (fxId, label) ->
                            FilterChip(
                                selected = transitionFx == fxId,
                                onClick = { transitionFx = fxId },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("DEFAULT ACTIVE SCREEN (WHEN PAUSED):", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        screenNames.forEach { (appId, name) ->
                            FilterChip(
                                selected = activeApp == appId,
                                onClick = { activeApp = appId },
                                label = { Text(name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 9 Screens in Rotation Sequence
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ACTIVE SCREENS IN ROTATION SEQUENCE (9 SCREENS):", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        screenNames.forEach { (appId, name) ->
                            val isEnabled = (carouselMask and (1 shl appId)) != 0
                            FilterChip(
                                selected = isEnabled,
                                onClick = {
                                    val newMask = carouselMask xor (1 shl appId)
                                    carouselMask = if (newMask == 0) 0x01 else newMask
                                },
                                leadingIcon = if (isEnabled) { { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp)) } } else null,
                                label = { Text(name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // 4. BATTERY TELEMETRY FORMAT
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "4. BATTERY TELEMETRY FORMAT",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "Configure how battery metrics are formatted on screen 6",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    batteryModes.forEach { (bMode, label) ->
                        FilterChip(
                            selected = batMode == bMode,
                            onClick = { batMode = bMode },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // 5. RETRO AMBIENT SCREENSAVER
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "5. RETRO AMBIENT SCREENSAVER",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("PROCEDURAL SCREENSAVER STYLE:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        screensaverStyles.forEach { (stId, label) ->
                            FilterChip(
                                selected = ssStyle == stId,
                                onClick = { ssStyle = stId },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("INACTIVITY TIMEOUT:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        screensaverTimeouts.forEach { (toMin, label) ->
                            FilterChip(
                                selected = ssTime == toMin,
                                onClick = { ssTime = toMin },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // 6. METEOROLOGICAL TELEMETRY (OPEN-METEO)
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "6. METEOROLOGICAL TELEMETRY (OPEN-METEO)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                )

                Text("PREDEFINED CITY PRESETS:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cityPresets.forEach { (city, coords) ->
                        FilterChip(
                            selected = weatherCity.equals(city, ignoreCase = true),
                            onClick = {
                                weatherCity = city
                                weatherLat = coords.first
                                weatherLon = coords.second
                            },
                            label = { Text(city, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = weatherCity,
                    onValueChange = { weatherCity = it },
                    label = { Text("City Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weatherLat.toString(),
                        onValueChange = { v -> v.toFloatOrNull()?.let { weatherLat = it } },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = weatherLon.toString(),
                        onValueChange = { v -> v.toFloatOrNull()?.let { weatherLon = it } },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // 7. FINANCIAL MARKETS & CRYPTO TICKER
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "7. FINANCIAL MARKETS & CRYPTO TICKER",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                )

                Text("ASSET & COMMODITY PRESETS:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    assetPresets.forEach { sym ->
                        FilterChip(
                            selected = cryptoSymbol.equals(sym, ignoreCase = true),
                            onClick = { cryptoSymbol = sym },
                            label = { Text(sym, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = cryptoSymbol,
                    onValueChange = { cryptoSymbol = it },
                    label = { Text("Custom Symbol / Ticker (e.g. BTC, ETH, TSLA, GOLD, EUR)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // 8. YOUTUBE CHANNEL TELEMETRY
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "8. YOUTUBE CHANNEL TELEMETRY",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                )

                OutlinedTextField(
                    value = ytChannel,
                    onValueChange = { ytChannel = it },
                    label = { Text("YouTube Channel ID or @Handle") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = ytKey,
                    onValueChange = { ytKey = it },
                    label = { Text(if (status?.ytHasKey == true) "Google Cloud API Key (Stored ✓)" else "Google Cloud API Key") },
                    placeholder = { Text(if (status?.ytHasKey == true) "•••••••• (Leave blank to keep unchanged)" else "Enter API key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // 9. HOME ASSISTANT & MQTT TELEMETRY
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "9. HOME ASSISTANT & MQTT",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = if (status?.mqttConnected == true) "● MQTT BROKER CONNECTED" else "○ MQTT Client Offline",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (status?.mqttConnected == true) Color(0xFF00E676) else Color.Gray
                            )
                        )
                    }
                    Switch(checked = mqttEn, onCheckedChange = { mqttEn = it })
                }

                AnimatedVisibility(visible = mqttEn) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = mqttSrv,
                                onValueChange = { mqttSrv = it },
                                label = { Text("Broker Host / IP") },
                                modifier = Modifier.weight(2f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = mqttPort.toString(),
                                onValueChange = { v -> v.toIntOrNull()?.let { mqttPort = it } },
                                label = { Text("Port") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = mqttUser,
                                onValueChange = { mqttUser = it },
                                label = { Text("Username") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = mqttPass,
                                onValueChange = { mqttPass = it },
                                label = { Text(if (status?.mqttHasPass == true) "Password (Stored ✓)" else "Password") },
                                placeholder = { Text(if (status?.mqttHasPass == true) "••••••••" else "") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = mqttPre,
                            onValueChange = { mqttPre = it },
                            label = { Text("Base Topic Prefix") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // 10. MULTI-NETWORK WI-FI ROAMING MEMORY (4 SLOTS)
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "10. WI-FI ROAMING MEMORY (4 SLOTS)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Stores up to 4 Wi-Fi profiles in hardware flash with auto-roaming",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    IconButton(onClick = { viewModel.refreshWifi() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh Wi-Fi Profiles")
                    }
                }

                // Slot selection chips
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (slot in 1..4) {
                        val prof = profiles.find { it.slot == slot }
                        val isConnected = prof?.isCurrentlyActive == true
                        val hasSsid = !prof?.ssid.isNullOrBlank()
                        val chipText = when {
                            isConnected -> "Slot $slot: ${prof.ssid} ●"
                            hasSsid -> "Slot $slot: ${prof.ssid}"
                            else -> "Slot $slot (Empty)"
                        }
                        FilterChip(
                            selected = selectedWifiSlot == slot,
                            onClick = { selectedWifiSlot = slot },
                            leadingIcon = if (isConnected) { { Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF00E676))) } } else null,
                            label = { Text(chipText, fontSize = 11.sp, fontWeight = if (isConnected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                // Selected Slot Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PROFILE #$selectedWifiSlot CONFIGURATION",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            )
                            if (currentSlotProfile?.isCurrentlyActive == true) {
                                Text(
                                    text = "● ACTIVE RF LINK",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                                )
                            }
                        }

                        OutlinedTextField(
                            value = wifiSsidInput,
                            onValueChange = { wifiSsidInput = it },
                            label = { Text("Slot $selectedWifiSlot SSID / Network Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = wifiPassInput,
                            onValueChange = { wifiPassInput = it },
                            label = { Text(if (currentSlotProfile?.hasPass == true) "Slot $selectedWifiSlot Password (Stored ✓)" else "Slot $selectedWifiSlot Password") },
                            placeholder = { Text(if (currentSlotProfile?.hasPass == true) "•••••••• (Leave blank to keep unchanged)" else "Enter Wi-Fi password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.connectWifiSlot(selectedWifiSlot) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Connect Slot #$selectedWifiSlot")
                            }
                            OutlinedButton(
                                onClick = { viewModel.deleteWifiSlot(selectedWifiSlot) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Clear Slot", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Wi-Fi RF Scanner Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.scanWifiNetworks() },
                        enabled = !uiState.isScanningWifi,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (uiState.isScanningWifi) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scanning RF...", fontSize = 12.sp)
                        } else {
                            Text("📡 Scan Nearby Wi-Fi Networks", fontSize = 12.sp)
                        }
                    }
                }

                if (uiState.scannedWifiList.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("TAP DETECTED NETWORK TO ASSIGN TO SLOT #$selectedWifiSlot:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.scannedWifiList.forEach { scanItem ->
                                FilterChip(
                                    selected = wifiSsidInput == scanItem.ssid,
                                    onClick = { wifiSsidInput = scanItem.ssid },
                                    label = { Text("${scanItem.ssid} (${scanItem.rssi} dBm)", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section F: Commit & Actions Bottom Bar
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "COMMIT TO HARDWARE STORAGE",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                )

                Button(
                    onClick = saveAllAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("💾 Save All Settings to Clock EEPROM & Flash", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DISCOVERY CARD
// -------------------------------------------------------------

@Composable
fun DiscoveryControlCard(
    discoveryState: DiscoveryState,
    manualIp: String,
    onManualIpChange: (String) -> Unit,
    onConnectManual: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onSelectDevice: (DiscoveredDevice) -> Unit,
    autoReconnect: Boolean = true,
    onAutoReconnectChange: (Boolean) -> Unit = {},
    lastSavedHost: String = ""
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "DEVICE AUTO-DISCOVERY & CONNECTION",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = manualIp,
                    onValueChange = onManualIpChange,
                    label = { Text("IP or Hostname") },
                    singleLine = true,
                    trailingIcon = {
                        if (manualIp.isNotEmpty()) {
                            IconButton(onClick = { onManualIpChange("") }) {
                                Text("✕", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onConnectManual,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Connect")
                }
            }

            if (lastSavedHost.isNotBlank() && manualIp != lastSavedHost) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Last device:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    FilterChip(
                        selected = false,
                        onClick = { onManualIpChange(lastSavedHost) },
                        label = { Text(lastSavedHost, fontSize = 12.sp, fontFamily = FontFamily.Monospace) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Auto-Reconnect on Launch",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "Automatically connects to last known device when app starts",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = autoReconnect,
                    onCheckedChange = onAutoReconnectChange
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (discoveryState is DiscoveryState.Scanning) {
                    Button(
                        onClick = onStopScan,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Stop Scanning")
                    }
                } else {
                    Button(
                        onClick = onStartScan,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🔍 Scan Local Network (mDNS & Subnet)")
                    }
                }
            }

            if (discoveryState is DiscoveryState.Scanning) {
                LinearProgressIndicator(
                    progress = { discoveryState.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Probing local network subnet... ${(discoveryState.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
            }

            val foundList = when (discoveryState) {
                is DiscoveryState.Scanning -> discoveryState.found
                is DiscoveryState.Finished -> discoveryState.devices
                else -> emptyList()
            }

            if (foundList.isNotEmpty()) {
                Text(
                    text = "Discovered Devices (${foundList.size}):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                foundList.forEach { dev ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectDevice(dev) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Ulanzi TC001 (${dev.status.appName})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "IP: ${dev.ip} • FW: v${dev.status.firmwareVersion} • RSSI: ${dev.status.rssi} dBm",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                )
                            }
                            Button(onClick = { onSelectDevice(dev) }) {
                                Text("Select")
                            }
                        }
                    }
                }
            }
        }
    }
}
