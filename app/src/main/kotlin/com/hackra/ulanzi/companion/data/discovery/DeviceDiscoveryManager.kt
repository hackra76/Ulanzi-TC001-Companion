package com.hackra.ulanzi.companion.data.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import com.hackra.ulanzi.companion.data.model.DeviceStatus
import com.hackra.ulanzi.companion.data.network.UlanziApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

data class DiscoveredDevice(
    val ip: String,
    val hostname: String,
    val status: DeviceStatus
)

sealed interface DiscoveryState {
    data object Idle : DiscoveryState
    data class Scanning(val progress: Float = 0f, val found: List<DiscoveredDevice> = emptyList()) : DiscoveryState
    data class Finished(val devices: List<DiscoveredDevice>) : DiscoveryState
    data class Error(val message: String) : DiscoveryState
}

class DeviceDiscoveryManager(
    private val context: Context,
    private val apiClient: UlanziApiClient
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager

    private val _discoveryState = MutableStateFlow<DiscoveryState>(DiscoveryState.Idle)
    val discoveryState: StateFlow<DiscoveryState> = _discoveryState.asStateFlow()

    private var scanJob: Job? = null

    fun startDiscovery(scope: CoroutineScope) {
        scanJob?.cancel()
        scanJob = scope.launch {
            _discoveryState.value = DiscoveryState.Scanning(progress = 0f)
            val foundDevices = mutableMapOf<String, DiscoveredDevice>()

            // 1. Immediate probe of standard known targets (mDNS, SoftAP, and local test loopbacks)
            val directTargets = listOf("ulanzi-tc001.local", "192.168.4.1", "10.0.2.2:18080", "127.0.0.1:18080")
            for (target in directTargets) {
                val status = apiClient.probe(target)
                if (status != null && status.ip.isNotBlank()) {
                    val resolvedIp = if (target.contains(":")) target else status.ip
                    val dev = DiscoveredDevice(ip = resolvedIp, hostname = target, status = status)
                    foundDevices[dev.ip] = dev
                    _discoveryState.value = DiscoveryState.Scanning(
                        progress = 0.2f,
                        found = foundDevices.values.toList()
                    )
                }
            }

            // 2. Subnet scan if connected to Wi-Fi
            val subnetPrefix = getWifiSubnetPrefix()
            if (subnetPrefix != null) {
                val ipBatchSize = 32
                val ips = (1..254).map { "$subnetPrefix.$it" }
                val total = ips.size

                ips.chunked(ipBatchSize).forEachIndexed { chunkIdx, chunk ->
                    val deferreds = chunk.map { ip ->
                        async(Dispatchers.IO) {
                            if (!foundDevices.containsKey(ip)) {
                                val status = apiClient.probe(ip)
                                if (status != null) {
                                    DiscoveredDevice(ip = ip, hostname = ip, status = status)
                                } else null
                            } else null
                        }
                    }

                    val results = deferreds.awaitAll().filterNotNull()
                    for (dev in results) {
                        foundDevices[dev.ip] = dev
                    }

                    val progress = 0.2f + (0.8f * ((chunkIdx + 1) * ipBatchSize).coerceAtMost(total) / total)
                    _discoveryState.value = DiscoveryState.Scanning(
                        progress = progress,
                        found = foundDevices.values.toList()
                    )
                }
            }

            _discoveryState.value = DiscoveryState.Finished(foundDevices.values.toList())
        }
    }

    fun stopDiscovery() {
        scanJob?.cancel()
        scanJob = null
        _discoveryState.value = DiscoveryState.Idle
    }

    private fun getWifiSubnetPrefix(): String? {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                ?: return null
            val ipInt = wifiManager.connectionInfo.ipAddress
            if (ipInt == 0) return null
            val b1 = ipInt and 0xff
            val b2 = (ipInt shr 8) and 0xff
            val b3 = (ipInt shr 16) and 0xff
            "$b1.$b2.$b3"
        } catch (_: Exception) {
            null
        }
    }

    fun observeNsdServices(): Flow<DiscoveredDevice> = callbackFlow {
        if (nsdManager == null) {
            close()
            return@callbackFlow
        }

        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {}

            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceType.contains("_http._tcp", ignoreCase = true)) {
                    nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            val host = serviceInfo.host?.hostAddress ?: return
                            launch(Dispatchers.IO) {
                                val status = apiClient.probe(host)
                                if (status != null) {
                                    trySend(DiscoveredDevice(ip = host, hostname = serviceInfo.serviceName, status = status))
                                }
                            }
                        }
                    })
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {}
            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                nsdManager.stopServiceDiscovery(this)
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
        }

        nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, listener)

        awaitClose {
            try {
                nsdManager.stopServiceDiscovery(listener)
            } catch (_: Exception) {}
        }
    }
}
