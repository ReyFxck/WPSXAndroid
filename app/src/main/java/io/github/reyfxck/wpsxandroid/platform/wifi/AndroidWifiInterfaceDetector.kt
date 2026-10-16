package io.github.reyfxck.wpsxandroid.platform.wifi

import io.github.reyfxck.wpsxandroid.core.capability.RootInterfaceListParser
import io.github.reyfxck.wpsxandroid.core.capability.WifiInterfaceClassifier
import io.github.reyfxck.wpsxandroid.core.capability.WifiInterfaceInfo
import io.github.reyfxck.wpsxandroid.platform.process.StructuredProcessRunner
import java.io.File
import java.net.NetworkInterface
import java.util.Collections

class AndroidWifiInterfaceDetector(
    private val processRunner: StructuredProcessRunner = StructuredProcessRunner(),
) {
    fun discover(): List<WifiInterfaceInfo> {
        val byName = linkedMapOf<String, WifiInterfaceInfo>()

        discoverFromNetworkInterfaces().forEach { byName[it.name] = it }
        discoverFromSysfs().forEach { discovered ->
            byName[discovered.name] = merge(byName[discovered.name], discovered)
        }

        return byName.values.sortedBy { it.name }
    }

    suspend fun discoverWithRootFallback(): List<WifiInterfaceInfo> {
        val passive = discover()
        val execution = processRunner.runFirstSuccessful(
            label = "root-net-interfaces",
            executableCandidates = listOf("su"),
            arguments = listOf("-c", "ls -1 /sys/class/net"),
            timeoutMillis = 5_000,
            maxOutputChars = 8_192,
        )

        if (execution.trace.exitCode != 0) return passive

        val byName = passive.associateByTo(linkedMapOf()) { it.name }
        RootInterfaceListParser.parse(execution.output)
            .mapNotNull(::infoForName)
            .forEach { discovered ->
                byName[discovered.name] = merge(byName[discovered.name], discovered)
            }

        return byName.values.sortedBy { it.name }
    }

    private fun discoverFromNetworkInterfaces(): List<WifiInterfaceInfo> {
        val interfaces = runCatching { NetworkInterface.getNetworkInterfaces() }.getOrNull() ?: return emptyList()
        return Collections.list(interfaces)
            .mapNotNull { networkInterface ->
                val name = networkInterface.name ?: return@mapNotNull null
                val base = infoForName(name) ?: return@mapNotNull null
                base.copy(isUp = runCatching { networkInterface.isUp }.getOrDefault(base.isUp))
            }
    }

    private fun discoverFromSysfs(): List<WifiInterfaceInfo> {
        val root = File("/sys/class/net")
        val entries = runCatching { root.listFiles()?.toList().orEmpty() }.getOrDefault(emptyList())
        return entries.mapNotNull { infoForName(it.name) }
    }

    private fun infoForName(name: String): WifiInterfaceInfo? {
        if (!WifiInterfaceClassifier.isSafeKernelInterfaceName(name)) return null

        val wirelessDir = File("/sys/class/net/$name/wireless")
        val sysfsWireless = runCatching { wirelessDir.exists() }.getOrDefault(false)
        val kind = WifiInterfaceClassifier.classify(name, sysfsWireless) ?: return null
        val isUp = runCatching {
            File("/sys/class/net/$name/operstate").readText().trim().equals("up", ignoreCase = true)
        }.getOrDefault(false)

        return WifiInterfaceInfo(
            name = name,
            kind = kind,
            isUp = isUp,
            sysfsWireless = sysfsWireless,
        )
    }

    private fun merge(
        current: WifiInterfaceInfo?,
        discovered: WifiInterfaceInfo,
    ): WifiInterfaceInfo = if (current == null) {
        discovered
    } else {
        current.copy(
            isUp = current.isUp || discovered.isUp,
            sysfsWireless = current.sysfsWireless || discovered.sysfsWireless,
        )
    }
}
