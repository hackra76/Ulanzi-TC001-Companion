package com.hackra.ulanzi.companion.ui.main

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.hackra.ulanzi.companion.data.model.DeviceStatus
import com.hackra.ulanzi.companion.data.model.DisplayMatrixResponse
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
    var manualIpInput by remember { mutableStateOf("ulanzi-tc001.local") }
    var notificationText by remember { mutableStateOf("Hello from Android!") }
    var selectedIcon by remember { mutableStateOf("bell") }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ULANZI TC001 COMPANION",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Tactile Field Instrument v2.2.0",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
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
            // 1. Connection Status Card
            ConnectionHeaderCard(
                connectionState = uiState.connectionState,
                onDisconnect = { viewModel.disconnect() }
            )

            // 2. Device Discovery & Manual Connection Section
            if (uiState.connectionState !is ConnectionState.Connected) {
                DiscoveryControlCard(
                    discoveryState = uiState.discoveryState,
                    manualIp = manualIpInput,
                    onManualIpChange = { manualIpInput = it },
                    onConnectManual = { viewModel.connect(manualIpInput) },
                    onStartScan = { viewModel.startDiscovery() },
                    onStopScan = { viewModel.stopDiscovery() },
                    onSelectDevice = { dev -> viewModel.connect(dev.ip) }
                )
            }

            // 3. Connected Dashboard
            if (uiState.connectionState is ConnectionState.Connected) {
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

                // Screen Switcher
                ScreenSwitcherCard(
                    activeApp = status?.activeApp ?: 0,
                    onSwitch = { appIdx -> viewModel.switchScreen(appIdx) }
                )

                // Push Notification Tester
                NotificationPushCard(
                    text = notificationText,
                    onTextChange = { notificationText = it },
                    selectedIcon = selectedIcon,
                    onIconChange = { selectedIcon = it },
                    onSend = { viewModel.sendNotification(notificationText, selectedIcon) }
                )
            }

            // 4. Notification Forwarding Service (Phase 2)
            NotificationForwardingDeckCard(
                uiState = uiState,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ConnectionHeaderCard(
    connectionState: ConnectionState,
    onDisconnect: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val (dotColor, statusText) = when (connectionState) {
                    is ConnectionState.Connected -> Color(0xFF00E676) to "CONNECTED: ${connectionState.device.ip}"
                    is ConnectionState.Connecting -> Color(0xFFFFD600) to "CONNECTING TO ${connectionState.target}..."
                    is ConnectionState.Error -> Color(0xFFFF1744) to "ERROR: ${connectionState.message}"
                    ConnectionState.Disconnected -> Color(0xFF757575) to "DISCONNECTED"
                }

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            if (connectionState is ConnectionState.Connected) {
                OutlinedButton(
                    onClick = onDisconnect,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Disconnect")
                }
            }
        }
    }
}

@Composable
fun DiscoveryControlCard(
    discoveryState: DiscoveryState,
    manualIp: String,
    onManualIpChange: (String) -> Unit,
    onConnectManual: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onSelectDevice: (DiscoveredDevice) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE 32×8 NEOPIXEL MATRIX STREAM",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                OutlinedButton(onClick = onRefresh, shape = RoundedCornerShape(6.dp)) {
                    Text("Refresh", fontSize = 12.sp)
                }
            }

            // Matrix Canvas Grid
            val pixels = matrix?.pixels ?: emptyList()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(32f / 8f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF101010))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(6.dp))
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    for (row in 0 until 8) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (col in 0 until 32) {
                                val hex = pixels.getOrNull(row)?.getOrNull(col) ?: "#000000"
                                val color = parseHexColor(hex)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(0.5.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    return try {
        val longVal = clean.toLong(16)
        if (clean.length == 6) {
            Color(longVal or 0xFF000000)
        } else {
            Color(longVal)
        }
    } catch (_: Exception) {
        Color.Black
    }
}

@Composable
fun TelemetryDeckCard(status: DeviceStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "TELEMETRY & HARDWARE METRICS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMetricItem("TEMP (IN)", "%.1f°C".format(status.temperatureIn))
                TelemetryMetricItem("HUMIDITY", "%.1f%%".format(status.humidityIn))
                TelemetryMetricItem("BATTERY", "${status.batteryPct}% (" + "%.2f".format(status.batteryVolts) + "V)")
                TelemetryMetricItem("WI-FI RSSI", "${status.rssi} dBm")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMetricItem("ACTIVE SCREEN", status.appName)
                TelemetryMetricItem("FIRMWARE", "v${status.firmwareVersion}")
                TelemetryMetricItem("DAY / NIGHT BRI", "${status.dayBri} / ${status.nightBri}")
                TelemetryMetricItem("AUTO-ROTATE", if (status.autoRotate) "ENABLED" else "PAUSED")
            }
        }
    }
}

@Composable
fun TelemetryMetricItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScreenSwitcherCard(
    activeApp: Int,
    onSwitch: (Int) -> Unit
) {
    val screens = listOf(
        0 to "0 Clock",
        1 to "1 Indoor",
        2 to "2 Weather",
        3 to "3 Crypto",
        4 to "4 YouTube",
        5 to "5 Airspace",
        6 to "6 Battery",
        7 to "7 Timer",
        8 to "8 Screensaver"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "SCREEN NAVIGATION & DECK SELECTOR",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                screens.forEach { (idx, name) ->
                    FilterChip(
                        selected = (activeApp == idx),
                        onClick = { onSwitch(idx) },
                        label = { Text(name, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
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
    onSend: () -> Unit
) {
    val icons = listOf("bell", "mail", "spotify", "warning", "heart", "clock")

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
                text = "PUSH INSTANT NOTIFICATION (/api/notify)",
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

            Text("Select 8×8 Icon:", style = MaterialTheme.typography.labelSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                icons.forEach { icon ->
                    SuggestionChip(
                        onClick = { onIconChange(icon) },
                        label = { Text(icon) },
                        border = if (selectedIcon == icon) {
                            FilterChipDefaults.filterChipBorder(enabled = true, selected = true)
                        } else null
                    )
                }
            }

            Button(
                onClick = onSend,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("🚀 Push Notification with Chime to Clock")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationForwardingDeckCard(
    uiState: MainScreenUiState,
    viewModel: MainScreenViewModel
) {
    val context = LocalContext.current
    val settings = uiState.notificationSettings
    val hasPermission = uiState.hasListenerPermission
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NOTIFICATION FORWARDING",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Android Background Relay Service",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (hasPermission && settings.isForwardingEnabled) Color(0xFF1B5E20)
                            else if (!hasPermission) Color(0xFFB71C1C)
                            else Color(0xFF424242)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (!hasPermission) "ACCESS NEEDED"
                               else if (settings.isForwardingEnabled) "ACTIVE"
                               else "PAUSED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            // 1. Permission Warning Banner if not granted
            if (!hasPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1F1F))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Notification Access Permission Required",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8A80)
                            )
                        )
                        Text(
                            text = "Grant notification listener access so Ulanzi Companion can intercept phone notifications (calls, messages, calendar) and forward them to your TC-001 matrix display.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f))
                        )
                        Button(
                            onClick = {
                                val intent = viewModel.getNotificationSettingsIntent()
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("Open Notification Access Settings")
                        }
                    }
                }
            }

            // 2. Master Toggle Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Master Relay Switch",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Forward intercepted notifications to /api/notify",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Switch(
                    checked = settings.isForwardingEnabled,
                    onCheckedChange = { viewModel.setNotificationForwarding(it) },
                    enabled = hasPermission
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 3. Category Filter Chips
            Text(
                text = "APP CATEGORIES TO FORWARD:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = settings.forwardCalls,
                    onClick = { viewModel.setForwardCalls(!settings.forwardCalls) },
                    label = { Text("📞 Phone & Calls") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FilterChip(
                    selected = settings.forwardMessaging,
                    onClick = { viewModel.setForwardMessaging(!settings.forwardMessaging) },
                    label = { Text("💬 Messaging") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FilterChip(
                    selected = settings.forwardCalendar,
                    onClick = { viewModel.setForwardCalendar(!settings.forwardCalendar) },
                    label = { Text("📅 Calendar") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FilterChip(
                    selected = settings.forwardOther,
                    onClick = { viewModel.setForwardOther(!settings.forwardOther) },
                    label = { Text("📦 Other Apps") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // 4. Formatting & Suppression Options
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = settings.ignoreOngoing,
                    onClick = { viewModel.setIgnoreOngoing(!settings.ignoreOngoing) },
                    label = { Text("Filter Ongoing (Music/Uploads)") }
                )
                FilterChip(
                    selected = settings.includeAppName,
                    onClick = { viewModel.setIncludeAppName(!settings.includeAppName) },
                    label = { Text("Prefix App Name [App]") }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 5. Sound & Duration Pickers
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "MATRIX CHIME / AUDIO TONE:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                val soundOptions = listOf(0 to "Mute", 1 to "Chime (1)", 2 to "Beep (2)", 3 to "Alarm (3)")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    soundOptions.forEach { (snd, label) ->
                        FilterChip(
                            selected = settings.sound == snd,
                            onClick = { viewModel.setNotificationSound(snd) },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "DISPLAY DURATION: ${settings.duration}s",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                val durationOptions = listOf(3, 5, 8, 10, 15)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    durationOptions.forEach { sec ->
                        FilterChip(
                            selected = settings.duration == sec,
                            onClick = { viewModel.setNotificationDuration(sec) },
                            label = { Text("${sec}s", fontSize = 12.sp) }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 6. Live Forwarded Notifications Stream Log
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE FORWARDED STREAM (${uiState.notificationLogs.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                if (uiState.notificationLogs.isNotEmpty()) {
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable { viewModel.clearNotificationLogs() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (uiState.notificationLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notifications intercepted yet.\nTrigger alerts on your phone to see live relays here.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        ),
                        lineHeight = 18.sp
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.notificationLogs.take(8).forEach { log ->
                        val (statusBg, statusFg, statusLabel) = when (log.status) {
                            NotificationDispatchStatus.SENT -> Triple(Color(0xFF1B5E20), Color(0xFFB9F6CA), "SENT")
                            NotificationDispatchStatus.SKIPPED_DUPLICATE -> Triple(Color(0xFF4A3800), Color(0xFFFFE082), "DUPE")
                            NotificationDispatchStatus.SKIPPED_FILTERED -> Triple(Color(0xFF37474F), Color(0xFFB0BEC5), "FILTERED")
                            NotificationDispatchStatus.SKIPPED_DISABLED -> Triple(Color(0xFF263238), Color(0xFF90A4AE), "OFF")
                            NotificationDispatchStatus.SKIPPED_DISCONNECTED -> Triple(Color(0xFF0D47A1), Color(0xFFBBDEFB), "DISCONNECTED")
                            NotificationDispatchStatus.ERROR -> Triple(Color(0xFFB71C1C), Color(0xFFFFCDD2), "ERROR")
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(statusBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = statusFg,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = log.appName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = timeFormat.format(Date(log.timestamp)),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                    Text(
                                        text = log.text,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
