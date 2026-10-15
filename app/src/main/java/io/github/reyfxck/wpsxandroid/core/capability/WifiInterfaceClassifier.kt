package io.github.reyfxck.wpsxandroid.core.capability

object WifiInterfaceClassifier {
    private val safeName = Regex("^[A-Za-z0-9_.-]{1,32}$")

    fun isSafeKernelInterfaceName(name: String): Boolean = safeName.matches(name)

    fun classify(name: String, sysfsWireless: Boolean): WifiInterfaceKind? {
        if (!isSafeKernelInterfaceName(name)) return null

        val lower = name.lowercase()
        return when {
            lower.startsWith("p2p") -> WifiInterfaceKind.P2P
            lower.startsWith("ap") -> WifiInterfaceKind.ACCESS_POINT
            lower.startsWith("wlan") || lower.startsWith("wifi") || sysfsWireless -> WifiInterfaceKind.WIFI
            else -> null
        }
    }
}
