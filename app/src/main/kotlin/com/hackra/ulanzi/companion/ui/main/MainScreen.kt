package com.hackra.ulanzi.companion.ui.main

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
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
                                text = tab.title,
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (y in 0 until 8) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val row = pixels.getOrNull(y).orEmpty()
                                for (x in 0 until 32) {
                                    val hex = row.getOrNull(x) ?: "#000000"
                                    val color = try {
                                        Color(android.graphics.Color.parseColor(hex))
                                    } catch (_: Exception) {
                                        Color(0xFF101418)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(1.5.dp))
                                            .background(color)
                                    )
                                }
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
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
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
                Column {
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
                    Text("◀ Previous Screen")
                }
                OutlinedButton(
                    onClick = onStepNext,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Next Screen ▶")
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
                Text("Suppress Ongoing (Music, Downloads)", style = MaterialTheme.typography.bodySmall)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HardwareSetupDeckCard(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    val prefs = uiState.devicePreferences
    val status = uiState.deviceStatus

    // Illumination
    var autoBri by remember(prefs.autoBrightness, status?.autoBri) {
        mutableStateOf(status?.autoBri ?: prefs.autoBrightness)
    }
    var dayBri by remember(prefs.dayBrightness, status?.dayBri) {
        mutableFloatStateOf((status?.dayBri ?: prefs.dayBrightness).toFloat())
    }
    var nightBri by remember(prefs.nightBrightness, status?.nightBri) {
        mutableFloatStateOf((status?.nightBri ?: prefs.nightBrightness).toFloat())
    }

    // Carousel
    var autoRotate by remember(status?.autoRotate) { mutableStateOf(status?.autoRotate ?: true) }
    var rotateSec by remember(prefs.carouselRotateSec, status?.rotateSec) {
        mutableFloatStateOf((status?.rotateSec ?: prefs.carouselRotateSec).toFloat())
    }
    var transitionFx by remember(status?.transitionFx) { mutableIntStateOf(status?.transitionFx ?: 0) }

    // Screen Mask Checkboxes (bitmask)
    var maskClock by remember { mutableStateOf(true) }
    var maskWeather by remember { mutableStateOf(true) }
    var maskCrypto by remember { mutableStateOf(true) }
    var maskYouTube by remember { mutableStateOf(true) }
    var maskBattery by remember { mutableStateOf(true) }
    var maskAmbient by remember { mutableStateOf(true) }

    // Time & Localization
    var timezoneStr by remember { mutableStateOf("CET-1CEST,M3.5.0,M10.5.0/3") }
    var timeFormat24h by remember { mutableStateOf(true) }
    var ntpServer by remember { mutableStateOf("pool.ntp.org") }

    // Online APIs
    var weatherCity by remember(status?.weatherCity) { mutableStateOf(status?.weatherCity ?: "Berlin,DE") }
    var weatherKey by remember { mutableStateOf("") }
    var cryptoSymbol by remember(status?.cryptoSymbol) { mutableStateOf(status?.cryptoSymbol ?: "BTC") }
    var cryptoCurrency by remember { mutableStateOf("USD") }
    var ytChannel by remember { mutableStateOf("") }

    // Multi-Network Wi-Fi
    var selectedWifiSlot by remember { mutableIntStateOf(0) }
    var wifiSsidInput by remember { mutableStateOf("") }
    var wifiPassInput by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Section A: Illumination & Auto-Brightness
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "1. ILLUMINATION & BRIGHTNESS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("LDR Ambient Auto-Brightness", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Adapts matrix brightness via onboard light sensor", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Switch(checked = autoBri, onCheckedChange = { autoBri = it })
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Daytime Brightness", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        Text("${dayBri.toInt()}%", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                    }
                    Slider(value = dayBri, onValueChange = { dayBri = it }, valueRange = 0f..100f, steps = 19)
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nighttime Brightness", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        Text("${nightBri.toInt()}%", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                    }
                    Slider(value = nightBri, onValueChange = { nightBri = it }, valueRange = 0f..100f, steps = 19)
                }
            }
        }

        // Section B: Carousel & Transitions
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "2. CAROUSEL & SCREEN TRANSITIONS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-Rotate Carousel", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Switch(checked = autoRotate, onCheckedChange = { autoRotate = it })
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Carousel Auto-Rotate Interval", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        Text("${rotateSec.toInt()}s", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                    }
                    Slider(value = rotateSec, onValueChange = { rotateSec = it }, valueRange = 3f..30f, steps = 26)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("TRANSITION FX:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    val fxList = listOf(0 to "Instant Cut", 1 to "Smooth Slide", 2 to "Cross Fade")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        fxList.forEach { (fxId, label) ->
                            FilterChip(
                                selected = transitionFx == fxId,
                                onClick = { transitionFx = fxId },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("SCREENS IN ROTATION MASK:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = maskClock, onCheckedChange = { maskClock = it })
                        Text("Clock", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(16.dp))
                        Checkbox(checked = maskWeather, onCheckedChange = { maskWeather = it })
                        Text("Weather", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(16.dp))
                        Checkbox(checked = maskCrypto, onCheckedChange = { maskCrypto = it })
                        Text("Crypto", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = maskYouTube, onCheckedChange = { maskYouTube = it })
                        Text("YouTube", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(16.dp))
                        Checkbox(checked = maskBattery, onCheckedChange = { maskBattery = it })
                        Text("Battery", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(16.dp))
                        Checkbox(checked = maskAmbient, onCheckedChange = { maskAmbient = it })
                        Text("Ambient SHT30", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Section C: Time, Date & NTP Sync
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "3. TIMEZONE & NTP CONFIGURATION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = timezoneStr,
                    onValueChange = { timezoneStr = it },
                    label = { Text("POSIX Timezone String") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("CET-1CEST,M3.5.0,M10.5.0/3", "UTC0", "EST5EDT,M3.2.0,M11.1.0", "PST8PDT,M3.2.0,M11.1.0").forEach { tz ->
                        FilterChip(
                            selected = timezoneStr == tz,
                            onClick = { timezoneStr = tz },
                            label = { Text(tz.substringBefore(","), fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("24-Hour Time Format", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Switch(checked = timeFormat24h, onCheckedChange = { timeFormat24h = it })
                }

                OutlinedTextField(
                    value = ntpServer,
                    onValueChange = { ntpServer = it },
                    label = { Text("NTP Time Server Host") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Section D: Online APIs & Market Feeds
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "4. ONLINE DATA FEEDS & API KEYS",
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
                    OutlinedTextField(
                        value = weatherCity,
                        onValueChange = { weatherCity = it },
                        label = { Text("City,Country") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cryptoSymbol,
                        onValueChange = { cryptoSymbol = it },
                        label = { Text("Crypto Symbol") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("BTC", "ETH", "SOL", "BNB", "DOGE", "XRP", "TSLA", "NVDA").forEach { sym ->
                        FilterChip(
                            selected = cryptoSymbol.equals(sym, ignoreCase = true),
                            onClick = { cryptoSymbol = sym },
                            label = { Text(sym, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = weatherKey,
                    onValueChange = { weatherKey = it },
                    label = { Text("OpenWeatherMap API Key (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Section E: Multi-Network Wi-Fi Memory
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
                    Text(
                        text = "5. MULTI-NETWORK WI-FI MEMORY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    IconButton(onClick = { viewModel.refreshWifi() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh Wi-Fi")
                    }
                }

                Text(
                    text = "Stores up to 4 Wi-Fi profiles in hardware flash with automatic best-signal roaming.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (slot in 0..3) {
                        FilterChip(
                            selected = selectedWifiSlot == slot,
                            onClick = { selectedWifiSlot = slot },
                            label = { Text("Slot #$slot", fontSize = 12.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = wifiSsidInput,
                        onValueChange = { wifiSsidInput = it },
                        label = { Text("Slot $selectedWifiSlot SSID") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = wifiPassInput,
                        onValueChange = { wifiPassInput = it },
                        label = { Text("Password") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.connectWifiSlot(selectedWifiSlot) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connect to Slot #$selectedWifiSlot")
                    }
                }
            }
        }

        // Section F: Commit & Actions
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "6. COMMIT TO HARDWARE EEPROM",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )

                Button(
                    onClick = {
                        var mask = 0
                        if (maskClock) mask = mask or 1
                        if (maskWeather) mask = mask or 2
                        if (maskCrypto) mask = mask or 4
                        if (maskYouTube) mask = mask or 8
                        if (maskBattery) mask = mask or 16
                        if (maskAmbient) mask = mask or 32

                        viewModel.saveHardwareSettings(
                            dayBri = dayBri.toInt(),
                            nightBri = nightBri.toInt(),
                            autoBri = autoBri,
                            rotateSec = rotateSec.toInt(),
                            autoRotate = autoRotate,
                            carouselMask = mask,
                            transitionFx = transitionFx,
                            city = weatherCity,
                            weatherKey = weatherKey,
                            cryptoSymbol = cryptoSymbol,
                            cryptoCurrency = cryptoCurrency,
                            ytChannel = ytChannel,
                            timezone = timezoneStr,
                            timeFormat24h = timeFormat24h,
                            ntpServer = ntpServer,
                            wifiSlot = if (wifiSsidInput.isNotBlank()) selectedWifiSlot else null,
                            wifiSsid = if (wifiSsidInput.isNotBlank()) wifiSsidInput else null,
                            wifiPass = if (wifiSsidInput.isNotBlank()) wifiPassInput else null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("💾 Save All Settings to Clock EEPROM & Flash")
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
