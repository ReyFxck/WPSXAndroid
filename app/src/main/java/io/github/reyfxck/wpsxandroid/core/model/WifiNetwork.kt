package io.github.reyfxck.wpsxandroid.core.model

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssiDbm: Int,
    val frequencyMhz: Int,
    val channel: Int?,
    val capabilities: String,
    val securityModes: Set<WifiSecurity>,
    val wpsAdvertised: Boolean,
)

enum class WifiSecurity {
    OPEN,
    WEP,
    WPA,
    WPA2,
    WPA3,
    OWE,
    UNKNOWN,
}
