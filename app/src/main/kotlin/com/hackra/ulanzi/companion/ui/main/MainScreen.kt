package com.hackra.ulanzi.companion.ui.main

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.OutlinedCard
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
import kotlinx.coroutines.delay

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
                            val fwVer = uiState.deviceStatus?.firmwareVersion?.let { if (it.startsWith("v", ignoreCase = true)) it else "v$it" }
                            val subTitle = if (fwVer != null) "FW $fwVer • $statusText" else statusText
                            Text(
                                text = subTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                actions = {
                    var showOverflowMenu by remember { mutableStateOf(false) }

                    if (uiState.firmwareUpdateState.hasUpdate) {
                        IconButton(onClick = { viewModel.selectTab(NavigationTab.SETTINGS) }) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    Icons.Filled.SystemUpdate,
                                    contentDescription = "Firmware Update Available",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF9100))
                                )
                            }
                        }
                    }
                    if (uiState.connectionState is ConnectionState.Connected) {
                        IconButton(onClick = { viewModel.refreshStatus() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh Telemetry")
                        }
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("About Companion") },
                                leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.setShowAboutDialog(true)
                                }
                            )
                            if (uiState.connectionState is ConnectionState.Connected) {
                                DropdownMenuItem(
                                    text = { Text("Reboot Clock", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showOverflowMenu = false
                                        showRebootConfirm = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Disconnect") },
                                    leadingIcon = { Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.disconnect()
                                    }
                                )
                            }
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
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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

            LaunchedEffect(uiState.actionMessage) {
                if (uiState.actionMessage != null) {
                    delay(3500L)
                    viewModel.clearActionMessage()
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

    if (uiState.showAboutDialog) {
        AboutDialog(
            onDismiss = { viewModel.setShowAboutDialog(false) }
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

    // Top Segmented Switcher: Live Stream vs. Pixel Canvas Studio
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val isLive = uiState.dashboardViewMode == DashboardViewMode.LIVE_STREAM
        Button(
            onClick = { viewModel.setDashboardViewMode(DashboardViewMode.LIVE_STREAM) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLive) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (isLive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = null
        ) {
            Icon(Icons.Filled.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Live Stream",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isLive) FontWeight.Bold else FontWeight.Normal)
            )
        }

        val isCanvas = uiState.dashboardViewMode == DashboardViewMode.CANVAS_STUDIO
        Button(
            onClick = { viewModel.setDashboardViewMode(DashboardViewMode.CANVAS_STUDIO) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCanvas) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (isCanvas) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = null
        ) {
            Icon(Icons.Filled.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Pixel Canvas",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isCanvas) FontWeight.Bold else FontWeight.Normal)
            )
        }
    }

    if (uiState.dashboardViewMode == DashboardViewMode.LIVE_STREAM) {
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
    } else {
        // Interactive Pixel Canvas Studio
        InteractivePixelCanvasCard(
            uiState = uiState,
            viewModel = viewModel
        )
    }
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

    // 2. Acoustic Signal & Buzzer Testbench Deck
    AcousticBuzzerDeckCard(uiState = uiState, viewModel = viewModel)

    // 3. Ambient Screensaver Deck
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
    FirmwareUpdateDeckCard(uiState = uiState, viewModel = viewModel)
    HardwareSetupDeckCard(uiState = uiState, viewModel = viewModel)
    AboutContactDeckCard(onOpenAboutDialog = { viewModel.setShowAboutDialog(true) })
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
                Column(modifier = Modifier.weight(1f)) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractivePixelCanvasCard(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "32×8 NEOPIXEL CANVAS STUDIO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Touch, drag, or stamp pixel art directly to matrix",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            // Interactive 32x8 Grid Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(32f / 8f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A0E14))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(uiState.canvasTool, uiState.canvasSelectedColor) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val cellW = size.width / 32f
                                    val cellH = size.height / 8f
                                    val x = (offset.x / cellW).toInt().coerceIn(0, 31)
                                    val y = (offset.y / cellH).toInt().coerceIn(0, 7)
                                    viewModel.drawPixel(x, y)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val cellW = size.width / 32f
                                    val cellH = size.height / 8f
                                    val x = (change.position.x / cellW).toInt().coerceIn(0, 31)
                                    val y = (change.position.y / cellH).toInt().coerceIn(0, 7)
                                    viewModel.drawPixel(x, y)
                                }
                            )
                        }
                        .pointerInput(uiState.canvasTool, uiState.canvasSelectedColor) {
                            detectTapGestures { offset ->
                                val cellW = size.width / 32f
                                val cellH = size.height / 8f
                                val x = (offset.x / cellW).toInt().coerceIn(0, 31)
                                val y = (offset.y / cellH).toInt().coerceIn(0, 7)
                                viewModel.drawPixel(x, y)
                            }
                        }
                ) {
                    val cellW = size.width / 32f
                    val cellH = size.height / 8f
                    val gapX = cellW * 0.14f
                    val gapY = cellH * 0.14f
                    val dotW = cellW - gapX
                    val dotH = cellH - gapY
                    val corner = CornerRadius(minOf(dotW, dotH) * 0.25f)

                    for (y in 0 until 8) {
                        val row = uiState.canvasPixels.getOrNull(y).orEmpty()
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
            }

            // Tool Selection & Active Color Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tools (Pen, Eraser, Bucket)
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = uiState.canvasTool == CanvasTool.PEN,
                        onClick = { viewModel.setCanvasTool(CanvasTool.PEN) },
                        label = { Text("Pen") },
                        leadingIcon = {
                            Icon(Icons.Filled.Brush, contentDescription = "Pen", modifier = Modifier.size(16.dp))
                        }
                    )
                    FilterChip(
                        selected = uiState.canvasTool == CanvasTool.ERASER,
                        onClick = { viewModel.setCanvasTool(CanvasTool.ERASER) },
                        label = { Text("Eraser") },
                        leadingIcon = {
                            Icon(Icons.Filled.CleaningServices, contentDescription = "Eraser", modifier = Modifier.size(16.dp))
                        }
                    )
                    FilterChip(
                        selected = uiState.canvasTool == CanvasTool.BUCKET,
                        onClick = { viewModel.setCanvasTool(CanvasTool.BUCKET) },
                        label = { Text("Fill") },
                        leadingIcon = {
                            Icon(Icons.Filled.FormatColorFill, contentDescription = "Bucket Fill", modifier = Modifier.size(16.dp))
                        }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Display Active Color Preview
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val activeColor = try {
                        Color(android.graphics.Color.parseColor(uiState.canvasSelectedColor))
                    } catch (_: Exception) {
                        Color.White
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(activeColor)
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                    Text(
                        text = uiState.canvasSelectedColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }

            // Vibrant Color Palette Swatches
            val paletteColors = listOf(
                "#FF0055", "#FF3333", "#FF9100", "#FFD600",
                "#00E676", "#00C853", "#00E5FF", "#2979FF",
                "#7C4DFF", "#D500F9", "#FFFFFF", "#808080", "#000000"
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paletteColors.forEach { hex ->
                    val isSelected = uiState.canvasSelectedColor.equals(hex, ignoreCase = true)
                    val swatchColor = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (_: Exception) {
                        Color.DarkGray
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable {
                                viewModel.setCanvasSelectedColor(hex)
                                if (uiState.canvasTool == CanvasTool.ERASER) {
                                    viewModel.setCanvasTool(CanvasTool.PEN)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            val checkTint = if (hex == "#FFFFFF" || hex == "#FFD600" || hex == "#00E5FF" || hex == "#00E676") Color.Black else Color.White
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = checkTint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Built-in Presets Stamps
            Text(
                text = "RETRO PRESETS & STAMPS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "heart" to "❤️ Love",
                    "pacman" to "👻 Pac-Man",
                    "space_invaders" to "👾 Invader",
                    "vu_meter" to "📊 Equalizer",
                    "rainbow" to "🌈 Rainbow",
                    "fire" to "🔥 Flame",
                    "smiley" to "😎 Shades"
                ).forEach { (presetKey, label) ->
                    OutlinedButton(
                        onClick = { viewModel.loadCanvasPreset(presetKey) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            HorizontalDivider()

            // Display Duration selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "MATRIX DISPLAY DURATION:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15, 30, 60, 180).forEach { sec ->
                        FilterChip(
                            selected = uiState.canvasDisplayDurationSec == sec,
                            onClick = { viewModel.setCanvasDuration(sec) },
                            label = { Text("${sec}s", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Send Button
                Button(
                    onClick = { viewModel.sendCanvasToClock() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSendingCanvas && uiState.connectionState is ConnectionState.Connected,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isSendingCanvas) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rendering on Hardware...")
                    } else {
                        Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.connectionState is ConnectionState.Connected) "Send to Clock Matrix" else "Connect Clock First",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Secondary Utility Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Clone from Clock Live Display
                    OutlinedButton(
                        onClick = { viewModel.grabDisplayToCanvas() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clone Clock", style = MaterialTheme.typography.labelSmall)
                    }

                    // Clear Canvas
                    OutlinedButton(
                        onClick = { viewModel.clearCanvas() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All", style = MaterialTheme.typography.labelSmall)
                    }

                    // Fill All
                    OutlinedButton(
                        onClick = { viewModel.fillCanvas(uiState.canvasSelectedColor) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.FormatColorFill, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fill All", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusDiodePill(
    name: String,
    active: Boolean,
    activeColor: Color
) {
    Surface(
        color = if (active) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (active) activeColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        if (active) activeColor else Color.Gray.copy(alpha = 0.4f),
                        CircleShape
                    )
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (active) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusDiodePill(name = "MATRIX", active = true, activeColor = Color(0xFF00E676))
                StatusDiodePill(
                    name = "AUDIO",
                    active = status.buzzerPlaying,
                    activeColor = Color(0xFFFF9800)
                )
                StatusDiodePill(name = "WIFI", active = status.state == "connected", activeColor = Color(0xFF00E676))
                StatusDiodePill(name = "MQTT", active = status.mqttConnected, activeColor = Color(0xFF00E676))
                if (status.freeHeap != null && status.freeHeap > 0) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Heap: ${status.freeHeap / 1024}K",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

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
        1 to "🌡️ Indoor SHT30",
        2 to "⛅ Outdoor Weather",
        3 to "🪙 Crypto & Stocks",
        4 to "▶ YouTube Analytics",
        5 to "✈️ Airspace Radar",
        6 to "🔋 Battery & Power",
        7 to "⏱️ Stopwatch & Timer",
        8 to "✨ Ambient Screensaver"
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
                    label = { Text("⏳ Countdown", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = mode == "stopwatch",
                    onClick = { mode = "stopwatch" },
                    label = { Text("⏱ Stopwatch", maxLines = 1, overflow = TextOverflow.Ellipsis) },
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

// -------------------------------------------------------------
// ACOUSTIC SIGNAL & BUZZER TESTBENCH DECK
// -------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AcousticBuzzerDeckCard(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    val status = uiState.deviceStatus
    val isPlaying = status?.buzzerPlaying ?: false

    var rtttlInput by remember { mutableStateOf("") }
    var selectedToneFreq by remember { mutableIntStateOf(2400) }
    var selectedToneDuration by remember { mutableIntStateOf(100) }

    val presets = listOf(
        "click" to "🔉 Click",
        "alert" to "⚠️ Alert",
        "success" to "✔️ Success",
        "notification" to "🔔 Notification",
        "alarm" to "🚨 Alarm"
    )

    val melodyPresets = listOf(
        "Mario" to "mario:d=4,o=5,b=100:16e6,16e6,32p,8e6,16c6,8e6,8g6,8p,8g",
        "Pac-Man" to "pacman:d=4,o=5,b=120:32b,32p,32b6,32p,32f#6,32p,32d#6,32p,32b6,32f#6,16d#6,32p,32c6,32p,32c7,32p,32g6,32p,32e6,32p,32c7,32g6,16e6",
        "Nokia" to "nokia:d=4,o=5,b=160:e6,d6,f#,g#,c#6,b,d,e,b,a,c#,e,a",
        "Star Wars" to "starwars:d=4,o=5,b=45:32f,32f,32f,8a#.,8f.6,32d#6,32d6,32c6,8a#.6,8f.6,32d#6,32d6,32c6,8a#.6,4f.6"
    )

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
                        text = "ACOUSTIC SIGNAL & BUZZER TESTBENCH",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Hardware Piezoelectric Melody & Tone Sequencer",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                if (isPlaying) {
                    Surface(
                        color = Color(0xFFFF9800).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFFF9800)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFFF9800), CircleShape)
                            )
                            Text(
                                text = "PLAYING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF9800)
                                )
                            )
                        }
                    }
                }
            }

            // Section 1: Melodic Presets
            Text(
                text = "MELODIC SOUND PRESETS:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { (presetKey, label) ->
                    FilledTonalButton(
                        onClick = { viewModel.triggerBuzzerPreset(presetKey) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(label, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = { viewModel.stopBuzzer() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("⏹ Stop Audio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Section 2: RTTTL Ringtone Melody Player
            Text(
                text = "RTTTL MELODY SEQUENCER:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "Quick Presets:",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                melodyPresets.forEach { (name, rtttl) ->
                    SuggestionChip(
                        onClick = {
                            rtttlInput = rtttl
                            viewModel.playCustomRtttl(rtttl)
                        },
                        label = { Text("🎶 $name", fontSize = 11.sp) }
                    )
                }
            }

            OutlinedTextField(
                value = rtttlInput,
                onValueChange = { rtttlInput = it },
                label = { Text("RTTTL String") },
                placeholder = { Text("Pacman:d=4,o=5,b=120:...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )

            Button(
                onClick = { viewModel.playCustomRtttl(rtttlInput) },
                enabled = rtttlInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("▶ Play Custom RTTTL Melody")
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Section 3: Precision Frequency Tone Generator
            Text(
                text = "PRECISION TONE GENERATOR (BEEP):",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val freqs = listOf(800, 1500, 2400, 3200, 4000)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Frequency: ${selectedToneFreq} Hz",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        freqs.forEach { f ->
                            FilterChip(
                                selected = selectedToneFreq == f,
                                onClick = { selectedToneFreq = f },
                                label = { Text("${f}Hz", fontSize = 10.sp) }
                            )
                        }
                    }
                }

                val durations = listOf(50, 100, 200, 500)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Duration: ${selectedToneDuration} ms",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        durations.forEach { d ->
                            FilterChip(
                                selected = selectedToneDuration == d,
                                onClick = { selectedToneDuration = d },
                                label = { Text("${d}ms", fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { viewModel.playCustomTone(selectedToneFreq, selectedToneDuration) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("🔊 Emit Tone (${selectedToneFreq}Hz / ${selectedToneDuration}ms)")
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
                                viewModel.saveHardwareSettings(ssStyle = styleId, ssTime = selectedTimeout, successMessage = "Screensaver preset synced to clock ✓")
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
                                viewModel.saveHardwareSettings(ssStyle = selectedStyle, ssTime = min, successMessage = "Screensaver timeout synced to clock ✓")
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
                        Spacer(modifier = Modifier.width(8.dp))
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
                    val sounds = listOf(
                        0 to "🔇 Silent",
                        1 to "🔉 Click",
                        2 to "⚠️ Alert",
                        3 to "✔️ Success",
                        4 to "🔔 Chime",
                        5 to "🚨 Alarm"
                    )
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

            Text("ACOUSTIC SOUND PRESET:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            val sounds = listOf(
                0 to "🔇 Silent",
                1 to "🔉 Click",
                2 to "⚠️ Alert",
                3 to "✔️ Success",
                4 to "🔔 Chime",
                5 to "🚨 Alarm"
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                sounds.forEach { (sndIdx, sndLabel) ->
                    FilterChip(
                        selected = sound == sndIdx,
                        onClick = { onSoundChange(sndIdx) },
                        label = { Text(sndLabel, fontSize = 11.sp) }
                    )
                }
            }

            Text("DISPLAY DURATION: ${duration}s", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            val durations = listOf(3, 5, 8, 10, 15)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                durations.forEach { durSec ->
                    FilterChip(
                        selected = duration == durSec,
                        onClick = { onDurationChange(durSec) },
                        label = { Text("${durSec}s", fontSize = 11.sp) }
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

@Composable
fun <T> CompactDropdownSelector(
    label: String,
    items: List<Pair<T, String>>,
    selectedValue: T,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = items.find { it.first == selectedValue }?.second ?: selectedValue.toString()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        leadingIcon?.invoke()
                        Text(
                            text = currentLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                items.forEach { (value, itemLabel) ->
                    val isSelected = value == selectedValue
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = itemLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        onClick = {
                            onValueChange(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ColorDropdownPicker(
    label: String,
    items: List<Pair<Int, Pair<String, Color>>>,
    selectedIdx: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val currentItem = items.find { it.first == selectedIdx } ?: items.firstOrNull()
    val currentColor = currentItem?.second?.second ?: Color.White
    val currentName = currentItem?.second?.first ?: "Color"

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(currentColor)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                        Text(
                            text = currentName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Select Color",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                items.forEach { (idx, pair) ->
                    val name = pair.first
                    val color = pair.second
                    val isSelected = idx == selectedIdx
                    DropdownMenuItem(
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                        },
                        text = {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        onClick = {
                            onColorSelected(idx)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

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

    var isInitializedForDevice by remember(status?.ip) { mutableStateOf(false) }
    var forceReloadCounter by remember { mutableIntStateOf(0) }
    var showRebootConfirmInDeck by remember { mutableStateOf(false) }

    // Sync fields from device only on initial connection or when user explicitly reloads
    LaunchedEffect(status?.ip, forceReloadCounter) {
        val s = status
        if (s != null && (!isInitializedForDevice || forceReloadCounter > 0)) {
            clkMode = s.clkMode
            dateFormat = s.dateFormat
            clockIcon = s.clockIcon
            textAlign = s.textAlign
            showSeconds = s.showSeconds
            weekdayBar = s.weekdayBar
            weekdayStartMon = s.weekdayStartMon
            weekdayActiveColIdx = s.weekdayActiveColIdx
            weekdayInactColIdx = s.weekdayInactColIdx
            weekdayWeekendColIdx = s.weekdayWeekendColIdx
            autoBri = s.autoBri
            dayBri = s.dayBri.toFloat()
            nightMode = s.nightMode
            nightBri = s.nightBri.toFloat()
            nightStart = s.nightStart
            nightEnd = s.nightEnd
            autoRotate = s.autoRotate
            rotateSec = s.rotateSec.toFloat()
            transitionFx = s.transitionFx
            activeApp = s.activeApp
            carouselMask = s.carouselMask
            batMode = s.batMode
            ssStyle = s.screensaverStyle ?: 0
            ssTime = s.screensaverTimeout ?: 5
            weatherCity = s.weatherCity ?: "Bratislava"
            weatherLat = s.weatherLat ?: 48.1486f
            weatherLon = s.weatherLon ?: 17.1077f
            cryptoSymbol = s.cryptoSymbol ?: "BTC"
            ytChannel = s.ytChannel ?: ""
            mqttEn = s.mqttEn
            mqttSrv = s.mqttSrv ?: ""
            mqttPort = s.mqttPort
            mqttUser = s.mqttUser ?: ""
            mqttPre = s.mqttPre
            isInitializedForDevice = true
        }
    }

    val profiles = status?.wifiProfiles.takeIf { !it.isNullOrEmpty() } ?: uiState.wifiStatus?.profiles.orEmpty()
    val currentSlotProfile = profiles.find { it.slot == selectedWifiSlot }

    LaunchedEffect(selectedWifiSlot, currentSlotProfile?.ssid) {
        wifiSsidInput = currentSlotProfile?.ssid.orEmpty()
        wifiPassInput = ""
    }

    fun autoSave(
        overrideClkMode: Int? = null,
        overrideDateFormat: Int? = null,
        overrideClockIcon: Int? = null,
        overrideTextAlign: Int? = null,
        overrideShowSeconds: Boolean? = null,
        overrideWeekdayBar: Boolean? = null,
        overrideWeekdayStartMon: Boolean? = null,
        overrideWeekdayActiveColIdx: Int? = null,
        overrideWeekdayInactColIdx: Int? = null,
        overrideWeekdayWeekendColIdx: Int? = null,
        overrideAutoBri: Boolean? = null,
        overrideDayBri: Float? = null,
        overrideNightMode: Boolean? = null,
        overrideNightBri: Float? = null,
        overrideNightStart: Int? = null,
        overrideNightEnd: Int? = null,
        overrideAutoRotate: Boolean? = null,
        overrideRotateSec: Float? = null,
        overrideTransitionFx: Int? = null,
        overrideActiveApp: Int? = null,
        overrideCarouselMask: Int? = null,
        overrideBatMode: Int? = null,
        overrideSsStyle: Int? = null,
        overrideSsTime: Int? = null,
        overrideCity: String? = null,
        overrideLat: Float? = null,
        overrideLon: Float? = null,
        overrideCrypto: String? = null,
        overrideMqttEn: Boolean? = null,
        feedbackMessage: String? = "Setting synced to clock ✓"
    ) {
        viewModel.saveHardwareSettings(
            clkMode = overrideClkMode ?: clkMode,
            dateFormat = overrideDateFormat ?: dateFormat,
            clockIcon = overrideClockIcon ?: clockIcon,
            textAlign = overrideTextAlign ?: textAlign,
            showSeconds = overrideShowSeconds ?: showSeconds,
            weekdayBar = overrideWeekdayBar ?: weekdayBar,
            weekdayStartMon = overrideWeekdayStartMon ?: weekdayStartMon,
            weekdayActiveColIdx = overrideWeekdayActiveColIdx ?: weekdayActiveColIdx,
            weekdayInactColIdx = overrideWeekdayInactColIdx ?: weekdayInactColIdx,
            weekdayWeekendColIdx = overrideWeekdayWeekendColIdx ?: weekdayWeekendColIdx,
            autoBri = overrideAutoBri ?: autoBri,
            dayBri = (overrideDayBri ?: dayBri).toInt(),
            nightMode = overrideNightMode ?: nightMode,
            nightBri = (overrideNightBri ?: nightBri).toInt(),
            nightStart = overrideNightStart ?: nightStart,
            nightEnd = overrideNightEnd ?: nightEnd,
            autoRotate = overrideAutoRotate ?: autoRotate,
            rotateSec = (overrideRotateSec ?: rotateSec).toInt(),
            transitionFx = overrideTransitionFx ?: transitionFx,
            activeApp = overrideActiveApp ?: activeApp,
            carouselMask = overrideCarouselMask ?: carouselMask,
            batMode = overrideBatMode ?: batMode,
            ssStyle = overrideSsStyle ?: ssStyle,
            ssTime = overrideSsTime ?: ssTime,
            weatherCity = overrideCity ?: weatherCity,
            weatherLat = overrideLat ?: weatherLat,
            weatherLon = overrideLon ?: weatherLon,
            cryptoSymbol = overrideCrypto ?: cryptoSymbol,
            ytChannel = ytChannel,
            ytKey = ytKey.ifBlank { null },
            mqttEn = overrideMqttEn ?: mqttEn,
            mqttSrv = mqttSrv,
            mqttPort = mqttPort,
            mqttUser = mqttUser,
            mqttPass = mqttPass.ifBlank { null },
            mqttPre = mqttPre,
            successMessage = feedbackMessage
        )
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
            wifiSlot = if (wifiPassInput.isNotBlank()) selectedWifiSlot else null,
            wifiSsid = if (wifiPassInput.isNotBlank()) wifiSsidInput.ifBlank { null } else null,
            wifiPass = wifiPassInput.ifBlank { null }
        )
    }

    if (showRebootConfirmInDeck) {
        AlertDialog(
            onDismissRequest = { showRebootConfirmInDeck = false },
            title = { Text("Reboot Ulanzi TC001?") },
            text = { Text("This will trigger a hardware restart of the clock via /api/reboot. The matrix will reconnect automatically after booting.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRebootConfirmInDeck = false
                        viewModel.rebootClock()
                    }
                ) {
                    Text("Reboot Now", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootConfirmInDeck = false }) {
                    Text("Cancel")
                }
            }
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
                        IconButton(onClick = {
                            forceReloadCounter++
                            viewModel.refreshStatus()
                            viewModel.refreshWifi()
                        }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Reload from Clock")
                        }
                        IconButton(onClick = { showRebootConfirmInDeck = true }) {
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
                Text(
                    text = "⚡ Real-time sync: Toggles, sliders & dropdowns transfer to clock immediately.",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        val cityDropdownItems = remember { cityPresets.map { it.first to it.first } }
        val assetDropdownItems = remember { assetPresets.map { it to it } }

        // 1. CLOCK & DISPLAY ARCHITECTURE
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "1. CLOCK & DISPLAY ARCHITECTURE",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CompactDropdownSelector(
                        label = "CLOCK MODE",
                        items = clockModes,
                        selectedValue = clkMode,
                        onValueChange = {
                            clkMode = it
                            autoSave(overrideClkMode = it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CompactDropdownSelector(
                        label = "DATE NOTATION",
                        items = dateFormats,
                        selectedValue = dateFormat,
                        onValueChange = {
                            dateFormat = it
                            autoSave(overrideDateFormat = it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CompactDropdownSelector(
                        label = "CLOCK ICON",
                        items = clockIcons,
                        selectedValue = clockIcon,
                        onValueChange = {
                            clockIcon = it
                            autoSave(overrideClockIcon = it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CompactDropdownSelector(
                        label = "TEXT ALIGNMENT",
                        items = textAlignments,
                        selectedValue = textAlign,
                        onValueChange = {
                            textAlign = it
                            autoSave(overrideTextAlign = it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // High Precision Seconds Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Seconds (HH:mm:ss)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("High-precision 3×5 seconds counter on display", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Switch(
                        checked = showSeconds,
                        onCheckedChange = {
                            showSeconds = it
                            autoSave(overrideShowSeconds = it)
                        }
                    )
                }

                // Weekday Bar Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Weekday Indicators (7 Micro-Bars)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Row 7 visual status indicators for day of the week", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Switch(
                        checked = weekdayBar,
                        onCheckedChange = {
                            weekdayBar = it
                            autoSave(overrideWeekdayBar = it)
                        }
                    )
                }

                // Weekday Customization Options (Dropdown Popups)
                AnimatedVisibility(visible = weekdayBar) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Week Starting Day
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Week Starting Day:",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = weekdayStartMon,
                                    onClick = {
                                        weekdayStartMon = true
                                        autoSave(overrideWeekdayStartMon = true)
                                    },
                                    label = { Text("Mon (ISO)", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = !weekdayStartMon,
                                    onClick = {
                                        weekdayStartMon = false
                                        autoSave(overrideWeekdayStartMon = false)
                                    },
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
                                    val isToday = (idx == 2) // demo Wednesday
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

                        // Compact Color Dropdown Pickers
                        ColorDropdownPicker(
                            label = "ACTIVE DAY PHOSPHOR COLOR",
                            items = weekdayActiveColors,
                            selectedIdx = weekdayActiveColIdx,
                            onColorSelected = {
                                weekdayActiveColIdx = it
                                autoSave(overrideWeekdayActiveColIdx = it)
                            }
                        )

                        ColorDropdownPicker(
                            label = "INACTIVE DAY CONTRAST COLOR",
                            items = weekdayInactiveColors,
                            selectedIdx = weekdayInactColIdx,
                            onColorSelected = {
                                weekdayInactColIdx = it
                                autoSave(overrideWeekdayInactColIdx = it)
                            }
                        )

                        ColorDropdownPicker(
                            label = "WEEKEND PHOSPHOR COLOR",
                            items = weekendColors,
                            selectedIdx = weekdayWeekendColIdx,
                            onColorSelected = {
                                weekdayWeekendColIdx = it
                                autoSave(overrideWeekdayWeekendColIdx = it)
                            }
                        )
                    }
                }
            }
        }

        // 2. CAROUSEL & SCREEN SEQUENCE
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "2. CAROUSEL & SCREEN SEQUENCE",
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
                    Switch(
                        checked = autoRotate,
                        onCheckedChange = {
                            autoRotate = it
                            autoSave(overrideAutoRotate = it)
                        }
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Screen Dwell Interval", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        Text("${rotateSec.toInt()}s", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                    }
                    Slider(
                        value = rotateSec,
                        onValueChange = { rotateSec = it },
                        onValueChangeFinished = { autoSave(overrideRotateSec = rotateSec) },
                        valueRange = 3f..120f,
                        steps = 117
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CompactDropdownSelector(
                        label = "TRANSITION FX",
                        items = transitionFxList,
                        selectedValue = transitionFx,
                        onValueChange = {
                            transitionFx = it
                            autoSave(overrideTransitionFx = it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CompactDropdownSelector(
                        label = "DEFAULT SCREEN",
                        items = screenNames,
                        selectedValue = activeApp,
                        onValueChange = {
                            activeApp = it
                            autoSave(overrideActiveApp = it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 9 Screens in Rotation Sequence
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ACTIVE SCREENS IN ROTATION (9 SCREENS):", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        screenNames.forEach { (appId, name) ->
                            val isEnabled = (carouselMask and (1 shl appId)) != 0
                            FilterChip(
                                selected = isEnabled,
                                onClick = {
                                    val newMask = carouselMask xor (1 shl appId)
                                    val updated = if (newMask == 0) 0x01 else newMask
                                    carouselMask = updated
                                    autoSave(overrideCarouselMask = updated)
                                },
                                leadingIcon = if (isEnabled) { { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp)) } } else null,
                                label = { Text(name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // 3. PHOTOMETRY & AMBIENT DISPLAY
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "3. PHOTOMETRY & AMBIENT DISPLAY",
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
                    Switch(
                        checked = autoBri,
                        onCheckedChange = {
                            autoBri = it
                            autoSave(overrideAutoBri = it)
                        }
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Day Brightness Ceiling", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        Text("${dayBri.toInt()}%", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                    }
                    Slider(
                        value = dayBri,
                        onValueChange = { dayBri = it },
                        onValueChangeFinished = { autoSave(overrideDayBri = dayBri) },
                        valueRange = 5f..100f,
                        steps = 19
                    )
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
                    Switch(
                        checked = nightMode,
                        onCheckedChange = {
                            nightMode = it
                            autoSave(overrideNightMode = it)
                        }
                    )
                }

                AnimatedVisibility(visible = nightMode) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Night Brightness Floor", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                Text("${nightBri.toInt()}%", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                            }
                            Slider(
                                value = nightBri,
                                onValueChange = { nightBri = it },
                                onValueChangeFinished = { autoSave(overrideNightBri = nightBri) },
                                valueRange = 1f..25f,
                                steps = 24
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Quiet Hours Schedule (24h format):", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = nightStart.toString(),
                                    onValueChange = { v ->
                                        v.toIntOrNull()?.let {
                                            val clamped = it.coerceIn(0, 23)
                                            nightStart = clamped
                                            autoSave(overrideNightStart = clamped)
                                        }
                                    },
                                    label = { Text("Start (0-23h)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Text("→", fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = nightEnd.toString(),
                                    onValueChange = { v ->
                                        v.toIntOrNull()?.let {
                                            val clamped = it.coerceIn(0, 23)
                                            nightEnd = clamped
                                            autoSave(overrideNightEnd = clamped)
                                        }
                                    },
                                    label = { Text("End (0-23h)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Combined Battery and Screensaver Dropdowns
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CompactDropdownSelector(
                        label = "BATTERY TELEMETRY",
                        items = batteryModes,
                        selectedValue = batMode,
                        onValueChange = {
                            batMode = it
                            autoSave(overrideBatMode = it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CompactDropdownSelector(
                        label = "SCREENSAVER STYLE",
                        items = screensaverStyles,
                        selectedValue = ssStyle,
                        onValueChange = {
                            ssStyle = it
                            autoSave(overrideSsStyle = it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                CompactDropdownSelector(
                    label = "SCREENSAVER INACTIVITY TIMEOUT",
                    items = screensaverTimeouts,
                    selectedValue = ssTime,
                    onValueChange = {
                        ssTime = it
                        autoSave(overrideSsTime = it)
                    }
                )
            }
        }

        // 4. INTEGRATIONS & DATA FEEDS
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "4. INTEGRATIONS & DATA FEEDS",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                )

                // Weather Telemetry Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "METEOROLOGICAL TELEMETRY (OPEN-METEO)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    )

                    CompactDropdownSelector(
                        label = "PRESET CITY",
                        items = cityDropdownItems,
                        selectedValue = weatherCity,
                        onValueChange = { selectedCity ->
                            weatherCity = selectedCity
                            cityPresets.find { it.first == selectedCity }?.let { (_, coords) ->
                                weatherLat = coords.first
                                weatherLon = coords.second
                                autoSave(overrideCity = selectedCity, overrideLat = coords.first, overrideLon = coords.second, feedbackMessage = "Weather location set to $selectedCity ✓")
                            }
                        }
                    )

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

                    Button(
                        onClick = { autoSave(overrideCity = weatherCity, overrideLat = weatherLat, overrideLon = weatherLon, feedbackMessage = "Weather coordinates applied ✓") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📍 Apply Weather Coordinates")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Crypto & Financial Markets Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FINANCIAL MARKETS & CRYPTO TICKER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    )

                    CompactDropdownSelector(
                        label = "ASSET PRESET",
                        items = assetDropdownItems,
                        selectedValue = cryptoSymbol,
                        onValueChange = {
                            cryptoSymbol = it
                            autoSave(overrideCrypto = it, feedbackMessage = "Crypto ticker set to $it ✓")
                        }
                    )

                    OutlinedTextField(
                        value = cryptoSymbol,
                        onValueChange = { cryptoSymbol = it },
                        label = { Text("Custom Symbol / Ticker (e.g. BTC, ETH, TSLA, GOLD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = { autoSave(overrideCrypto = cryptoSymbol, feedbackMessage = "Crypto symbol applied ✓") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📊 Apply Crypto Symbol")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // YouTube Telemetry Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "YOUTUBE CHANNEL TELEMETRY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
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

                    Button(
                        onClick = {
                            viewModel.saveHardwareSettings(
                                ytChannel = ytChannel,
                                ytKey = ytKey.ifBlank { null },
                                successMessage = "YouTube settings applied ✓"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("▶ Apply YouTube Settings")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Home Assistant & MQTT Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HOME ASSISTANT & MQTT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
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
                    Switch(
                        checked = mqttEn,
                        onCheckedChange = {
                            mqttEn = it
                            autoSave(overrideMqttEn = it, feedbackMessage = if (it) "MQTT enabled ✓" else "MQTT disabled ✓")
                        }
                    )
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

                        Button(
                            onClick = {
                                viewModel.saveHardwareSettings(
                                    mqttEn = mqttEn,
                                    mqttSrv = mqttSrv,
                                    mqttPort = mqttPort,
                                    mqttUser = mqttUser,
                                    mqttPass = mqttPass.ifBlank { null },
                                    mqttPre = mqttPre,
                                    successMessage = "MQTT configuration applied ✓"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📡 Apply MQTT Configuration")
                        }
                    }
                }
            }
        }

        // 5. MULTI-NETWORK WI-FI ROAMING & SCANNER
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "5. WI-FI ROAMING MEMORY (4 SLOTS)",
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
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            )
                            if (currentSlotProfile?.isCurrentlyActive == true) {
                                Spacer(modifier = Modifier.width(8.dp))
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

                        Button(
                            onClick = {
                                viewModel.saveHardwareSettings(
                                    wifiSlot = selectedWifiSlot,
                                    wifiSsid = wifiSsidInput.ifBlank { null },
                                    wifiPass = wifiPassInput.ifBlank { null },
                                    successMessage = "Wi-Fi Profile #$selectedWifiSlot saved to clock!"
                                )
                            },
                            enabled = wifiSsidInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("💾 Save Profile #$selectedWifiSlot to Memory")
                        }
                    }
                }

                // Wi-Fi RF Scanner Section
                Button(
                    onClick = { viewModel.scanWifiNetworks() },
                    enabled = !uiState.isScanningWifi,
                    modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = "Instant settings sync immediately upon change. Tap below to execute a complete full-state EEPROM/Flash write cycle.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ulanzi TC001 (${dev.status.appName})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "IP: ${dev.ip} • FW: v${dev.status.firmwareVersion} • RSSI: ${dev.status.rssi} dBm",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
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


// =============================================================
// FIRMWARE UPDATE & ABOUT DECKS
// =============================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FirmwareUpdateDeckCard(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    val fwState = uiState.firmwareUpdateState
    val currentFw = uiState.deviceStatus?.firmwareVersion?.let { if (it.startsWith("v", ignoreCase = true)) it else "v$it" } ?: "v2.4.0"
    val isConnected = uiState.connectionState is ConnectionState.Connected
    val context = LocalContext.current

    var selectedTabIdx by remember { mutableIntStateOf(0) } // 0 = Cloud OTA, 1 = Local .bin Flash
    var customCloudUrl by remember(fwState.downloadUrl) { mutableStateOf(fwState.downloadUrl ?: "") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showFlashConfirm by remember { mutableStateOf(false) }
    var showCloudOtaConfirm by remember { mutableStateOf(false) }
    var showReleaseNotesDialog by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            var name = "firmware.bin"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIdx) ?: "firmware.bin"
                    }
                }
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                selectedFileName = name
                selectedFileBytes = bytes
            } catch (e: Exception) {
                // Ignore read error, handled during flash
            }
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DEVICE FIRMWARE & OTA UPDATE",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Cloud OTA releases, GitHub check & local .bin flashing",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            // Version comparison overview card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CLOCK FIRMWARE",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = if (isConnected) currentFw else "Clock Offline",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isConnected) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "LATEST GITHUB RELEASE",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = fwState.latestVersion ?: "Not Checked",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (fwState.hasUpdate) Color(0xFFFF9100) else MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    // Prominent Check GitHub button visible in both Portrait & Landscape
                    Button(
                        onClick = { viewModel.checkForFirmwareUpdate() },
                        enabled = !fwState.isChecking && !fwState.isUpdating,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (fwState.isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(if (fwState.isChecking) "Checking GitHub Releases..." else "Check GitHub for Updates")
                    }

                    if (fwState.hasUpdate) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Update Available: ${fwState.latestVersion}!",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (!fwState.releaseNotes.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    TextButton(
                                        onClick = { showReleaseNotesDialog = true }
                                    ) {
                                        Text("Release Notes", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    } else if (fwState.latestVersion != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Device is up to date with the latest official release.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }

            // In-Progress / Error / Success Banners
            AnimatedVisibility(visible = fwState.isUpdating) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fwState.updateStage ?: "Flashing firmware...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${fwState.progressPercent}%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { fwState.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Text(
                                text = "DO NOT unplug or turn off clock power during firmware flashing!",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = fwState.isSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                            Text(
                                text = fwState.updateStage ?: "Firmware updated successfully! Clock is rebooting...",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(onClick = { viewModel.dismissFirmwareStatus() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            AnimatedVisibility(visible = fwState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Text(
                                text = fwState.errorMessage ?: "Update failed",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            )
                        }
                        IconButton(onClick = { viewModel.dismissFirmwareStatus() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Method Selector: Cloud OTA vs Local Flash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTabIdx == 0,
                    onClick = { selectedTabIdx = 0 },
                    label = { Text("Cloud OTA (1-Click)") },
                    leadingIcon = { Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedTabIdx == 1,
                    onClick = { selectedTabIdx = 1 },
                    label = { Text("Local File (.bin)") },
                    leadingIcon = { Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedTabIdx == 0) {
                // Cloud OTA Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Cloud OTA instructs the Ulanzi TC001 to download the compiled binary directly over Wi-Fi from GitHub Releases and flash it into the OTA partition.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    OutlinedTextField(
                        value = customCloudUrl,
                        onValueChange = { customCloudUrl = it },
                        label = { Text("Firmware Binary URL (.bin)") },
                        placeholder = { Text("https://github.com/.../firmware.bin") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (fwState.downloadUrl != null) {
                                IconButton(onClick = { customCloudUrl = fwState.downloadUrl ?: "" }) {
                                    Icon(Icons.Filled.Download, contentDescription = "Fill Latest GitHub URL")
                                }
                            }
                        }
                    )
                    Button(
                        onClick = { showCloudOtaConfirm = true },
                        enabled = isConnected && customCloudUrl.isNotBlank() && !fwState.isUpdating,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Start Cloud OTA Update")
                    }
                    if (!isConnected) {
                        Text(
                            text = "⚠️ Clock must be connected to companion app to trigger OTA.",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                // Local File .bin Flashing Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Upload a pre-compiled firmware.bin from your Android downloads/storage directly over HTTP (/update endpoint) to the clock's ESP32 flash partition.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedFileName ?: "No file chosen",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = if (selectedFileName != null) FontFamily.Monospace else FontFamily.Default
                                    )
                                )
                                val sizeText = selectedFileBytes?.let { "${it.size / 1024} KB (${String.format(Locale.US, "%.2f", it.size / 1048576f)} MB)" } ?: "Supported: compiled .bin binaries"
                                Text(
                                    text = sizeText,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { filePicker.launch("*/*") },
                                enabled = !fwState.isUpdating,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Choose File")
                            }
                        }
                    }

                    Button(
                        onClick = { showFlashConfirm = true },
                        enabled = isConnected && selectedFileBytes != null && !fwState.isUpdating,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Flash Firmware Binary to Clock")
                    }
                    if (!isConnected) {
                        Text(
                            text = "⚠️ Connect clock first to enable local flashing.",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog: Cloud OTA
    if (showCloudOtaConfirm) {
        AlertDialog(
            onDismissRequest = { showCloudOtaConfirm = false },
            title = { Text("Confirm Cloud OTA Update") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("The clock will directly stream and install the firmware binary from:")
                    Text(
                        text = customCloudUrl,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                    )
                    Text("The matrix will reboot automatically upon successful flash. Do not disconnect power.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCloudOtaConfirm = false
                        viewModel.installCloudOta(customCloudUrl)
                    }
                ) {
                    Text("Proceed with OTA")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloudOtaConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog: Local File Flash
    if (showFlashConfirm) {
        AlertDialog(
            onDismissRequest = { showFlashConfirm = false },
            title = { Text("Confirm Local Firmware Flash") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("You are about to flash:")
                    Text(
                        text = "${selectedFileName} (${(selectedFileBytes?.size ?: 0) / 1024} KB)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    )
                    Text(
                        text = "⚠️ CRITICAL: Ensure the Ulanzi TC-001 has steady USB-C power. Interrupting the flash may require USB flashing via esptool.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFlashConfirm = false
                        selectedFileBytes?.let { bytes ->
                            viewModel.uploadLocalFirmware(bytes, selectedFileName ?: "firmware.bin")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Flash Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFlashConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Release Notes Dialog
    if (showReleaseNotesDialog && !fwState.releaseNotes.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { showReleaseNotesDialog = false },
            title = {
                Text("${fwState.releaseTitle ?: fwState.latestVersion ?: "Release Notes"}")
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    fwState.publishedAt?.take(10)?.let { pubDate ->
                        Text("Published: $pubDate", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = fwState.releaseNotes ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showReleaseNotesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AboutContactDeckCard(
    onOpenAboutDialog: () -> Unit
) {
    val context = LocalContext.current
    fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ABOUT & DEVELOPER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                TextButton(onClick = onOpenAboutDialog) {
                    Text("Full Details")
                }
            }

            Text(
                text = "Ulanzi TC001 Companion v1.0.8 is an open-source hardware companion app engineered by Radovan Dvorský (@hackra76) for the Ulanzi TC-001 Smart Pixel Clock running custom firmware (Evolved).",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { openBrowser("https://github.com/hackra76/Ulanzi-TC001-Companion") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Companion Repo", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Button(
                    onClick = { openBrowser("https://buymeacoffee.com/hackra76") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF813F)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Buy Me a Coffee", color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    val appVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.8"
        } catch (_: Exception) {
            "1.0.8"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Text(
                        text = "Ulanzi TC001 Companion",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Version $appVersion (Release Build)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "A complete, modern Android companion app for the Ulanzi TC-001 Smart Pixel Clock running custom ESP32 firmware (Ulanzi-TC001-Evolved).",
                    style = MaterialTheme.typography.bodySmall
                )

                HorizontalDivider()

                Text(
                    text = "KEY CAPABILITIES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("• 32×8 Neopixel live stream & Canvas Studio pixel drawing", style = MaterialTheme.typography.bodySmall)
                    Text("• Multi-profile Wi-Fi management with local subnet discovery", style = MaterialTheme.typography.bodySmall)
                    Text("• Live media metadata sync (Spotify, YouTube, Music apps)", style = MaterialTheme.typography.bodySmall)
                    Text("• Android notification forwarding with custom sounds & icons", style = MaterialTheme.typography.bodySmall)
                    Text("• Ambient photometry, night mode & carousel controls", style = MaterialTheme.typography.bodySmall)
                    Text("• 1-Click cloud OTA updates & local .bin firmware flashing", style = MaterialTheme.typography.bodySmall)
                }

                HorizontalDivider()

                Text(
                    text = "DEVELOPER & CREDITS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                )
                Text(
                    text = "Created by Radovan Dvorský (@hackra76)\nOpen-source hardware enthusiast & developer.",
                    style = MaterialTheme.typography.bodySmall
                )

                HorizontalDivider()

                Text(
                    text = "LINKS & RESOURCES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                )

                OutlinedButton(
                    onClick = { openBrowser("https://github.com/hackra76/Ulanzi-TC001-Companion") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Companion App GitHub Repository")
                }

                OutlinedButton(
                    onClick = { openBrowser("https://github.com/hackra76/Ulanzi-TC001-Evolved") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Firmware Evolved GitHub Repository")
                }

                OutlinedButton(
                    onClick = { openBrowser("https://github.com/hackra76/Ulanzi-TC001-Companion/issues") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Report Bug / Request Feature")
                }

                OutlinedButton(
                    onClick = { openBrowser("https://github.com/hackra76/Ulanzi-TC001-Companion/releases") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Releases & Changelogs")
                }

                Button(
                    onClick = { openBrowser("https://buymeacoffee.com/hackra76") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF813F)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Support on Buy Me a Coffee", color = Color.White)
                }

                HorizontalDivider()

                Text(
                    text = "LEGAL & DISCLAIMER",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                )
                Text(
                    text = "Released under the MIT License.\nUlanzi is a registered trademark of Shenzhen Ulanzi Technology Co., Ltd. This companion project is an independent open-source tool and is not officially affiliated with or endorsed by Ulanzi.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
