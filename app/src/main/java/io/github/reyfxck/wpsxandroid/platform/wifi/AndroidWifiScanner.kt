package io.github.reyfxck.wpsxandroid.platform.wifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.ScanResult as AndroidScanResult
import android.net.wifi.WifiManager
import android.os.Build
import io.github.reyfxck.wpsxandroid.core.model.WifiNetwork
import io.github.reyfxck.wpsxandroid.core.wifi.ScanResult
import io.github.reyfxck.wpsxandroid.core.wifi.WifiCapabilityParser
import io.github.reyfxck.wpsxandroid.core.wifi.WifiScanner
import io.github.reyfxck.wpsxandroid.core.wifi.wifiChannelFromFrequency
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class AndroidWifiScanner(context: Context) : WifiScanner {
    private val appContext = context.applicationContext
    private val wifiManager = appContext.getSystemService(WifiManager::class.java)
    private val locationManager = appContext.getSystemService(LocationManager::class.java)

    override suspend fun scan(): ScanResult {
        if (!hasFineLocationPermission()) {
            return ScanResult.PermissionRequired(setOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !locationManager.isLocationEnabled) {
            return ScanResult.Unavailable(
                "Android requires Location services to be enabled for Wi-Fi scan results.",
            )
        }

        val result = try {
            withTimeoutOrNull(SCAN_TIMEOUT_MS) { awaitScanResult() }
        } catch (security: SecurityException) {
            return ScanResult.PermissionRequired(setOf(Manifest.permission.ACCESS_FINE_LOCATION))
        } catch (failure: RuntimeException) {
            return ScanResult.Unavailable(failure.message ?: "Wi-Fi scan failed.")
        }

        return result ?: cachedResults(isFresh = false)
    }

    private suspend fun awaitScanResult(): ScanResult = suspendCancellableCoroutine { continuation ->
        var registered = false

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) return

                val updated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (registered) {
                    runCatching { appContext.unregisterReceiver(this) }
                    registered = false
                }

                if (continuation.isActive) {
                    continuation.resume(cachedResults(isFresh = updated))
                }
            }
        }

        try {
            val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                appContext.registerReceiver(receiver, filter)
            }
            registered = true

            @Suppress("DEPRECATION")
            val started = wifiManager.startScan()
            if (!started && continuation.isActive) {
                runCatching { appContext.unregisterReceiver(receiver) }
                registered = false
                continuation.resume(cachedResults(isFresh = false))
            }
        } catch (failure: Throwable) {
            if (registered) {
                runCatching { appContext.unregisterReceiver(receiver) }
                registered = false
            }
            if (continuation.isActive) {
                continuation.resumeWith(Result.failure(failure))
            }
        }

        continuation.invokeOnCancellation {
            if (registered) {
                runCatching { appContext.unregisterReceiver(receiver) }
                registered = false
            }
        }
    }

    private fun cachedResults(isFresh: Boolean): ScanResult {
        @Suppress("DEPRECATION")
        val networks = wifiManager.scanResults
            .map(::mapNetwork)
            .distinctBy { it.bssid.uppercase() }
            .sortedByDescending(WifiNetwork::rssiDbm)

        return ScanResult.Success(networks = networks, isFresh = isFresh)
    }

    private fun mapNetwork(result: AndroidScanResult): WifiNetwork {
        val parsed = WifiCapabilityParser.parse(result.capabilities.orEmpty())
        return WifiNetwork(
            ssid = result.SSID.orEmpty(),
            bssid = result.BSSID.orEmpty(),
            rssiDbm = result.level,
            frequencyMhz = result.frequency,
            channel = wifiChannelFromFrequency(result.frequency),
            capabilities = result.capabilities.orEmpty(),
            securityModes = parsed.securityModes,
            wpsAdvertised = parsed.wpsAdvertised,
        )
    }

    private fun hasFineLocationPermission(): Boolean =
        appContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private companion object {
        const val SCAN_TIMEOUT_MS = 12_000L
    }
}
