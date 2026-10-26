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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
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
                TelemetryMetricItem("BATTERY", "${status.batteryPct}% (%.2fV)".format(status.batteryVolts))
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
