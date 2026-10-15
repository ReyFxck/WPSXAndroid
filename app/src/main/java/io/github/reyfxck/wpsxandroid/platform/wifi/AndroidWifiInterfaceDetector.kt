package io.github.reyfxck.wpsxandroid.platform.wifi

import io.github.reyfxck.wpsxandroid.core.capability.WifiInterfaceClassifier
import io.github.reyfxck.wpsxandroid.core.capability.WifiInterfaceInfo
import java.io.File
import java.net.NetworkInterface
import java.util.Collections

class AndroidWifiInterfaceDetector {
    fun discover(): List<WifiInterfaceInfo> {
        val interfaces = runCatching { NetworkInterface.getNetworkInterfaces() }.getOrNull() ?: return emptyList()

        return Collections.list(interfaces)
            .mapNotNull { networkInterface ->
                val name = networkInterface.name ?: return@mapNotNull null
                if (!WifiInterfaceClassifier.isSafeKernelInterfaceName(name)) return@mapNotNull null

                val sysfsWireless = runCatching {
                    File("/sys/class/net/$name/wireless").exists()
                }.getOrDefault(false)
                val kind = WifiInterfaceClassifier.classify(name, sysfsWireless) ?: return@mapNotNull null
                val isUp = runCatching { networkInterface.isUp }.getOrDefault(false)

                WifiInterfaceInfo(
                    name = name,
                    kind = kind,
                    isUp = isUp,
                    sysfsWireless = sysfsWireless,
                )
            }
            .sortedBy { it.name }
    }
}
