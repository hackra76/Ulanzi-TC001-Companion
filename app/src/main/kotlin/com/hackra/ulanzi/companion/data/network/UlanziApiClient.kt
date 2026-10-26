package com.hackra.ulanzi.companion.data.network

import com.hackra.ulanzi.companion.data.model.CustomAppPayload
import com.hackra.ulanzi.companion.data.model.DeviceStatus
import com.hackra.ulanzi.companion.data.model.DisplayMatrixResponse
import com.hackra.ulanzi.companion.data.model.GenericStatusResponse
import com.hackra.ulanzi.companion.data.model.NotifyPayload
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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
            val request = Request.Builder()
                .url("$baseUrl/reboot")
                .post("".toRequestBody())
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 200 && response.code != 302) {
                    throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                }
            }
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
}
