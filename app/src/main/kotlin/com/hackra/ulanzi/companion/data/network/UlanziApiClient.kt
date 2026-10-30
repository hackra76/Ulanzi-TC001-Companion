package com.hackra.ulanzi.companion.data.network

import com.hackra.ulanzi.companion.data.model.BuzzerResponse
import com.hackra.ulanzi.companion.data.model.CanvasPayload
import com.hackra.ulanzi.companion.data.model.CustomAppPayload
import com.hackra.ulanzi.companion.data.model.DeviceStatus
import com.hackra.ulanzi.companion.data.model.DisplayMatrixResponse
import com.hackra.ulanzi.companion.data.model.GenericStatusResponse
import com.hackra.ulanzi.companion.data.model.GitHubRelease
import com.hackra.ulanzi.companion.data.model.NotifyPayload
import com.hackra.ulanzi.companion.data.model.OtaCheckResponse
import com.hackra.ulanzi.companion.data.model.ScreenSwitchResult
import com.hackra.ulanzi.companion.data.model.ScreensaverResponse
import com.hackra.ulanzi.companion.data.model.TimerResponse
import com.hackra.ulanzi.companion.data.model.WifiProfile
import com.hackra.ulanzi.companion.data.model.WifiResponse
import com.hackra.ulanzi.companion.data.model.WifiScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class UlanziApiClient(
    initialBaseUrl: String = "http://ulanzi-tc001.local"
) {
    var baseUrl: String = initialBaseUrl
        private set

    fun setDeviceAddress(hostOrIp: String) {
        val cleanHost = hostOrIp.removePrefix("http://").removePrefix("https://").trimEnd('/')
        baseUrl = "http://$cleanHost"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val probeClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(800, TimeUnit.MILLISECONDS)
        .build()

    private val uploadClient: OkHttpClient = client.newBuilder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    suspend fun getStatus(): Result<DeviceStatus> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$baseUrl/status")
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                json.decodeFromString<DeviceStatus>(body)
            }
        }
    }

    suspend fun getDisplayMatrix(): Result<DisplayMatrixResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$baseUrl/api/display")
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                json.decodeFromString<DisplayMatrixResponse>(body)
            }
        }
    }

    suspend fun sendCanvasPixels(
        pixels: List<List<String>>,
        durationSec: Int = 60
    ): Result<GenericStatusResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = CanvasPayload(pixels = pixels, duration = durationSec)
            val bodyJson = json.encodeToString(CanvasPayload.serializer(), payload)
            val request = Request.Builder()
                .url("$baseUrl/api/display")
                .post(bodyJson.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val fallbackReq = Request.Builder()
                        .url("$baseUrl/api/draw")
                        .post(bodyJson.toRequestBody(jsonMediaType))
                        .build()
                    client.newCall(fallbackReq).execute().use { fallbackResp ->
                        if (!fallbackResp.isSuccessful) throw IllegalStateException("HTTP ${fallbackResp.code}: ${fallbackResp.message}")
                        val body = fallbackResp.body?.string().orEmpty()
                        if (body.isBlank()) GenericStatusResponse(status = "ok") else json.decodeFromString<GenericStatusResponse>(body)
                    }
                } else {
                    val body = response.body?.string().orEmpty()
                    if (body.isBlank()) GenericStatusResponse(status = "ok") else json.decodeFromString<GenericStatusResponse>(body)
                }
            }
        }
    }

    suspend fun switchScreen(appIndex: Int, autoRotate: Boolean? = null): Result<ScreenSwitchResult> = withContext(Dispatchers.IO) {
        runCatching {
            var url = "$baseUrl/api/screen?set=$appIndex"
            if (autoRotate != null) {
                url += "&auto=${if (autoRotate) 1 else 0}"
            }
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                json.decodeFromString<ScreenSwitchResult>(body)
            }
        }
    }

    suspend fun sendNotification(payload: NotifyPayload): Result<GenericStatusResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val bodyJson = json.encodeToString(NotifyPayload.serializer(), payload)
            val request = Request.Builder()
                .url("$baseUrl/api/notify")
                .post(bodyJson.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                json.decodeFromString<GenericStatusResponse>(body)
            }
        }
    }

    suspend fun pushCustomApp(name: String, payload: CustomAppPayload): Result<GenericStatusResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val bodyJson = json.encodeToString(CustomAppPayload.serializer(), payload)
            val request = Request.Builder()
                .url("$baseUrl/api/custom?name=$name")
                .post(bodyJson.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                json.decodeFromString<GenericStatusResponse>(body)
            }
        }
    }

    suspend fun deleteCustomApp(name: String): Result<GenericStatusResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$baseUrl/api/custom?name=$name")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                json.decodeFromString<GenericStatusResponse>(body)
            }
        }
    }

    suspend fun controlTimer(
        action: String,
        mode: String? = null,
        duration: Int? = null
    ): Result<TimerResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val builder = "$baseUrl/api/timer".toHttpUrlOrNull()?.newBuilder()
                ?: throw IllegalArgumentException("Invalid URL: $baseUrl/api/timer")
            builder.addQueryParameter("action", action)
            if (mode != null) builder.addQueryParameter("mode", mode)
            if (duration != null) builder.addQueryParameter("duration", duration.toString())

            val request = Request.Builder()
                .url(builder.build())
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                json.decodeFromString<TimerResponse>(body)
            }
        }
    }

    suspend fun controlScreensaver(
        style: Int? = null,
        timeout: Int? = null,
        action: String? = null
    ): Result<ScreensaverResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val builder = "$baseUrl/api/screensaver".toHttpUrlOrNull()?.newBuilder()
                ?: throw IllegalArgumentException("Invalid URL: $baseUrl/api/screensaver")
            if (style != null) builder.addQueryParameter("style", style.toString())
            if (timeout != null) builder.addQueryParameter("timeout", timeout.toString())
            if (action != null) builder.addQueryParameter("action", action)

            val request = Request.Builder()
                .url(builder.build())
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                json.decodeFromString<ScreensaverResponse>(body)
            }
        }
    }

    suspend fun controlBuzzer(
        sound: String? = null,
        rtttl: String? = null,
        freq: Int? = null,
        durationMs: Int? = null,
        stop: Boolean = false
    ): Result<BuzzerResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val builder = "$baseUrl/api/buzzer".toHttpUrlOrNull()?.newBuilder()
                ?: throw IllegalArgumentException("Invalid URL: $baseUrl/api/buzzer")
            if (stop) {
                builder.addQueryParameter("stop", "1")
            } else if (!rtttl.isNullOrBlank()) {
                builder.addQueryParameter("rtttl", rtttl)
            } else if (freq != null && freq > 0) {
                builder.addQueryParameter("freq", freq.toString())
                if (durationMs != null && durationMs > 0) {
                    builder.addQueryParameter("duration", durationMs.toString())
                }
            } else if (!sound.isNullOrBlank()) {
                builder.addQueryParameter("sound", sound)
            }

            val request = Request.Builder()
                .url(builder.build())
                .header("Accept", "application/json")
                .post("{}".toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                if (body.isBlank() || body.trim() == "ok") {
                    BuzzerResponse(status = "ok")
                } else {
                    json.decodeFromString<BuzzerResponse>(body)
                }
            }
        }
    }

    suspend fun getWifiStatus(): Result<WifiResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$baseUrl/api/wifi")
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                json.decodeFromString<WifiResponse>(body)
            }
        }
    }

    suspend fun saveSettings(params: Map<String, String>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val formBuilder = FormBody.Builder()
            params.forEach { (k, v) -> formBuilder.add(k, v) }
            formBuilder.add("ajax", "1")

            val request = Request.Builder()
                .url("$baseUrl/save")
                .header("X-Requested-With", "XMLHttpRequest")
                .post(formBuilder.build())
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
            }
        }
    }

    suspend fun rebootDevice(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. Try POST /api/reboot (Standard REST API)
            val req1 = Request.Builder()
                .url("$baseUrl/api/reboot")
                .post("".toRequestBody())
                .build()

            var lastCode = -1
            try {
                client.newCall(req1).execute().use { response ->
                    lastCode = response.code
                    if (response.isSuccessful || response.code == 200 || response.code == 302) {
                        return@runCatching
                    }
                }
            } catch (_: Exception) {}

            // 2. Try POST /reboot (Legacy web server endpoint)
            val req2 = Request.Builder()
                .url("$baseUrl/reboot")
                .post("".toRequestBody())
                .build()

            try {
                client.newCall(req2).execute().use { response ->
                    lastCode = response.code
                    if (response.isSuccessful || response.code == 200 || response.code == 302) {
                        return@runCatching
                    }
                }
            } catch (_: Exception) {}

            // 3. Try GET /api/reboot (Some firmware variants use GET)
            val req3 = Request.Builder()
                .url("$baseUrl/api/reboot")
                .get()
                .build()

            try {
                client.newCall(req3).execute().use { response ->
                    if (response.isSuccessful || response.code == 200 || response.code == 302) {
                        return@runCatching
                    }
                }
            } catch (_: Exception) {}

            throw IllegalStateException(
                if (lastCode == 404) "Reboot endpoint not found on device (HTTP 404). Check firmware support for /api/reboot."
                else "HTTP $lastCode: Failed to trigger device reboot"
            )
        }
    }

    suspend fun connectWifiSlot(slot: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$baseUrl/api/wifi?connect=$slot")
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
            }
        }
    }

    suspend fun scanWifi(): Result<List<WifiScanResult>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$baseUrl/scan")
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                json.decodeFromString<List<WifiScanResult>>(body)
            }
        }
    }

    suspend fun deleteWifiSlot(slot: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val formBuilder = FormBody.Builder()
            formBuilder.add("del_wifi_$slot", "1")
            formBuilder.add("ajax", "1")

            val request = Request.Builder()
                .url("$baseUrl/save")
                .header("X-Requested-With", "XMLHttpRequest")
                .post(formBuilder.build())
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
            }
        }
    }

    suspend fun probe(hostOrIp: String): DeviceStatus? = withContext(Dispatchers.IO) {
        try {
            val cleanHost = hostOrIp.removePrefix("http://").removePrefix("https://").trimEnd('/')
            val request = Request.Builder()
                .url("http://$cleanHost/status")
                .header("Accept", "application/json")
                .get()
                .build()

            probeClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val status = json.decodeFromString<DeviceStatus>(body)
                    if (status.firmwareVersion.isNotBlank()) status else null
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun checkOtaUpdate(currentFirmwareVersion: String = ""): Result<OtaCheckResponse> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. First probe GitHub Releases API directly for rich release notes & binaries
            try {
                val ghRequest = Request.Builder()
                    .url("https://api.github.com/repos/hackra76/Ulanzi-TC001-Evolved/releases/latest")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Ulanzi-TC001-Companion")
                    .get()
                    .build()
                client.newCall(ghRequest).execute().use { ghResp ->
                    if (ghResp.isSuccessful) {
                        val ghBody = ghResp.body?.string().orEmpty()
                        val release = json.decodeFromString<GitHubRelease>(ghBody)
                        val latestVer = release.tagName.removePrefix("v").trim()
                        val currVer = currentFirmwareVersion.removePrefix("v").trim()
                        val hasUpd = if (currVer.isNotBlank() && latestVer.isNotBlank()) {
                            isVersionNewer(latestVer, currVer)
                        } else false

                        val binAsset = release.assets.find { it.name.endsWith(".bin") && !it.name.contains("factory") }
                            ?: release.assets.firstOrNull { it.name.endsWith(".bin") }
                        val dlUrl = binAsset?.browserDownloadUrl.orEmpty()

                        return@withContext Result.success(
                            OtaCheckResponse(
                                currentVersion = currentFirmwareVersion,
                                latestVersion = release.tagName,
                                hasUpdate = hasUpd,
                                downloadUrl = dlUrl,
                                title = release.name.ifBlank { release.tagName },
                                releaseNotes = release.body,
                                publishedAt = release.publishedAt,
                                htmlUrl = release.htmlUrl
                            )
                        )
                    }
                }
            } catch (_: Exception) {
                // Fallback to clock on-board endpoint
            }

            // 2. Fallback to clock on-board REST endpoint
            val request = Request.Builder()
                .url("$baseUrl/api/ota/check")
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                json.decodeFromString<OtaCheckResponse>(body)
            }
        }
    }

    suspend fun startCloudOta(downloadUrl: String): Result<GenericStatusResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$baseUrl/api/ota/install?url=${URLEncoder.encode(downloadUrl, "UTF-8")}"
            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) GenericStatusResponse(status = "started", app = "OTA") else json.decodeFromString<GenericStatusResponse>(body)
            }
        }
    }

    suspend fun uploadFirmwareBinary(
        bytes: ByteArray,
        fileName: String,
        onProgress: (Int) -> Unit
    ): Result<GenericStatusResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val rawBody = bytes.toRequestBody("application/octet-stream".toMediaType())
            val countingBody = CountingRequestBody(rawBody) { written, total ->
                val pct = if (total > 0) ((written * 100) / total).toInt() else 0
                onProgress(pct.coerceIn(0, 100))
            }
            val multipart = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("update", fileName, countingBody)
                .build()

            val request = Request.Builder()
                .url("$baseUrl/update")
                .post(multipart)
                .build()

            uploadClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) GenericStatusResponse(status = "success", app = "Flashed") else json.decodeFromString<GenericStatusResponse>(body)
            }
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        try {
            val latestParts = latest.split('.').map { it.toIntOrNull() ?: 0 }
            val currentParts = current.split('.').map { it.toIntOrNull() ?: 0 }
            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        } catch (_: Exception) {
            return latest != current
        }
        return false
    }
}
