package io.github.reyfxck.wpsxandroid.platform.wifi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager

class AndroidWifiConnectionInspector(context: Context) {
    private val appContext = context.applicationContext
    private val wifiManager = appContext.getSystemService(WifiManager::class.java)

    @Suppress("DEPRECATION")
    fun connectedBssid(): String? {
        if (!wifiManager.isWifiEnabled) return null
        if (
            appContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val bssid = runCatching { wifiManager.connectionInfo?.bssid }.getOrNull()?.trim()
        return bssid
            ?.takeIf { it.isNotBlank() }
            ?.takeUnless { it.equals("02:00:00:00:00:00", ignoreCase = true) }
    }
}
